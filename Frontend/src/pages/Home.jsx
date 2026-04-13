import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Home.css';

const Home = () => {
  const { isAuthenticated, role } = useAuth();

  const getDashboardLink = () => {
    if (!isAuthenticated) return '/login';
    
    if (role === 'ADMIN') return '/admin/dashboard';
    if (role === 'CUSTOMER') return '/customer/dashboard';
    if (role === 'SERVICEPROVIDER') return '/provider/dashboard';
    
    return '/login';
  };

  return (
    <div className="home-container">
      <div className="hero-section">
        <div className="hero-content">
          <h1 className="hero-title">
            <span className="hero-icon">🏠</span>
            Welcome to LifeSamadhan
          </h1>
          <p className="hero-subtitle">
            Your trusted platform for connecting customers with verified service providers
          </p>
          <div className="hero-actions">
            {!isAuthenticated ? (
              <>
                <Link to="/register" className="btn btn-primary btn-lg">
                  Get Started
                </Link>
                <Link to="/login" className="btn btn-outline btn-lg">
                  Login
                </Link>
              </>
            ) : (
              <Link to={getDashboardLink()} className="btn btn-primary btn-lg">
                Go to Dashboard
              </Link>
            )}
          </div>
        </div>
      </div>

      <div className="features-section">
        <div className="container">
          <h2 className="section-title">Why Choose LifeSamadhan?</h2>
          
          <div className="features-grid">
            <div className="feature-card">
              <div className="feature-icon">⚡</div>
              <h3>Fast & Easy</h3>
              <p>Request services in just a few clicks and get matched with verified providers instantly</p>
            </div>

            <div className="feature-card">
              <div className="feature-icon">✓</div>
              <h3>Verified Providers</h3>
              <p>All service providers are thoroughly verified and approved by our admin team</p>
            </div>

            <div className="feature-card">
              <div className="feature-icon">🔒</div>
              <h3>Secure & Safe</h3>
              <p>OTP verification ensures the right provider reaches you, with secure payment options</p>
            </div>

            <div className="feature-card">
              <div className="feature-icon">⭐</div>
              <h3>Quality Assured</h3>
              <p>Rate and review service providers to maintain quality standards</p>
            </div>

            <div className="feature-card">
              <div className="feature-icon">💰</div>
              <h3>Transparent Pricing</h3>
              <p>Clear hourly rates and no hidden charges - pay only for services rendered</p>
            </div>

            <div className="feature-card">
              <div className="feature-icon">🤝</div>
              <h3>Reliable Support</h3>
              <p>24/7 support to help you with any issues or questions</p>
            </div>
          </div>
        </div>
      </div>

      <div className="cta-section">
        <div className="cta-content">
          <h2>Ready to get started?</h2>
          <p>Join thousands of satisfied customers and service providers</p>
          {!isAuthenticated && (
            <Link to="/register" className="btn btn-primary btn-lg">
              Create Your Account
            </Link>
          )}
        </div>
      </div>
    </div>
  );
};

export default Home;
