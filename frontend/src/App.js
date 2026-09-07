import React from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import Login from "./pages/Login";
import AdminDashboard from "./pages/AdminDashboard";
import CustomerDashboard from "./pages/CustomerDashboard";

function RequireRole({ role, children }) {
  const token = localStorage.getItem("token");
  const savedRole = localStorage.getItem("role");
  if (!token) return <Navigate to="/login" replace />;
  if (role && savedRole !== role) return <Navigate to="/login" replace />;
  return children;
}

export default function App() {
  const token = localStorage.getItem("token");
  const role = localStorage.getItem("role");

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route
          path="/admin"
          element={
            <RequireRole role="ADMIN">
              <AdminDashboard />
            </RequireRole>
          }
        />
        <Route
          path="/customer"
          element={
            <RequireRole role="CUSTOMER">
              <CustomerDashboard />
            </RequireRole>
          }
        />
        <Route
          path="/"
          element={
            token ? (
              <Navigate to={role === "ADMIN" ? "/admin" : "/customer"} replace />
            ) : (
              <Navigate to="/login" replace />
            )
          }
        />
      </Routes>
    </BrowserRouter>
  );
}
