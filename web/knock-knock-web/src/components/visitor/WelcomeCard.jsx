import React from "react";

export default function WelcomeCard({
  title,
  primaryActionLabel,
  onPrimaryAction,
  icon,
}) {
  return (
    <div className="mb-8 flex items-center justify-between rounded-2xl bg-slate-900 text-white px-8 py-6 relative overflow-hidden">
      <div>
        <h1 className="text-2xl font-bold">{title}</h1>
        {primaryActionLabel && onPrimaryAction && (
          <button
            onClick={onPrimaryAction}
            className="mt-4 inline-flex items-center rounded-lg bg-blue-600 px-4 py-2 text-sm font-semibold text-white shadow-sm transition hover:bg-blue-700"
          >
            <span className="mr-1">{primaryActionLabel}</span>
          </button>
        )}
      </div>
      {icon && (
        <div className="pointer-events-none opacity-10">{icon}</div>
      )}
    </div>
  );
}
