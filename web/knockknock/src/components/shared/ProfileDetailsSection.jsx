import React from "react";
import { Mail, Calendar } from "lucide-react";
import ProfileField from "@components/shared/ProfileField";

export default function ProfileDetailsSection({
  profile,
  profileForm,
  isEditing,
  isGoogle,
  joinedDate,
  onProfileChange,
}) {
  return (
    <div className="border-t border-gray-100">
      <div className="divide-y divide-gray-100">
        {/* Full name */}
        <ProfileField
          label="Full name"
          name="fullName"
          value={profileForm.fullName}
          onChange={onProfileChange}
          isEditing={isEditing}
        />

        {/* Contact number */}
        <ProfileField
          label="Contact number"
          name="contactNumber"
          value={profileForm.contactNumber}
          onChange={onProfileChange}
          isEditing={isEditing}
        />

        {/* Email address (read-only) */}
        <div className="py-7 px-6 sm:px-8 flex flex-col sm:flex-row gap-4 sm:gap-8 items-start sm:items-center">
          <div className="sm:w-1/3 max-w-[280px]">
            <div className="text-[14px] font-medium text-gray-900">Email address</div>
            <p className="text-[13px] text-gray-500 mt-1">
              Primary email linked to your account.
            </p>
          </div>
          <div className="flex-1 w-full max-w-2xl">
            <div className="w-full max-w-md px-3.5 py-2.5 rounded-lg bg-gray-50/80 border border-gray-200 text-gray-500 cursor-not-allowed flex items-center justify-between gap-3">
              <div className="flex items-center gap-2.5 min-w-0">
                <Mail className="w-4 h-4 text-gray-400" />
                <span className="truncate text-[15px]">{profile.email}</span>
              </div>
              {isGoogle && (
                <img
                  src="https://fonts.gstatic.com/s/i/productlogos/googleg/v6/24px.svg"
                  alt="Google account"
                  className="w-5 h-5 flex-shrink-0"
                />
              )}
            </div>
          </div>
        </div>

        {/* Account created (read-only) */}
        <div className="py-7 px-6 sm:px-8 flex flex-col sm:flex-row gap-4 sm:gap-8 items-start sm:items-center">
          <div className="sm:w-1/3 max-w-[280px]">
            <div className="text-[14px] font-medium text-gray-900">Account created</div>
            <p className="text-[13px] text-gray-500 mt-1">
              The date you joined the platform.
            </p>
          </div>
          <div className="flex-1 w-full max-w-2xl">
            <div className="w-full max-w-md px-3.5 py-2.5 rounded-lg bg-gray-50/80 border border-gray-200 text-gray-600 cursor-not-allowed flex items-center gap-2.5">
              <Calendar className="w-4 h-4 text-gray-400" />
              <span className="text-[15px]">{joinedDate}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
