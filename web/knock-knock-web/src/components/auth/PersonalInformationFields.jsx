import React from "react";
import AuthInput from "@components/auth/AuthInput";

export default function PersonalInformationFields({ formData, onChange }) {
  return (
    <div>
      <div className="mb-4 mt-1 border-b border-slate-100 pb-1 text-[11px] font-extrabold tracking-[0.12em] text-[#2d6df6]">
        PERSONAL INFORMATION
      </div>

      <div className="mb-4">
        <AuthInput
          label="FULL NAME"
          type="text"
          name="fullName"
          placeholder="Juan Dela Cruz"
          value={formData.fullName}
          onChange={onChange}
        />
      </div>

      <div className="grid gap-4 md:grid-cols-2">
        <AuthInput
          label="EMAIL ADDRESS"
          type="email"
          name="email"
          placeholder="name@email.com"
          value={formData.email}
          onChange={onChange}
        />
        <AuthInput
          label="CONTACT NUMBER"
          type="text"
          name="contactNumber"
          placeholder="09123456789"
          value={formData.contactNumber}
          onChange={onChange}
        />
      </div>
    </div>
  );
}
