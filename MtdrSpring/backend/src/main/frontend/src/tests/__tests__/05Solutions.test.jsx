// 5. Reporte Mostrar KPI Horas trabajadas  y Tareas completadas por PERSONA por semana/sprint

/* 
    Involved files: 
        - src/App.jsx
        - src/routes/Reports.jsx
*/

global.ResizeObserver = class {
    observe() {}
    unobserve() {}
    disconnect() {}
  };
  
  import React from "react";
  import { render, screen, waitFor } from "@testing-library/react";
  import "@testing-library/jest-dom";
  import userEvent from "@testing-library/user-event";
  import { MemoryRouter, Route, Routes } from "react-router-dom";
  import Reports from "../../routes/Reports";
  
  import { server } from "../testServer";
  
  beforeAll(() => server.listen());
  afterEach(() => server.resetHandlers());
  afterAll(() => server.close());
  
  const renderWithRouter = (ui, { route = "/projects/123/reports" } = {}) => {
    window.history.pushState({}, "Test page", route);
    return render(
      <MemoryRouter initialEntries={[route]}>
        <Routes>
          <Route path="/projects/:projectId/reports" element={ui} />
        </Routes>
      </MemoryRouter>,
    );
  };
  
  describe("Completed Tasks List in Sprint Reports", () => {
      beforeAll(() => {
          jest.spyOn(console, 'warn').mockImplementation((msg) => {
            if (
              msg.includes('React Router Future Flag Warning')
            ) return;
            // Si no es el warning específico, lo imprimimos
            console.warn(msg);
          });
        });  
        test("displays task details when selecting sprint and team", async () => {
          renderWithRouter(<Reports />);
        
          // Wait for initial data load
          await waitFor(() => {
            expect(screen.getByText("Sprint 1")).toBeInTheDocument();
          });
          
          // Select filters
          userEvent.selectOptions(screen.getByLabelText("Filtrar por"), "sprint");
          userEvent.selectOptions(screen.getByLabelText("Sprint"), "1");
          userEvent.selectOptions(screen.getByLabelText("Miembro"), "101");
          
          // Generate report and wait for data
          userEvent.click(screen.getByText("Generar Reporte"));
          
          // Wait for kpi graph to render
          await waitFor(() => {
            expect(screen.getByText(/dificultad de las tareas/i)).toBeInTheDocument();
          });
          
          // Verify Team Report Data.
          expect(screen.getByLabelText('report-user')).toHaveTextContent(/alex lozoya/i);   
          expect(screen.getByLabelText('report-tasksdone')).toHaveTextContent('1');
          expect(screen.getByLabelText('report-storypoints')).toHaveTextContent('5');
          expect(screen.getByLabelText('report-realhours')).toHaveTextContent('6');
          expect(screen.getByLabelText('report-estimatedhours')).toHaveTextContent('8');
          expect(screen.getByLabelText('report-kpi1')).toHaveTextContent('133');
          expect(screen.getByLabelText('report-kpi2')).toHaveTextContent('5');
        })
  });