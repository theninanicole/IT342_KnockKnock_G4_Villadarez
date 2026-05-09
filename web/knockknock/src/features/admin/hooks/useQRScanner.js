import { useCallback, useEffect, useRef } from "react";
import { BrowserQRCodeReader } from "@zxing/browser";

export default function useQRScanner({ onScanSuccess, onError }) {
  const videoRef = useRef(null);
  const codeReaderRef = useRef(null);
  const controlsRef = useRef(null);
  const isActiveRef = useRef(true);
  const onScanSuccessRef = useRef(onScanSuccess);
  const onErrorRef = useRef(onError);

  useEffect(() => {
    onScanSuccessRef.current = onScanSuccess;
    onErrorRef.current = onError;
  }, [onScanSuccess, onError]);

  const stopScanner = useCallback(() => {
    try {
      isActiveRef.current = false;
      controlsRef.current?.stop();
      controlsRef.current = null;
      codeReaderRef.current?.reset?.();

      const video = videoRef.current;
      if (video && video.srcObject) {
        const stream = video.srcObject;
        stream.getTracks().forEach((track) => track.stop());
        video.srcObject = null;
      }
    } catch (err) {
      console.warn("Stop error:", err);
    }
  }, []);

  const startScanner = useCallback(async () => {
    try {
      isActiveRef.current = true;
      codeReaderRef.current = new BrowserQRCodeReader();

      controlsRef.current = await codeReaderRef.current.decodeFromConstraints(
        {
          video: {
            facingMode: { ideal: "environment" },
          },
        },
        videoRef.current,
        (result, err) => {
          if (result && isActiveRef.current) {
            isActiveRef.current = false;
            stopScanner();
            onScanSuccessRef.current?.(result.getText());
          }

          if (err && err.name !== "NotFoundException") {
            onErrorRef.current?.(err);
          }
        }
      );
    } catch (err) {
      console.error("Scanner start error:", err);
      onErrorRef.current?.(err);
    }
  }, [stopScanner]);

  useEffect(() => {
    startScanner();

    return () => {
      stopScanner();
    };
  }, [startScanner, stopScanner]);

  return {
    videoRef,
    stopScanner,
  };
}
