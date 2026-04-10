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
          className={`w-full max-w-md px-3.5 py-2.5 rounded-lg text-[15px] outline-none border transition-all ${
            isEditing
              ? "bg-white border-gray-300 text-gray-900 focus:ring-4 focus:ring-gray-900/5 focus:border-gray-500 shadow-sm"
              : "bg-gray-50/50 border-gray-200 text-gray-600 cursor-default"
          }`}
        />
      </div>
    </div>
  );
}
