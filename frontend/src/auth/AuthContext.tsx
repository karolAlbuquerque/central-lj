import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
  type ReactNode
} from "react";
import { apiUrl } from "../config/api";
import { api, getAuthToken, SESSION_EXPIRED_EVENT, setAuthToken, TOKEN_KEY } from "../services/api";
import type { AuthUser } from "../types/auth";

type AuthState = {
  user: AuthUser | null;
  bootstrapping: boolean;
  login: (email: string, password: string) => Promise<AuthUser>;
  logout: () => void;
  refreshMe: () => Promise<void>;
};

const AuthContext = createContext<AuthState | null>(null);

function isPublicAuthRoute(): boolean {
  if (typeof window === "undefined") return false;
  const p = window.location.pathname;
  return p === "/login" || p === "/cadastro";
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [bootstrapping, setBootstrapping] = useState(true);
  const refreshSeq = useRef(0);

  const refreshMe = useCallback(async () => {
    const token = getAuthToken();
    if (!token) {
      setUser(null);
      return;
    }
    const seq = ++refreshSeq.current;
    const tokenAtStart = token;
    try {
      const me = await api.getAuthMe();
      if (seq !== refreshSeq.current) return;
      setUser(me);
    } catch {
      if (seq !== refreshSeq.current) return;
      // Login pode ter trocado o token enquanto /me ainda respondia com 401.
      if (getAuthToken() !== tokenAtStart) return;
      setAuthToken(null);
      setUser(null);
    }
  }, []);

  useEffect(() => {
    const onSessionExpired = () => {
      refreshSeq.current += 1;
      setAuthToken(null);
      setUser(null);
    };
    const onStorage = (e: StorageEvent) => {
      if (e.key !== TOKEN_KEY) return;
      refreshSeq.current += 1;
      if (!e.newValue) {
        setUser(null);
        return;
      }
      void refreshMe();
    };
    window.addEventListener(SESSION_EXPIRED_EVENT, onSessionExpired);
    window.addEventListener("storage", onStorage);
    return () => {
      window.removeEventListener(SESSION_EXPIRED_EVENT, onSessionExpired);
      window.removeEventListener("storage", onStorage);
    };
  }, [refreshMe]);

  useEffect(() => {
    let cancelled = false;
    async function init() {
      try {
        if (!cancelled) {
          if (isPublicAuthRoute()) {
            setAuthToken(null);
            setUser(null);
          } else {
            await refreshMe();
          }
        }
      } finally {
        if (!cancelled) {
          setBootstrapping(false);
        }
      }
    }
    void init();
    return () => {
      cancelled = true;
    };
  }, [refreshMe]);

  const login = useCallback(async (email: string, password: string) => {
    refreshSeq.current += 1;
    const res = await api.login(email, password);
    setAuthToken(res.accessToken);
    setUser(res.user);
    return res.user;
  }, []);

  const logout = useCallback(async () => {
    const t = getAuthToken();
    if (t) {
      try {
        await fetch(apiUrl("/api/auth/logout"), {
          method: "POST",
          headers: { Accept: "application/json", Authorization: `Bearer ${t}` }
        });
      } catch {
        /* noop */
      }
    }
    setAuthToken(null);
    setUser(null);
  }, []);

  const value = useMemo(
    () => ({ user, bootstrapping, login, logout, refreshMe }),
    [user, bootstrapping, login, logout, refreshMe]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthState {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error("useAuth fora de AuthProvider");
  }
  return ctx;
}
