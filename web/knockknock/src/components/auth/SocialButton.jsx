import React from "react";

export default function SocialButton({
  onClick,
  label = "Google Account",
  iconSrc = "https://fonts.gstatic.com/s/i/productlogos/googleg/v6/24px.svg",
  iconAlt = "Google",
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      className="mb-6 flex w-full items-center justify-center gap-3 rounded-xl border border-slate-200 bg-white py-3.5 text-[15px] font-semibold text-slate-900 transition hover:bg-slate-50"
    >
      <img src={iconSrc} alt={iconAlt} className="h-5 w-5" />
      {label}
    </button>
  );
}
