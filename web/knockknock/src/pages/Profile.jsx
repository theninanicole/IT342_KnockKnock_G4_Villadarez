import React from "react";
import Sidebar from "@components/shared/Sidebar";
import TopBar from "@components/shared/Topbar";
import { useProfileActions } from "@hooks/useProfileActions";
import ProfileHeader from "@components/shared/ProfileHeader";
import ProfileDetailsSection from "@components/shared/ProfileDetailsSection";
import PasswordChangeCard from "@components/shared/PasswordChangeCard";
import CondoDetailsCard from "@components/shared/CondoDetailsCard";

export default function Profile() {
  const {
    user,
    profile,
    loading,
    isEditing,
    profileForm,
    passwordForm,
    savingProfile,
    changingPassword,
    isGoogle,
    joinedDate,
    isCondoAdmin,
    condo,
    setIsEditing,
    handleProfileChange,
    handlePasswordChange,
    handleCancelEdit,
    handleSaveProfile,
    handleChangePassword,
  } = useProfileActions();

  if (loading) {
    return (
      <div className="flex min-h-screen min-w-full bg-slate-50">
        <Sidebar role={user?.role || "VISITOR"} />
        <div className="flex flex-col flex-1">
          <TopBar title="Account" />
          <div className="w-full px-4 py-6 sm:px-8 sm:py-8">
            <p className="text-sm text-gray-500">Loading profile...</p>
          </div>
        </div>
      </div>
    );
  }

  if (!profile) {
    return (
      <div className="flex min-h-screen min-w-full bg-slate-50">
        <Sidebar role={user?.role || "VISITOR"} />
        <div className="flex flex-col flex-1">
          <TopBar title="Account" />
          <div className="w-full px-4 py-6 sm:px-8 sm:py-8">
            <p className="text-sm text-red-500">Unable to load profile.</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="flex min-h-screen min-w-full bg-slate-50">
      <Sidebar role={profile.role || "VISITOR"} />
      <div className="flex flex-col flex-1">
        <TopBar title="Account" />
        <div className="w-full px-4 py-6 sm:px-8 sm:py-8">
          <div className="max-w-5xl mx-auto mt-4 sm:mt-8 space-y-6">
            {/* Main profile card */}
            <div className="bg-white rounded-[24px] shadow-sm border border-gray-200 overflow-hidden">
              <ProfileHeader
                fullName={profile.fullName}
                isEditing={isEditing}
                savingProfile={savingProfile}
                onEdit={() => setIsEditing(true)}
                onCancel={handleCancelEdit}
                onSave={handleSaveProfile}
              />

              {/* Form rows */}
              <ProfileDetailsSection
                profile={profile}
                profileForm={profileForm}
                isEditing={isEditing}
                isGoogle={isGoogle}
                joinedDate={joinedDate}
                onProfileChange={handleProfileChange}
              />
            </div>

            {/* Condo details card (admin only) */}
            {isCondoAdmin && condo && <CondoDetailsCard condo={condo} />}

            {/* Change password card (keep existing behavior) */}
            {!isGoogle && (
              <PasswordChangeCard
                passwordForm={passwordForm}
                onPasswordChange={handlePasswordChange}
                onChangePassword={handleChangePassword}
                changingPassword={changingPassword}
              />
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
