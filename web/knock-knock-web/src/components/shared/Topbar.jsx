import React, { useContext } from "react";
import { AuthContext } from "@store/AuthContext";

export default function Topbar({ title }) {
  const { user } = useContext(AuthContext);

  const getInitials = (name) =>
    name?.split(" ")
      .map((n) => n[0])
      .join("")
      .toUpperCase() || "U";

  return (
    <div className="sticky top-0 z-10 flex h-20 items-center justify-between border-b border-slate-200 bg-white px-6 sm:px-8">
      <h3 className="text-2xl font-bold text-slate-900">{title}</h3>
      <div className="flex items-center gap-4">
        <div className="flex flex-col text-right">
          <span className="text-sm font-bold text-slate-900">{user?.fullName}</span>
          <span className="text-xs font-semibold text-blue-600">
            {user?.role === "VISITOR" ? "Visitor" : "Condominium Admin"}
          </span>
        </div>
        <div className="flex h-11 w-11 items-center justify-center rounded-full border-2 border-slate-200 bg-blue-600 text-sm font-bold text-white">
          {getInitials(user?.fullName)}
        </div>
      </div>
    </div>
  );
}
