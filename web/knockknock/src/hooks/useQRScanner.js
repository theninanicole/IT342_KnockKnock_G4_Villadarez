import { useEffect, useRef } from "react";
import { BrowserQRCodeReader } from "@zxing/browser";

export default function useQRScanner({ onScanSuccess, onError }) {
  const videoRef = useRef(null);
  const codeReaderRef = useRef(null);
  const controlsRef = useRef(null);
  const isActiveRef = useRef(true);

  useEffect(() => {
    // eslint-disable-next-line
    startScanner();

    return () => {
        // eslint-disable-next-line
      stopScanner();
    };
  }, []);

  const startScanner = async () => {
    try {
      codeReaderRef.current = new BrowserQRCodeReader();

      controlsRef.current = await codeReaderRef.current.decodeFromConstraints(
        {
          video: {
            facingMode: { ideal: "environment" }, // rear cam
          },
        },
        videoRef.current,
        (result, err) => {
          if (result && isActiveRef.current) {
            isActiveRef.current = false;

            stopScanner(); 

            onScanSuccess?.(result.getText());
          }

          if (err && err.name !== "NotFoundException") {
            onError?.(err);
          }
        }
      );
    } catch (err) {
      console.error("Scanner start error:", err);
      onError?.(err);
    }
  };

  const stopScanner = () => {
    try {
      isActiveRef.current = false;
      controlsRef.current?.stop();
      codeReaderRef.current?.reset();

      const video = videoRef.current;
      if (video && video.srcObject) {
        const stream = video.srcObject;
        stream.getTracks().forEach(track => track.stop());
        video.srcObject = null;
      }

    } catch (err) {
      console.warn("Stop error:", err);
    }
  };

  return {
    videoRef,
    stopScanner,
  };
}