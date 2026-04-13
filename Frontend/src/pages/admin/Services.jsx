import React, { useState, useEffect } from 'react';
import { getAllServices, getServiceCategories } from '../../api/auth';
import { createService, updateService, deleteService } from '../../api/admin';
import Table from '../../components/common/Table';
import StatusBadge from '../../components/common/StatusBadge';
import Modal from '../../components/common/Modal';
import Alert from '../../components/common/Alert';
import Spinner from '../../components/common/Spinner';
import '../admin/ProviderTypes.css';

const Services = () => {
  const [services, setServices] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [alert, setAlert] = useState(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingService, setEditingService] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    categoryId: '',
    basePrice: '',
    status: 'ACTIVE'
  });
  const [errors, setErrors] = useState({});

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const [servicesData, categoriesData] = await Promise.all([
        getAllServices(),
        getServiceCategories()
      ]);
      setServices(servicesData);
      setCategories(categoriesData.filter(c => c.status === 'ACTIVE'));
    } catch (error) {
      setAlert({ type: 'error', message: error.message || 'Failed to load data' });
    } finally {
      setLoading(false);
    }
  };

  const handleAdd = () => {
    setEditingService(null);
    setFormData({ name: '', description: '', categoryId: '', basePrice: '', status: 'ACTIVE' });
    setErrors({});
    setIsModalOpen(true);
  };

  const handleEdit = (service) => {
    setEditingService(service);
    setFormData({
      name: service.name,
      description: service.description || '',
      categoryId: service.categoryId,
      basePrice: service.basePrice || '',
      status: service.status
    });
    setErrors({});
    setIsModalOpen(true);
  };

  const handleDelete = async (service) => {
    if (!window.confirm(`Are you sure you want to delete "${service.name}"?`)) {
      return;
    }

    try {
      await deleteService(service.id);
      setAlert({ type: 'success', message: 'Service deleted successfully' });
      loadData();
    } catch (error) {
      setAlert({ type: 'error', message: error.message || 'Failed to delete service' });
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
    if (!formData.categoryId) {
      newErrors.categoryId = 'Category is required';
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
      const payload = {
        ...formData,
        categoryId: parseInt(formData.categoryId),
        basePrice: formData.basePrice ? parseFloat(formData.basePrice) : null
      };

      if (editingService) {
        await updateService(editingService.id, payload);
        setAlert({ type: 'success', message: 'Service updated successfully' });
      } else {
        await createService(payload);
        setAlert({ type: 'success', message: 'Service created successfully' });
      }
      setIsModalOpen(false);
      loadData();
    } catch (error) {
      setAlert({ type: 'error', message: error.message || 'Operation failed' });
    }
  };

  const getCategoryName = (categoryId) => {
    const category = categories.find(c => c.id === categoryId);
    return category ? category.name : 'Unknown';
  };

  const columns = [
    { header: 'Name', field: 'name', width: '25%' },
    { 
      header: 'Category', 
      width: '20%',
      render: (row) => getCategoryName(row.categoryId)
    },
    { header: 'Description', field: 'description', width: '30%' },
    {
      header: 'Base Price',
      width: '10%',
      render: (row) => row.basePrice ? `₹${row.basePrice}` : '-'
    },
    {
      header: 'Status',
      width: '15%',
      render: (row) => <StatusBadge status={row.status} />
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
    return <Spinner message="Loading services..." />;
  }

  return (
    <div className="admin-page">
      <div className="admin-header">
        <div>
          <h1>Services</h1>
          <p>Manage available services under categories</p>
        </div>
        <button className="btn btn-primary" onClick={handleAdd}>
          + Add Service
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
          data={services}
          actions={actions}
          emptyMessage="No services found. Add one to get started!"
        />
      </div>

      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={editingService ? 'Edit Service' : 'Add Service'}
        size="md"
      >
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label" htmlFor="name">Service Name *</label>
            <input
              type="text"
              id="name"
              name="name"
              className={`form-input ${errors.name ? 'error' : ''}`}
              value={formData.name}
              onChange={handleChange}
              placeholder="e.g., Pipe Repair, AC Installation"
            />
            {errors.name && <span className="form-error">{errors.name}</span>}
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="categoryId">Category *</label>
            <select
              id="categoryId"
              name="categoryId"
              className={`form-select ${errors.categoryId ? 'error' : ''}`}
              value={formData.categoryId}
              onChange={handleChange}
            >
              <option value="">Select Category</option>
              {categories.map(cat => (
                <option key={cat.id} value={cat.id}>{cat.name}</option>
              ))}
            </select>
            {errors.categoryId && <span className="form-error">{errors.categoryId}</span>}
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="basePrice">Base Price (₹)</label>
            <input
              type="number"
              id="basePrice"
              name="basePrice"
              className="form-input"
              value={formData.basePrice}
              onChange={handleChange}
              placeholder="Optional base price"
              min="0"
            />
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="description">Description</label>
            <textarea
              id="description"
              name="description"
              className="form-textarea"
              value={formData.description}
              onChange={handleChange}
              placeholder="Brief description of the service"
              rows="3"
            />
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
              {editingService ? 'Update' : 'Create'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default Services;
