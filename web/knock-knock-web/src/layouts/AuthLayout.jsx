export default function AuthLayout({ children }) {
  return (
    <div className="min-h-screen flex flex-col bg-slate-100">
      <main className="flex-1 flex items-center justify-center py-10">{children}</main>
    </div>
  );
}
