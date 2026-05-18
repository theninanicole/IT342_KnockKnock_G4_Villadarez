import React, { useState } from "react";
import { Eye, EyeOff } from "lucide-react";

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
  const [isPasswordVisible, setIsPasswordVisible] = useState(false);
  const isPasswordInput = type === "password";
  const inputType = isPasswordInput && isPasswordVisible ? "text" : type;

  return (
    <div className="space-y-2">
      <label className="block text-[11px] font-extrabold tracking-[0.08em] text-slate-400">
        {label}
      </label>
      <div className="relative">
        <input
          type={inputType}
          name={name}
          value={value}
          onChange={onChange}
          placeholder={placeholder}
          required={required}
          className={`w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-[15px] text-slate-900 outline-none transition focus:border-[#2d6df6] focus:bg-white focus:ring-4 focus:ring-[#2d6df6]/10 ${
            isPasswordInput ? "pr-12" : ""
          }`}
          {...rest}
        />
        {isPasswordInput && (
          <button
            type="button"
            onClick={() => setIsPasswordVisible((visible) => !visible)}
            className="absolute inset-y-0 right-3 flex w-8 items-center justify-center text-slate-400 transition hover:text-slate-600 focus:outline-none focus-visible:text-[#2d6df6]"
            aria-label={isPasswordVisible ? "Hide password" : "Show password"}
            title={isPasswordVisible ? "Hide password" : "Show password"}
          >
            {isPasswordVisible ? <EyeOff size={18} /> : <Eye size={18} />}
          </button>
        )}
      </div>
    </div>
  );
}
