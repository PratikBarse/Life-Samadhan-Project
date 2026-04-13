import React, { useEffect, useState } from 'react';

const Alert = ({ type = 'info', message, onClose, autoDismiss = true, duration = 5000 }) => {
  const [visible, setVisible] = useState(true);

  useEffect(() => {
    if (autoDismiss && visible) {
      const timer = setTimeout(() => {
        setVisible(false);
        if (onClose) onClose();
      }, duration);

      return () => clearTimeout(timer);
    }
  }, [visible, autoDismiss, duration, onClose]);

  if (!visible) return null;

  const alertClass = `alert alert-${type}`;
  
  
  const icons = {
    success: '✓',
    error: '✕',
    warning: '⚠',
    info: 'ℹ',
  };

  const handleClose = () => {
    setVisible(false);
    if (onClose) onClose();
  };

  return (
    <div className={alertClass}>
      <span className="alert-icon">{icons[type]}</span>
      <span className="alert-message">{message}</span>
      <button 
        className="alert-close" 
        onClick={handleClose}
        aria-label="Close alert"
      >
        ✕
      </button>
    </div>
  );
};

export default Alert;
