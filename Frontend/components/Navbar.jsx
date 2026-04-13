import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import '../../styles/navbar.css';

const Navbar = () => {
  const { user, role, logout, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className="navbar">
      <div className="navbar-container">
        <Link to="/" className="navbar-brand">
          <span className="brand-icon">🏠</span>
          LifeSamadhan
        </Link>

        <div className="navbar-menu">
          <Link to="/about" className="navbar-link">About Us</Link>
          <Link to="/contact" className="navbar-link">Contact</Link>
          {!isAuthenticated ? (
            <>
              <Link to="/login" className="navbar-link">Login</Link>
              <Link to="/register" className="btn btn-primary">Register</Link>
            </>
          ) : (
            <>
              <span className="navbar-user">
                <span className="user-icon">👤</span>
                {user?.name || 'User'}
              </span>
              <span className="navbar-role">{role}</span>
              <button onClick={handleLogout} className="btn btn-outline btn-sm">
                Logout
              </button>
            </>
          )}
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
