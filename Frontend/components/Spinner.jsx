import React from 'react';

const Spinner = ({ size = 'md', message = '' }) => {
  const sizeClass = {
    sm: 'spinner-sm',
    md: 'spinner-md',
    lg: 'spinner-lg',
  }[size] || 'spinner-md';

  return (
    <div className="loading-container">
      <div className={`spinner ${sizeClass}`}></div>
      {message && <p>{message}</p>}
    </div>
  );
};

export default Spinner;
