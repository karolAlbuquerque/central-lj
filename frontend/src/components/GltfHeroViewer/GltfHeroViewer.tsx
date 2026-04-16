import { Suspense, useEffect, useLayoutEffect, useMemo, useRef } from "react";
import { Canvas, useThree } from "@react-three/fiber";
import { Center, OrbitControls, useAnimations, useGLTF } from "@react-three/drei";
import { Box3, NoToneMapping, PCFSoftShadowMap, SRGBColorSpace, Texture, Vector3 } from "three";
import type { Group } from "three";
import styles from "./GltfHeroViewer.module.css";

export type GltfHeroViewerProps = {
  modelUrl: string;
  /** Escala aplicada ao grupo (ou multiplicador após `normalizeDimensions`). */
  scale?: number;
  groupPosition?: readonly [number, number, number];
  cameraFov?: number;
  cameraPosition?: readonly [number, number, number];
  /**
   * Ajusta escala pelo AABB do GLB — necessário quando o arquivo vem em cm/m
   * ou unidades enormes; senão a câmera fica fora do modelo (tela “vazia”).
   */
  normalizeDimensions?: boolean;
  /** Maior dimensão desejada do AABB em espaço mundo após normalizar (× `scale`). */
  normalizedTargetSize?: number;
  /**
   * Se false, não reproduz clips do GLB (pose de repouso).
   * Útil quando alguma animação distorce meshes (ex.: capa com AABB enorme).
   */
  autoPlayAnimations?: boolean;
  /** Rotação Euler (rad) do grupo do modelo, após `Center` — ex.: `[0, Math.PI, 0]` para virar de costas → frente. */
  modelRotation?: readonly [number, number, number];
  /** Habilita rotação manual com mouse (OrbitControls). */
  enableMouseOrbit?: boolean;
};

function ClearSceneBackground() {
  const scene = useThree((s) => s.scene);
  const gl = useThree((s) => s.gl);
  useEffect(() => {
    scene.background = null;
    gl.setClearColor(0x000000, 0);
  }, [scene, gl]);
  return null;
}

function CanvasResizeSync() {
  const gl = useThree((s) => s.gl);
  const setSize = useThree((s) => s.setSize);
  const invalidate = useThree((s) => s.invalidate);

  useLayoutEffect(() => {
    const el = gl.domElement.parentElement;
    if (!el) return;

    const sync = () => {
      const w = el.clientWidth;
      const h = el.clientHeight;
      if (w > 0 && h > 0) setSize(w, h);
      invalidate();
    };

    sync();
    const r0 = requestAnimationFrame(sync);
    const r1 = requestAnimationFrame(() => requestAnimationFrame(sync));

    const ro = new ResizeObserver(() => sync());
    ro.observe(el);
    window.addEventListener("resize", sync);

    return () => {
      cancelAnimationFrame(r0);
      cancelAnimationFrame(r1);
      ro.disconnect();
      window.removeEventListener("resize", sync);
    };
  }, [gl, setSize, invalidate]);

  return null;
}

function EnhanceModelQuality({ root }: { root: Group }) {
  const gl = useThree((s) => s.gl);
  const invalidate = useThree((s) => s.invalidate);

  useEffect(() => {
    const maxAnisotropy = gl.capabilities.getMaxAnisotropy();

    root.traverse((obj) => {
      const node = obj as { isMesh?: boolean; castShadow?: boolean; receiveShadow?: boolean; material?: unknown };
      if (!node.isMesh) return;

      node.castShadow = true;
      node.receiveShadow = true;

      const materials = Array.isArray(node.material) ? node.material : node.material ? [node.material] : [];
      materials.forEach((mat) => {
        const typed = mat as {
          map?: Texture;
          normalMap?: Texture;
          roughnessMap?: Texture;
          metalnessMap?: Texture;
          emissiveMap?: Texture;
          aoMap?: Texture;
          needsUpdate?: boolean;
        };

        [typed.map, typed.normalMap, typed.roughnessMap, typed.metalnessMap, typed.emissiveMap, typed.aoMap]
          .filter((tex): tex is Texture => Boolean(tex))
          .forEach((tex) => {
            tex.anisotropy = Math.max(tex.anisotropy, maxAnisotropy);
            tex.needsUpdate = true;
          });

        typed.needsUpdate = true;
      });
    });

    invalidate();
  }, [root, gl, invalidate]);

  return null;
}

type SceneProps = {
  modelUrl: string;
  scale: number;
  groupPosition: readonly [number, number, number];
  normalizeDimensions: boolean;
  normalizedTargetSize: number;
  autoPlayAnimations: boolean;
  modelRotation: readonly [number, number, number];
};

function Scene({
  modelUrl,
  scale,
  groupPosition,
  normalizeDimensions,
  normalizedTargetSize,
  autoPlayAnimations,
  modelRotation
}: SceneProps) {
  const { scene, animations } = useGLTF(modelUrl);
  const animRootRef = useRef<Group>(null);
  const { actions } = useAnimations(animations, animRootRef);

  const uniformScale = useMemo(() => {
    if (!normalizeDimensions) return scale;
    scene.updateMatrixWorld(true);
    const box = new Box3().setFromObject(scene);
    if (box.isEmpty()) return scale;
    const sz = new Vector3();
    box.getSize(sz);
    const m = Math.max(sz.x, sz.y, sz.z, 1e-6);
    return (normalizedTargetSize / m) * scale;
  }, [scene, scale, normalizeDimensions, normalizedTargetSize]);

  useEffect(() => {
    if (!autoPlayAnimations || !actions) return;
    const list = Object.values(actions).filter((a): a is NonNullable<typeof a> => Boolean(a));
    list.forEach((action) => {
      action.reset().fadeIn(0.45).play();
    });
    return () => {
      list.forEach((action) => {
        action.fadeOut(0.35);
      });
    };
  }, [actions, autoPlayAnimations]);

  return (
    <>
      <CanvasResizeSync />
      <ClearSceneBackground />
      <EnhanceModelQuality root={scene} />
      <ambientLight intensity={0.95} color="#ffffff" />
      <hemisphereLight args={["#f5f8ff", "#404550", 0.85]} />
      <directionalLight position={[6, 12, 8]} intensity={1.6} color="#ffffff" castShadow />
      <directionalLight position={[-5, 4, -4]} intensity={0.55} color="#ffffff" />
      <directionalLight position={[0, -2, 6]} intensity={0.35} color="#e8eef8" />
      <spotLight position={[4, 10, 2]} angle={0.55} penumbra={0.65} intensity={0.9} color="#fffaf2" />
      <group
        position={groupPosition as [number, number, number]}
        scale={uniformScale}
        rotation={modelRotation as [number, number, number]}
      >
        <Center object={scene} precise>
          <group ref={animRootRef}>
            <primitive object={scene} dispose={null} />
          </group>
        </Center>
      </group>
    </>
  );
}

export function GltfHeroViewer({
  modelUrl,
  scale = 3.45,
  groupPosition = [0, -0.04, 0] as const,
  cameraFov = 44,
  cameraPosition = [0, 0.12, 10.2] as const,
  normalizeDimensions = false,
  normalizedTargetSize = 2.35,
  autoPlayAnimations = true,
  modelRotation = [0, 0, 0] as const,
  enableMouseOrbit = false
}: GltfHeroViewerProps) {
  useEffect(() => {
    useGLTF.preload(modelUrl);
  }, [modelUrl]);

  return (
    <div className={styles.wrap} aria-hidden>
      <Suspense fallback={<div className={styles.fallback}>Carregando modelo…</div>}>
        <Canvas
          className={styles.canvas}
          frameloop="always"
          onCreated={({ gl }) => {
            gl.shadowMap.enabled = true;
            gl.shadowMap.type = PCFSoftShadowMap;
          }}
          camera={{
            fov: cameraFov,
            near: 0.1,
            far: 200,
            position: [...cameraPosition] as [number, number, number]
          }}
          dpr={[1, Math.min(typeof window !== "undefined" ? window.devicePixelRatio * 1.5 : 2, 3)]}
          gl={{
            antialias: true,
            alpha: true,
            powerPreference: "high-performance",
            stencil: false,
            depth: true,
            toneMapping: NoToneMapping,
            toneMappingExposure: 1,
            outputColorSpace: SRGBColorSpace
          }}
        >
          {enableMouseOrbit ? (
            <OrbitControls
              makeDefault
              enablePan={false}
              enableDamping
              dampingFactor={0.08}
              minDistance={3}
              maxDistance={28}
            />
          ) : null}
          <Scene
            modelUrl={modelUrl}
            scale={scale}
            groupPosition={groupPosition}
            normalizeDimensions={normalizeDimensions}
            normalizedTargetSize={normalizedTargetSize}
            autoPlayAnimations={autoPlayAnimations}
            modelRotation={modelRotation}
          />
        </Canvas>
      </Suspense>
    </div>
  );
}
