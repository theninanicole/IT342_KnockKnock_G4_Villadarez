import React from "react";

export default function CondoDetailsCard({ condo }) {
  if (!condo) return null;

  return (
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
  );
}
