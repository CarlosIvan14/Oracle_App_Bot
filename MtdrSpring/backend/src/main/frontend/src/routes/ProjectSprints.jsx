// src/routes/ProjectSprints.js
import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import config from "../config";

const IconPlus = () => <span className="text-lg">＋</span>;
const IconCancel = () => <span className="text-lg">✕</span>;

export default function ProjectSprints() {
  const { projectId } = useParams();
  const navigate = useNavigate();

  const [sprints, setSprints] = useState([]);
  const [roleUser, setRoleUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [showModal, setShowModal] = useState(false);
  const [newName, setNewName] = useState("");
  const [newDate, setNewDate] = useState("");
  const [newDesc, setNewDesc] = useState("Active");

  // 1) Carga sprints + rol
  useEffect(() => {
    Promise.all([
      fetch(`${config.apiBaseUrl}/api/sprints/project/${projectId}`).then(
        (r) => (r.ok ? r.json() : Promise.reject("Error sprints")),
      ),
      fetch(
        `${config.apiBaseUrl}/api/project-users/role-user/project-id/${projectId}/user-id/${
          JSON.parse(localStorage.getItem("user")).idUser
        }`,
      ).then((r) => (r.ok ? r.text() : Promise.reject("Error rol"))),
    ])
      .then(([ss, roleText]) => {
        setSprints(ss);
        setRoleUser(roleText.trim());
      })
      .catch(setError)
      .finally(() => setLoading(false));
  }, [projectId]);

  // 2) Listener global para “openAddSprint”
  useEffect(() => {
    const onOpen = () => setShowModal(true);
    window.addEventListener("openAddSprint", onOpen);
    return () => window.removeEventListener("openAddSprint", onOpen);
  }, []);

  // 3) Toggle estado
  const toggle = (sprint) => {
    const d = sprint.description === "Active" ? "idle" : "Active";
    fetch(`${config.apiBaseUrl}/api/sprints/${sprint.id_sprint}`, {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ description: d }),
    })
      .then((r) => {
        if (!r.ok) throw new Error("Error patch");
        setSprints((s) =>
          s.map((x) =>
            x.id_sprint === sprint.id_sprint ? { ...x, description: d } : x,
          ),
        );
      })
      .catch(alert);
  };

  // 4) Crear sprint
  const create = () => {
    if (!newName || !newDate) return alert("Completa los campos");
    const payload = {
      creation_ts: newDate,
      description: newDesc,
      name: newName,
      project: { id_project: +projectId },
    };
    fetch(`${config.apiBaseUrl}/api/sprints`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    })
      .then((r) => (r.ok ? r.json() : Promise.reject("Error crear")))
      .then((s) => setSprints((ss) => [...ss, s]))
      .catch(alert)
      .finally(() => {
        setShowModal(false);
        setNewName("");
        setNewDate("");
        setNewDesc("Active");
      });
  };

  if (loading) return <p className="text-center mt-8 text-white">Cargando…</p>;
  if (error) return <p className="text-center mt-8 text-red-500">{error}</p>;

  // Filtrado para developers
  const visible =
    roleUser === "developer"
      ? sprints.filter((s) => s.description !== "idle")
      : sprints;

  return (
    <div className="p-6">
      <h1 className="text-3xl font-bold mb-6 text-center text-white">
        Sprints del Proyecto {projectId}
      </h1>

      <div className="grid grid-cols-4 gap-4 mb-8">
        {visible.map((s) => (
          <div
            key={s.id_sprint}
            className="bg-black bg-opacity-20 p-4 rounded-2xl hover:bg-opacity-30 cursor-pointer"
            onClick={() =>
              navigate(`/projects/${projectId}/sprint/${s.id_sprint}`)
            }
          >
            <div className="flex justify-between items-center">
              <div>
                <h2 className="text-xl font-semibold text-white">{s.name}</h2>
                <p className="text-white">
                  Estado: {s.description === "Active" ? "🟢" : "🔴"}
                </p>
              </div>
              {roleUser === "manager" && (
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    toggle(s);
                  }}
                  className={`rounded-full text-white font-bold py-1 px-2 ${
                    s.description === "Active"
                      ? "bg-red-600 hover:bg-red-700"
                      : "bg-green-600 hover:bg-green-700"
                  }`}
                >
                  {s.description === "Active" ? "Deshabilitar" : "Habilitar"}
                </button>
              )}
            </div>
          </div>
        ))}
      </div>

      {/* Modal Crear Sprint */}
      {showModal && roleUser === "manager" && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center"
          onClick={() => setShowModal(false)}
        >
          <div
            className="max-w-md w-full p-6 bg-customDark bg-opacity-50 text-white rounded-xl shadow-lg"
            onClick={(e) => e.stopPropagation()}
          >
            <h2 className="text-xl font-bold mb-4">Crear Sprint</h2>
            <div className="space-y-4">
              <div>
                <label className="block mb-1">Nombre</label>
                <input
                  value={newName}
                  onChange={(e) => setNewName(e.target.value)}
                  className="w-full rounded-full p-2 border bg-customDarkligth"
                />
              </div>
              <div>
                <label className="block mb-1">Fecha</label>
                <input
                  type="date"
                  value={newDate}
                  onChange={(e) => setNewDate(e.target.value)}
                  className="w-full rounded-full p-2 border bg-customDarkligth"
                />
              </div>
              <div>
                <input
                  type="hidden"
                  value={newDesc}
                  onChange={(e) => setNewDesc(e.target.value)}
                  className="w-full rounded-full p-2 border bg-customDarkligth"
                />
              </div>
            </div>
            <div className="mt-6 flex justify-end space-x-4">
              <button
                onClick={() => setShowModal(false)}
                className="rounded-full border px-4 py-2 hover:bg-gray-200"
              >
                <IconCancel />
              </button>
              <button
                onClick={create}
                className="rounded-full border border-green-500 px-4 py-2 text-green-500 hover:bg-green-500 hover:text-white"
              >
                <IconPlus /> Crear
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
