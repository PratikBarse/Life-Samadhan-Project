import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { register, getActiveProviderTypes, getServiceCategories, getServicesByCategory, getLocations } from '../api/auth';
import Alert from '../components/common/Alert';
import '../styles/auth.css';

const Register = () => {
  const navigate = useNavigate();
  
  const [role, setRole] = useState('');
  const [providerTypes, setProviderTypes] = useState([]);
  const [categories, setCategories] = useState([]);
  const [locations, setLocations] = useState([]);

  const [selectedCategory, setSelectedCategory] = useState('');
  
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    mobile: '',
    password: '',
    confirmPassword: '',
    
    address: '',
    
    providerType: '',
    hourlyRate: '',
    city: '', 
    state: '', 
    locationId: '' 
  });
  
  const [errors, setErrors] = useState({});
  const [alert, setAlert] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    
    const loadData = async () => {
      try {
        const [types, categoriesList, locs] = await Promise.all([
          getActiveProviderTypes(),
          getServiceCategories(),
          getLocations()
        ]);
        setProviderTypes(types);
        setCategories(categoriesList.filter(c => c.status === 'ACTIVE'));
        setLocations(locs);
      } catch (error) {
        console.error('Failed to load form data:', error);
      }
    };
    
    if (role === 'SERVICEPROVIDER') {
      loadData();
    }
  }, [role]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }));
    }
  };

  const handleCategoryChange = (e) => {
    const categoryId = e.target.value;
    setSelectedCategory(categoryId);
  };

  const validate = () => {
    const newErrors = {};
    
    if (!role) {
      newErrors.role = 'Please select a role';
    }
    
    if (!formData.name || formData.name.length < 3) {
      newErrors.name = 'Name must be at least 3 characters';
    }
    
    if (!formData.email || !/\S+@\S+\.\S+/.test(formData.email)) {
      newErrors.email = 'Valid email is required';
    }
    
    if (!formData.mobile || !/^[6-9]\d{9}$/.test(formData.mobile)) {
      newErrors.mobile = 'Valid 10-digit Indian mobile number required';
    }
    
    const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;
    if (!formData.password || !passwordRegex.test(formData.password)) {
      newErrors.password = 'Password must be 8+ chars with uppercase, lowercase, number & special char';
    }
    
    if (formData.password !== formData.confirmPassword) {
      newErrors.confirmPassword = 'Passwords do not match';
    }

    if (role === 'CUSTOMER') {
      if (!formData.address) {
        newErrors.address = 'Address is required for service delivery';
      }
    }
    
    
    if (role === 'SERVICEPROVIDER') {
      if (!formData.providerType) {
        newErrors.providerType = 'Provider type is required';
      }
      
      if (!formData.hourlyRate || formData.hourlyRate <= 0) {
        newErrors.hourlyRate = 'Valid hourly rate is required';
      }
      
      
      if (!formData.locationId) {
        newErrors.city = 'Location is required'; 
      }
      
      if (!selectedCategory) {
        newErrors.role = 'Service Category is required';
      }
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

    setLoading(true);
    setAlert(null);

    try {
      const registrationData = {
        name: formData.name,
        email: formData.email,
        mobile: formData.mobile,
        password: formData.password,
        role: role,
      };
      
      if (role === 'CUSTOMER') {
          registrationData.address = formData.address;
          registrationData.locationId = parseInt(formData.locationId);
      }

      
      if (role === 'SERVICEPROVIDER') {
        registrationData.providerType = formData.providerType;
        registrationData.hourlyRate = parseFloat(formData.hourlyRate);
        
        
        registrationData.locationId = parseInt(formData.locationId);
        registrationData.serviceCategoryId = selectedCategory; 
      }
      
      await register(registrationData);
      
      setAlert({
        type: 'success',
        message: 'Registration successful! Redirecting to login...',
      });
      
      setTimeout(() => {
        navigate('/login');
      }, 1000);
    } catch (error) {
      const msg = error.response?.data?.message || (typeof error.response?.data === 'string' ? error.response.data : error.message) || 'Registration failed.';
      setAlert({
        type: 'error',
        message: msg,
      });
      setLoading(false); 
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card auth-card-wide">
        <div className="auth-header">
          <h1>Create Account</h1>
          <p>Register for LifeSamadhan</p>
        </div>

        {alert && (
          <Alert
            type={alert.type}
            message={alert.message}
            onClose={() => setAlert(null)}
          />
        )}

        <form onSubmit={handleSubmit} className="auth-form">
          {}
          <div className="form-group">
            <label className="form-label">Select Role *</label>
            <div className="role-selector">
              <label className={`role-option ${role === 'CUSTOMER' ? 'selected' : ''}`}>
                <input
                  type="radio"
                  name="role"
                  value="CUSTOMER"
                  checked={role === 'CUSTOMER'}
                  onChange={(e) => setRole(e.target.value)}
                />
                <span className="role-icon">👤</span>
                <span className="role-name">Customer</span>
              </label>
              
              <label className={`role-option ${role === 'SERVICEPROVIDER' ? 'selected' : ''}`}>
                <input
                  type="radio"
                  name="role"
                  value="SERVICEPROVIDER"
                  checked={role === 'SERVICEPROVIDER'}
                  onChange={(e) => setRole(e.target.value)}
                />
                <span className="role-icon">🔧</span>
                <span className="role-name">Service Provider</span>
              </label>
            </div>
            {errors.role && <span className="form-error">{errors.role}</span>}
          </div>

          {role && (
            <>
              {}
              <div className="form-row">
                <div className="form-group">
                  <label className="form-label" htmlFor="name">Full Name *</label>
                  <input
                    type="text"
                    id="name"
                    name="name"
                    className={`form-input ${errors.name ? 'error' : ''}`}
                    value={formData.name}
                    onChange={handleChange}
                    placeholder="Enter full name"
                  />
                  {errors.name && <span className="form-error">{errors.name}</span>}
                </div>

                <div className="form-group">
                  <label className="form-label" htmlFor="mobile">Mobile Number *</label>
                  <input
                    type="tel"
                    id="mobile"
                    name="mobile"
                    className={`form-input ${errors.mobile ? 'error' : ''}`}
                    value={formData.mobile}
                    onChange={handleChange}
                    placeholder="Enter mobile number"
                    maxLength="10"
                  />
                  {errors.mobile && <span className="form-error">{errors.mobile}</span>}
                </div>
              </div>

              <div className="form-group">
                <label className="form-label" htmlFor="email">Email *</label>
                <input
                  type="email"
                  id="email"
                  name="email"
                  autoComplete="email"
                  className={`form-input ${errors.email ? 'error' : ''}`}
                  value={formData.email}
                  onChange={handleChange}
                  placeholder="Enter email address"
                />
                {errors.email && <span className="form-error">{errors.email}</span>}
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label className="form-label" htmlFor="password">Password *</label>
                  <input
                    type="password"
                    id="password"
                    name="password"
                    autoComplete="new-password"
                    className={`form-input ${errors.password ? 'error' : ''}`}
                    value={formData.password}
                    onChange={handleChange}
                    placeholder="Enter password"
                  />
                  {errors.password && <span className="form-error">{errors.password}</span>}
                </div>

                <div className="form-group">
                  <label className="form-label" htmlFor="confirmPassword">Confirm Password *</label>
                  <input
                    type="password"
                    id="confirmPassword"
                    name="confirmPassword"
                    autoComplete="new-password"
                    className={`form-input ${errors.confirmPassword ? 'error' : ''}`}
                    value={formData.confirmPassword}
                    onChange={handleChange}
                    placeholder="Enter password"
                  />
                  {errors.confirmPassword && <span className="form-error">{errors.confirmPassword}</span>}
                </div>
              </div>

              {}
              {role === 'CUSTOMER' && (
                  <>
                  <div className="form-group">
                    <label className="form-label" htmlFor="address">Address *</label>
                    <textarea
                      id="address"
                      name="address"
                      className={`form-input ${errors.address ? 'error' : ''}`}
                      value={formData.address}
                      onChange={handleChange}
                      placeholder="Enter complete address"
                      rows="3"
                    />
                    {errors.address && <span className="form-error">{errors.address}</span>}
                  </div>
                  
                  <div className="form-group">
                    <label className="form-label" htmlFor="location">Service Location *</label>
                    <select
                      id="location"
                      name="locationId"
                      className={`form-select ${errors.city ? 'error' : ''}`}
                      value={formData.locationId || ''}
                      onChange={(e) => {
                          const val = e.target.value;
                          setFormData(prev => ({ ...prev, locationId: val }));
                      }}
                    >
                      <option value="">Select Service Area</option>
                      {locations.map(loc => (
                        <option key={loc.id} value={loc.id}>{loc.district}, {loc.state} ({loc.pincode})</option>
                      ))}
                    </select>
                    <p className="form-help">Select your local service area.</p>
                  </div>
                  </>
              )}

              {}
              {role === 'SERVICEPROVIDER' && (
                <>
                  <div className="form-divider">
                    <span>Service Provider Information</span>
                  </div>

                  <div className="form-row">
                    <div className="form-group">
                      <label className="form-label" htmlFor="providerType">Type of Service Provider *</label>
                      <select
                        id="providerType"
                        name="providerType"
                        className={`form-select ${errors.providerType ? 'error' : ''}`}
                        value={formData.providerType}
                        onChange={handleChange}
                      >
                        <option value="">Select type</option>
                        {providerTypes.map(type => (
                          <option key={type.id} value={type.name}>{type.name}</option>
                        ))}
                      </select>
                      {errors.providerType && <span className="form-error">{errors.providerType}</span>}
                    </div>

                    <div className="form-group">
                      <label className="form-label" htmlFor="hourlyRate">Hourly Rate (₹) *</label>
                      <input
                        type="number"
                        id="hourlyRate"
                        name="hourlyRate"
                        className={`form-input ${errors.hourlyRate ? 'error' : ''}`}
                        value={formData.hourlyRate}
                        onChange={handleChange}
                        placeholder="Enter hourly rate"
                        min="0"
                      />
                      {errors.hourlyRate && <span className="form-error">{errors.hourlyRate}</span>}
                    </div>
                  </div>

                  <div className="form-group">
                    <label className="form-label" htmlFor="location">Service Location *</label>
                    <select
                      id="location"
                      name="locationId"
                      className={`form-select ${errors.city ? 'error' : ''}`}
                      value={formData.locationId || ''}
                      onChange={(e) => {
                          const val = e.target.value;
                          setFormData(prev => ({ ...prev, locationId: val }));
                          
                          
                      }}
                    >
                      <option value="">Select Service Area</option>
                      {locations.map(loc => (
                        <option key={loc.id} value={loc.id}>{loc.district}, {loc.state} ({loc.pincode})</option>
                      ))}
                    </select>
                    {errors.city && <span className="form-error">Location is required</span>}
                    <p className="form-help">Select the primary area where you offer services.</p>
                  </div>

                  {}
                  <div className="form-group">
                    <label className="form-label" htmlFor="category">Service Category *</label>
                    <select
                      id="category"
                      name="category"
                      className="form-select"
                      value={selectedCategory}
                      onChange={handleCategoryChange}
                    >
                      <option value="">Select Category</option>
                      {categories.map(category => (
                        <option key={category.id} value={category.id}>{category.name}</option>
                      ))}
                    </select>
                    <p className="form-help">Select your primary area of work (e.g. Plumber)</p>
                  </div>
                </>
              )}

              <button 
                type="submit" 
                className="btn btn-primary w-full btn-lg"
                disabled={loading}
              >
                {loading ? 'Registering...' : 'Register'}
              </button>
            </>
          )}
        </form>

        <div className="auth-footer">
          <p>Already have an account? <Link to="/login">Login here</Link></p>
        </div>
      </div>
    </div>
  );
};

export default Register;
