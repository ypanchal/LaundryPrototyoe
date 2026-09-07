import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api";

export default function Login() {
  const [phone, setPhone] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      const res = await api.post("/auth/login", { phone, password });
      const { token, role, name, phone: userPhone } = res.data;
      localStorage.setItem("token", token);
      localStorage.setItem("role", role);
      localStorage.setItem("name", name);
      localStorage.setItem("phone", userPhone);
      navigate(role === "ADMIN" ? "/admin" : "/customer");
    } catch (err) {
      setError(err.response?.data?.error || "Login failed. Please try again.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="centered-page">
      <form className="card" onSubmit={handleSubmit}>
        <h1>Sparkle Laundry</h1>
        <p className="subtitle">Log in with your phone number</p>

        <label>Phone number</label>
        <input
          value={phone}
          onChange={(e) => setPhone(e.target.value)}
          placeholder="e.g. 9999999999"
          required
        />

        <label>Password</label>
        <input
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />

        {error && <p className="error">{error}</p>}

        <button type="submit" disabled={loading}>
          {loading ? "Logging in..." : "Log in"}
        </button>

        <div className="hint">
          <strong>Demo logins</strong>
          <div>Admin: admin / admin123</div>
          <div>Customer: 9999999999 / customer123</div>
        </div>
      </form>
    </div>
  );
}
