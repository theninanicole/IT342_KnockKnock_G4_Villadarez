import React, { useEffect, useRef } from "react";
import { Html5Qrcode } from "html5-qrcode";
import { QrCode, X } from "lucide-react";

export default function QRScanner({ onScanSuccess, onClose }) {
  const qrCodeRef = useRef(null);
  const isStoppingRef = useRef(false);
  const scanHandledRef = useRef(false);

  // Keep refs of callbacks to avoid re-running the effect when they change
  const onScanSuccessRef = useRef(onScanSuccess);
  const onCloseRef = useRef(onClose);

  useEffect(() => {
    onScanSuccessRef.current = onScanSuccess;
    onCloseRef.current = onClose;
  }, [onScanSuccess, onClose]);

  useEffect(() => {
    const scannerId = "qr-reader";
    const html5QrCode = new Html5Qrcode(scannerId);
    qrCodeRef.current = html5QrCode;

    const startScanner = async () => {
      try {
        await html5QrCode.start(
          { facingMode: "environment" },
          { fps: 10, qrbox: { width: 250, height: 250 } },
          async (decodedText, decodedResult) => {
            if (scanHandledRef.current) return;
            scanHandledRef.current = true;

            try {
              // Gracefully stop before triggering parent callbacks
              await stopScanner();
              onScanSuccessRef.current?.(decodedText, decodedResult);
              onCloseRef.current?.();
            } catch (err) {
              console.error("Error during scan stop:", err);
            }
          },
          () => {
            // Standard scanning noise, no action needed
          }
        );
      } catch (err) {
        console.error("Failed to start QR scanner", err);
      }
    };

    startScanner();

    return () => {
      // Logic for when the component unmounts
      if (html5QrCode.isScanning) {
        html5QrCode.stop()
          .then(() => html5QrCode.clear())
          .catch((e) => console.warn("Cleanup on unmount failed", e));
      }
    };
  }, []);

  async function stopScanner() {
    const instance = qrCodeRef.current;

    if (!instance || isStoppingRef.current) return;
    isStoppingRef.current = true;

    try {
      // Use the library's built-in state check
      if (instance.isScanning) {
        await instance.stop();
        await instance.clear();
      }

      // Manual cleanup of the video element just in case
      const video = document.querySelector(`#qr-reader video`);
      if (video) {
        const stream = video.srcObject;
        if (stream) {
          stream.getTracks().forEach((track) => track.stop());
          video.srcObject = null;
        }
      }
    } catch (err) {
      console.warn("Stop sequence encountered an issue:", err);
    } finally {
      isStoppingRef.current = false;
    }
  }

  const handleCloseClick = async () => {
    await stopScanner();
    onCloseRef.current?.();
  };

  return (
    <div className="fixed inset-0 bg-slate-900/40 backdrop-blur-sm flex items-center justify-center z-50 p-4 font-sans">
      <div className="max-w-[480px] w-full bg-white rounded-[24px] shadow-2xl ring-1 ring-gray-900/5 flex flex-col overflow-hidden">
        
        {/* HEADER */}
        <div className="flex items-center justify-between px-6 pt-5 pb-3 border-b border-gray-50">
          <div className="flex items-center gap-3">
            <div className="rounded-2xl bg-blue-50 text-blue-600 p-2.5">
              <QrCode className="w-5 h-5" />
            </div>
            <div>
              <h4 className="text-base font-semibold text-gray-900">
                Scan visitor QR code
              </h4>
              <p className="text-xs text-gray-500 mt-0.5">
                Align the QR inside the frame to verify.
              </p>
            </div>
          </div>

          <button
            type="button"
            onClick={handleCloseClick}
            className="text-gray-400 hover:text-gray-900 transition-colors p-1.5 rounded-full hover:bg-gray-100"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* SCANNER BODY */}
        <div className="px-6 pt-4 pb-5 space-y-4 bg-gray-50/60">
          <div className="rounded-2xl bg-black/95 border border-slate-800 shadow-inner flex items-center justify-center overflow-hidden">
            <div
              id="qr-reader"
              className="w-full h-72 max-h-[320px] rounded-[18px] overflow-hidden"
            />
          </div>

          <div className="text-[11px] text-gray-500 flex items-center justify-between gap-3">
            <span>• Point the camera at the visitor&apos;s QR code.</span>
          </div>
        </div>
      </div>
    </div>
  );
}