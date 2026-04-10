import { useCallback, useContext, useEffect, useState } from "react";
import { AuthContext } from "@store/AuthContext";
import { getCurrentUser, updateUserProfile, changeUserPassword } from "@api/apiServices";
import { formatJoinedDate } from "@utils/dateUtils";

export const useProfileActions = () => {
  const { user, login } = useContext(AuthContext);

  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [isEditing, setIsEditing] = useState(false);
  const [profileForm, setProfileForm] = useState({ fullName: "", contactNumber: "" });
  const [passwordForm, setPasswordForm] = useState({
    currentPassword: "",
    newPassword: "",
    confirmPassword: "",
  });
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
        // eslint-disable-next-line no-alert
        alert(err?.response?.data?.error || "Failed to load profile");
      } finally {
        setLoading(false);
      }
    };

    load();
  }, []);

  const handleProfileChange = useCallback((event) => {
    const { name, value } = event.target;
    setProfileForm((prev) => ({ ...prev, [name]: value }));
  }, []);

  const handlePasswordChange = useCallback((event) => {
    const { name, value } = event.target;
    setPasswordForm((prev) => ({ ...prev, [name]: value }));
  }, []);

  const handleCancelEdit = useCallback(() => {
    if (profile) {
      setProfileForm({
        fullName: profile.fullName || "",
        contactNumber: profile.contactNumber ? String(profile.contactNumber) : "",
      });
    }
    setIsEditing(false);
  }, [profile]);

  const handleSaveProfile = useCallback(async () => {
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
      // eslint-disable-next-line no-alert
      alert("Profile updated successfully");
    } catch (err) {
      const msg =
        err?.response?.data?.error ||
        err?.response?.data?.message ||
        "Failed to update profile";
      // eslint-disable-next-line no-alert
      alert(msg);
    } finally {
      setSavingProfile(false);
    }
  }, [login, profileForm.contactNumber, profileForm.fullName, user]);

  const handleChangePassword = useCallback(async () => {
    setChangingPassword(true);
    try {
      await changeUserPassword(passwordForm);
      setPasswordForm({ currentPassword: "", newPassword: "", confirmPassword: "" });
      // eslint-disable-next-line no-alert
      alert("Password changed successfully");
    } catch (err) {
      const msg =
        err?.response?.data?.error ||
        err?.response?.data?.message ||
        "Failed to change password";
      // eslint-disable-next-line no-alert
      alert(msg);
    } finally {
      setChangingPassword(false);
    }
  }, [passwordForm]);

  const isGoogle = Boolean(
    profile?.authProvider && profile.authProvider.toLowerCase() === "google"
  );

  const joinedDate = profile
    ? formatJoinedDate(
        profile.createdAt ||
          profile.createdDate ||
          profile.joinedAt ||
          profile.accountCreatedAt
      )
    : "—";

  const isCondoAdmin = profile?.role === "CONDOMINIUM_ADMIN";
  const condo = profile?.condo || null;

  return {
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
  };
};
