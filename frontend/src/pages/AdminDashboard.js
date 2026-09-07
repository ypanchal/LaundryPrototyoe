import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api";
import InvoiceItemsEditor from "../components/InvoiceItemsEditor";
import InvoiceTable from "../components/InvoiceTable";

export default function AdminDashboard() {
  const [tab, setTab] = useState("create"); // "create" | "all"
  const [customerPhone, setCustomerPhone] = useState("");
  const [customerName, setCustomerName] = useState("");
  const [items, setItems] = useState([{ clothType: "", serviceType: "WASH_AND_IRON", quantity: 1, pricePerUnit: 0 }]);
  const [invoices, setInvoices] = useState([]);
  const [searchPhone, setSearchPhone] = useState("");
  const [message, setMessage] = useState(null);
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const navigate = useNavigate();
  const name = localStorage.getItem("name");

  useEffect(() => {
    if (tab === "all") loadInvoices(searchPhone);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [tab]);

  async function loadInvoices(phone) {
    try {
      const token = localStorage.getItem("token");
      console.log("Auth Token:", token);
      const res = await api.get("/admin/invoices", {
        params: phone ? { phone } : {},
        headers: {
        Authorization: `Bearer ${token}`,
      },
      });
      setInvoices(res.data);
    } catch (err) {
      setError("Could not load invoices.");
    }
  }

  function handleSearchChange(e) {
    const value = e.target.value;
    setSearchPhone(value);
    loadInvoices(value);
  }

  function logout() {
    localStorage.clear();
    navigate("/login");
  }

  async function handleCreateInvoice(e) {
    e.preventDefault();
    setError("");
    setMessage(null);
    setSubmitting(true);
    try {
      const res = await api.post("/admin/invoices", {
        customerPhone,
        customerName,
        items: items.map((it) => ({
          ...it,
          quantity: Number(it.quantity),
          pricePerUnit: Number(it.pricePerUnit),
        })),
      });
      const { invoice, newCustomerTempPassword } = res.data;
      let msg = `Invoice #${invoice.id} created for ${customerPhone}. Total: ₹${invoice.totalAmount.toFixed(2)}.`;
      if (newCustomerTempPassword) {
        msg += ` A new customer login was created - share these with them: phone ${customerPhone}, password ${newCustomerTempPassword}.`;
      }
      setMessage(msg);
      setCustomerPhone("");
      setCustomerName("");
      setItems([{ clothType: "", serviceType: "WASH_AND_IRON", quantity: 1, pricePerUnit: 0 }]);
    } catch (err) {
      setError(err.response?.data?.error || "Could not create invoice.");
    } finally {
      setSubmitting(false);
    }
  }

  async function handleExportPdf(invoiceId) {
    try {
      const response = await api.get(`/admin/invoices/${invoiceId}/pdf`, {
        responseType: "blob",
      });

      const blob = new Blob([response.data], { type: "application/pdf" });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = `invoice-${invoiceId}.pdf`;
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      setError("Could not export invoice PDF.");
    }
  }

  async function handleSendWhatsApp(invoiceId) {
    try {
      const res = await api.post(`/admin/invoices/${invoiceId}/whatsapp`);
      const whatsappMessage = res.data?.message || "Invoice sent on WhatsApp.";
      if (res.data?.sent) {
        setMessage(whatsappMessage);
        setError("");
      } else {
        setError(whatsappMessage);
        setMessage(null);
      }
    } catch (err) {
      setError(err.response?.data?.message || "Could not send invoice on WhatsApp.");
      setMessage(null);
    }
  }

  async function handleStatusChange(invoiceId, status) {
    try {
      await api.put(`/admin/invoices/${invoiceId}/status`, { status });
      loadInvoices(searchPhone);
    } catch (err) {
      setError("Could not update status.");
    }
  }

  return (
    <div className="dashboard">
      <header className="dashboard-header">
        <h1>Sparkle Laundry - Admin</h1>
        <div>
          <span className="muted">{name}</span>
          <button className="secondary-btn" onClick={logout}>Log out</button>
        </div>
      </header>

      <nav className="tabs">
        <button className={tab === "create" ? "active" : ""} onClick={() => setTab("create")}>New Invoice</button>
        <button className={tab === "all" ? "active" : ""} onClick={() => setTab("all")}>All Invoices</button>
      </nav>

      {error && <p className="error">{error}</p>}

      {tab === "create" && (
        <form className="card create-invoice-card" onSubmit={handleCreateInvoice}>
          <h2>Create invoice</h2>

          <label>Customer phone number</label>
          <input value={customerPhone} onChange={(e) => setCustomerPhone(e.target.value)} required placeholder="10-digit number" />

          <label>Customer name (optional, used if this is a new customer)</label>
          <input value={customerName} onChange={(e) => setCustomerName(e.target.value)} placeholder="e.g. Ramesh Kumar" />

          <h3>Clothes received</h3>
          <InvoiceItemsEditor items={items} setItems={setItems} />

          <button type="submit" disabled={submitting}>{submitting ? "Saving..." : "Save invoice"}</button>

          {message && <p className="success">{message}</p>}
        </form>
      )}

      {tab === "all" && (
        <div className="card wide">
          <h2>All invoices</h2>
          <label>Search by mobile number</label>
          <input
            value={searchPhone}
            onChange={handleSearchChange}
            placeholder="e.g. 9999 (partial number works too)"
            style={{ maxWidth: 320, marginBottom: 14 }}
          />
          <InvoiceTable invoices={invoices} showCustomerColumn onStatusChange={handleStatusChange} onExportPdf={handleExportPdf} onSendWhatsApp={handleSendWhatsApp} />
        </div>
      )}
    </div>
  );
}