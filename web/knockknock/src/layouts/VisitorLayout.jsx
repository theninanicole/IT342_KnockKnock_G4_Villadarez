import Sidebar from "@components/shared/Sidebar";
import Topbar from "@components/shared/Topbar";
import { useAuth } from "@hooks/useAuth";

export default function VisitorLayout({ children }) {
  const { user } = useAuth();

  return (
    <div className="min-h-screen bg-slate-100 flex">
      <Sidebar role={user?.role || "VISITOR"} />
      <div className="flex-1 flex flex-col">
        <Topbar />
        <main className="p-4 sm:p-6 lg:p-8">{children}</main>
      </div>
    </div>
  );
}
