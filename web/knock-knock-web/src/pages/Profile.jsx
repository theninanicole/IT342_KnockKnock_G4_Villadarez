import React, { useContext, useEffect, useState } from "react";
import Sidebar from "../components/Sidebar";
import TopBar from "../components/Topbar";
import "./Dashboard.css";
import { Mail, Calendar } from "lucide-react";
import { AuthContext } from "../context/AuthContext";
import { getCurrentUser, updateUserProfile, changeUserPassword } from "../services/apiServices";

export default function Profile() {
  const { user, login } = useContext(AuthContext);
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [isEditing, setIsEditing] = useState(false);
  const [profileForm, setProfileForm] = useState({ fullName: "", contactNumber: "" });
  const [passwordForm, setPasswordForm] = useState({ currentPassword: "", newPassword: "", confirmPassword: "" });
  const [savingProfile, setSavingProfile] = useState(false);
  const [changingPassword, setChangingPassword] = useState(false);

  useEffect(() => {
    const load = async () => {
      try {
        const data = await getCurrentUser();
        const apiUser = data.user || data;
        setProfile(apiUser);
        setProfileForm({
          fullName: apiUser.fullName || "",
          contactNumber: apiUser.contactNumber ? String(apiUser.contactNumber) : "",
        });
      } catch (err) {
        alert(err?.response?.data?.error || "Failed to load profile");
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  const handleProfileChange = (e) => {
    const { name, value } = e.target;
    setProfileForm((prev) => ({ ...prev, [name]: value }));
  };

  const handlePasswordChange = (e) => {
    const { name, value } = e.target;
    setPasswordForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleCancelEdit = () => {
    if (profile) {
      setProfileForm({
        fullName: profile.fullName || "",
        contactNumber: profile.contactNumber ? String(profile.contactNumber) : "",
      });
    }
    setIsEditing(false);
  };

  const handleSaveProfile = async () => {
    setSavingProfile(true);
    try {
      await updateUserProfile({
        fullName: profileForm.fullName,
        contactNumber: profileForm.contactNumber,
      });
      setProfile((prev) =>
        prev
          ? { ...prev, fullName: profileForm.fullName, contactNumber: profileForm.contactNumber }
          : prev
      );

      const updatedUser = user
        ? { ...user, fullName: profileForm.fullName, contactNumber: profileForm.contactNumber }
        : null;
      if (updatedUser) {
        await login(updatedUser);
      }

      setIsEditing(false);
      alert("Profile updated successfully");
    } catch (err) {
      const msg =
        err?.response?.data?.error ||
        err?.response?.data?.message ||
        "Failed to update profile";
      alert(msg);
    } finally {
      setSavingProfile(false);
    }
  };

  const handleChangePassword = async () => {
    setChangingPassword(true);
    try {
      await changeUserPassword(passwordForm);
      setPasswordForm({ currentPassword: "", newPassword: "", confirmPassword: "" });
      alert("Password changed successfully");
    } catch (err) {
      const msg =
        err?.response?.data?.error ||
        err?.response?.data?.message ||
        "Failed to change password";
      alert(msg);
    } finally {
      setChangingPassword(false);
    }
  };

  if (loading) {
    return (
      <div className="dashboard-layout">
        <Sidebar role={user?.role || "VISITOR"} />
        <div className="main-content">
          <TopBar title="Account" />
          <div className="content-inner">
            <p>Loading profile...</p>
          </div>
        </div>
      </div>
    );
  }

  if (!profile) {
    return (
      <div className="dashboard-layout">
        <Sidebar role={user?.role || "VISITOR"} />
        <div className="main-content">
          <TopBar title="Account" />
          <div className="content-inner">
            <p>Unable to load profile.</p>
          </div>
        </div>
      </div>
    );
  }

  const isGoogle = profile.authProvider && profile.authProvider.toLowerCase() === "google";

  const getInitials = (name) => {
    if (!name) return "?";
    const parts = String(name).trim().split(/\s+/);
    if (parts.length === 1) return parts[0].charAt(0).toUpperCase();
    return `${parts[0].charAt(0)}${parts[1].charAt(0)}`.toUpperCase();
  };

  const formatJoinedDate = (value) => {
    if (!value) return "—";
    try {
      const date = new Date(value);
      if (Number.isNaN(date.getTime())) return String(value);
      return date.toLocaleDateString("en-US", {
        year: "numeric",
        month: "long",
        day: "numeric",
      });
    } catch {
      return String(value);
    }
  };

  const joinedDate = formatJoinedDate(
    profile.createdAt || profile.createdDate || profile.joinedAt || profile.accountCreatedAt
  );

  const isCondoAdmin = profile.role === "CONDOMINIUM_ADMIN";
  const condo = profile.condo || null;

  return (
    <div className="dashboard-layout">
      <Sidebar role={profile.role || "VISITOR"} />
      <div className="main-content">
        <TopBar title="Account" />
        <div className="content-inner">
          <div className="max-w-5xl mx-auto mt-4 sm:mt-8 space-y-6">
            {/* Main profile card */}
            <div className="bg-white rounded-[24px] shadow-sm border border-gray-200 overflow-hidden">
              {/* Pastel banner */}
              <div className="h-48 w-full bg-gradient-to-r from-rose-50 via-teal-50 to-violet-100" />

              {/* Header with avatar and actions */}
              <div className="px-6 sm:px-8 -mt-12 pb-4 sm:pb-6 relative">
                <div className="flex items-start justify-between gap-4 sm:gap-6">
                  <div className="flex items-center gap-4 sm:gap-6">
                    <div className="w-24 h-24 bg-blue-600 border-4 border-white rounded-full flex items-center justify-center text-[32px] font-bold text-white shadow-md">
                      {getInitials(profile.fullName)}
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
                          onClick={handleCancelEdit}
                          className="border border-gray-300 text-gray-700 bg-white rounded-lg px-4 py-2 text-sm font-medium shadow-sm"
                          disabled={savingProfile}
                        >
                          Cancel
                        </button>
                        <button
                          type="button"
                          onClick={handleSaveProfile}
                          className="bg-gray-900 text-white rounded-lg px-4 py-2 text-sm font-medium shadow-sm hover:bg-black disabled:opacity-60"
                          disabled={savingProfile}
                        >
                          {savingProfile ? "Saving..." : "Save"}
                        </button>
                      </>
                    ) : (
                      <button
                        type="button"
                        onClick={() => setIsEditing(true)}
                        className="border border-gray-300 text-gray-700 bg-white rounded-lg px-4 py-2 text-sm font-medium shadow-sm"
                      >
                        Edit
                      </button>
                    )}
                  </div>
                </div>
              </div>

              {/* Form rows */}
              <div className="border-t border-gray-100">
                <div className="divide-y divide-gray-100">
                  {/* Full name */}
                  <div className="py-7 px-6 sm:px-8 flex flex-col sm:flex-row gap-4 sm:gap-8 items-start sm:items-center">
                    <div className="sm:w-1/3 max-w-[280px]">
                      <div className="text-[14px] font-medium text-gray-900">Full name</div>
                    </div>
                    <div className="flex-1 w-full max-w-2xl">
                      <input
                        type="text"
                        name="fullName"
                        value={profileForm.fullName}
                        onChange={handleProfileChange}
                        disabled={!isEditing}
                        className={`w-full max-w-md px-3.5 py-2.5 rounded-lg text-[15px] outline-none border transition-all ${
                          isEditing
                            ? "bg-white border-gray-300 text-gray-900 focus:ring-4 focus:ring-gray-900/5 focus:border-gray-500 shadow-sm"
                            : "bg-gray-50/50 border-gray-200 text-gray-600 cursor-default"
                        }`}
                      />
                    </div>
                  </div>

                  {/* Contact number */}
                  <div className="py-7 px-6 sm:px-8 flex flex-col sm:flex-row gap-4 sm:gap-8 items-start sm:items-center">
                    <div className="sm:w-1/3 max-w-[280px]">
                      <div className="text-[14px] font-medium text-gray-900">Contact number</div>
                    </div>
                    <div className="flex-1 w-full max-w-2xl">
                      <input
                        type="text"
                        name="contactNumber"
                        value={profileForm.contactNumber}
                        onChange={handleProfileChange}
                        disabled={!isEditing}
                        className={`w-full max-w-md px-3.5 py-2.5 rounded-lg text-[15px] outline-none border transition-all ${
                          isEditing
                            ? "bg-white border-gray-300 text-gray-900 focus:ring-4 focus:ring-gray-900/5 focus:border-gray-500 shadow-sm"
                            : "bg-gray-50/50 border-gray-200 text-gray-600 cursor-default"
                        }`}
                      />
                    </div>
                  </div>

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
            </div>

            {/* Condo details card (admin only) */}
            {isCondoAdmin && condo && (
              <div className="bg-white rounded-[24px] shadow-sm border border-gray-200 overflow-hidden">
                <div className="px-6 sm:px-8 py-6 border-b border-gray-100 flex items-center justify-between">
                  <div>
                    <h3 className="text-[16px] font-semibold text-gray-900">Condominium</h3>
                    <p className="text-[13px] text-gray-500 mt-1">
                      Information about the condominium you manage.
                    </p>
                  </div>
                </div>

                <div className="divide-y divide-gray-100">
                  <div className="py-5 px-6 sm:px-8 flex flex-col sm:flex-row gap-4 sm:gap-8 items-start sm:items-center">
                    <div className="sm:w-1/3 max-w-[280px]">
                      <div className="text-[14px] font-medium text-gray-900">Name</div>
                    </div>
                    <div className="flex-1 w-full max-w-2xl">
                      <div className="w-full max-w-md px-3.5 py-2.5 rounded-lg bg-gray-50/80 border border-gray-200 text-[15px] text-gray-700 cursor-not-allowed">
                        {condo.name || "—"}
                      </div>
                    </div>
                  </div>

                  <div className="py-5 px-6 sm:px-8 flex flex-col sm:flex-row gap-4 sm:gap-8 items-start sm:items-center">
                    <div className="sm:w-1/3 max-w-[280px]">
                      <div className="text-[14px] font-medium text-gray-900">Code</div>
                    </div>
                    <div className="flex-1 w-full max-w-2xl">
                      <div className="w-full max-w-md px-3.5 py-2.5 rounded-lg bg-gray-50/80 border border-gray-200 text-[15px] text-gray-700 cursor-not-allowed">
                        {condo.code || "—"}
                      </div>
                    </div>
                  </div>

                  <div className="py-5 px-6 sm:px-8 flex flex-col sm:flex-row gap-4 sm:gap-8 items-start sm:items-center">
                    <div className="sm:w-1/3 max-w-[280px]">
                      <div className="text-[14px] font-medium text-gray-900">Address</div>
                    </div>
                    <div className="flex-1 w-full max-w-2xl">
                      <div className="w-full max-w-md px-3.5 py-2.5 rounded-lg bg-gray-50/80 border border-gray-200 text-[15px] text-gray-700 cursor-not-allowed whitespace-pre-line">
                        {condo.address || "—"}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            )}

            {/* Change password card (keep existing behavior) */}
            {!isGoogle && (
              <div className="account-card">
                <div className="account-body">
                  <h3 className="account-section-title">Change Password</h3>
                  <div className="account-grid">
                    <div className="account-field">
                      <label>Current Password</label>
                      <input
                        type="password"
                        name="currentPassword"
                        value={passwordForm.currentPassword}
                        onChange={handlePasswordChange}
                      />
                    </div>
                    <div className="account-field">
                      <label>New Password</label>
                      <input
                        type="password"
                        name="newPassword"
                        value={passwordForm.newPassword}
                        onChange={handlePasswordChange}
                      />
                    </div>
                    <div className="account-field">
                      <label>Confirm New Password</label>
                      <input
                        type="password"
                        name="confirmPassword"
                        value={passwordForm.confirmPassword}
                        onChange={handlePasswordChange}
                      />
                    </div>
                  </div>
                  <div className="account-actions-row">
                    <button
                      type="button"
                      className="primary-btn"
                      onClick={handleChangePassword}
                      disabled={changingPassword}
                    >
                      {changingPassword ? "Updating..." : "Update Password"}
                    </button>
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
