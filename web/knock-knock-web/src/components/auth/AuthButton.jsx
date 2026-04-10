import React from "react";

export default function AuthButton({ label, type = "submit", loading = false, ...rest }) {
  return (
    <button
      type={type}
      disabled={loading || rest.disabled}
      className={`mt-2 w-full rounded-xl bg-[#2d6df6] py-4 text-[16px] font-semibold text-white shadow-sm transition hover:bg-[#1e56d1] disabled:opacity-70 disabled:cursor-not-allowed`}
      {...rest}
    >
      {loading ? "Please wait..." : label}
    </button>
  );
}
