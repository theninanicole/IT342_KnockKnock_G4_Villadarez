import React, { useEffect, useRef, useState } from "react";
import { X, FileText } from "lucide-react";
import { toast } from "react-toastify";
import api, { checkInVisit } from "@api/apiServices";

export default function AdminVisitDetailsModal({ isOpen, onClose, visit, onCheckedIn }) {
  const [files, setFiles] = useState([]);
  const [loadingFiles, setLoadingFiles] = useState(false);
  const [checkingIn, setCheckingIn] = useState(false);
  const prevIsOpenRef = useRef(false);

  useEffect(() => {
    if (isOpen && !prevIsOpenRef.current && visit?.visitId) {
      fetchVisitFiles(visit.visitId);
    }
    if (!isOpen && prevIsOpenRef.current) {
      setFiles([]);
    }
    prevIsOpenRef.current = isOpen;
  }, [isOpen, visit?.visitId]);

  const fetchVisitFiles = async (visitId) => {
    try {
      setLoadingFiles(true);
      const response = await api.get(`/visits/${visitId}/files`);
      setFiles(response.data || []);
    } catch (error) {
      console.error("[AdminVisitDetailsModal] Error fetching files:", error);
      setFiles([]);
    } finally {
      setLoadingFiles(false);
    }
  };

  if (!isOpen || !visit) return null;

  const handleViewFile = (file) => {
    if (file.fileUrl) {
      window.open(file.fileUrl, "_blank");
    } else {
      toast.error("File URL not available");
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return "-";
    const date = new Date(dateString);
    return date.toLocaleDateString("en-US", {
      year: "numeric",
      month: "long",
      day: "numeric",
    });
  };

  const handleCheckIn = async () => {
    try {
      setCheckingIn(true);
      const updatedVisit = await checkInVisit(visit.visitId);
      toast.success("Visitor checked in successfully");
      if (onCheckedIn) {
        onCheckedIn(updatedVisit);
      }
      onClose();
    } catch (error) {
      console.error("[AdminVisitDetailsModal] Error during check-in:", error);
      const message =
        error?.response?.data?.message ||
        error?.response?.data?.error ||
        "Failed to check in visitor";
      toast.error(message);
    } finally {
      setCheckingIn(false);
    }
  };

  const canCheckIn = visit.status === "SCHEDULED";
  const showSecurityChecklist = visit.status !== "CHECKED-OUT";

  return (
    <div className="fixed inset-0 bg-slate-900/40 backdrop-blur-sm flex items-center justify-center z-50 p-4 font-sans">
      <div className="max-w-[460px] w-full bg-white rounded-[24px] shadow-2xl ring-1 ring-gray-900/5 flex flex-col max-h-[90vh]">
        <div className="flex justify-between items-center px-6 pt-6 pb-4 flex-shrink-0">
          <div>
            <h2 className="text-xl font-semibold text-blue-600">Visit Details</h2>
            <p className="text-xs text-slate-400 mt-1 font-mono">
              {visit.referenceNumber}
            </p>
          </div>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-900 transition-colors p-1.5 rounded-full hover:bg-gray-100"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="px-6 pb-4 space-y-4 overflow-y-auto flex-1">
          <div className="text-sm text-slate-500">
            <div className="font-semibold text-slate-900 mb-1">Visitor</div>
            <div>{visit.visitor?.fullName}</div>
          </div>

          <div className="grid grid-cols-2 gap-4 text-sm">
            <div>
              <div className="text-slate-500 mb-1">Unit Number</div>
              <div className="font-medium text-slate-900">{visit.unitNumber}</div>
            </div>
            <div>
              <div className="text-slate-500 mb-1">Date of Visit</div>
              <div className="font-medium text-slate-900">
                {formatDate(visit.visitDate)}
              </div>
            </div>
          </div>

          <div className="text-sm">
            <div className="text-slate-500 mb-1">Purpose of Visit</div>
            <div className="font-medium text-slate-900">
              {visit.purpose}
            </div>
          </div>

          <div className="pt-2">
            <label className="text-sm text-gray-500 mb-2 block">ID Document</label>
            {files && files.length > 0 ? (
              <div className="space-y-3">
                {files.map((file, index) => (
                  <div
                    key={index}
                    onClick={() => handleViewFile(file)}
                    className="flex items-center justify-between p-3.5 bg-white border border-gray-200 rounded-2xl cursor-pointer group hover:shadow-sm transition-shadow"
                  >
                    <div className="flex items-center gap-3.5 overflow-hidden flex-1">
                      <div className="p-2 rounded-xl bg-blue-50 text-blue-600">
                        <FileText className="w-4 h-4" />
                      </div>
                      <span className="text-sm font-medium text-gray-700 truncate max-w-[200px]">
                        {file.fileName || "Document"}
                      </span>
                    </div>
                    <button className="text-sm text-gray-400 font-medium px-2 transition-colors group-hover:text-blue-600">
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

          {showSecurityChecklist && (
            <div className="mt-4 p-3 rounded-2xl bg-amber-50 border border-amber-100 text-xs text-amber-800">
              <div className="font-semibold mb-1">Security Checklist</div>
              <p>Verify that the person checking in matches the individual shown in the uploaded ID.</p>
            </div>
          )}
        </div>

        {canCheckIn && (
          <div className="px-6 pb-6 pt-4 border-t border-gray-50 flex-shrink-0 bg-white rounded-b-[24px]">
            <button
              onClick={handleCheckIn}
              disabled={checkingIn}
              className="w-full px-4 py-2.5 text-sm font-semibold text-white rounded-xl shadow-sm disabled:opacity-70 disabled:cursor-not-allowed"
              style={{ backgroundColor: "#2663EB" }}
            >
              {checkingIn ? "Checking In..." : "Check In"}
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
