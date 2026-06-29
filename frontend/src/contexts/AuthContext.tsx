import { createContext, useContext, useState, type PropsWithChildren } from 'react';
import { clearAuthState, loadAuthState, saveAuthState } from '../lib/storage';

type AuthContextValue = {
  token: string | null;
  username: string | null;
  isAuthenticated: boolean;
  signIn: (token: string, username: string) => void;
  signOut: () => void;
};

const initialState = loadAuthState();

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: PropsWithChildren) {
  const [token, setToken] = useState<string | null>(initialState?.token ?? null);
  const [username, setUsername] = useState<string | null>(initialState?.username ?? null);

  function signIn(nextToken: string, nextUsername: string) {
    setToken(nextToken);
    setUsername(nextUsername);
    saveAuthState({
      token: nextToken,
      username: nextUsername,
    });
  }

  function signOut() {
    setToken(null);
    setUsername(null);
    clearAuthState();
  }

  return (
    <AuthContext.Provider
      value={{
        token,
        username,
        isAuthenticated: Boolean(token),
        signIn,
        signOut,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used inside AuthProvider');
  }
  return context;
}
