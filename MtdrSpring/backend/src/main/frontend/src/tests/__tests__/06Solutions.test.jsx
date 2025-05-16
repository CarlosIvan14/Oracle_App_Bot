// //06 Solutions.test.jsx
// // 6. Reporte KPI Horas trabajadas y Tareas completadas por EQUIPO por semana/sprint

// /*
//     Involved files:
//         - src/App.jsx
//         - src/routes/Reports.jsx
// */

// import React from "react";
// import { render, screen, waitFor } from "@testing-library/react";
// import "@testing-library/jest-dom";
// import userEvent from "@testing-library/user-event";
// import { MemoryRouter, Route, Routes } from "react-router-dom";

// import Reports from "../../routes/Reports";
// import { server } from "../testServer";

// global.ResizeObserver = class {
//   observe() {}
//   unobserve() {}
//   disconnect() {}
// };

// /**
//  * Configura el servidor MSW antes de todos los tests y lo detiene al finalizar.
//  */
// beforeAll(() => server.listen());
// afterEach(() => server.resetHandlers());
// afterAll(() => server.close());

// /**
//  * Renderiza un componente dentro de un router simulado,
//  * posicionándose en la ruta indicada.
//  *
//  * @param ui Componente React a renderizar.
//  * @param options.route Ruta inicial (por defecto "/projects/123/reports").
//  */
// const renderWithRouter = (ui, { route = "/projects/123/reports" } = {}) => {
//   window.history.pushState({}, "Test page", route);
//   return render(
//     <MemoryRouter initialEntries={[route]}>
//       <Routes>
//         <Route path="/projects/:projectId/reports" element={ui} />
//       </Routes>
//     </MemoryRouter>,
//   );
// };

// describe("Reporte de Equipo con KPIs", () => {
//   beforeAll(() => {
//     // Suprime warnings irrelevantes de React Router
//     jest.spyOn(console, "warn").mockImplementation((msg) => {
//       if (msg.includes("React Router Future Flag Warning")) return;
//       console.warn(msg);
//     });
//   });

//   test("muestra KPIs de equipo tras filtrar y generar reporte", async () => {
//     renderWithRouter(<Reports />);

//     // Espera a que cargue la lista de sprints
//     await waitFor(() => {
//       expect(screen.getByText("Sprint 1")).toBeInTheDocument();
//     });

//     // Aplica filtros de sprint y miembro
//     userEvent.selectOptions(screen.getByLabelText("Filtrar por"), "sprint");
//     userEvent.selectOptions(screen.getByLabelText("Sprint"), "1");
//     userEvent.selectOptions(screen.getByLabelText("Miembro"), "all");

//     // Genera el reporte de KPIs
//     userEvent.click(screen.getByText("Generar Reporte"));

//     // Espera a que el gráfico de KPIs se renderice
//     await waitFor(() => {
//       expect(screen.getByText(/dificultad de las tareas/i)).toBeInTheDocument();
//     });

//     // Verifica los datos del reporte de equipo
//     expect(screen.getByLabelText("report-user")).toHaveTextContent(
//       /todo el equipo/i,
//     );
//     expect(screen.getByLabelText("report-tasksdone")).toHaveTextContent("1");
//     expect(screen.getByLabelText("report-storypoints")).toHaveTextContent("5");
//     expect(screen.getByLabelText("report-realhours")).toHaveTextContent("6");
//     expect(screen.getByLabelText("report-estimatedhours")).toHaveTextContent(
//       "8",
//     );
//     expect(screen.getByLabelText("report-kpi1")).toHaveTextContent("133");
//     expect(screen.getByLabelText("report-kpi2")).toHaveTextContent("5");
//   });
// });
