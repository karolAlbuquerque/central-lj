import { useEffect, useRef } from "react";
import styles from "./ModelViewer.module.css";

const MODEL_VIEWER_SCRIPT_ID = "google-model-viewer-script";
const MODEL_VIEWER_SCRIPT_SRC = "https://unpkg.com/@google/model-viewer/dist/model-viewer.min.js";

function ensureModelViewerScript() {
  if (typeof window === "undefined") return;
  if (document.getElementById(MODEL_VIEWER_SCRIPT_ID)) return;

  const script = document.createElement("script");
  script.id = MODEL_VIEWER_SCRIPT_ID;
  script.type = "module";
  script.src = MODEL_VIEWER_SCRIPT_SRC;
  document.head.appendChild(script);
}

type ModelViewerProps = {
  src: string;
  alt: string;
  cameraOrbit?: string;
  minCameraOrbit?: string;
  maxCameraOrbit?: string;
  fieldOfView?: string;
  minFieldOfView?: string;
  maxFieldOfView?: string;
};

export function ModelViewer({
  src,
  alt,
  cameraOrbit = "34deg 76deg 205%",
  minCameraOrbit = "auto 35deg 145%",
  maxCameraOrbit = "auto 120deg 330%",
  fieldOfView = "34deg",
  minFieldOfView = "22deg",
  maxFieldOfView = "50deg"
}: ModelViewerProps) {
  const viewerRef = useRef<HTMLElement | null>(null);

  useEffect(() => {
    ensureModelViewerScript();
  }, []);

  useEffect(() => {
    if (typeof window === "undefined") return;
    const el = viewerRef.current as
      | (HTMLElement & { minimumRenderScale?: number; maximumRenderScale?: number })
      | null;
    if (!el) return;

    let active = true;
    customElements
      .whenDefined("model-viewer")
      .then(() => {
        if (!active || !el) return;
        // Evita blur/pixelização durante rotação por escala dinâmica de render.
        el.minimumRenderScale = 1;
        el.maximumRenderScale = 1.75;
      })
      .catch(() => {
        // script/cdn indisponível: mantém comportamento padrão.
      });

    return () => {
      active = false;
    };
  }, [src]);

  return (
    <div className={styles.wrap}>
      <model-viewer
        ref={viewerRef}
        className={styles.viewer}
        src={src}
        alt={alt}
        loading="eager"
        reveal="auto"
        camera-controls
        touch-action="pan-y"
        camera-orbit={cameraOrbit}
        min-camera-orbit={minCameraOrbit}
        max-camera-orbit={maxCameraOrbit}
        field-of-view={fieldOfView}
        min-field-of-view={minFieldOfView}
        max-field-of-view={maxFieldOfView}
        environment-image="neutral"
        exposure="1.08"
        shadow-intensity="0.48"
        shadow-softness="0.72"
        interaction-prompt="none"
      />
    </div>
  );
}
