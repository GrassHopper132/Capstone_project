import { createContext, useCallback, useContext, useMemo, useReducer } from "react";

const AppContext = createContext(null);

const initialState = { theme: "light", notice: null };

/** Global UI state. Kept in a reducer because notices and theme change independently. */
function reducer(state, action) {
  switch (action.type) {
    case "TOGGLE_THEME":
      return { ...state, theme: state.theme === "light" ? "dark" : "light" };
    case "NOTIFY":
      return { ...state, notice: { kind: action.kind, text: action.text } };
    case "DISMISS":
      return { ...state, notice: null };
    default:
      return state;
  }
}

export function AppProvider({ children }) {
  const [state, dispatch] = useReducer(reducer, initialState);

  const notify = useCallback((kind, text) => dispatch({ type: "NOTIFY", kind, text }), []);
  const dismiss = useCallback(() => dispatch({ type: "DISMISS" }), []);
  const toggleTheme = useCallback(() => dispatch({ type: "TOGGLE_THEME" }), []);

  const value = useMemo(
    () => ({ ...state, notify, dismiss, toggleTheme }),
    [state, notify, dismiss, toggleTheme]
  );

  return <AppContext.Provider value={value}>{children}</AppContext.Provider>;
}

export function useApp() {
  const context = useContext(AppContext);
  if (!context) {
    throw new Error("useApp must be used inside an AppProvider");
  }
  return context;
}