import type { DetailedHTMLProps, HTMLAttributes } from "react";

declare module "react" {
  namespace JSX {
    interface IntrinsicElements {
      "model-viewer": DetailedHTMLProps<HTMLAttributes<HTMLElement>, HTMLElement> & {
        src?: string;
        alt?: string;
        poster?: string;
        loading?: "auto" | "lazy" | "eager";
        reveal?: "auto" | "interaction" | "manual";
        "camera-controls"?: boolean;
        "touch-action"?: string;
        "auto-rotate"?: boolean;
        "auto-rotate-delay"?: string;
        "rotation-per-second"?: string;
        "camera-orbit"?: string;
        "min-camera-orbit"?: string;
        "max-camera-orbit"?: string;
        "field-of-view"?: string;
        "min-field-of-view"?: string;
        "max-field-of-view"?: string;
        "environment-image"?: string;
        exposure?: string;
        "shadow-intensity"?: string;
        "shadow-softness"?: string;
        "interaction-prompt"?: "none" | "auto" | "when-focused";
        "interaction-prompt-style"?: "basic" | "wiggle";
        "disable-zoom"?: boolean;
      };
    }
  }
}
