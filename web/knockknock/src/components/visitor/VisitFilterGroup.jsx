import React from "react";

export default function VisitFilterGroup({ activeFilter, counts, onFilterChange }) {
  return (
    <div className="flex flex-wrap gap-2">
      <button
        type="button"
        onClick={() => onFilterChange("all")}
        className={`px-4 py-2.5 text-sm font-medium rounded-xl border border-gray-100 transition-all cursor-pointer ${
          activeFilter === "all"
            ? "bg-blue-600 text-white shadow-sm"
            : "bg-gray-50 text-gray-500 hover:text-gray-700 hover:bg-gray-100/50"
        }`}
      >
        All ({counts.total})
      </button>
      <button
        type="button"
        onClick={() => onFilterChange("scheduled")}
        className={`px-4 py-2.5 text-sm font-medium rounded-xl border border-gray-100 transition-all cursor-pointer ${
          activeFilter === "scheduled"
            ? "bg-blue-600 text-white shadow-sm"
            : "bg-gray-50 text-gray-500 hover:text-gray-700 hover:bg-gray-100/50"
        }`}
      >
        Scheduled ({counts.scheduled})
      </button>
      <button
        type="button"
        onClick={() => onFilterChange("checked-in")}
        className={`px-4 py-2.5 text-sm font-medium rounded-xl border border-gray-100 transition-all cursor-pointer ${
          activeFilter === "checked-in"
            ? "bg-blue-600 text-white shadow-sm"
            : "bg-gray-50 text-gray-500 hover:text-gray-700 hover:bg-gray-100/50"
        }`}
      >
        Checked-in ({counts.checkedIn})
      </button>
      <button
        type="button"
        onClick={() => onFilterChange("checked-out")}
        className={`px-4 py-2.5 text-sm font-medium rounded-xl border border-gray-100 transition-all cursor-pointer ${
          activeFilter === "checked-out"
            ? "bg-blue-600 text-white shadow-sm"
            : "bg-gray-50 text-gray-500 hover:text-gray-700 hover:bg-gray-100/50"
        }`}
      >
        Checked-out ({counts.checkedOut})
      </button>
      <button
        type="button"
        onClick={() => onFilterChange("cancelled")}
        className={`px-4 py-2.5 text-sm font-medium rounded-xl border border-gray-100 transition-all cursor-pointer ${
          activeFilter === "cancelled"
            ? "bg-blue-600 text-white shadow-sm"
            : "bg-gray-50 text-gray-500 hover:text-gray-700 hover:bg-gray-100/50"
        }`}
      >
        Cancelled ({counts.cancelled})
      </button>
      <button
        type="button"
        onClick={() => onFilterChange("missed")}
        className={`px-4 py-2.5 text-sm font-medium rounded-xl border border-gray-100 transition-all cursor-pointer ${
          activeFilter === "missed"
            ? "bg-blue-600 text-white shadow-sm"
            : "bg-gray-50 text-gray-500 hover:text-gray-700 hover:bg-gray-100/50"
        }`}
      >
        Missed ({counts.missed})
      </button>
    </div>
  );
}
