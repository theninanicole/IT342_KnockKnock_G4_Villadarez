import React from "react";

export default function PasswordChangeCard({
  passwordForm,
  onPasswordChange,
  onChangePassword,
  changingPassword,
}) {
  return (
    <div className="bg-white rounded-2xl border border-gray-200 shadow-[0_18px_35px_rgba(15,23,42,0.12)] overflow-hidden">
      <div className="px-6 py-6 sm:px-8">
        <h3 className="text-[15px] font-semibold text-gray-900 mb-4">Change Password</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 md:gap-6">
          <div className="space-y-2">
            <label className="block text-[11px] font-extrabold tracking-[0.08em] text-slate-400 uppercase">
              Current Password
            </label>
            <input
              type="password"
              name="currentPassword"
              value={passwordForm.currentPassword}
              onChange={onPasswordChange}
              className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-[15px] text-slate-900 outline-none transition focus:border-[#2d6df6] focus:bg-white focus:ring-4 focus:ring-[#2d6df6]/10"
            />
          </div>
          <div className="space-y-2">
            <label className="block text-[11px] font-extrabold tracking-[0.08em] text-slate-400 uppercase">
              New Password
            </label>
            <input
              type="password"
              name="newPassword"
              value={passwordForm.newPassword}
              onChange={onPasswordChange}
              className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-[15px] text-slate-900 outline-none transition focus:border-[#2d6df6] focus:bg-white focus:ring-4 focus:ring-[#2d6df6]/10"
            />
          </div>
          <div className="space-y-2 md:col-span-2">
            <label className="block text-[11px] font-extrabold tracking-[0.08em] text-slate-400 uppercase">
              Confirm New Password
            </label>
            <input
              type="password"
              name="confirmPassword"
              value={passwordForm.confirmPassword}
              onChange={onPasswordChange}
              className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-[15px] text-slate-900 outline-none transition focus:border-[#2d6df6] focus:bg-white focus:ring-4 focus:ring-[#2d6df6]/10"
            />
          </div>
        </div>
        <div className="mt-5 flex justify-end">
          <button
            type="button"
            className="border border-gray-300 text-gray-700 bg-white rounded-lg px-4 py-2 text-sm font-medium shadow-sm disabled:cursor-not-allowed"
            onClick={onChangePassword}
            disabled={changingPassword}
          >
            {changingPassword ? "Updating..." : "Update Password"}
          </button>
        </div>
      </div>
    </div>
  );
}
