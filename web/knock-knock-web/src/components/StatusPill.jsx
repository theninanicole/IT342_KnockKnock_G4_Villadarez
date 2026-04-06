import React from "react";

const normalizeStatusCode = (status) => {
  if (!status) return "";
  return String(status).trim().toUpperCase();
};

const getStatusMeta = (status) => {
  const code = normalizeStatusCode(status);

  switch (code) {
    case "SCHEDULED":
      return { label: "Scheduled", classes: "bg-blue-50 text-blue-600" };
    case "CHECKED-IN":
      return { label: "Checked-in", classes: "bg-emerald-50 text-[#10B981]" };
    case "CHECKED-OUT":
      return { label: "Checked-out", classes: "bg-gray-100 text-gray-600" };
    case "CANCELLED":
      return { label: "Cancelled", classes: "bg-orange-50 text-orange-600" };
    case "MISSED":
      return { label: "Missed", classes: "bg-red-50 text-red-600" };
    default:
      return { label: status || "Unknown", classes: "bg-gray-100 text-gray-600" };
  }
};

export default function StatusPill({ status, className = "" }) {
  const { label, classes } = getStatusMeta(status);

  return (
    <span
      className={`px-4 py-1.5 rounded-full text-xs font-medium capitalize whitespace-nowrap ${classes} ${className}`}
    >
      {label}
    </span>
  );
}
