import React from "react";

const SERVICE_TYPES = ["WASH", "IRON", "WASH_AND_IRON", "DRY_CLEAN"];
const CLOTH_TYPES = ["Shirt", "Tshirt", "Jeans", "Trouser", "Saree", "kurta"];

export default function InvoiceItemsEditor({ items, setItems }) {
  function updateItem(index, field, value) {
    const updated = [...items];
    updated[index] = { ...updated[index], [field]: value };
    setItems(updated);
  }

  function addRow() {
    setItems([...items, { clothType: "", serviceType: "WASH_AND_IRON", quantity: 1, pricePerUnit: 0 }]);
  }

  function removeRow(index) {
    setItems(items.filter((_, i) => i !== index));
  }

  const total = items.reduce((sum, it) => sum + (Number(it.quantity) || 0) * (Number(it.pricePerUnit) || 0), 0);

  return (
    <div className="items-editor">
      <table>
        <thead>
          <tr>
            <th>Cloth type</th>
            <th>Service</th>
            <th>Qty</th>
            <th>Price/unit (₹)</th>
            <th>Line total</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {items.map((item, i) => (
            <tr key={i}>
              <td>
                <select
                  value={item.clothType}
                  onChange={(e) => updateItem(i, "clothType", e.target.value)}
                  required
                >
                  <option value="">Select cloth type</option>
                  {CLOTH_TYPES.map((cloth) => (
                    <option key={cloth} value={cloth}>{cloth}</option>
                  ))}
                </select>
              </td>
              <td>
                <select value={item.serviceType} onChange={(e) => updateItem(i, "serviceType", e.target.value)}>
                  {SERVICE_TYPES.map((s) => (
                    <option key={s} value={s}>{s.replace(/_/g, " ")}</option>
                  ))}
                </select>
              </td>
              <td>
                <input
                  type="number" min="1" value={item.quantity}
                  onChange={(e) => updateItem(i, "quantity", e.target.value)}
                  required
                />
              </td>
              <td>
                <input
                  type="number" min="0" step="0.5" value={item.pricePerUnit}
                  onChange={(e) => updateItem(i, "pricePerUnit", e.target.value)}
                  required
                />
              </td>
              <td>₹{((Number(item.quantity) || 0) * (Number(item.pricePerUnit) || 0)).toFixed(2)}</td>
              <td>
                {items.length > 1 && (
                  <button type="button" className="link-btn" onClick={() => removeRow(i)}>Remove</button>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      <button type="button" className="secondary-btn" onClick={addRow}>+ Add item</button>

      <div className="grand-total">Total: ₹{total.toFixed(2)}</div>
    </div>
  );
}
