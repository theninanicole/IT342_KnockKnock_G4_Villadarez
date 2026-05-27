import React from "react";

export default function CheckOutModal({ isOpen, onClose, onCheckOut, visit }) {
  if (!isOpen) return null;
  return (
    <div className="fixed inset-0 bg-slate-900/40 backdrop-blur-sm flex items-center justify-center z-50 p-4 font-sans">
      <div className="max-w-[380px] w-full bg-white rounded-[24px] shadow-2xl ring-1 ring-gray-900/5 flex flex-col">
        <div className="flex justify-between items-center px-6 pt-6 pb-4 flex-shrink-0">
          <h2 className="text-xl font-semibold text-gray-900">Confirm Check-Out</h2>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-900 transition-colors p-1.5 rounded-full hover:bg-gray-100"
          >
            <span className="sr-only">Close</span>
            <svg width="20" height="20" fill="none" viewBox="0 0 24 24"><path stroke="currentColor" strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M18 6 6 18M6 6l12 12"/></svg>
          </button>
        </div>
        <div className="px-6 pb-6">
          <p className="text-base text-gray-700 mb-6">
            Are you sure you want to check out <span className="font-semibold">{visit.visitor?.fullName}</span>?
          </p>
          <button
            className="w-full py-2.5 text-base font-semibold rounded-xl transition-all bg-[#2663eb] text-white hover:bg-[#1e54d1]"
            onClick={() => onCheckOut(visit)}
          >
            Confirm Check-Out
          </button>
        </div>
      </div>
    </div>
  );
}
