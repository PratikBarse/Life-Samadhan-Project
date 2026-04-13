import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getAllProviders, getAllProviderTypes } from '../../api/admin';
import { getServiceCategories } from '../../api/auth';
import Spinner from '../../components/common/Spinner';
import './AdminDashboard.css';

const AdminDashboard = () => {
  const [stats, setStats] = useState({
    pendingProviders: 0,
    verifiedProviders: 0,
    totalCategories: 0,
    totalProviderTypes: 0,
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadStats();
  }, []);

  const loadStats = async () => {
    try {
      setLoading(true);
      const [providers, types, categories] = await Promise.all([
        getAllProviders(null),
        getAllProviderTypes(),
        getServiceCategories()
      ]);

      setStats({
        pendingProviders: providers.filter(p => !p.verified).length,
        verifiedProviders: providers.filter(p => p.verified).length,
        totalCategories: categories.length,
        totalProviderTypes: types.length,
      });
    } catch (error) {
      console.error('Failed to load stats:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <Spinner message="Loading dashboard..." />;
  }

  return (
    <div className="dashboard-container">
      <div className="dashboard-header">
        <h1>Admin Dashboard</h1>
        <p>Manage your platform</p>
      </div>

      <div className="container">
        {}
        <div className="stats-grid">
          <div className="stat-card stat-warning">
            <div className="stat-icon">⏳</div>
            <div className="stat-content">
              <h3>{stats.pendingProviders}</h3>
              <p>Pending Providers</p>
            </div>
          </div>

          <div className="stat-card stat-success">
            <div className="stat-icon">✓</div>
            <div className="stat-content">
              <h3>{stats.verifiedProviders}</h3>
              <p>Verified Providers</p>
            </div>
          </div>

          <div className="stat-card stat-primary">
            <div className="stat-icon">📂</div>
            <div className="stat-content">
              <h3>{stats.totalCategories}</h3>
              <p>Categories</p>
            </div>
          </div>

          <div className="stat-card stat-primary">
            <div className="stat-icon">🏷️</div>
            <div className="stat-content">
              <h3>{stats.totalProviderTypes}</h3>
              <p>Provider Types</p>
            </div>
          </div>
        </div>

        {}
        <h2 className="section-title">Management</h2>
        <div className="dashboard-grid">
          <Link to="/admin/provider-types" className="dashboard-card">
            <div className="card-icon">🏷️</div>
            <h3>Provider Types</h3>
            <p>Freelancer, Professional, Daily Wage...</p>
            <span className="card-badge">{stats.totalProviderTypes} types</span>
          </Link>

          <Link to="/admin/categories" className="dashboard-card">
            <div className="card-icon">📂</div>
            <h3>Service Categories</h3>
            <p>Plumber, Carpenter, Electrician...</p>
            <span className="card-badge">{stats.totalCategories} categories</span>
          </Link>

          <Link to="/admin/providers" className="dashboard-card dashboard-card-highlight">
            <div className="card-icon">👥</div>
            <h3>Service Providers</h3>
            <p>Verify and manage providers</p>
            {stats.pendingProviders > 0 && (
              <span className="card-badge badge-warning">{stats.pendingProviders} pending</span>
            )}
          </Link>

            <Link to="/admin/locations" className="dashboard-card">
              <div className="card-icon">📍</div>
              <h3>Locations</h3>
              <p>Manage serviceable areas</p>
              <span className="card-badge">Manage Cities</span>
            </Link>
        </div>
      </div>
    </div>
  );
};

export default AdminDashboard;
