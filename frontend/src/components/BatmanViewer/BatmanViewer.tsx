import { useGLTF } from "@react-three/drei";
import { GltfHeroViewer } from "../GltfHeroViewer/GltfHeroViewer";
import { HERO_GLTF_VIEW_PRESET } from "../GltfHeroViewer/heroViewPreset";

useGLTF.preload("/batman.glb");

export function BatmanViewer() {
  return <GltfHeroViewer modelUrl="/batman.glb" {...HERO_GLTF_VIEW_PRESET} />;
}
