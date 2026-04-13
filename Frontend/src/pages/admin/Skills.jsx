import React, { useState, useEffect } from 'react';
import { getPendingSkills, approveSkill, rejectSkill } from '../../api/admin';
import Table from '../../components/common/Table';
import StatusBadge from '../../components/common/StatusBadge';
import Alert from '../../components/common/Alert';
import Spinner from '../../components/common/Spinner';
import '../admin/ProviderTypes.css';

const Skills = () => {
  const [skills, setSkills] = useState([]);
  const [loading, setLoading] = useState(true);
  const [alert, setAlert] = useState(null);

  useEffect(() => {
    loadPendingSkills();
  }, []);

  const loadPendingSkills = async () => {
    try {
      setLoading(true);
      const data = await getPendingSkills();
      setSkills(data);
    } catch (error) {
      setAlert({ type: 'error', message: error.message || 'Failed to load pending skills' });
    } finally {
      setLoading(false);
    }
  };

  const handleApprove = async (skill) => {
    try {
      await approveSkill(skill.id);
      setAlert({ type: 'success', message: 'Skill approved successfully' });
      loadPendingSkills();
    } catch (error) {
      setAlert({ type: 'error', message: error.message || 'Failed to approve skill' });
    }
  };

  const handleReject = async (skill) => {
    if (!window.confirm('Are you sure you want to reject this skill?')) {
      return;
    }

    try {
      await rejectSkill(skill.id);
      setAlert({ type: 'success', message: 'Skill rejected successfully' });
      loadPendingSkills();
    } catch (error) {
      setAlert({ type: 'error', message: error.message || 'Failed to reject skill' });
    }
  };

  const columns = [
    { 
      header: 'Provider Name', 
      width: '25%',
      render: (row) => row.provider?.user?.name || 'N/A'
    },
    {
      header: 'Provider Type',
      width: '15%',
      render: (row) => row.provider?.providerType || 'N/A'
    },
    { 
      header: 'Service', 
      width: '25%',
      render: (row) => row.service?.name || 'N/A'
    },
    {
      header: 'Category',
      width: '15%',
      render: (row) => row.service?.category?.name || 'N/A'
    },
    {
      header: 'Status',
      width: '10%',
      render: (row) => <StatusBadge status={row.status} />
    },
    {
      header: 'Requested',
      width: '10%',
      render: (row) => new Date(row.createdAt).toLocaleDateString()
    }
  ];

  const actions = (row) => (
    <>
      <button className="btn btn-sm btn-primary" onClick={() => handleApprove(row)}>
        Approve
      </button>
      <button className="btn btn-sm btn-danger" onClick={() => handleReject(row)}>
        Reject
      </button>
    </>
  );

  if (loading) {
    return <Spinner message="Loading pending skills..." />;
  }

  return (
    <div className="admin-page">
      <div className="admin-header">
        <div>
          <h1>Skill Approvals</h1>
          <p>Review and approve provider skill requests</p>
        </div>
        <button className="btn btn-secondary" onClick={loadPendingSkills}>
          ↻ Refresh
        </button>
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
          data={skills}
          actions={actions}
          emptyMessage="No pending skill approvals. All skills have been processed!"
        />
      </div>
    </div>
  );
};

export default Skills;
