import React from "react";

export default function AuthInput({
  label,
  type = "text",
  value,
  onChange,
  placeholder,
  name,
  required = true,
  ...rest
}) {
  return (
    <div className="space-y-2">
      <label className="block text-[11px] font-extrabold tracking-[0.08em] text-slate-400">
        {label}
      </label>
      <input
        type={type}
        name={name}
        value={value}
        onChange={onChange}
        placeholder={placeholder}
        required={required}
        className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-[15px] text-slate-900 outline-none transition focus:border-[#2d6df6] focus:bg-white focus:ring-4 focus:ring-[#2d6df6]/10"
        {...rest}
      />
    </div>
  );
}
