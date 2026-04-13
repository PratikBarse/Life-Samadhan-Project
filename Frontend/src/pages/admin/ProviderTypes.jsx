import React, { useState, useEffect } from 'react';
import { getAllProviderTypes, createProviderType, updateProviderType, deleteProviderType } from '../../api/admin';
import Table from '../../components/common/Table';
import StatusBadge from '../../components/common/StatusBadge';
import Modal from '../../components/common/Modal';
import Alert from '../../components/common/Alert';
import Spinner from '../../components/common/Spinner';
import './ProviderTypes.css';

const ProviderTypes = () => {
  const [providerTypes, setProviderTypes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [alert, setAlert] = useState(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingType, setEditingType] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    status: 'ACTIVE'
  });
  const [errors, setErrors] = useState({});

  useEffect(() => {
    loadProviderTypes();
  }, []);

  const loadProviderTypes = async () => {
    try {
      setLoading(true);
      const data = await getAllProviderTypes();
      setProviderTypes(data);
    } catch (error) {
      setAlert({ type: 'error', message: error.message || 'Failed to load provider types' });
    } finally {
      setLoading(false);
    }
  };

  const handleAdd = () => {
    setEditingType(null);
    setFormData({ name: '', status: 'ACTIVE' });
    setErrors({});
    setIsModalOpen(true);
  };

  const handleEdit = (type) => {
    setEditingType(type);
    setFormData({
      name: type.name,
      status: type.status
    });
    setErrors({});
    setIsModalOpen(true);
  };

  const handleDelete = async (type) => {
    if (!window.confirm(`Are you sure you want to delete "${type.name}"?`)) {
      return;
    }

    try {
      
      await deleteProviderType(type.id);
      setAlert({ type: 'success', message: 'Provider type deleted successfully' });
      loadProviderTypes();
    } catch (error) {
      
      if (error.message && error.message.includes('service provider(s) are using this type')) {
        const forceConfirm = window.confirm(
          `${error.message}\n\nDo you want to FORCE DELETE anyway?\n\nWarning: This will set "${type.name}" to NULL for all affected providers.`
        );
        
        if (forceConfirm) {
          try {
            
            await deleteProviderType(type.id, true);
            setAlert({ type: 'success', message: 'Provider type force deleted successfully. Affected providers updated.' });
            loadProviderTypes();
          } catch (forceError) {
            setAlert({ type: 'error', message: forceError.message || 'Failed to force delete provider type' });
          }
        }
      } else {
        setAlert({ type: 'error', message: error.message || 'Failed to delete provider type' });
      }
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }));
    }
  };

  const validate = () => {
    const newErrors = {};
    if (!formData.name || formData.name.length < 3) {
      newErrors.name = 'Name must be at least 3 characters';
    }
    return newErrors;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    const newErrors = validate();
    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    try {
      if (editingType) {
        await updateProviderType(editingType.id, formData);
        setAlert({ type: 'success', message: 'Provider type updated successfully' });
      } else {
        await createProviderType(formData);
        setAlert({ type: 'success', message: 'Provider type created successfully' });
      }
      setIsModalOpen(false);
      loadProviderTypes();
    } catch (error) {
      setAlert({ type: 'error', message: error.message || 'Operation failed' });
    }
  };

  const columns = [
    { header: 'Name', field: 'name', width: '70%' },
    {
      header: 'Status',
      width: '15%',
      render: (row) => <StatusBadge status={row.status} />
    },
    {
      header: 'Created',
      width: '15%',
      render: (row) => new Date(row.createdAt).toLocaleDateString()
    }
  ];

  const actions = (row) => (
    <>
      <button className="btn btn-sm btn-outline" onClick={() => handleEdit(row)}>
        Edit
      </button>
      <button className="btn btn-sm btn-danger" onClick={() => handleDelete(row)}>
        Delete
      </button>
    </>
  );

  if (loading) {
    return <Spinner message="Loading provider types..." />;
  }

  return (
    <div className="admin-page">
      <div className="admin-header">
        <div>
          <h1>Provider Types</h1>
          <p>Manage service provider categories</p>
        </div>
        <button className="btn btn-primary" onClick={handleAdd}>
          + Add Provider Type
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
          data={providerTypes}
          actions={actions}
          emptyMessage="No provider types found. Add one to get started!"
        />
      </div>

      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={editingType ? 'Edit Provider Type' : 'Add Provider Type'}
        size="md"
      >
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label" htmlFor="name">Name *</label>
            <input
              type="text"
              id="name"
              name="name"
              className={`form-input ${errors.name ? 'error' : ''}`}
              value={formData.name}
              onChange={handleChange}
              placeholder="e.g., Professional, Freelancer, Daily Wage Workers"
            />
            {errors.name && <span className="form-error">{errors.name}</span>}
          </div>



          <div className="form-group">
            <label className="form-label" htmlFor="status">Status</label>
            <select
              id="status"
              name="status"
              className="form-select"
              value={formData.status}
              onChange={handleChange}
            >
              <option value="ACTIVE">Active</option>
              <option value="INACTIVE">Inactive</option>
            </select>
          </div>

          <div className="modal-actions">
            <button type="button" className="btn btn-outline" onClick={() => setIsModalOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary">
              {editingType ? 'Update' : 'Create'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default ProviderTypes;
