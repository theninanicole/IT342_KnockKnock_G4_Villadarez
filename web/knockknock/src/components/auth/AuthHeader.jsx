import React from "react";
import { ShieldCheck } from "lucide-react";

export default function AuthHeader({ title = "KnockKnock", subtitle }) {
  return (
    <div className="bg-[#2d6df6] text-white text-center px-8 pt-12 pb-10">
      <div className="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-2xl bg-white/20">
        <ShieldCheck size={32} color="white" strokeWidth={1.5} />
      </div>
      <h1 className="text-3xl font-bold tracking-tight">{title}</h1>
      {subtitle && <p className="mt-2 text-sm opacity-90">{subtitle}</p>}
    </div>
  );
}
