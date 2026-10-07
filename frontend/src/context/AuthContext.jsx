import { createContext, useCallback, useContext, useMemo, useState } from "react";
import { tokenStore } from "../api/client";
import * as authApi from "../api/auth";

const AuthContext = createContext(null);
const USER_KEY = "mam.user";

function readStoredUser() {
  try {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(readStoredUser);
  const [token, setToken] = useState(() => tokenStore.get());

  const adopt = useCallback((auth) => {
    const profile = { email: auth.email, fullName: auth.fullName, role: auth.role };
    tokenStore.set(auth.token);
    localStorage.setItem(USER_KEY, JSON.stringify(profile));
    setToken(auth.token);
    setUser(profile);
    return profile;
  }, []);

  const signIn = useCallback(async (email, password) => {
    return adopt(await authApi.login(email, password));
  }, [adopt]);

  /** Registration returns the same payload as login, so the new account is signed in straight away. */
  const signUp = useCallback(async (payload) => {
    return adopt(await authApi.register(payload));
  }, [adopt]);

  const signOut = useCallback(() => {
    tokenStore.clear();
    localStorage.removeItem(USER_KEY);
    setToken(null);
    setUser(null);
  }, []);

  const value = useMemo(
    () => ({ user, token, signIn, signUp, signOut, isAuthenticated: Boolean(token) }),
    [user, token, signIn, signUp, signOut]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used inside an AuthProvider");
  }
  return context;
}