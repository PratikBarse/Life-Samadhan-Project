import React, { createContext, useState, useContext, useEffect } from 'react';
import { setAuthToken, removeAuthToken, setUserData, getUserData, getUserRole } from '../utils/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [role, setRole] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    
    const storedUser = getUserData();
    const storedRole = getUserRole();
    
    if (storedUser && storedRole) {
      setUser(storedUser);
      setRole(storedRole);
    }
    
    setLoading(false);
  }, []);

  const login = (token, userData, userRole) => {
    setAuthToken(token);
    setUserData(userData, userRole);
    setUser(userData);
    setRole(userRole);
  };

  const logout = () => {
    removeAuthToken();
    setUser(null);
    setRole(null);
  };

  const value = {
    user,
    role,
    loading,
    login,
    logout,
    isAuthenticated: !!user,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
