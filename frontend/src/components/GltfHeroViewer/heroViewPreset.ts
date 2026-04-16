/**
 * Preset compartilhado: mesmo enquadramento / escala / câmera para GLBs de herói
 * (Lanterna Verde na Nova missão, Batman no painel).
 */
export const HERO_GLTF_VIEW_PRESET = {
  normalizeDimensions: true as const,
  normalizedTargetSize: 3.05,
  scale: 1.45,
  groupPosition: [0, -0.5, 0] as const,
  cameraFov: 38,
  cameraPosition: [0, 0.06, 7.35] as const
};
