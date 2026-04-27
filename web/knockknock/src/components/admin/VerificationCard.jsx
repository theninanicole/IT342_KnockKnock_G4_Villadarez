import React from "react";
import { QrCode } from "lucide-react";
import SearchInput from "@components/shared/SearchInput";

export default function VerificationCard({
  searchTerm,
  onSearchChange,
  onVerify,
  onQrClick,
}) {
  return (
    <div className="bg-white rounded-2xl border border-gray-100 p-12 shadow-sm flex flex-col items-center">
      <h3 className="text-[22px] font-bold text-gray-900 mb-2">Verification</h3>
      <p className="text-gray-500 text-[15px] mb-8">
        Scan QR or enter reference number presented by the visitor.
      </p>

      <div className="flex items-center gap-4 w-full max-w-4xl">
        <SearchInput
          value={searchTerm}
          onChange={onSearchChange}
          placeholder="Search reference number..."
          className="flex-1 flex items-center bg-gray-50/80 rounded-xl px-4 py-3.5 border border-gray-100 transition-colors focus-within:bg-white focus-within:border-gray-300"
        />
        <button
          type="button"
          onClick={onQrClick}
          className="bg-blue-600 hover:bg-blue-700 text-white p-3.5 rounded-xl transition-colors shadow-sm flex-shrink-0"
        >
          <QrCode className="w-6 h-6" />
        </button>
        <button
          className="bg-[#111827] hover:bg-gray-800 text-white px-10 py-3.5 rounded-xl font-medium text-[15px] transition-colors shadow-sm flex-shrink-0"
          onClick={onVerify}
        >
          Verify
        </button>
      </div>
    </div>
  );
}
