import React, { useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import notificationService from '../../services/notificationService';
import { getAuthToken } from '../../utils/api';

const NotificationManager = () => {
  const { isAuthenticated, user } = useAuth();

  useEffect(() => {
    if (isAuthenticated) {
      const token = getAuthToken();
      if (token && user) {
        notificationService.startConnection(token, user);
      }
    } else {
      notificationService.stopConnection();
    }
  }, [isAuthenticated, user]);

  return null;
};

export default NotificationManager;
