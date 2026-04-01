import { useEffect, useRef } from "react";

const STREAM_PATH = "/api/missions/stream";

/**
 * Atualizações: SSE (mission-update) + polling de segurança + refresh ao voltar à aba.
 * O backend publica eventos após cada transição de status no workflow Kafka.
 */
export function useMissionUpdates(onRefresh: () => void | Promise<void>, pollMs = 12000) {
  const cb = useRef(onRefresh);
  cb.current = onRefresh;

  useEffect(() => {
    const run = () => void cb.current();

    const es = new EventSource(STREAM_PATH);
    es.addEventListener("mission-update", run);
    es.addEventListener("connected", () => {});

    es.onerror = () => {
      es.close();
    };

    const interval = window.setInterval(run, pollMs);

    const onVis = () => {
      if (document.visibilityState === "visible") run();
    };
    document.addEventListener("visibilitychange", onVis);

    run();

    return () => {
      es.close();
      window.clearInterval(interval);
      document.removeEventListener("visibilitychange", onVis);
    };
  }, [pollMs]);
}
