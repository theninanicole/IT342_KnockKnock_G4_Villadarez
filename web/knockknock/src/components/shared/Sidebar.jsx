import React, { useContext, useEffect, useState } from "react";
import { ShieldCheck, LayoutDashboard, CalendarDays, Bell, User, LogOut, History, ClipboardList } from "lucide-react";
import { useNavigate, useLocation, Link } from "react-router-dom";
import { AuthContext } from "@store/AuthContext";
import { fetchNotifications } from "@api/apiServices";

export default function Sidebar({ role }) {
  const navigate = useNavigate();
  const location = useLocation();
  const { logout } = useContext(AuthContext);
  const [unreadCount, setUnreadCount] = useState(0);

  const handleLogout = () => {
    logout();
    navigate("/");
  };

  useEffect(() => {
    const loadUnread = async () => {
      try {
        const notifs = await fetchNotifications({ isRead: false, limit: 50 });
        setUnreadCount(Array.isArray(notifs) ? notifs.length : 0);
      } catch (e) {
        console.error("[Sidebar] Failed to fetch unread notifications", e);
      }
    };

    loadUnread();
  }, []);

  const currentRole = role?.toUpperCase();
  const navItems = currentRole === "VISITOR" 
    ? [
        { name: "Dashboard", path: "/visitor-dashboard", icon: <LayoutDashboard size={20} /> },
        { name: "My Visits", path: "/my-visits", icon: <CalendarDays size={20} /> },
        { name: "Notifications", path: "/notifications", icon: <Bell size={20} />, showBadge: true },
      ]
    : [
        { name: "Dashboard", path: "/admin-dashboard", icon: <LayoutDashboard size={20} /> },
        { name: "All Visits", path: "/admin/all-visits", icon: <ClipboardList size={20} /> },
        { name: "Status History", path: "/admin/history", icon: <History size={20} /> },
      ];

  const baseNavItemClasses =
    "mb-1 flex w-full items-center gap-3 rounded-[10px] px-4 py-3 text-[15px] font-medium transition-colors";

  const getNavItemClasses = (path, extra = "") => {
    const isActive = location.pathname === path;
    const activeClasses = "bg-[#2663eb] text-white";
    const inactiveClasses = "text-slate-400 hover:bg-white/5 hover:text-white";
    return `${baseNavItemClasses} ${isActive ? activeClasses : inactiveClasses} ${extra}`.trim();
  };

  return (
    <div className="sticky top-0 flex h-screen w-[260px] flex-col border-r border-slate-800 bg-[#0E172A] text-slate-400">
      <div className="flex items-center gap-3 px-8 py-10">
        <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-[#2663eb] text-lg font-semibold text-white">
          <ShieldCheck size={22} strokeWidth={1.5} />
        </div>
        <h2 className="text-xl font-bold text-white">KnockKnock</h2>
      </div>

      <div className="flex-1 px-4">
        <p className="mt-6 mb-3 ml-2 text-[11px] font-bold tracking-[0.1em] text-slate-600">
          GENERAL
        </p>
        {navItems.map((item) => (
          <Link key={item.name} to={item.path} className={getNavItemClasses(item.path)}>
            {item.icon}
            <span>{item.name}</span>
            {item.showBadge && unreadCount > 0 && (
              <span className="ml-auto flex h-5 min-w-[22px] items-center justify-center rounded-full bg-red-500 px-2 text-[11px] font-semibold text-red-50">
                {unreadCount > 9 ? "9+" : unreadCount}
              </span>
            )}
          </Link>
        ))}
      </div>

      <div className="border-t border-slate-800 px-4 py-4">
        <Link to="/profile" className={getNavItemClasses("/profile")}>
          <User size={20} />
          <span>Account</span>
        </Link>
        <button
          type="button"
          onClick={handleLogout}
          className="mt-1 flex w-full items-center gap-3 rounded-[10px] px-4 py-3 text-[15px] font-medium text-red-400 transition-colors hover:bg-red-500/10 hover:text-red-300"
        >
          <LogOut size={20} />
          <span>Logout</span>
        </button>
      </div>
    </div>
  );
}