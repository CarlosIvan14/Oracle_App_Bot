import React, { useState, useEffect } from "react";
import { BrowserRouter, Routes, Route, useNavigate } from "react-router-dom";
import { ThemeProvider } from "./context/ThemeContext";

const Login = React.lazy(() => import("./routes/Login"));
const Home = React.lazy(() => import("./routes/Home"));
const ProjectSprints = React.lazy(() => import("./routes/ProjectSprints"));
const SprintTasks = React.lazy(() => import("./routes/SprintTasks"));
const AllTasksCalendar = React.lazy(() => import("./routes/AllTasksCalendar"));
const UsersList = React.lazy(() => import("./routes/UsersList"));
const Reports = React.lazy(() => import("./routes/Reports"));
const Profile = React.lazy(() => import("./routes/Profile"));
const PrivateLayout = React.lazy(() => import("./components/PrivateLayout"));

// Custom hook for auth management
function useAuth() {
  const [user, setUser] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const storedUser = localStorage.getItem("user");
    if (storedUser) {
      try {
        setUser(JSON.parse(storedUser));
      } catch (e) {
        console.error("Failed to parse user data", e);
        localStorage.removeItem("user");
      }
    }
  }, []);

  const handleLogin = (userData) => {
    localStorage.setItem("user", JSON.stringify(userData));
    setUser(userData);
    navigate("/home");
  };

  const handleLogout = () => {
    localStorage.removeItem("user");
    setUser(null);
    navigate("/");
  };

  return { user, handleLogin, handleLogout };
}

function AppRoutes() {
  const { user, handleLogin, handleLogout } = useAuth();

  return (
    <Routes>
      <Route
        path="/"
        element={
          <React.Suspense>
            <Login onLogin={handleLogin} />
          </React.Suspense>
        }
      />

      <Route
        path="/"
        element={
          <React.Suspense>
                          <PrivateLayout user={user} onLogout={handleLogout} />
          </React.Suspense>
        }
      >
        <Route
          path="home"
          element={
            <React.Suspense>
              <Home />
            </React.Suspense>
          }
        />

        <Route
          path="projects/:projectId"
          element={
            <React.Suspense>
              <ProjectSprints />
            </React.Suspense>
          }
        />

        <Route
          path="projects/:projectId/sprint/:sprintId"
          element={
            <React.Suspense>
              <SprintTasks />
            </React.Suspense>
          }
        />

        <Route
          path="projects/:projectId/sprint/:sprintId/all"
          element={
            <React.Suspense>
              <AllTasksCalendar />
            </React.Suspense>
          }
        />

        <Route
          path="projects/:projectId/users"
          element={
            <React.Suspense>
              <UsersList />
            </React.Suspense>
          }
        />

        <Route
          path="projects/:projectId/reports"
          element={
            <React.Suspense>
              <Reports />
            </React.Suspense>
          }
        />

        <Route
          path="profile"
          element={
            <React.Suspense>
              <Profile user={user} />
            </React.Suspense>
          }
        />
      </Route>

      <Route
        path="*"
        element={
          <React.Suspense>
            <div>NOT FOUND</div>
          </React.Suspense>
        }
      />
    </Routes>
  );
}

function App() {
  // Added initialization logging
  useEffect(() => {
    console.log("App initialized at path:", window.location.pathname);
  }, []);

  return (
    <ThemeProvider>
      <BrowserRouter basename="/">
        <AppRoutes />
      </BrowserRouter>
    </ThemeProvider>
  );
}

export default App;
