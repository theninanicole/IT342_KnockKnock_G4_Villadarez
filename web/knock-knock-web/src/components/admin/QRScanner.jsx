import React, { useEffect, useRef } from "react";
import { Html5Qrcode } from "html5-qrcode";
import { QrCode, X } from "lucide-react";

// QR scanner that immediately starts the camera when opened and
// stops it cleanly when closed or after a successful scan.
export default function QRScanner({ onScanSuccess, onClose }) {
  const qrCodeRef = useRef(null);
  const isStoppingRef = useRef(false);

  useEffect(() => {
    const scannerId = "qr-reader";
    let html5QrCode;

    try {
      html5QrCode = new Html5Qrcode(scannerId);
    } catch (err) {
      console.error("Failed to initialize QR scanner", err);
      return;
    }

    qrCodeRef.current = html5QrCode;

    const config = {
      fps: 10,
      qrbox: {
        width: 250,
        height: 250,
      },
    };

    html5QrCode
      .start(
        { facingMode: "environment" },
        config,
        (decodedText, decodedResult) => {
          // On successful scan: stop camera, then notify listeners and close.
          // eslint-disable-next-line react-hooks/immutability
          stopScanner(() => {
            if (onScanSuccess) {
              onScanSuccess(decodedText, decodedResult);
            }
            if (onClose) {
              onClose();
            }
          });
        },
        (errorMessage) => {
          // Frequent scan errors are normal while scanning; log quietly.
          console.debug("QR scan error", errorMessage);
        }
      )
      .catch((err) => {
        console.error("Failed to start QR scanner", err);
      });

    return () => {
      // On unmount, always try to stop and clear the scanner.
      stopScanner();
    };
  }, [onScanSuccess, onClose]);

  function stopScanner(afterStop) {
    const instance = qrCodeRef.current;
    if (!instance || isStoppingRef.current) {
      if (afterStop) afterStop();
      return;
    }

    isStoppingRef.current = true;
    try {
      instance
        .stop()
        .catch((err) => {
          console.warn("Failed to stop QR scanner", err);
        })
        .finally(() => {
          instance
            .clear()
            .catch((err) =>
              console.warn("Failed to clear QR scanner", err)
            )
            .finally(() => {
              qrCodeRef.current = null;
              isStoppingRef.current = false;
              if (afterStop) afterStop();
            });
        });
    } catch (err) {
      console.warn("Failed to synchronously stop QR scanner", err);
      qrCodeRef.current = null;
      isStoppingRef.current = false;
      if (afterStop) afterStop();
    }
  }

  const handleCloseClick = () => {
    // Close modal immediately; cleanup happens on unmount via stopScanner.
    if (onClose) {
      onClose();
    }
  };

  return (
    <div className="fixed inset-0 bg-slate-900/40 backdrop-blur-sm flex items-center justify-center z-50 p-4 font-sans">
      <div className="max-w-[480px] w-full bg-white rounded-[24px] shadow-2xl ring-1 ring-gray-900/5 flex flex-col overflow-hidden">
        <div className="flex items-center justify-between px-6 pt-5 pb-3 border-b border-gray-50">
          <div className="flex items-center gap-3">
            <div className="rounded-2xl bg-blue-50 text-blue-600 p-2.5">
              <QrCode className="w-5 h-5" />
            </div>
            <div>
              <h4 className="text-base font-semibold text-gray-900">Scan visitor QR code</h4>
              <p className="text-xs text-gray-500 mt-0.5">
                Align the QR inside the frame to verify a visit quickly.
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

        <div className="px-6 pt-4 pb-5 space-y-4 bg-gray-50/60">
          <div className="rounded-2xl bg-black/95 border border-slate-800 shadow-inner flex items-center justify-center overflow-hidden">
            <div
              id="qr-reader"
              className="w-full h-72 max-h-[320px] rounded-[18px] overflow-hidden"
            />
          </div>
          <div className="text-[11px] text-gray-500 flex items-center justify-between gap-3">
            <span>
              • Point the camera at the visitor&apos;s QR code.
            </span>
            <span className="text-gray-400">
              Camera permission is required the first time.
            </span>
          </div>
        </div>
      </div>
    </div>
  );
}
