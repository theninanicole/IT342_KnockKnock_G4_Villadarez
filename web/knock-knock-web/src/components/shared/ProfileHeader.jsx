import React from "react";
import { getInitials } from "@utils/userHelpers";

export default function ProfileHeader({
  fullName,
  isEditing,
  savingProfile,
  onEdit,
  onCancel,
  onSave,
}) {
  const initials = getInitials(fullName);

  return (
    <>
      {/* Pastel banner */}
      <div className="h-48 w-full bg-gradient-to-r from-rose-50 via-teal-50 to-violet-100" />

      {/* Header with avatar and actions */}
      <div className="px-6 sm:px-8 -mt-12 pb-4 sm:pb-6 relative">
        <div className="flex items-start justify-between gap-4 sm:gap-6">
          <div className="flex items-center gap-4 sm:gap-6">
            <div className="w-24 h-24 bg-blue-600 border-4 border-white rounded-full flex items-center justify-center text-[32px] font-bold text-white shadow-md">
              {initials}
            </div>
            <div className="flex flex-col gap-1">
              <h2 className="text-[22px] font-bold text-gray-900">Profile</h2>
              <p className="text-[15px] text-gray-500">Update your personal details.</p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            {isEditing ? (
              <>
                <button
                  type="button"
                  onClick={onCancel}
                  className="border border-gray-300 text-gray-700 bg-white rounded-lg px-4 py-2 text-sm font-medium shadow-sm"
                  disabled={savingProfile}
                >
                  Cancel
                </button>
                <button
                  type="button"
                  onClick={onSave}
                  className="bg-gray-900 text-white rounded-lg px-4 py-2 text-sm font-medium shadow-sm hover:bg-black disabled:opacity-60"
                  disabled={savingProfile}
                >
                  {savingProfile ? "Saving..." : "Save"}
                </button>
              </>
            ) : (
              <button
                type="button"
                onClick={onEdit}
                className="border border-gray-300 text-gray-700 bg-white rounded-lg px-4 py-2 text-sm font-medium shadow-sm"
              >
                Edit
              </button>
            )}
          </div>
        </div>
      </div>
    </>
  );
}
