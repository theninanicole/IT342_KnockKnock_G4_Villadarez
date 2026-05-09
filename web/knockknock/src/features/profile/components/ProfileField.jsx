import React from "react";

export default function ProfileField({
  label,
  name,
  value,
  onChange,
  isEditing,
  type = "text",
}) {
  return (
    <div className="py-7 px-6 sm:px-8 flex flex-col sm:flex-row gap-4 sm:gap-8 items-start sm:items-center">
      <div className="sm:w-1/3 max-w-[280px]">
        <div className="text-[14px] font-medium text-gray-900">{label}</div>
      </div>
      <div className="flex-1 w-full max-w-2xl">
        <input
          type={type}
          name={name}
          value={value}
          onChange={onChange}
          disabled={!isEditing}
          className={`w-full max-w-md px-4 py-3 rounded-xl text-[15px] outline-none border transition focus:border-[#2d6df6] focus:bg-white focus:ring-4 focus:ring-[#2d6df6]/10 ${
            isEditing
              ? "bg-slate-50 border-slate-200 text-slate-900"
              : "bg-gray-50/50 border-gray-200 text-gray-600 cursor-default"
          }`}
        />
      </div>
    </div>
  );
}
