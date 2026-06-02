import { useEffect, useRef } from "react";
import { apiUrl } from "../config/api";
import { getAuthToken } from "../services/api";

const STREAM_PATH = "/api/missions/stream";

async function consumeMissionUpdates(signal: AbortSignal, onUpdate: () => void) {
  const token = getAuthToken();
  if (!token) return;

  const response = await fetch(apiUrl(STREAM_PATH), {
    headers: {
      Accept: "text/event-stream",
      Authorization: `Bearer ${token}`
    },
    signal
  });
  if (!response.ok || !response.body) return;

  const reader = response.body.getReader();
  const decoder = new TextDecoder();
  let buffer = "";

  while (!signal.aborted) {
    const { done, value } = await reader.read();
    if (done) break;
    buffer += decoder.decode(value, { stream: true });

    const events = buffer.split(/\r?\n\r?\n/);
    buffer = events.pop() ?? "";
    for (const event of events) {
      if (event.split(/\r?\n/).some((line) => line === "event:mission-update")) {
        onUpdate();
      }
    }
  }
}

/**
 * Atualizações: SSE (mission-update) + polling de segurança + refresh ao voltar à aba.
 * O backend publica eventos após cada transição de status no workflow Kafka.
 */
export function useMissionUpdates(onRefresh: () => void | Promise<void>, pollMs = 12000) {
  const cb = useRef(onRefresh);
  cb.current = onRefresh;

  useEffect(() => {
    const run = () => void cb.current();
    const controller = new AbortController();

    void consumeMissionUpdates(controller.signal, run).catch(() => {});

    const interval = window.setInterval(run, pollMs);

    const onVis = () => {
      if (document.visibilityState === "visible") run();
    };
    document.addEventListener("visibilitychange", onVis);

    run();

    return () => {
      controller.abort();
      window.clearInterval(interval);
      document.removeEventListener("visibilitychange", onVis);
    };
  }, [pollMs]);
}
