import React from "react";
import { FILTERS } from "@hooks/useAllVisitsList";

export default function VisitFilterBar({ activeFilter, onFilterChange }) {
  return (
    <div className="inline-flex items-center bg-gray-50/80 p-1 rounded-xl border border-gray-100">
      {FILTERS.map((filter) => {
        const isActive = activeFilter === filter;
        return (
          <button
            key={filter}
            type="button"
            onClick={() => onFilterChange(filter)}
            className={`px-4 py-2 text-sm font-medium rounded-lg transition-all ${
              isActive
                ? "bg-white text-blue-600 shadow-sm"
                : "text-gray-500 hover:text-gray-700 hover:bg-gray-100/50"
            }`}
          >
            {filter}
          </button>
        );
      })}
    </div>
  );
}
