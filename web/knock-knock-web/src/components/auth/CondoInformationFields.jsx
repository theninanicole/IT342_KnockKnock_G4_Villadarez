import React from "react";
import AuthInput from "@components/auth/AuthInput";

export default function CondoInformationFields({ formData, onChange, googleAdminStep }) {
  return (
    <div className="space-y-4">
      <div className="mt-2 border-b border-slate-100 pb-1 text-[11px] font-extrabold tracking-[0.12em] text-[#2d6df6]">
        CONDOMINIUM DETAILS
      </div>
      {googleAdminStep && (
        <p className="text-xs font-medium text-slate-500">
          Step 2: Google sign-in successful. Please enter your condominium details to complete
          registration.
        </p>
      )}
      <AuthInput
        label="CONDOMINIUM NAME"
        type="text"
        name="condoName"
        placeholder="e.g. Makati Tower"
        value={formData.condoName}
        onChange={onChange}
      />
      <AuthInput
        label="CONDOMINIUM ADDRESS"
        type="text"
        name="condoAddress"
        placeholder="123 Ayala Avenue..."
        value={formData.condoAddress}
        onChange={onChange}
      />
      <AuthInput
        label="CONDOMINIUM CONTACT (OPTIONAL)"
        type="text"
        name="condoContact"
        placeholder="e.g. (02) 123 4567 or mobile number"
        value={formData.condoContact}
        onChange={onChange}
        required={false}
      />
    </div>
  );
}
