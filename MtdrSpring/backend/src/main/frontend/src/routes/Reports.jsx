// src/pages/Reports.jsx
import React, { useEffect, useState, useMemo } from "react";
import config from "../config";
import DatePicker from "react-datepicker";
import "react-datepicker/dist/react-datepicker.css";
import { useParams } from "react-router-dom";
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer, // KPI-1
  PieChart,
  Pie,
  Cell, // KPI-2
} from "recharts";

/* ---------- colores fijos para cada developer ---------- */
const PALETTE = [
  "#82ca9d",
  "#ffc658",
  "#ff7300",
  "#0088fe",
  "#00c49f",
  "#ffbb28",
  "#ff8042",
  "#8884d8",
];

function Reports() {
  const { projectId } = useParams();

  /* ------------- estados base (KPIs originales) ---------------- */
  const [sprints, setSprints] = useState([]);
  const [members, setMembers] = useState([]); // {id:idProjectUser,name}
  const [memberColors, setMemberColors] = useState({});
  const [filterType, setFilterType] = useState("sprint");
  const [selectedSprint, setSelectedSprint] = useState(null);
  const [selectedMember, setSelectedMember] = useState(null);
  const [selectedDate, setSelectedDate] = useState("");
  const [reportData, setReportData] = useState(null);

  /* ----- estados de las 3 GRÁFICAS NUEVAS ---------------------- */
  const [totalHoursBySprint, setTotalHoursBySprint] = useState([]); // gráfica 1
  const [hoursByDevPerSprint, setHoursByDevPerSprint] = useState([]); // gráfica 2
  const [tasksByDevPerSprint, setTasksByDevPerSprint] = useState([]); // gráfica 3

  /* ================= OBTENER SPRINTS =========================== */
  useEffect(() => {
    fetch(`${config.apiBaseUrl}/api/sprints/project/${projectId}`)
      .then((r) => r.json())
      .then((data) =>
        setSprints(data.map((s) => ({ id: s.id_sprint, name: s.name }))),
      )
      .catch(console.error);
  }, [projectId]);

  /* ================= OBTENER DEVELOPERS ======================== */
  useEffect(() => {
    fetch(`${config.apiBaseUrl}/api/projects/${projectId}`)
      .then((r) => r.json())
      .then((data) => {
        const devs = data.projectUsers.map((pu, i) => ({
          id: pu.idProjectUser, // idProjectUser
          name: pu.user.name,
          color: PALETTE[i % PALETTE.length],
        }));
        setMembers([
          { id: "all", name: "Todo el equipo", color: "#8884d8" },
          ...devs,
        ]);
        const colors = Object.fromEntries(devs.map((d) => [d.id, d.color]));
        colors.all = "#8884d8";
        setMemberColors(colors);
      })
      .catch(console.error);
  }, [projectId]);

  /* ============================================================= *
   *                BOTÓN  «Generar Reporte»                       *
   * ============================================================= */
  const handleGenerateReport = async () => {
    if (!selectedMember) {
      alert("Selecciona un miembro.");
      return;
    }

    /* ---------- 1. KPI ORIGINALES (sin cambios) ---------------- */
    const isTeam = selectedMember === "all";
    let doneCountURL = "",
      doneDataURL = "";
    if (filterType === "sprint") {
      if (!selectedSprint) {
        alert("Selecciona un sprint.");
        return;
      }
      doneCountURL = isTeam
        ? `${config.apiBaseUrl}/api/task-assignees/team-sprint/${selectedSprint}/done/count`
        : `${config.apiBaseUrl}/api/task-assignees/user/${selectedMember}/sprint/${selectedSprint}/done/count`;
      doneDataURL = isTeam
        ? `${config.apiBaseUrl}/api/task-assignees/team-sprint/${selectedSprint}/done`
        : `${config.apiBaseUrl}/api/task-assignees/user/${selectedMember}/sprint/${selectedSprint}/done`;
    } else if (filterType === "week") {
      if (!selectedDate) {
        alert("Selecciona fecha.");
        return;
      }
      doneCountURL = isTeam
        ? `${config.apiBaseUrl}/api/task-assignees/team-week/${selectedDate}/project/${projectId}/done/count`
        : `${config.apiBaseUrl}/api/task-assignees/user/${selectedMember}/week/${selectedDate}/done/count`;
      doneDataURL = isTeam
        ? `${config.apiBaseUrl}/api/task-assignees/team-week/${selectedDate}/project/${projectId}/done`
        : `${config.apiBaseUrl}/api/task-assignees/user/${selectedMember}/week/${selectedDate}/done`;
    } else {
      /* month */
      if (!selectedDate) {
        alert("Selecciona fecha.");
        return;
      }
      doneCountURL = isTeam
        ? `${config.apiBaseUrl}/api/task-assignees/team-month/${selectedDate}/project/${projectId}/done/count`
        : `${config.apiBaseUrl}/api/task-assignees/user/${selectedMember}/month/${selectedDate}/done/count`;
      doneDataURL = isTeam
        ? `${config.apiBaseUrl}/api/task-assignees/team-month/${selectedDate}/project/${projectId}/done`
        : `${config.apiBaseUrl}/api/task-assignees/user/${selectedMember}/month/${selectedDate}/done`;
    }

    try {
      /* ------------------ llamadas paralelas básicas ------------ */
      const [cnt, data] = await Promise.all([
        fetch(doneCountURL).then((r) => r.json()),
        fetch(doneDataURL).then((r) => r.json()),
      ]);

      /* ------ métricas KPI ----- */
      let est = 0,
        real = 0,
        spTot = 0,
        spFreq = {};
      const processed = data.map((d) => {
        const { task, ...rest } = d;
        const { sprint, ...t } = task;
        est += t.estimatedHours;
        real += t.realHours;
        spTot += t.storyPoints;
        spFreq[t.storyPoints] = (spFreq[t.storyPoints] || 0) + 1;
        return { ...rest, task: t };
      });
      const spChart = Object.entries(spFreq).map(([sp, c]) => ({
        name: `${sp} SP`,
        value: c,
        percent: (
          (c / (Object.values(spFreq).reduce((a, b) => a + b, 0) || 1)) *
          100
        ).toFixed(1),
      }));
      const kpi1 = Number(((est / (real || 1)) * 100).toFixed(2));
      const kpi2 = Number((spTot / (data.length || 1)).toFixed(2));

      /* ===========================================================
         =            NUEVAS 3 GRÁFICAS (siempre)                  =
         ===========================================================*/

      /* ---- Gráfica 1  (team hours por sprint) ----------------- */
      const g1 = await Promise.all(
        sprints.map(async (sp) => {
          const h = await fetch(
            `${config.apiBaseUrl}/api/task-assignees/team-sprint/${sp.id}/real-hours`,
          )
            .then((r) => r.json())
            .catch(() => ({ hours: 0 }));
          console.log("hinsideof:", h);
          return { sprintName: sp.name, hours: h?.hours ?? 0 };
        }),
      );
      console.log("team-sprint-realhours:", g1);
      setTotalHoursBySprint(g1);

      /* ---- Prepara estructura vacía por sprint ---------------- */
      const devs = members.filter((m) => m.id !== "all");
      const makeBlank = () => {
        const row = {};
        devs.forEach((d) => {
          row[d.id] = 0;
        });
        return row;
      };

      /* ---- Horas y tareas por dev para CADA sprint ------------- */
      const g2Promises = sprints.map(async (sp) => {
        const row = { sprintName: sp.name, ...makeBlank() };
        await Promise.all(
          devs.map(async (d) => {
            const h = await fetch(
              `${config.apiBaseUrl}/api/task-assignees/user/${d.id}/sprint/${sp.id}/real-hours`,
            )
              .then((r) => r.json())
              .catch(() => ({ hours: 0 }));
            row[d.id] = h?.hours ?? 0;
          }),
        );
        return row;
      });
      console.log("user-sprint-realhours:", g2Promises);

      const g3Promises = sprints.map(async (sp) => {
        const row = { sprintName: sp.name, ...makeBlank() };
        await Promise.all(
          devs.map(async (d) => {
            const c = await fetch(
              `${config.apiBaseUrl}/api/task-assignees/user/${d.id}/sprint/${sp.id}/done/count`,
            )
              .then((r) => r.json())
              .catch(() => 0);
            row[d.id] = c ?? 0;
          }),
        );
        return row;
      });

      const [g2, g3] = await Promise.all([
        Promise.all(g2Promises),
        Promise.all(g3Promises),
      ]);
      setHoursByDevPerSprint(g2);
      setTasksByDevPerSprint(g3);

      /* --------------- guardar KPI / tabla --------------------- */
      setReportData({
        user:
          members.find((m) => String(m.id) === String(selectedMember))?.name ??
          "—",
        tasksDone: cnt,
        realHours: real,
        estimatedHours: est,
        storyPoints: spTot,
        spFrequencyChartData: spChart,
        kpi1,
        kpi2,
        tasksData: processed.sort((a, b) =>
          (a.projectUser?.user?.name ?? "").localeCompare(
            b.projectUser?.user?.name ?? "",
            "es",
            { sensitivity: "base" },
          ),
        ),
      });
    } catch (e) {
      console.error(e);
    }
  };

  /* datos KPI-1 original */
  const barChartData = useMemo(
    () =>
      reportData?.tasksData?.map((r) => ({
        name: r.task.name,
        real: r.task.realHours,
        estimated: r.task.estimatedHours,
      })) ?? [],
    [reportData],
  );

  const PIE_COLORS = [
    "#ffffff",
    "#d3d3d3",
    "#a9a9a9",
    "#808080",
    "#585858",
    "#2f2f2f",
    "#1a1a1a",
    "#000000",
  ];

  /* ============================================================= */
  /* =========================== UI ============================== */
  /* ============================================================= */
  return (
    <div className="p-8 space-y-10">
      <h1 className="text-3xl font-extrabold text-white">Reportes</h1>

      {/* ---------- CONTROLES (igual) ---------------------------- */}
      <div className="flex flex-wrap gap-4 items-end">
        {/* filtro */}
        <div>
          <label htmlFor="filterTypeSelect" className="block text-sm text-gray-200 font-bold mb-1">
            Filtrar por
          </label>
          <select
            id="filterTypeSelect"
            value={filterType}
            onChange={(e) => setFilterType(e.target.value)}
            className="bg-customDark bg-opacity-30 text-white px-3 py-2 rounded-xl"
          >
            <option value="sprint">sprint</option>
            <option value="week">semana</option>
            <option value="month">mes</option>
          </select>
        </div>

        {filterType === "sprint" && (
          <div>
            <label id="sprintSelect" className="block text-sm text-gray-200 font-bold mb-1">
              Sprint
            </label>
            <select
              id="sprintSelect"
              value={selectedSprint ?? ""}
              onChange={(e) => setSelectedSprint(e.target.value)}
              className="bg-customDark bg-opacity-30 text-white px-3 py-2 rounded-xl"
            >
              <option value="" disabled>
                Selecciona un sprint
              </option>
              {sprints.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.name}
                </option>
              ))}
            </select>
          </div>
        )}

        {filterType !== "sprint" && (
          <div>
            <label className="block text-sm text-gray-200 font-bold mb-1">
              {filterType === "week" ? "Semana" : "Mes"}
            </label>
            <DatePicker
              selected={selectedDate ? new Date(selectedDate) : null}
              onChange={(d) =>
                setSelectedDate(d ? d.toISOString().slice(0, 10) : "")
              }
              className="bg-customDark bg-opacity-30 text-white px-3 py-2 rounded-xl"
              placeholderText="Selecciona fecha"
              dateFormat="yyyy-MM-dd"
            />
          </div>
        )}

        {/* miembro */}
        <div>
          <label htmlFor="memberSelect" className="block text-sm text-gray-200 font-bold mb-1">
            Miembro
          </label>
          <select
            id="memberSelect"
            value={selectedMember ?? ""}
            onChange={(e) => setSelectedMember(e.target.value)}
            className="bg-customDark bg-opacity-30 text-white px-3 py-2 rounded-xl"
          >
            <option value="" disabled>
              Selecciona un miembro
            </option>
            {members.map((m) => (
              <option key={m.id} value={m.id}>
                {m.name}
              </option>
            ))}
          </select>
        </div>

        <button
          onClick={handleGenerateReport}
          className="bg-transparent text-white px-3 py-2 rounded-xl border border-cyan-500 hover:bg-cyan-500/20 transition"
        >
          Generar Reporte
        </button>
      </div>

      {/* ================= GRÁFICA 1 ================= */}
      {totalHoursBySprint.length > 0 && (
        <div>
          <h2 className="text-white font-semibold mb-2">
            Gráfica 1: Horas Totales trabajadas por Sprint
          </h2>
          <ResponsiveContainer width="100%" height={260}>
            <BarChart data={totalHoursBySprint}>
              <CartesianGrid strokeDasharray="3 3" stroke="#555" />
              <XAxis dataKey="sprintName" stroke="#ccc" />
              <YAxis stroke="#ccc" />
              <Tooltip />
              <Bar dataKey="hours" fill="#c084fc" name="Horas" />
            </BarChart>
          </ResponsiveContainer>
        </div>
      )}

      {/* ================= GRÁFICA 2 ================= */}
      {hoursByDevPerSprint.length > 0 && (
        <div>
          <h2 className="text-white font-semibold mb-2">
            Gráfica 2: <b>Horas Trabajadas</b> por Developer por Sprint
          </h2>
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={hoursByDevPerSprint}>
              <CartesianGrid strokeDasharray="3 3" stroke="#555" />
              <XAxis dataKey="sprintName" stroke="#ccc" />
              <YAxis stroke="#ccc" />
              <Tooltip />
              <Legend />
              {members
                .filter((m) => m.id !== "all")
                .map((d) => (
                  <Bar
                    key={d.id}
                    dataKey={String(d.id)}
                    name={d.name}
                    fill={memberColors[d.id]}
                  />
                ))}
            </BarChart>
          </ResponsiveContainer>
        </div>
      )}

      {/* ================= GRÁFICA 3 ================= */}
      {tasksByDevPerSprint.length > 0 && (
        <div>
          <h2 className="text-white font-semibold mb-2">
            Gráfica 3: <b>Tareas Completadas</b> por Developer por Sprint
          </h2>
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={tasksByDevPerSprint}>
              <CartesianGrid strokeDasharray="3 3" stroke="#555" />
              <XAxis dataKey="sprintName" stroke="#ccc" />
              <YAxis stroke="#ccc" />
              <Tooltip />
              <Legend />
              {members
                .filter((m) => m.id !== "all")
                .map((d) => (
                  <Bar
                    key={d.id}
                    dataKey={String(d.id)}
                    name={d.name}
                    fill={memberColors[d.id]}
                  />
                ))}
            </BarChart>
          </ResponsiveContainer>
        </div>
      )}

      {/* ================= CARDS KPI RESUMEN ================ */}
      {reportData && (
        <div className="grid grid-cols-1 sm:grid-cols-5 gap-6 mt-10">
          {[
            { l: "Usuario", v: reportData.user },
            {
              l: "Tareas Completadas",
              v: reportData.tasksDone,
              c: "green-400",
            },
            { l: "Story Points", v: reportData.storyPoints, c: "green-400" },
            { l: "Horas Reales", v: reportData.realHours, c: "blue-400" },
            {
              l: "Horas Estimadas",
              v: reportData.estimatedHours,
              c: "blue-400",
            },
          ].map((k) => (
            <div key={k.l} className="bg-black/30 rounded-2xl p-6 shadow-lg">
              <h3 className="text-sm text-gray-400">{k.l}</h3>
              <p className={`text-2xl font-bold text-${k.c ?? "white"} mt-1`}>
                {k.v}
              </p>
            </div>
          ))}
        </div>
      )}

      {/* ================= KPI-1 + BAR detalle ==================== */}
      {barChartData.length > 0 && (
        <div className="grid grid-cols-6 gap-6 mt-10">
          <div className="col-span-1 bg-black/30 rounded-2xl p-6 shadow-lg">
            <h4 className="text-sm text-gray-400">KPI 1 Time Efficiency</h4>
            <p className="text-3xl font-bold text-blue-400">
              {reportData?.kpi1}
            </p>
          </div>
          <div className="col-span-5 bg-black/30 rounded-2xl p-4">
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={barChartData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#555" />
                <XAxis dataKey="name" stroke="#ccc" />
                <YAxis stroke="#ccc" />
                <Tooltip />
                <Legend />
                <Bar
                  dataKey="estimated"
                  fill="#d3d3d3"
                  name="Horas Estimadas"
                />
                <Bar dataKey="real" fill="#808080" name="Horas Reales" />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      )}

      {/* ================= KPI-2 PIE ============================== */}
      {reportData?.spFrequencyChartData?.length > 0 && (
        <div className="grid grid-cols-6 gap-6 mt-10">
          <div className="col-span-1 bg-black/30 rounded-2xl p-6 shadow-lg">
            <h4 className="text-sm text-gray-400">KPI 2 Tryhard Score</h4>
            <p className="text-3xl font-bold text-blue-400">
              {reportData.kpi2}
            </p>
          </div>
          <div className="col-span-5 bg-black/30 rounded-2xl flex justify-center p-4">
            <PieChart width={400} height={400}>
              <Pie
                data={reportData.spFrequencyChartData}
                cx="50%"
                cy="50%"
                outerRadius={150}
                dataKey="value"
                label={({ name, value, percent }) =>
                  `${name}: ${value} (${percent}%)`
                }
              >
                {reportData.spFrequencyChartData.map((_, i) => (
                  <Cell key={i} fill={PIE_COLORS[i % PIE_COLORS.length]} />
                ))}
              </Pie>
              <Tooltip
                formatter={(v, n, p) => [`${v} tasks`, p.payload.name]}
              />
              <Legend />
            </PieChart>
          </div>
        </div>
      )}

      {/* ================= TABLA DE TAREAS ======================== */}
      {reportData?.tasksData?.length > 0 && (
        <div className="mt-10">
          <h2 className="text-xl font-semibold text-white mb-4">
            Reporte de Tareas terminadas (último Sprint)
          </h2>
          <div className="overflow-auto rounded-lg shadow ring-1 ring-black/50">
            <table className="min-w-full text-sm bg-black/50 text-white">
              <thead className="bg-black/30 uppercase text-xs font-semibold">
                <tr>
                  <th className="px-6 py-3 text-left">Developer</th>
                  <th className="px-6 py-3 text-left">Tarea</th>
                  <th className="px-6 py-3 text-center">Story Pts</th>
                  <th className="px-6 py-3 text-center">Estado</th>
                  <th className="px-6 py-3 text-center">Horas Reales</th>
                  <th className="px-6 py-3 text-center">Horas Estimadas</th>
                  <th className="px-6 py-3 text-center">KPI 1&nbsp;%</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-700">
                {reportData.tasksData.map((r) => {
                  const t = r.task;
                  const dev = r.projectUser?.user?.name ?? "—";
                  const rh = t.realHours || 0,
                    eh = t.estimatedHours || 0;
                  const kpi = (((eh || 1) / (rh || 1)) * 100).toFixed(0);
                  return (
                    <tr key={t.id}>
                      <td className="px-6 py-3">{dev}</td>
                      <td className="px-6 py-3">{t.name}</td>
                      <td className="px-6 py-3 text-center">{t.storyPoints}</td>
                      <td className="px-6 py-3 text-center">{t.status}</td>
                      <td className="px-6 py-3 text-center">{rh}</td>
                      <td className="px-6 py-3 text-center">{eh}</td>
                      <td className="px-6 py-3 text-center">{kpi}%</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}

export default Reports;
