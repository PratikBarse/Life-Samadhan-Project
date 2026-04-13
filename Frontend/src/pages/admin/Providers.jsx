import React, { useState, useEffect } from 'react';
import { getAllProviders, verifyProvider, updateProviderStatus, deleteProvider } from '../../api/admin';
import Table from '../../components/common/Table';
import StatusBadge from '../../components/common/StatusBadge';
import Alert from '../../components/common/Alert';
import Spinner from '../../components/common/Spinner';
import '../admin/ProviderTypes.css';

const Providers = () => {
  const [providers, setProviders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [alert, setAlert] = useState(null);
  const [filter, setFilter] = useState('pending'); 

  useEffect(() => {
    loadProviders();
  }, [filter]);

  const loadProviders = async () => {
    try {
      setLoading(true);
      const verified = filter === 'verified' ? true : filter === 'pending' ? false : null;
      const data = await getAllProviders(verified);
      setProviders(data);
    } catch (error) {
      setAlert({ type: 'error', message: error.message || 'Failed to load providers' });
    } finally {
      setLoading(false);
    }
  };

  const handleVerify = async (provider) => {
    if (!window.confirm(`Verify provider "${provider.user?.name}"?`)) {
      return;
    }

    try {
      await verifyProvider(provider.id);
      setAlert({ type: 'success', message: 'Provider verified successfully' });
      loadProviders();
    } catch (error) {
      setAlert({ type: 'error', message: error.message || 'Failed to verify provider' });
    }
  };

  const handleToggleStatus = async (provider) => {
    const newStatus = provider.user?.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
    if (!window.confirm(`Set provider "${provider.user?.name}" to ${newStatus}?`)) {
      return;
    }

    try {
      await updateProviderStatus(provider.id, newStatus);
      setAlert({ type: 'success', message: `Provider set to ${newStatus}` });
      loadProviders();
    } catch (error) {
      setAlert({ type: 'error', message: error.message || 'Failed to update status' });
    }
  };

  const handleDelete = async (provider) => {
    if (!window.confirm(`Are you sure you want to delete provider "${provider.user?.name}"? This action cannot be undone.`)) {
      return;
    }

    try {
      await deleteProvider(provider.id);
      setAlert({ type: 'success', message: 'Provider deleted successfully' });
      loadProviders();
    } catch (error) {
      setAlert({ type: 'error', message: error.message || 'Failed to delete provider' });
    }
  };

  const columns = [
    { 
      header: 'Name', 
      width: '15%',
      render: (row) => row.user?.name || 'N/A'
    },
    { 
      header: 'Email', 
      width: '15%',
      render: (row) => row.user?.email || 'N/A'
    },
    {
      header: 'Type',
      field: 'providerType',
      width: '12%'
    },
    {
      header: 'Location',
      width: '12%',
      render: (row) => `${row.city || 'N/A'}, ${row.state || 'N/A'}`
    },
    {
      header: 'Rate',
      width: '8%',
      render: (row) => row.hourlyRate ? `₹${row.hourlyRate}` : 'N/A'
    },
    {
      header: 'Status',
      width: '10%',
      render: (row) => <StatusBadge status={row.user?.status || 'INACTIVE'} />
    },
    {
      header: 'Verified',
      width: '10%',
      render: (row) => <StatusBadge status={row.verified ? 'VERIFIED' : 'PENDING'} />
    },
    {
      header: 'Registered',
      width: '10%',
      render: (row) => row.user?.createdAt ? new Date(row.user.createdAt).toLocaleDateString() : 'N/A'
    }
  ];

  const actions = (row) => (
    <>
      {!row.verified && (
        <button className="btn btn-sm btn-primary" onClick={() => handleVerify(row)}>
          Verify
        </button>
      )}
      <button 
        className={`btn btn-sm ${row.user?.status === 'ACTIVE' ? 'btn-warning' : 'btn-success'}`}
        onClick={() => handleToggleStatus(row)}
        title={`Set to ${row.user?.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'}`}
      >
        {row.user?.status === 'ACTIVE' ? 'Deactivate' : 'Activate'}
      </button>
      <button className="btn btn-sm btn-danger" onClick={() => handleDelete(row)}>
        Delete
      </button>
    </>
  );

  if (loading) {
    return <Spinner message="Loading providers..." />;
  }

  return (
    <div className="admin-page">
      <div className="admin-header">
        <div>
          <h1>Service Providers</h1>
          <p>Verify, manage status, and control service providers</p>
        </div>
        <div className="filter-buttons">
          <button 
            className={`btn ${filter === 'pending' ? 'btn-primary' : 'btn-outline'} btn-sm`}
            onClick={() => setFilter('pending')}
          >
            Pending ({providers.filter(p => !p.verified).length})
          </button>
          <button 
            className={`btn ${filter === 'verified' ? 'btn-primary' : 'btn-outline'} btn-sm`}
            onClick={() => setFilter('verified')}
          >
            Verified ({providers.filter(p => p.verified).length})
          </button>
          <button 
            className={`btn ${filter === 'all' ? 'btn-primary' : 'btn-outline'} btn-sm`}
            onClick={() => setFilter('all')}
          >
            All
          </button>
        </div>
      </div>

      {alert && (
        <Alert
          type={alert.type}
          message={alert.message}
          onClose={() => setAlert(null)}
        />
      )}

      <div className="admin-content">
        <Table
          columns={columns}
          data={providers}
          actions={actions}
          emptyMessage={`No ${filter} providers found.`}
        />
      </div>
    </div>
  );
};

export default Providers;
