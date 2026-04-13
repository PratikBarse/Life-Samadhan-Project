import React from 'react';
import './StatusBadge.css';

const StatusBadge = ({ status }) => {
  const getStatusClass = (status) => {
    const statusUpper = status?.toUpperCase();
    switch (statusUpper) {
      case 'ACTIVE':
      case 'APPROVED':
      case 'COMPLETED':
      case 'VERIFIED':
        return 'status-badge-success';
      case 'PENDING':
      case 'IN_PROGRESS':
      case 'ASSIGNED':
        return 'status-badge-warning';
      case 'INACTIVE':
      case 'REJECTED':
      case 'CANCELLED':
        return 'status-badge-danger';
      default:
        return 'status-badge-default';
    }
  };

  return (
    <span className={`status-badge ${getStatusClass(status)}`}>
      {status}
    </span>
  );
};

export default StatusBadge;
