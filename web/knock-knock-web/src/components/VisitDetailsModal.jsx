import React, { useState, useEffect, useRef } from "react";
import { X, FileText, QrCode } from "lucide-react";
import { QRCodeSVG } from "qrcode.react";
import { toast } from "react-toastify";
import api from "../services/apiServices";

/**
 * VisitDetailsModal - Displays visit details with optional QR code generation
 * Props:
 * - isOpen: boolean - Controls modal visibility
 * - onClose: function - Called when modal should close
 * - visit: object - Visit data { id, condominium, unitNumber, dateOfVisit, purpose, idFileName, qrCode }
 * - onGenerateQR: function - Called when Generate QR is clicked
 * - onEdit: function - Called when Edit is clicked
 * - onCancelVisit: function - Called when Cancel Visit is clicked
 */
export default function VisitDetailsModal({
  isOpen,
  onClose,
  visit,
  onGenerateQR,
  onEdit,
  onCancelVisit,
}) {
  const [isGenerating, setIsGenerating] = useState(false);
  const [showConfirmCancel, setShowConfirmCancel] = useState(false);
  const [files, setFiles] = useState([]);
  const [loadingFiles, setLoadingFiles] = useState(false);
  const prevIsOpenRef = useRef(false);

  // Fetch files whenever modal opens (transition from closed to open)
  useEffect(() => {
    // If modal just opened
    if (isOpen && !prevIsOpenRef.current && visit?.visitId) {
      console.log("[VisitDetailsModal] Modal OPENED - fetching fresh files for visit:", visit.visitId);
      fetchVisitFiles(visit.visitId);
    }
    // If modal just closed
    if (!isOpen && prevIsOpenRef.current) {
      console.log("[VisitDetailsModal] Modal CLOSED - clearing files");
      setFiles([]);
    }
    prevIsOpenRef.current = isOpen;
  }, [isOpen, visit?.visitId]);

  const fetchVisitFiles = async (visitId) => {
    try {
      setLoadingFiles(true);
      const token = localStorage.getItem("token");
      console.log("[VisitDetailsModal] Token:", token ? "EXISTS" : "MISSING");
      console.log("[VisitDetailsModal] Fetching files for visit:", visitId);
      const response = await api.get(`/visits/files/visit/${visitId}`);
      console.log("[VisitDetailsModal] Files response:", response.data);
      setFiles(response.data || []);
    } catch (error) {
      console.error("[VisitDetailsModal] Error fetching files:", error);
      console.error("[VisitDetailsModal] Response status:", error.response?.status);
      console.error("[VisitDetailsModal] Response data:", error.response?.data);
      setFiles([]);
    } finally {
      setLoadingFiles(false);
    }
  };

  if (!isOpen || !visit) return null;

  const handleGenerateQR = async () => {
    setIsGenerating(true);
    try {
      await onGenerateQR(visit.visitId);
      toast.success("QR code generated successfully!");
    } catch (error) {
      console.error("Error generating QR code:", error);
      toast.error("Failed to generate QR code");
    } finally {
      setIsGenerating(false);
    }
  };

  const handleCancelVisit = async () => {
    try {
      await onCancelVisit(visit.visitId);
      toast.success("Visit cancelled successfully!");
      setShowConfirmCancel(false);
      onClose();
    } catch (error) {
      console.error("Error cancelling visit:", error);
      toast.error("Failed to cancel visit");
    }
  };

  const handleEdit = () => {
    onEdit(visit);
    onClose();
  };

  const handleViewFile = (file) => {
    if (file.fileUrl) {
      window.open(file.fileUrl, "_blank");
    } else {
      toast.error("File URL not available");
    }
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString("en-US", {
      year: "numeric",
      month: "long",
      day: "numeric",
    });
  };

  const isPastVisit = (() => {
    const visitDateValue = visit?.visitDate || visit?.dateOfVisit;
    if (!visitDateValue) return false;
    const visitDateObj = new Date(visitDateValue);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    return visitDateObj < today;
  })();

  const isEditable = visit?.status === "SCHEDULED" && !isPastVisit;
  const isCancellable = isEditable;

  // Generate QR code data - includes visit reference number and ID
  const qrCodeData = JSON.stringify({
    visitId: visit.visitId,
    referenceNumber: visit.referenceNumber,
    condominium: visit.condo?.name || visit.condominium,
    unitNumber: visit.unitNumber,
    date: visit.visitDate || visit.dateOfVisit,
  });

  return (
    <div className="fixed inset-0 bg-slate-900/40 backdrop-blur-sm flex items-center justify-center z-50 p-4 font-sans">
      
      {/* Modal Container */}
      <div className="max-w-[420px] w-full bg-white rounded-[24px] shadow-2xl ring-1 ring-gray-900/5 flex flex-col max-h-[90vh]">
        
        {/* Header (Sticky) */}
        <div className="flex justify-between items-center px-6 pt-6 pb-4 flex-shrink-0">
          <h2 className="text-xl font-semibold" style={{ color: '#2663EB' }}>Visit Details</h2>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-900 transition-colors p-1.5 rounded-full hover:bg-gray-100"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Body Content (Scrollable) */}
        <div className="px-6 pb-2 space-y-6 overflow-y-auto flex-1">
          
          {/* Condominium */}
          <div>
            <label className="text-sm text-gray-500 mb-1.5 block">
              Condominium
            </label>
            <p className="text-base font-medium text-gray-900">
              {visit.condo?.name || visit.condominium}
            </p>
          </div>

          {/* 2-Column Grid */}
          <div className="grid grid-cols-2 gap-6">
            <div>
              <label className="text-sm text-gray-500 mb-1.5 block">
                Unit Number
              </label>
              <p className="text-base font-medium text-gray-900">
                {visit.unitNumber}
              </p>
            </div>
            <div>
              <label className="text-sm text-gray-500 mb-1.5 block">
                Date of Visit
              </label>
              <p className="text-base font-medium text-gray-900">
                {formatDate(visit.visitDate || visit.dateOfVisit)}
              </p>
            </div>
          </div>

          {/* Purpose of Visit */}
          <div>
            <label className="text-sm text-gray-500 mb-1.5 block">
              Purpose of Visit
            </label>
            <p className="text-base font-medium text-gray-900">
              {visit.purpose || visit.purposeOfVisit}
            </p>
          </div>

          {/* ID Document Component */}
          <div className="pt-2">
            <label className="text-sm text-gray-500 mb-2 block">
              ID Document
            </label>
            {files && files.length > 0 ? (
              <div className="space-y-3">
                {files.map((file, index) => (
                  <div
                    key={index}
                    onClick={() => handleViewFile(file)}
                    className="flex items-center justify-between p-3.5 bg-white border border-gray-200 rounded-2xl cursor-pointer group hover:shadow-sm transition-shadow"
                  >
                    <div className="flex items-center gap-3.5 overflow-hidden flex-1">
                      <div className="p-2 rounded-xl" style={{ backgroundColor: 'rgba(38, 99, 235, 0.1)', color: '#2663EB' }}>
                        <FileText className="w-4 h-4" />
                      </div>
                      <span className="text-sm font-medium text-gray-700 truncate max-w-[180px]">
                        {file.fileName || "Document"}
                      </span>
                    </div>
                    <button
                      className="text-sm text-gray-400 font-medium px-2 transition-colors group-hover:text-[#2663EB]"
                    >
                      View
                    </button>
                  </div>
                ))}
              </div>
            ) : loadingFiles ? (
              <div className="p-4 text-sm text-gray-500 bg-gray-50/50 rounded-2xl border border-gray-100 text-center">
                Loading files...
              </div>
            ) : (
              <div className="p-4 text-sm text-gray-500 bg-gray-50/50 rounded-2xl border border-gray-100 text-center">
                No file uploaded
              </div>
            )}
          </div>

          {/* QR Code Section (If Exists) */}
          {visit.qrCode && (
            <div className="pt-6 border-t border-gray-100 flex flex-col items-center pb-4">
              <label className="text-sm text-gray-500 mb-4 block text-center">
                Entry QR Code
              </label>
              <div className="p-4 bg-white border border-gray-200/80 rounded-2xl shadow-sm">
                <QRCodeSVG
                  value={visit.qrCode || qrCodeData}
                  size={160}
                  level="H"
                  includeMargin={true}
                />
              </div>
              <p className="text-xs text-gray-400 mt-4 text-center">
                This QR code can be scanned for quick check-in
              </p>
            </div>
          )}

        </div>

        {/* Footer Actions (Sticky) */}
        <div className="flex flex-col-reverse sm:flex-row justify-between items-center gap-3 px-6 pb-6 pt-6 border-t border-gray-50 flex-shrink-0 bg-white rounded-b-[24px]">
          
          {/* Destructive Action */}
          <button
            onClick={isCancellable ? () => setShowConfirmCancel(true) : undefined}
            disabled={!isCancellable}
            className="w-full sm:w-auto px-4 py-2 text-sm font-medium text-red-600 hover:text-red-700 hover:bg-red-50 rounded-xl transition-all disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:bg-transparent disabled:hover:text-red-400"
            title={isCancellable ? "" : "Only upcoming scheduled visits can be cancelled"}
          >
            Cancel Visit
          </button>
          
          {/* Primary & Secondary Actions */}
          <div className="flex w-full sm:w-auto gap-2">
            <button
              onClick={isEditable ? handleEdit : undefined}
              disabled={!isEditable}
              className="flex-1 sm:flex-none px-5 py-2.5 text-sm font-medium rounded-xl transition-all disabled:opacity-50 disabled:cursor-not-allowed"
              style={{
                color: isEditable ? '#2663EB' : '#9CA3AF',
                backgroundColor: 'transparent',
              }}
              onMouseEnter={(e) => {
                if (isEditable) {
                  e.currentTarget.style.backgroundColor = 'rgba(38, 99, 235, 0.05)';
                }
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.backgroundColor = 'transparent';
              }}
              title={isEditable ? '' : 'Only upcoming scheduled visits can be edited'}
            >
              Edit
            </button>
            {!visit.qrCode && isEditable && (
              <button
                onClick={handleGenerateQR}
                disabled={isGenerating}
                className="flex-1 sm:flex-none flex items-center justify-center gap-2 px-6 py-2.5 text-sm font-medium text-white rounded-xl transition-all shadow-sm disabled:opacity-75 disabled:cursor-not-allowed"
                style={{ backgroundColor: '#2663EB' }}
                onMouseEnter={(e) => !isGenerating && (e.currentTarget.style.backgroundColor = '#1e4bb8')}
                onMouseLeave={(e) => e.currentTarget.style.backgroundColor = '#2663EB'}
              >
                {isGenerating ? (
                   <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                ) : (
                  <QrCode className="w-4 h-4" />
                )}
                {isGenerating ? "Generating..." : "Generate QR"}
              </button>
            )}
          </div>
        </div>

      </div>

      {/* Confirmation Modal for Cancel Visit */}
      {showConfirmCancel && isCancellable && (
        <div className="fixed inset-0 bg-slate-900/40 backdrop-blur-sm flex items-center justify-center z-[60] p-4 font-sans">
          <div className="bg-white rounded-[24px] p-6 max-w-[340px] w-full shadow-2xl ring-1 ring-gray-900/5">
            <h3 className="text-xl font-semibold text-gray-900 mb-2 tracking-tight">
              Cancel Visit?
            </h3>
            <p className="text-sm text-gray-500 mb-6 leading-relaxed">
              Are you sure you want to cancel this visit? This action cannot be undone.
            </p>
            <div className="flex gap-3">
              <button
                onClick={() => setShowConfirmCancel(false)}
                className="flex-1 px-4 py-2.5 text-sm font-medium text-gray-700 bg-gray-50 hover:bg-gray-100 rounded-xl transition-colors"
              >
                Keep Visit
              </button>
              <button
                onClick={handleCancelVisit}
                className="flex-1 px-4 py-2.5 text-sm font-medium text-white bg-red-600 hover:bg-red-700 rounded-xl transition-colors shadow-sm"
              >
                Yes, Cancel
              </button>
            </div>
          </div>
        </div>
      )}

    </div>
  );
}