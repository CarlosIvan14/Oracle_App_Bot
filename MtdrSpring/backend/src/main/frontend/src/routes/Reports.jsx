// src/pages/Reports.jsx
import React, { useEffect, useState, useMemo } from "react";
import config from "../config";
import DatePicker from "react-datepicker";
import "react-datepicker/dist/react-datepicker.css";
import { useParams } from "react-router-dom";
import {
  BarChart,
  ComposedChart,
  Area,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
} from "recharts";

/* ---------- colores fijos para cada developer ---------- */
/* ---------- colores fijos para cada developer (azul) ---------- */
const PALETTE = [
  "#3ae7d7", // blue
  "#730000", // red
  "#007308", // green
  "#3b82f6", // darkblue 
  "#ffb800", // yellow
  "#6c0064", // pink

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
  // src/pages/Reports.jsx (arriba, justo dentro de Reports)
  const EXCLUDED = ["OswaldoR", "Lorena"];

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
  useEffect(() => {
    if (sprints.length === 0 || members.length === 0) return;
  
    const devs = members.filter((m) => m.id !== "all");
    const makeBlank = () => {
      const row = {};
      devs.forEach((d) => { row[d.id] = 0; });
      return row;
    };
  
    (async () => {
      // 1) Gráfica 1: total hours por sprint
      const g1 = await Promise.all(
        sprints.map(async (sp) => {
          const raw = await fetch(
            `${config.apiBaseUrl}/api/task-assignees/team-sprint/${sp.id}/real-hours`
          ).then((r) => r.json()).catch(() => 0);
          return { sprintName: sp.name, hours: Number(raw) || 0 };
        })
      );
      setTotalHoursBySprint(g1);
  
      // 2) Gráfica 2: horas por dev por sprint
      const g2 = await Promise.all(
        sprints.map(async (sp) => {
          const row = { sprintName: sp.name, ...makeBlank() };
          await Promise.all(
            devs.map(async (d) => {
              const raw = await fetch(
                `${config.apiBaseUrl}/api/task-assignees/user/${d.id}/sprint/${sp.id}/real-hours`
              ).then((r) => r.json()).catch(() => 0);
              row[d.id] = Number(raw) || 0;
            })
          );
          return row;
        })
      );
      setHoursByDevPerSprint(g2);
  
      // 3) Gráfica 3: tareas completadas por dev por sprint
      const g3 = await Promise.all(
        sprints.map(async (sp) => {
          const row = { sprintName: sp.name, ...makeBlank() };
          await Promise.all(
            devs.map(async (d) => {
              const c = await fetch(
                `${config.apiBaseUrl}/api/task-assignees/user/${d.id}/sprint/${sp.id}/done/count`
              ).then((r) => r.json()).catch(() => 0);
              row[d.id] = Number(c) || 0;
            })
          );
          return row;
        })
      );
      setTasksByDevPerSprint(g3);
    })();
  
  }, [sprints, members]);
  
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
  "#eff6ff", // blue-50
  "#dbeafe", // blue-100
  "#bfdbfe", // blue-200
  "#93c5fd", // blue-300
  "#60a5fa", // blue-400
  "#3b82f6", // blue-500
  "#2563eb", // blue-600
  "#1d4ed8", // blue-700
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
          <label
            htmlFor="filterTypeSelect"
            className="block text-sm text-gray-200 font-bold mb-1"
          >
            Filtrar por
          </label>
          <select
            id="filterTypeSelect"
            value={filterType}
            onChange={(e) => setFilterType(e.target.value)}
            className="bg-[#212233] bg-opacity-30 text-white px-3 py-2 rounded-xl"
          >
            <option value="sprint">sprint</option>
            <option value="week">semana</option>
            <option value="month">mes</option>
          </select>
        </div>

        {filterType === "sprint" && (
          <div>
            <label
              id="sprintSelect"
              className="block text-sm text-gray-200 font-bold mb-1"
            >
              Sprint
            </label>
            <select
              id="sprintSelect"
              value={selectedSprint ?? ""}
              onChange={(e) => setSelectedSprint(e.target.value)}
              className="bg-[#212233] bg-opacity-30 text-white px-3 py-2 rounded-xl"
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
              className="bg-[#212233] bg-opacity-30 text-white px-3 py-2 rounded-xl"
              placeholderText="Selecciona fecha"
              dateFormat="yyyy-MM-dd"
            />
          </div>
        )}

        {/* miembro */}
        <div>
          <label
            htmlFor="memberSelect"
            className="block text-sm text-gray-200 font-bold mb-1"
          >
            Miembro
          </label>
          <select
            id="memberSelect"
            value={selectedMember ?? ""}
            onChange={(e) => setSelectedMember(e.target.value)}
            className="bg-[#212233] bg-opacity-30 text-white px-3 py-2 rounded-xl"
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
      <div className="p-4 rounded bg-black bg-opacity-10">
        <h2 className="text-white font-semibold mb-2">
          Gráfica 1: Horas Totales trabajadas por Sprint
        </h2>
        <ResponsiveContainer width="100%" height={260}>
          <BarChart data={totalHoursBySprint}>
            {/* Fondo tenue dentro del SVG */}
            <rect x="0" y="0" width="100%" height="100%" fill="rgba(0,0,0,0.1)" />
            {/* Gradiente negro→azul */}
            <defs>
              <linearGradient id="blackToRed700" x1="0" y1="1" x2="0" y2="0">
                <stop offset="0%" stopColor="#007dff" />
                <stop offset="100%" stopColor="#60a5fa" /> 
              </linearGradient>
            </defs>

            <CartesianGrid strokeDasharray="3 3" stroke="#555" />
            <XAxis dataKey="sprintName" stroke="#ccc" />
            <YAxis stroke="#ccc" />
            <Tooltip
              contentStyle={{ backgroundColor: 'rgba(0,0,0,0.3)' }}
              labelStyle={{ color: '#fff' }}
              itemStyle={{ color: '#fff' }}
            />

            <Bar
              dataKey="hours"
              name="Horas"
              fill="url(#blackToRed700)"
              onMouseOver={e => e.target.setAttribute('fill','rgba(0,0,0,0.3)')}
              onMouseOut={e => e.target.setAttribute('fill','url(#blackToRed700)')}
            />
          </BarChart>
        </ResponsiveContainer>
      </div>
    )}

      {/* ================= GRÁFICA 2 ================= */}
      {hoursByDevPerSprint.length > 0 && (
        <div className="p-4 rounded bg-black bg-opacity-10">
          <h2 className="text-white font-semibold mb-2">
            Gráfica 2: <b>Horas Trabajadas</b> por Developer por Sprint
          </h2>
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={hoursByDevPerSprint}>
              {/* un gradiente por cada dev, oscuro abajo→su color arriba */}
              <defs>
                {members.filter(m => m.id !== "all" && !EXCLUDED.includes(m.name)).map(dev => (
                  <linearGradient
                    key={dev.id}
                    id={`grad-g2-${dev.id}`}
                    x1="0" y1="1" x2="0" y2="0"
                  >
                    <stop offset="0%" stopColor="#312D2A" />
                    <stop offset="100%" stopColor={dev.color} />
                  </linearGradient>
                ))}
              </defs>

              <CartesianGrid strokeDasharray="3 3" stroke="#555" />
              <XAxis dataKey="sprintName" tick={{ fill: '#fff' }} />
              <YAxis tick={{ fill: '#fff' }} />
              <Tooltip
                contentStyle={{ backgroundColor: 'rgba(0,0,0,0.3)' }}
                labelStyle={{ color: '#fff' }}
                itemStyle={{ color: '#fff' }}
              />
              <Legend wrapperStyle={{ color: '#fff' }} />

              {members.filter(m => m.id !== "all" && !EXCLUDED.includes(m.name)).map(dev => (
                <Bar
                  key={dev.id}
                  dataKey={String(dev.id)}
                  name={dev.name}
                  fill={`url(#grad-g2-${dev.id})`}
                  onMouseOver={e => e.target.setAttribute('fill','rgba(0,0,0,0.3)')}
                  onMouseOut={e => e.target.setAttribute('fill',`url(#grad-g2-${dev.id})`)}
                />
              ))}
            </BarChart>
          </ResponsiveContainer>
        </div>
      )}

      {/* ================= GRÁFICA 3 ================= */}
      {tasksByDevPerSprint.length > 0 && (
        <div className="p-4 rounded bg-black bg-opacity-10">
          <h2 className="text-white font-semibold mb-2">
            Gráfica 3: <b>Tareas Completadas</b> por Developer por Sprint
          </h2>
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={tasksByDevPerSprint}>
              {/* mismo patrón de gradientes que en G2 */}
              <defs>
                {members.filter(m => m.id !== "all" && !EXCLUDED.includes(m.name)).map(dev => (
                  <linearGradient
                    key={dev.id}
                    id={`grad-g3-${dev.id}`}
                    x1="0" y1="1" x2="0" y2="0"
                  >
                    <stop offset="0%" stopColor="#312D2A" />
                    <stop offset="100%" stopColor={dev.color} />
                  </linearGradient>
                ))}
              </defs>

              <CartesianGrid strokeDasharray="3 3" stroke="#555" />
              <XAxis dataKey="sprintName" tick={{ fill: '#fff' }} />
              <YAxis tick={{ fill: '#fff' }} />
              <Tooltip
                contentStyle={{ backgroundColor: 'rgba(0,0,0,0.3)' }}
                labelStyle={{ color: '#fff' }}
                itemStyle={{ color: '#fff' }}
              />
              <Legend wrapperStyle={{ color: '#fff' }} />

              {members.filter(m => m.id !== "all" && !EXCLUDED.includes(m.name)).map(dev => (
                <Bar
                  key={dev.id}
                  dataKey={String(dev.id)}
                  name={dev.name}
                  fill={`url(#grad-g3-${dev.id})`}
                  onMouseOver={e => e.target.setAttribute('fill','rgba(0,0,0,0.3)')}
                  onMouseOut={e => e.target.setAttribute('fill',`url(#grad-g3-${dev.id})`)}
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
              c: "blue-400",
            },
            { l: "Story Points", v: reportData.storyPoints, c: "blue-400" },
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

     {/* ================= KPI-1 + COMPOSED detalle con scroll y área ==================== */}
    {barChartData.length > 0 && (
      <div className="grid grid-cols-6 gap-6 mt-10">
        {/* KPI 1 */}
        <div className="col-span-1 bg-black/30 rounded-2xl p-6 shadow-lg">
          <h4 className="text-sm text-gray-400">KPI 1 Time Efficiency</h4>
          <p className="text-3xl font-bold text-blue-400">
            {reportData?.kpi1}
          </p>
        </div>

        {/* Chart con scroll */}
        <div className="col-span-5 bg-black/30 rounded-2xl p-4 overflow-x-auto">
          <div
            style={{
              width: `${Math.max(barChartData.length * 80, 600)}px`,
              minWidth: '100%',
              height: '300px',
            }}
          >
            <ResponsiveContainer width="100%" height="100%">
            <ComposedChart
              data={barChartData}
              margin={{ top: 20, right: 20, bottom: 60, left: 0 }}
            >
              {/* degradados de área */}
              <defs>
                {/* área real */}
                <linearGradient id="areaGradientReal" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stopColor="#60a5fa" stopOpacity={0.6}/>
                  <stop offset="100%" stopColor="#60a5fa" stopOpacity={0.1}/>
                </linearGradient>
                {/* área estimada */}
                <linearGradient id="areaGradientEst" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stopColor="#1d4ed8" stopOpacity={0.6}/>
                  <stop offset="100%" stopColor="#1d4ed8" stopOpacity={0.1}/>
                </linearGradient>
              </defs>

              <CartesianGrid strokeDasharray="3 3" stroke="#555" />
              <XAxis
                dataKey="name"
                  tick={false}
                  axisLine={false}
                  tickLine={false}
              />
              <YAxis tick={{ fill: '#fff' }} />
              <Tooltip
                contentStyle={{ backgroundColor: 'rgba(0,0,0,0.3)' }}
                labelStyle={{ color: '#fff' }}
                itemStyle={{ color: '#fff' }}
              />
              <Legend wrapperStyle={{ color: '#fff' }} />

              {/* Área Horas Reales */}
              <Area
                type="monotone"
                dataKey="real"
                name="Horas Reales (área)"
                stroke="#60a5fa"
                fill="url(#areaGradientReal)"
                activeDot={{ r: 6, strokeWidth: 2, stroke: '#60a5fa' }}
              />

              {/* Área Horas Estimadas */}
              <Area
                type="monotone"
                dataKey="estimated"
                name="Horas Estimadas (área)"
                stroke="#1d4ed8"
                fill="url(#areaGradientEst)"
                activeDot={{ r: 6, strokeWidth: 2, stroke: '#1d4ed8' }}
              />
              {/* Barras: estimadas y reales */}
              <Bar
                dataKey="estimated"
                name="Horas Estimadas"
                barSize={20}
                fill="#1d4ed8"
              />
              <Bar
                dataKey="real"
                name="Horas Reales"
                barSize={20}
                fill="#60a5fa"
              />
            </ComposedChart>
          </ResponsiveContainer>

          </div>
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
