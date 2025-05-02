import React, { useEffect, useState } from "react";
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
  ResponsiveContainer, // BarChart KPI 1
  PieChart,
  Pie,
  Cell, // Pie Chart KPI 2
} from "recharts";

function Reports() {
  const { projectId } = useParams();
  const [sprints, setSprints] = useState([]);
  const [members, setMembers] = useState([]);
  const [filterType, setFilterType] = useState("sprint");
  const [selectedSprint, setSelectedSprint] = useState(null);
  const [selectedMember, setSelectedMember] = useState(null);
  const [selectedDate, setSelectedDate] = useState("");
  const [reportData, setReportData] = useState(null);

  useEffect(() => {
    fetch(`http://localhost:8081/api/sprints/project/${projectId}`)
      .then((res) => res.json())
      .then((data) => {
        const simplified = data.map((s) => ({
          id: s.id_sprint,
          name: s.name,
        }));
        setSprints(simplified);
      })
      .catch((err) => console.error("Error fetching sprints:", err));
  }, []);

  useEffect(() => {
    fetch(`http://localhost:8081/api/projects/${projectId}`)
      .then((res) => res.json())
      .then((data) => {
        const users = data.projectUsers.map((pu) => ({
          id: pu.idProjectUser,
          name: pu.user.name,
        }));
        // Agregar opción "Todos"
        setMembers([{ id: "all", name: "Todo el equipo" }, ...users]);
      })
      .catch((err) => console.error("Error fetching members:", err));
  }, []);

  const handleGenerateReport = async () => {
    if (!selectedMember) {
      alert("Selecciona un miembro.");
      return;
    }

    let doneTasksCountEndpoint = "";
    let doneTasksDataEndpoint = "";
    // let realHoursEndpoint = '';
    // let timeLogsEndpoint = '';

    const isTeam = selectedMember === "all";

    if (filterType === "sprint") {
      if (!selectedSprint) {
        alert("Selecciona un sprint.");
        return;
      }
      doneTasksCountEndpoint = isTeam
        ? `http://localhost:8081/api/task-assignees/team-sprint/${selectedSprint}/done/count`
        : `http://localhost:8081/api/task-assignees/user/${selectedMember}/sprint/${selectedSprint}/done/count`;

      doneTasksDataEndpoint = isTeam
        ? `http://localhost:8081/api/task-assignees/team-sprint/${selectedSprint}/done`
        : `http://localhost:8081/api/task-assignees/user/${selectedMember}/sprint/${selectedSprint}/done`;

      // realHoursEndpoint = isTeam
      //   ? `http://localhost:8081/api/timelogs/team-sprint/${selectedSprint}/real-hours`
      //   : `http://localhost:8081/api/timelogs/individual-sprint/${selectedSprint}/${selectedMember}/real-hours`;

      // timeLogsEndpoint = isTeam
      //    ? `http://localhost:8081/api/timelogs/team-sprint/${selectedSprint}/time-logs`
      //    : `http://localhost:8081/api/timelogs/individual-sprint/${selectedSprint}/${selectedMember}/time-logs`;
    } else if (filterType === "week") {
      if (!selectedDate) {
        alert("Selecciona una semana.");
        return;
      }
      doneTasksCountEndpoint = isTeam
        ? `http://localhost:8081/api/task-assignees/team-week/${selectedDate}/project/${projectId}/done/count`
        : `http://localhost:8081/api/task-assignees/user/${selectedMember}/week/${selectedDate}/done/count`;

      doneTasksDataEndpoint = isTeam
        ? `http://localhost:8081/api/task-assignees/team-week/${selectedDate}/project/${projectId}/done`
        : `http://localhost:8081/api/task-assignees/user/${selectedMember}/week/${selectedDate}/done`;

      // realHoursEndpoint = isTeam
      //   ? `http://localhost:8081/api/timelogs/team-week/${selectedDate}/${projectId}/real-hours`
      //   : `http://localhost:8081/api/timelogs/individual-week/${selectedDate}/${selectedMember}/real-hours`;

      // timeLogsEndpoint = isTeam
      //   ? `http://localhost:8081/api/timelogs/team-week/${selectedDate}/${projectId}/time-logs`
      //   : `http://localhost:8081/api/timelogs/individual-week/${selectedDate}/${selectedMember}/time-logs`;
    } else if (filterType === "month") {
      if (!selectedDate) {
        alert("Selecciona un mes.");
        return;
      }
      doneTasksCountEndpoint = isTeam
        ? `http://localhost:8081/api/task-assignees/team-month/${selectedDate}/project/${projectId}/done/count`
        : `http://localhost:8081/api/task-assignees/user/${selectedMember}/month/${selectedDate}/done/count`;

      doneTasksDataEndpoint = isTeam
        ? `http://localhost:8081/api/task-assignees/team-month/${selectedDate}/project/${projectId}/done`
        : `http://localhost:8081/api/task-assignees/user/${selectedMember}/month/${selectedDate}/done`;

      // realHoursEndpoint = isTeam
      //   ? `http://localhost:8081/api/timelogs/team-month/${selectedDate}/${projectId}/real-hours`
      //   : `http://localhost:8081/api/timelogs/individual-month/${selectedDate}/${selectedMember}/real-hours`;

      // timeLogsEndpoint = isTeam
      //   ? `http://localhost:8081/api/timelogs/team-month/${selectedDate}/${projectId}/time-logs`
      //   : `http://localhost:8081/api/timelogs/individual-month/${selectedDate}/${selectedMember}/time-logs`;
    }

    try {
      const [
        doneRes,
        doneDataRes,
        // hoursRes,
        // timeLogsRes,
      ] = await Promise.all([
        fetch(doneTasksCountEndpoint),
        fetch(doneTasksDataEndpoint),
        // fetch(realHoursEndpoint),
        // fetch(timeLogsEndpoint)
      ]);

      const [
        doneCount,
        doneData,
        // realHours,
        // timeLogs,
      ] = await Promise.all([
        doneRes.json(),
        doneDataRes.json(),
        // hoursRes.json(),
        // timeLogsRes.json(),
      ]);

      const selectedUser = members.find(
        (m) => m.id.toString() === selectedMember.toString(),
      );

      let estimatedHours = 0;
      let realHours = 0;
      let storyPoints = 0;
      const storyPointsFrequency = {};

      const processedData = doneData.map((item) => {
        const { task, ...rest } = item;
        const { sprint, ...taskData } = task;

        estimatedHours = estimatedHours + task.estimatedHours;
        realHours = realHours + task.realHours;
        let sp = task.storyPoints;
        storyPoints = storyPoints + sp;
        storyPointsFrequency[sp] = (storyPointsFrequency[sp] || 0) + 1;

        return {
          ...rest,
          task: taskData,
        };
      });

      const totalSp = Object.values(storyPointsFrequency).reduce(
        (a, b) => a + b,
        0,
      );

      const spFrequencyChartData = Object.entries(storyPointsFrequency).map(
        ([sp, count]) => ({
          name: `${sp} SP`,
          value: count,
          percent: ((count / totalSp) * 100).toFixed(1),
        }),
      );

      let kpi1 = (estimatedHours / realHours) * 100 || 0;
      kpi1 = Number(kpi1.toFixed(2));

      let kpi2 = storyPoints / doneData.length || 0;
      kpi2 = Number(kpi2.toFixed(2));

      // const processedTimeLogs = timeLogs.map(item => {
      //   const { idTimeLogs, startTs, endTs } = item;
      //   return {
      //     idTimeLogs, startTs, endTs,
      //   };
      // });

      setReportData({
        user: selectedUser?.name || "—",
        tasksDone: doneCount ?? "—",
        realHours: realHours ?? "—",
        tasksData: processedData ?? [],
        // timeLogs: processedTimeLogs ?? [],
        estimatedHours,
        storyPoints,
        spFrequencyChartData: spFrequencyChartData ?? [],
        kpi1,
        kpi2,
      });
    } catch (error) {
      console.error("Error generando el reporte:", error);
    }
  };

  // CHARTS KPI 1 and KPI 2.

  const barChartData = reportData?.tasksData?.map((row) => {
    return {
      name: row.task.name,
      real: row.task.realHours || 0,
      estimated: row.task.estimatedHours || 0,
    };
  });

  const COLORS = [
    "#ffffff", // Blanco
    "#d3d3d3", // Gris claro
    "#a9a9a9", // Gris medio
    "#808080", // Gris oscuro
    "#585858", // Gris muy oscuro
    "#2f2f2f", // Gris casi negro
    "#1a1a1a", // Gris casi negro
    "#000000", // Negro
  ];

  return (
    <div className="p-6 space-y-6">
      <h1 className="text-3xl font-bold text-white">Reportes</h1>

      <div className="flex flex-wrap items-end gap-4">
        <div>
<<<<<<< HEAD
          <label className="block mb-1 text-sm text-gray-200 font-bold">
=======
          <label htmlFor="filterTypeSelect" className="block mb-1 text-sm text-gray-200 font-bold">
>>>>>>> test
            Filtrar por
          </label>
          <select
            id="filterTypeSelect"
            value={filterType}
            onChange={(e) => setFilterType(e.target.value)}
            className="bg-customDark bg-opacity-30 text-white px-3 py-2 rounded-xl"
          >
<<<<<<< HEAD
            <option value="sprint">Sprint</option>
            <option value="week">Semana</option>
            <option value="month">Mes</option>
=======
            <option value="sprint">sprint</option>
            <option value="week">semana</option>
            <option value="month">mes</option>
>>>>>>> test
          </select>
        </div>

        {filterType === "sprint" && (
          <div>
<<<<<<< HEAD
            <label className="block mb-1 text-sm text-gray-200 font-bold">
              Sprint
            </label>
            <select
=======
            <label htmlFor="sprintSelect" className="block mb-1 text-sm text-gray-200 font-bold">
              Sprint
            </label>
            <select
              id="sprintSelect"
>>>>>>> test
              value={selectedSprint ?? ""}
              onChange={(e) => setSelectedSprint(e.target.value)}
              className="bg-customDark bg-opacity-30 rounded-xl text-white px-3 py-2"
            >
<<<<<<< HEAD
              <option value="" disabled>
                Selecciona un sprint
              </option>
              {sprints.map((s) => (
                <option key={s.id} value={s.id}>
=======
              <option key="sprint-none" value="" disabled>
                Selecciona un sprint
              </option>
              {sprints.map((s) => (
                <option key={`sprint-${s.id}`} value={s.id}>
>>>>>>> test
                  {s.name}
                </option>
              ))}
            </select>
          </div>
        )}

        {filterType !== "sprint" && (
          <div>
            <label className="block mb-1 text-sm text-gray-200 font-bold">
              {filterType === "week" ? "Semana" : "Mes"}
            </label>
            <DatePicker
              selected={selectedDate ? new Date(selectedDate) : null}
              onChange={(date) =>
                setSelectedDate(date ? date.toISOString().slice(0, 10) : "")
              }
              /* — apariencia del input “cerrado” — */
              className="bg-customDark bg-opacity-30 rounded-xl text-white px-3 py-2"
              placeholderText="Selecciona fecha"
              dateFormat="yyyy-MM-dd"
              /* — apariencia del pop‑over — */
              calendarClassName="!bg-customDark !bg-opacity-30 text-white rounded-xl p-2"
              dayClassName={() =>
                "rounded-full transition hover:bg-black hover:bg-opacity-40"
              }
            />
          </div>
        )}

        <div>
<<<<<<< HEAD
          <label className="block mb-1 text-sm text-gray-200 font-bold">
            Miembro
          </label>
          <select
=======
          <label htmlFor="memberSelect" className="block mb-1 text-sm text-gray-200 font-bold">
            Miembro
          </label>
          <select
            id="memberSelect"
>>>>>>> test
            value={selectedMember ?? ""}
            onChange={(e) => setSelectedMember(e.target.value)}
            className="bg-customDark bg-opacity-30 text-white px-3 py-2 rounded-xl"
          >
            <option value="" disabled>
              Selecciona un miembro
            </option>
            {members.map((m) => (
<<<<<<< HEAD
              <option key={m.id} value={m.id}>
=======
              <option key={`sprint-${m.id}`} value={m.id}>
>>>>>>> test
                {m.name}
              </option>
            ))}
          </select>
        </div>

        <button
          onClick={handleGenerateReport}
          className="bg-transparent text-white px-3 py-2 rounded-xl hover:border-2  hover:border-cyan-500 hover:text-white transition"
        >
          Generar Reporte
        </button>
      </div>

      <div className="mt-6 grid grid-cols-1 sm:grid-cols-5 gap-6">
        <div className="bg-black bg-opacity-30 rounded-2xl p-6 shadow-lg">
          <h2 className="text-sm text-gray-400">Usuario</h2>
<<<<<<< HEAD
          <p className="text-2xl font-semibold text-white mt-1">
=======
          <p aria-label="report-user" className="text-2xl font-semibold text-white mt-1">
>>>>>>> test
            {reportData?.user ?? "—"}
          </p>
        </div>

        <div className="bg-black bg-opacity-30 rounded-2xl p-6 shadow-lg">
          <h2 className="text-sm text-gray-400">Tareas Completadas</h2>
<<<<<<< HEAD
          <p className="text-3xl font-bold text-green-400 mt-1">
=======
          <p aria-label="report-tasksdone" className="text-3xl font-bold text-green-400 mt-1">
>>>>>>> test
            {reportData?.tasksDone ?? "—"}
          </p>
        </div>

        <div className="bg-black bg-opacity-30 rounded-2xl p-6 shadow-lg">
          <h2 className="text-sm text-gray-400">Story Points</h2>
<<<<<<< HEAD
          <p className="text-3xl font-bold text-green-400 mt-1">
=======
          <p aria-label="report-storypoints" className="text-3xl font-bold text-green-400 mt-1">
>>>>>>> test
            {reportData?.storyPoints ?? "—"}
          </p>
        </div>

        <div className="bg-black bg-opacity-30 rounded-2xl p-6 shadow-lg">
          <h2 className="text-sm text-gray-400">Horas Reales</h2>
<<<<<<< HEAD
          <p className="text-3xl font-bold text-blue-400 mt-1">
=======
          <p aria-label="report-realhours" className="text-3xl font-bold text-blue-400 mt-1">
>>>>>>> test
            {reportData?.realHours ?? "—"}
          </p>
        </div>

        <div className="bg-black bg-opacity-30 rounded-2xl p-6 shadow-lg">
          <h2 className="text-sm text-gray-400">Horas Estimadas</h2>
<<<<<<< HEAD
          <p className="text-3xl font-bold text-blue-400 mt-1">
=======
          <p aria-label="report-estimatedhours" className="text-3xl font-bold text-blue-400 mt-1">
>>>>>>> test
            {reportData?.estimatedHours ?? "—"}
          </p>
        </div>
      </div>

      {/* Row 2 - BarChart + KPI 1 */}
      <div className="mt-10 grid grid-cols-6 gap-6">
        {/* KPI 1 Time Efficiency Score */}
        <div className="col-span-1 bg-black bg-opacity-30 rounded-2xl p-6 shadow-lg">
          <h2 className="text-sm text-gray-400">KPI 1 Time Efficiency Score</h2>
<<<<<<< HEAD
          <p className="text-3xl font-bold text-blue-400 mt-1">
=======
          <p aria-label="report-kpi1" className="text-3xl font-bold text-blue-400 mt-1">
>>>>>>> test
            {reportData?.kpi1 ?? "—"}
          </p>
        </div>

        {/* BarChart KPI 1 */}
        {barChartData && barChartData.length > 0 && (
          <div className="col-span-5 bg-black bg-opacity-30 p-4 rounded-2xl">
            <h2 className="text-xl font-semibold mb-4 text-white">
              Comparación de Horas por Tarea
            </h2>
            <ResponsiveContainer width="100%" height={300}>
              <BarChart
                data={barChartData}
                margin={{ top: 20, right: 30, left: 0, bottom: 5 }}
              >
                <CartesianGrid strokeDasharray="3 3" stroke="#555" />
                <XAxis stroke="#ccc" />
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
        )}
      </div>

      {/* Row 3 - PieChart + KPI 2 */}
      <div className="mt-10 grid grid-cols-6 gap-6">
        {/* KPI 2 Tryhard Score */}
        <div className="col-span-1 bg-black bg-opacity-30 rounded-2xl p-6 shadow-lg">
          <h2 className="text-sm text-gray-400">KPI 2 Tryhard Score</h2>
<<<<<<< HEAD
          <p className="text-3xl font-bold text-blue-400 mt-1">
=======
          <p aria-label="report-kpi2" className="text-3xl font-bold text-blue-400 mt-1">
>>>>>>> test
            {reportData?.kpi2 ?? "—"}
          </p>
        </div>

        {/* PieChart KPI 2 */}
        {reportData?.spFrequencyChartData?.length > 0 && (
          <div className="col-span-5 bg-black bg-opacity-30 p-4 rounded-2xl flex justify-center items-center">
            <div className="flex justify-center items-center">
              <h2 className="text-xl font-semibold mb-4 text-white">
                Dificultad de las tareas
              </h2>
              <PieChart width={400} height={400}>
                <Pie
                  data={reportData.spFrequencyChartData}
                  cx="50%"
                  cy="50%"
                  label={({ name, value, percent }) =>
                    `${name}: ${value} (${percent}%)`
                  }
                  outerRadius={150}
                  fill="#8884d8"
                  dataKey="value"
                >
                  {reportData.spFrequencyChartData.map((_, index) => (
                    <Cell
                      key={`cell-${index}`}
                      fill={COLORS[index % COLORS.length]}
                    />
                  ))}
                </Pie>
                <Tooltip
                  formatter={(value, name, props) => [
                    `${value} tasks`,
                    props.payload.name,
                  ]}
                />
                <Legend />
              </PieChart>
            </div>
          </div>
        )}
      </div>

      {/* TASKS TABLE with rows: member, task, storypoints, status, realHours, estimatedHours, kpi score */}
      {reportData?.tasksData?.length > 0 && (
        <div className="mt-10">
          <h2 className="text-xl font-semibold mb-4 text-white">
<<<<<<< HEAD
            Tareas Completadas
=======
            Tareas Completadas:
>>>>>>> test
          </h2>
          <div className="overflow-auto rounded-lg shadow ring-1 ring-black ring-opacity-5">
            <table className="min-w-full bg-black bg-opacity-50 text-white text-sm">
              <thead className="bg-black bg-opacity-30 text-left text-xs font-semibold uppercase tracking-wider">
                <tr>
                  <th className="px-6 py-3">Miembro</th>
                  <th className="px-6 py-3">Tarea</th>
                  <th className="px-6 py-3">Story Points</th>
                  <th className="px-6 py-3">Estado</th>
                  <th className="px-6 py-3">Horas Reales</th>
                  <th className="px-6 py-3">Horas Estimadas</th>
                  <th className="px-6 py-3">KPI 1 Time Efficiency Score (%)</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {reportData.tasksData.map((row) => {
                  const task = row.task;
                  const user = row.projectUser?.user?.name || "—";
                  const realHours = task.realHours || 0;
                  const estimatedHours = task.estimatedHours || 1; // Avoid division by 0
                  const kpi = (estimatedHours / realHours) * 100 || 0; // Avoid division by 0

                  return (
                    <tr
<<<<<<< HEAD
                      key={idx}
=======
                      key={`row-task-${row.task.id}`}
>>>>>>> test
                      className="hover:bg-black bg-opacity-50 transition duration-200"
                    >
                      <td className="px-6 py-4">{user}</td>
                      <td className="px-6 py-4">{task.name}</td>
                      <td className="px-6 py-4">{task.storyPoints}</td>
                      <td className="px-6 py-4">{task.status}</td>
                      <td className="px-6 py-4">{realHours}</td>
                      <td className="px-6 py-4">{estimatedHours}</td>
                      <td className="px-6 py-4">{kpi}%</td>
                    </tr>
                  );
                })}
                <tr className="font-semibold bg-black ">
                  <td className="px-6 py-4">Total</td>
                  <td className="px-6 py-4"></td>
                  <td className="px-6 py-4">
                    {reportData.tasksData.reduce(
                      (sum, row) => sum + (row.task.storyPoints || 0),
                      0,
                    )}
                  </td>
                  <td className="px-6 py-4"></td>
                  <td className="px-6 py-4">
                    {reportData.tasksData.reduce(
                      (sum, row) => sum + (row.task.realHours || 0),
                      0,
                    )}
                  </td>
                  <td className="px-6 py-4">
                    {reportData.tasksData.reduce(
                      (sum, row) => sum + (row.task.estimatedHours || 0),
                      0,
                    )}
                  </td>
                  <td className="px-6 py-4">{reportData.kpi1}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}

export default Reports;
