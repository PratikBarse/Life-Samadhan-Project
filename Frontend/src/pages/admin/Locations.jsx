import React, { useState, useEffect } from 'react';
import { getAllLocations, createLocation, updateLocation, deleteLocation } from '../../api/admin';
import Alert from '../../components/common/Alert';
import Modal from '../../components/common/Modal';

const Locations = () => {
  const [locations, setLocations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [alert, setAlert] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [editingLocation, setEditingLocation] = useState(null);
  
  const [formData, setFormData] = useState({
    country: 'India',
    state: '',
    district: '',
    pincode: '',
    status: 'ACTIVE'
  });

  useEffect(() => {
    loadLocations();
  }, []);

  const loadLocations = async () => {
    try {
      setLoading(true);
      const data = await getAllLocations();
      setLocations(data);
    } catch (error) {
      setAlert({ type: 'error', message: 'Failed to load locations' });
    } finally {
        setLoading(false);
    }
  };

  const handleCreate = () => {
    setEditingLocation(null);
    setFormData({ country: 'India', state: '', district: '', pincode: '', status: 'ACTIVE' });
    setShowModal(true);
  };

  const handleEdit = (loc) => {
    setEditingLocation(loc);
    setFormData({
      country: loc.country,
      state: loc.state,
      district: loc.district,
      pincode: loc.pincode,
      status: loc.status
    });
    setShowModal(true);
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this location?')) return;
    
    try {
      await deleteLocation(id);
      setAlert({ type: 'success', message: 'Location deleted successfully' });
      loadLocations();
    } catch (error) {
       setAlert({ type: 'error', message: 'Failed to delete location' });
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.state || !formData.district || !formData.pincode) {
        setAlert({ type: 'error', message: 'Please fill all required fields' });
        return;
    }

    try {
      if (editingLocation) {
        await updateLocation(editingLocation.id, formData);
        setAlert({ type: 'success', message: 'Location updated successfully' });
      } else {
        await createLocation(formData);
        setAlert({ type: 'success', message: 'Location created successfully' });
      }
      setShowModal(false);
      loadLocations();
    } catch (error) {
      setAlert({ type: 'error', message: 'Operation failed' });
    }
  };

  return (
    <div className="admin-content">
      <div className="page-header">
        <h1>Location Management</h1>
        <button className="btn btn-primary" onClick={handleCreate}>+ Add Location</button>
      </div>

      {alert && <Alert type={alert.type} message={alert.message} onClose={() => setAlert(null)} />}

      <div className="card">
         <div className="table-responsive">
            <table className="table">
                <thead>
                    <tr>
                        <th>State</th>
                        <th>District / City</th>
                        <th>Pincode</th>
                        <th>Status</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    {loading ? (
                        <tr><td colSpan="5">Loading...</td></tr>
                    ) : locations.length === 0 ? (
                        <tr><td colSpan="5">No locations found. Add one to get started.</td></tr>
                    ) : (
                        locations.map(loc => (
                            <tr key={loc.id}>
                                <td>{loc.state}</td>
                                <td>{loc.district}</td>
                                <td>{loc.pincode}</td>
                                <td>
                                    <span className={`status-badge ${loc.status.toLowerCase()}`}>
                                        {loc.status}
                                    </span>
                                </td>
                                <td>
                                    <button className="btn btn-sm btn-outline mr-2" onClick={() => handleEdit(loc)}>Edit</button>
                                    <button className="btn btn-sm btn-danger" onClick={() => handleDelete(loc.id)}>Delete</button>
                                </td>
                            </tr>
                        ))
                    )}
                </tbody>
            </table>
         </div>
      </div>

      <Modal
        isOpen={showModal}
        onClose={() => setShowModal(false)}
        title={editingLocation ? 'Edit Location' : 'Add New Location'}
      >
        <form onSubmit={handleSubmit}>
            <div className="form-group">
                <label>Country</label>
                <input type="text" className="form-input" value={formData.country} readOnly />
            </div>
            <div className="form-group">
                <label>State *</label>
                <input 
                    type="text" className="form-input" 
                    value={formData.state}
                    onChange={(e) => setFormData({...formData, state: e.target.value})}
                    placeholder="e.g. Maharashtra"
                />
            </div>
            <div className="form-group">
                <label>District / City *</label>
                <input 
                    type="text" className="form-input" 
                    value={formData.district}
                    onChange={(e) => setFormData({...formData, district: e.target.value})}
                    placeholder="e.g. Mumbai"
                />
            </div>
            <div className="form-group">
                <label>Pincode *</label>
                <input 
                    type="text" className="form-input" 
                    value={formData.pincode}
                    onChange={(e) => setFormData({...formData, pincode: e.target.value})}
                    placeholder="e.g. 400001"
                />
            </div>
            <div className="form-group">
                <label>Status</label>
                <select 
                    className="form-select"
                    value={formData.status}
                    onChange={(e) => setFormData({...formData, status: e.target.value})}
                >
                    <option value="ACTIVE">ACTIVE</option>
                    <option value="INACTIVE">INACTIVE</option>
                </select>
            </div>
            <div className="modal-actions">
                <button type="submit" className="btn btn-primary w-full">Save Location</button>
            </div>
        </form>
      </Modal>
    </div>
  );
};

export default Locations;
