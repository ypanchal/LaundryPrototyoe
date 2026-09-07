import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api";
import InvoiceTable from "../components/InvoiceTable";

export default function CustomerDashboard() {
  const [invoices, setInvoices] = useState([]);
  const [error, setError] = useState("");
  const navigate = useNavigate();
  const name = localStorage.getItem("name");
  const phone = localStorage.getItem("phone");

  useEffect(() => {
    api.get("/customer/invoices")
      .then((res) => setInvoices(res.data))
      .catch(() => setError("Could not load your invoices."));
  }, []);

  function logout() {
    localStorage.clear();
    navigate("/login");
  }

  return (
    <div className="dashboard">
      <header className="dashboard-header">
        <h1>Sparkle Laundry</h1>
        <div>
          <span className="muted">{name} ({phone})</span>
          <button className="secondary-btn" onClick={logout}>Log out</button>
        </div>
      </header>

      <div className="card wide">
        <h2>Your past orders</h2>
        {error && <p className="error">{error}</p>}
        <InvoiceTable invoices={invoices} showCustomerColumn={false} />
      </div>
    </div>
  );
}
