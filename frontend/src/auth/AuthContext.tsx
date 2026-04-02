import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode
} from "react";
import { apiUrl } from "../config/api";
import { api, getAuthToken, setAuthToken } from "../services/api";
import type { AuthUser } from "../types/auth";

type AuthState = {
  user: AuthUser | null;
  bootstrapping: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
  refreshMe: () => Promise<void>;
};

const AuthContext = createContext<AuthState | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [bootstrapping, setBootstrapping] = useState(true);

  const refreshMe = useCallback(async () => {
    const token = getAuthToken();
    if (!token) {
      setUser(null);
      return;
    }
    try {
      const me = await api.getAuthMe();
      setUser(me);
    } catch {
      setAuthToken(null);
      setUser(null);
    }
  }, []);

  useEffect(() => {
    let cancelled = false;
    async function init() {
      try {
        if (!cancelled) {
          await refreshMe();
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
    const res = await api.login(email, password);
    setAuthToken(res.accessToken);
    setUser(res.user);
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
