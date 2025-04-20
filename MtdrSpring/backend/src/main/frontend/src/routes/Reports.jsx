import React, { useEffect, useState } from 'react';
import DatePicker from 'react-datepicker';
import "react-datepicker/dist/react-datepicker.css";
import { useParams }       from 'react-router-dom';
function Reports() {
  const { projectId } = useParams(); 
  const [sprints, setSprints] = useState([]);
  const [members, setMembers] = useState([]);
  const [filterType, setFilterType] = useState('sprint');
  const [selectedSprint, setSelectedSprint] = useState(null);
  const [selectedMember, setSelectedMember] = useState(null);
  const [selectedDate, setSelectedDate] = useState('');
  const [reportData, setReportData] = useState(null);

  useEffect(() => {
    fetch(`http://localhost:8081/api/sprints/project/${projectId}`)
      .then(res => res.json())
      .then(data => {
        const simplified = data.map(s => ({
          id: s.id_sprint,
          name: s.name,
        }));
        setSprints(simplified);
      })
      .catch(err => console.error('Error fetching sprints:', err));
  }, []);

  useEffect(() => {
    fetch(`http://localhost:8081/api/projects/${projectId}`)
      .then(res => res.json())
      .then(data => {
        const users = data.projectUsers.map(pu => ({
          id: pu.idProjectUser,
          name: pu.user.name,
        }));
        // Agregar opción "Todos"
        setMembers([{ id: 'all', name: 'Todo el equipo' }, ...users]);
      })
      .catch(err => console.error('Error fetching members:', err));
  }, []);

  const handleGenerateReport = async () => {
    if (!selectedMember) {
      alert('Selecciona un miembro.');
      return;
    }

    let doneTasksCountEndpoint = '';
    let doneTasksDataEndpoint = '';
    let realHoursEndpoint = '';

    const isTeam = selectedMember === 'all';

    if (filterType === 'sprint') {
      if (!selectedSprint) {
        alert('Selecciona un sprint.');
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

    } else if (filterType === 'week') {
      if (!selectedDate) {
        alert('Selecciona una semana.');
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

    } else if (filterType === 'month') {
      if (!selectedDate) {
        alert('Selecciona un mes.');
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
    }

    try {
      const [
        doneRes, 
        doneDataRes, 
        // hoursRes
      ] = await Promise.all([
        fetch(doneTasksCountEndpoint),
        fetch(doneTasksDataEndpoint),
        // fetch(realHoursEndpoint),
      ]);

      const [
        doneCount, 
        doneData, 
        // realHours
      ] = await Promise.all([
        doneRes.json(),
        doneDataRes.json(),
        // hoursRes.json(),
      ]);

      const selectedUser = members.find(m => m.id.toString() === selectedMember.toString());

      let estimatedHours = 0;
      let realHours = 0;

      const processedData = doneData.map(item => {
        const { task, ...rest } = item;
        const { sprint, ...taskData } = task;

        estimatedHours = estimatedHours + task.estimatedHours;
        realHours = realHours + task.realHours;

        
        return {
          ...rest,
          task: taskData,
        };
      });
    
      setReportData({
        user: selectedUser?.name || '—',
        tasksDone: doneCount ?? '—',
        realHours: realHours ?? '—',
        tasksData: processedData ?? [],
        estimatedHours,
      });
    } catch (error) {
      console.error('Error generando el reporte:', error);
    }
  };

  return (
    <div className="p-6 space-y-6">
      <h1 className="text-3xl font-bold text-white">Reportes</h1>

      <div className="flex flex-wrap items-end gap-4">
        <div>
          <label className="block mb-1 text-sm text-gray-200 font-bold">Filtrar por</label>
          <select
            value={filterType}
            onChange={(e) => setFilterType(e.target.value)}
            className="bg-customDark bg-opacity-30 text-white px-3 py-2 rounded-xl"
          >
            <option value="sprint" >Sprint</option>
            <option value="week" >Semana</option>
            <option value="month" >Mes</option>
          </select>
        </div>

        {filterType === 'sprint' && (
          <div>
            <label className="block mb-1 text-sm text-gray-200 font-bold">Sprint</label>
            <select
              value={selectedSprint ?? ''}
              onChange={(e) => setSelectedSprint(e.target.value)}
              className="bg-customDark bg-opacity-30 rounded-xl text-white px-3 py-2"
            >
              <option value="" disabled>Selecciona un sprint</option>
              {sprints.map((s) => (
                <option key={s.id} value={s.id}>{s.name}</option>
              ))}
            </select>
          </div>
        )}

        {filterType !== 'sprint' && (
          <div>
            <label className="block mb-1 text-sm text-gray-200 font-bold">
              {filterType === 'week' ? 'Semana' : 'Mes'}
            </label>
            <DatePicker
              selected={selectedDate ? new Date(selectedDate) : null}
              onChange={date =>
                setSelectedDate(date ? date.toISOString().slice(0,10) : '')
              }
              /* — apariencia del input “cerrado” — */
              className='bg-customDark bg-opacity-30 rounded-xl text-white px-3 py-2'
              placeholderText="Selecciona fecha"
              dateFormat="yyyy-MM-dd"
              /* — apariencia del pop‑over — */
              calendarClassName="!bg-customDark !bg-opacity-30 text-white rounded-xl p-2"
              dayClassName={() =>
                "rounded-full transition hover:bg-black hover:bg-opacity-40"}
            />
          </div>
        )}

        <div>
          <label className="block mb-1 text-sm text-gray-200 font-bold">Miembro</label>
          <select
            value={selectedMember ?? ''}
            onChange={(e) => setSelectedMember(e.target.value)}
            className="bg-customDark bg-opacity-30 text-white px-3 py-2 rounded-xl"
          >
            <option value="" disabled>Selecciona un miembro</option>
            {members.map((m) => (
              <option key={m.id} value={m.id}>{m.name}</option>
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

      <div className="mt-6 grid grid-cols-1 sm:grid-cols-4 gap-6">
        <div className="bg-black bg-opacity-30 rounded-2xl p-6 shadow-lg">
          <h2 className="text-sm text-gray-400">Usuario</h2>
          <p className="text-2xl font-semibold text-white mt-1">
            {reportData?.user ?? '—'}
          </p>
        </div>

        <div className="bg-black bg-opacity-30 rounded-2xl p-6 shadow-lg">
          <h2 className="text-sm text-gray-400">Tareas Completadas</h2>
          <p className="text-3xl font-bold text-green-400 mt-1">
            {reportData?.tasksDone ?? '—'}
          </p>
        </div>

        <div className="bg-black bg-opacity-30 rounded-2xl p-6 shadow-lg">
          <h2 className="text-sm text-gray-400">Horas Reales</h2>
          <p className="text-3xl font-bold text-blue-400 mt-1">
            {reportData?.realHours ?? '—'}
          </p>
        </div>

        <div className="bg-black bg-opacity-30 rounded-2xl p-6 shadow-lg">
          <h2 className="text-sm text-gray-400">Horas Estimadas</h2>
          <p className="text-3xl font-bold text-blue-400 mt-1">
            {reportData?.estimatedHours ?? '—'}
          </p>
        </div>
      </div>

      {/* TASKS TABLE with rows: member, task, storypoints, status, realHours, estimatedHours, kpi score */}
      {reportData?.tasksData?.length > 0 && (
        <div className="mt-10">
          <h2 className="text-xl font-semibold mb-4 text-white">Tareas Completadas</h2>
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
                  <th className="px-6 py-3">KPI Score (%)</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {reportData.tasksData.map((row, idx) => {
                  const task = row.task;
                  const user = row.projectUser?.user?.name || '—';
                  const realHours = task.realHours || 0;
                  const estimatedHours = task.estimatedHours || 1; // Avoid division by 0
                  const kpi = Math.min((realHours / estimatedHours) * 100, 200).toFixed(0);

                  return (
                    <tr key={idx} className="hover:bg-black bg-opacity-50 transition duration-200">
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
                        {reportData.tasksData.reduce((sum, row) => sum + (row.task.storyPoints || 0), 0)}
                      </td>
                      <td className="px-6 py-4"></td>
                      <td className="px-6 py-4">
                        {reportData.tasksData.reduce((sum, row) => sum + (row.task.realHours || 0), 0)}
                      </td>
                      <td className="px-6 py-4">
                        {reportData.tasksData.reduce((sum, row) => sum + (row.task.estimatedHours || 0), 0)}
                      </td>
                      <td className="px-6 py-4">
                        {(() => {
                          const kpis = reportData.tasksData.map(row => {
                            const real = row.task.realHours || 0;
                            const est = row.task.estimatedHours || 1;
                            return (est / real) * 100;
                          });
                          const avgKPI = kpis.length ? (kpis.reduce((a, b) => a + b, 0) / kpis.length) : 0;
                          return `${avgKPI.toFixed(0)}%`;
                        })()}
                      </td>
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