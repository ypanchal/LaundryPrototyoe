import React, { useState } from "react";

export default function InvoiceTable({ invoices, showCustomerColumn, onStatusChange, onExportPdf, onSendWhatsApp }) {
  const [expandedId, setExpandedId] = useState(null);

  if (!invoices || invoices.length === 0) {
    return <p className="empty-state">No invoices yet.</p>;
  }

  return (
    <table className="invoice-table">
      <thead>
        <tr>
          <th>Invoice #</th>
          <th>Date</th>
          {showCustomerColumn && <th>Customer</th>}
          <th>Items</th>
          <th>Total</th>
          <th>Status</th>
          {onExportPdf && <th>Export</th>}
          {onSendWhatsApp && <th>WhatsApp</th>} 
        </tr>
      </thead>
      <tbody>
        {invoices.map((inv) => (
          <React.Fragment key={inv.id}>
            <tr>
              <td>
                <button className="link-btn" onClick={() => setExpandedId(expandedId === inv.id ? null : inv.id)}>
                  #{inv.id} {expandedId === inv.id ? "▲" : "▼"}
                </button>
              </td>
              <td>{inv.createdDate}</td>
              {showCustomerColumn && <td>{inv.customerName || "-"} <br /><span className="muted">{inv.customerPhone}</span></td>}
              <td>{inv.items.length} item(s)</td>
              <td>₹{inv.totalAmount.toFixed(2)}</td>
              <td>
                {onStatusChange ? (
                  <select value={inv.status} onChange={(e) => onStatusChange(inv.id, e.target.value)}>
                    <option value="RECEIVED">RECEIVED</option>
                    <option value="IN_PROGRESS">IN PROGRESS</option>
                    <option value="READY_FOR_PICKUP">READY FOR PICKUP</option>
                    <option value="DELIVERED">DELIVERED</option>
                  </select>
                ) : (
                  <span className={`status-badge status-${inv.status}`}>{inv.status.replace(/_/g, " ")}</span>
                )}
              </td>
              {onExportPdf && (
                <td>
                  <button type="button" className="export-btn" onClick={() => onExportPdf(inv.id)}>Export PDF</button>
                </td>
              )}
              {onSendWhatsApp && (
                <td>
                  <button type="button" className="whatsapp-btn" onClick={() => onSendWhatsApp(inv.id)} disabled={!inv.customerPhone}>
                    Send WhatsApp
                  </button>
                </td>
              )}
            </tr>
            {expandedId === inv.id && (
              <tr className="details-row">
                <td colSpan={(showCustomerColumn ? 6 : 5) + (onExportPdf ? 1 : 0) + (onSendWhatsApp ? 1 : 0)}>
                  <table className="sub-table">
                    <thead>
                      <tr><th>Cloth</th><th>Service</th><th>Qty</th><th>Price/unit</th><th>Line total</th></tr>
                    </thead>
                    <tbody>
                      {inv.items.map((it) => (
                        <tr key={it.id}>
                          <td>{it.clothType}</td>
                          <td>{it.serviceType.replace(/_/g, " ")}</td>
                          <td>{it.quantity}</td>
                          <td>₹{it.pricePerUnit.toFixed(2)}</td>
                          <td>₹{(it.quantity * it.pricePerUnit).toFixed(2)}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </td>
              </tr>
            )}
          </React.Fragment>
        ))}
      </tbody>
    </table>
  );
}
