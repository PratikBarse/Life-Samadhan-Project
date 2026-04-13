import React, { useState, useEffect } from 'react';
import { getServiceCategories } from '../../api/auth';
import { createCategory, updateCategory, deleteCategory } from '../../api/admin';
import Table from '../../components/common/Table';
import StatusBadge from '../../components/common/StatusBadge';
import Modal from '../../components/common/Modal';
import Alert from '../../components/common/Alert';
import Spinner from '../../components/common/Spinner';
import '../admin/ProviderTypes.css';

const Categories = () => {
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [alert, setAlert] = useState(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingCategory, setEditingCategory] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    status: 'ACTIVE'
  });
  const [errors, setErrors] = useState({});

  useEffect(() => {
    loadCategories();
  }, []);

  const loadCategories = async () => {
    try {
      setLoading(true);
      const data = await getServiceCategories();
      setCategories(data);
    } catch (error) {
      setAlert({ type: 'error', message: error.message || 'Failed to load categories' });
    } finally {
      setLoading(false);
    }
  };

  const handleAdd = () => {
    setEditingCategory(null);
    setFormData({ name: '', status: 'ACTIVE' });
    setErrors({});
    setIsModalOpen(true);
  };

  const handleEdit = (category) => {
    setEditingCategory(category);
    setFormData({
      name: category.name,
      status: category.status
    });
    setErrors({});
    setIsModalOpen(true);
  };

  const handleDelete = async (category) => {
    if (!window.confirm(`Are you sure you want to delete "${category.name}"?`)) {
      return;
    }

    try {
      await deleteCategory(category.id);
      setAlert({ type: 'success', message: 'Category deleted successfully' });
      loadCategories();
    } catch (error) {
      setAlert({ type: 'error', message: error.message || 'Failed to delete category' });
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

  const [submitting, setSubmitting] = useState(false);

  

  const handleSubmit = async (e) => {
    e.preventDefault();

    const newErrors = validate();
    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    setSubmitting(true);
    try {
      if (editingCategory) {
        await updateCategory(editingCategory.id, formData);
        setAlert({ type: 'success', message: 'Category updated successfully' });
      } else {
        await createCategory(formData);
        setAlert({ type: 'success', message: 'Category created successfully' });
      }
      setIsModalOpen(false);
      loadCategories();
    } catch (error) {
      setAlert({ type: 'error', message: error.message || 'Operation failed' });
    } finally {
      setSubmitting(false);
    }
  };

  const columns = [
    { header: 'Name', field: 'name', width: '80%' },
    {
      header: 'Status',
      width: '20%',
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
    return <Spinner message="Loading categories..." />;
  }

  return (
    <div className="admin-page">
      <div className="admin-header">
        <div>
          <h1>Service Categories</h1>
          <p>Manage service categories for organization</p>
        </div>
        <button className="btn btn-primary" onClick={handleAdd}>
          + Add Category
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
          data={categories}
          actions={actions}
          emptyMessage="No categories found. Add one to get started!"
        />
      </div>

      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={editingCategory ? 'Edit Category' : 'Add Category'}
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
              placeholder="e.g., Plumber, Electrician"
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
            <button type="button" className="btn btn-outline" onClick={() => setIsModalOpen(false)} disabled={submitting}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={submitting}>
              {submitting ? 'Saving...' : (editingCategory ? 'Update' : 'Create')}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default Categories;
