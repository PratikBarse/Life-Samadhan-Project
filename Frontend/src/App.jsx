import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import Navbar from './components/common/Navbar';
import ProtectedRoute from './components/auth/ProtectedRoute';
import RoleRoute from './components/auth/RoleRoute';
import NotificationManager from './components/common/NotificationManager';


import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';
import AboutUs from './pages/AboutUs';
import ContactUs from './pages/ContactUs';


import AdminDashboard from './pages/admin/AdminDashboard';
import ProviderTypes from './pages/admin/ProviderTypes';
import Categories from './pages/admin/Categories';
import Services from './pages/admin/Services';
import Providers from './pages/admin/Providers';
import Skills from './pages/admin/Skills';
import Locations from './pages/admin/Locations';


import CustomerDashboard from './pages/customer/CustomerDashboard';


import ProviderDashboard from './pages/provider/ProviderDashboard';


import './styles/global.css';

function App() {
  return (
    <AuthProvider>
      <NotificationManager />
      <BrowserRouter>
        <div className="app">
          <Navbar />
          <Routes>
            {}
            <Route path="/" element={<Home />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/about" element={<AboutUs />} />
            <Route path="/contact" element={<ContactUs />} />

            {}
            <Route
              path="/admin/dashboard"
              element={
                <ProtectedRoute>
                  <RoleRoute allowedRoles={['ADMIN']}>
                    <AdminDashboard />
                  </RoleRoute>
                </ProtectedRoute>
              }
            />
            <Route
              path="/admin/provider-types"
              element={
                <ProtectedRoute>
                  <RoleRoute allowedRoles={['ADMIN']}>
                    <ProviderTypes />
                  </RoleRoute>
                </ProtectedRoute>
              }
            />
            <Route
              path="/admin/categories"
              element={
                <ProtectedRoute>
                  <RoleRoute allowedRoles={['ADMIN']}>
                    <Categories />
                  </RoleRoute>
                </ProtectedRoute>
              }
            />
            <Route
              path="/admin/services"
              element={
                <ProtectedRoute>
                  <RoleRoute allowedRoles={['ADMIN']}>
                    <Services />
                  </RoleRoute>
                </ProtectedRoute>
              }
            />
            <Route
              path="/admin/providers"
              element={
                <ProtectedRoute>
                  <RoleRoute allowedRoles={['ADMIN']}>
                    <Providers />
                  </RoleRoute>
                </ProtectedRoute>
              }
            />
            <Route
              path="/admin/skills"
              element={
                <ProtectedRoute>
                  <RoleRoute allowedRoles={['ADMIN']}>
                    <Skills />
                  </RoleRoute>
                </ProtectedRoute>
              }
            />

            <Route
              path="/admin/locations"
              element={
                <ProtectedRoute>
                  <RoleRoute allowedRoles={['ADMIN']}>
                    <Locations />
                  </RoleRoute>
                </ProtectedRoute>
              }
            />

            {}
            <Route
              path="/customer/dashboard"
              element={
                <ProtectedRoute>
                  <RoleRoute allowedRoles={['CUSTOMER']}>
                    <CustomerDashboard />
                  </RoleRoute>
                </ProtectedRoute>
              }
            />

            {}
            <Route
              path="/provider/dashboard"
              element={
                <ProtectedRoute>
                  <RoleRoute allowedRoles={['SERVICEPROVIDER']}>
                    <ProviderDashboard />
                  </RoleRoute>
                </ProtectedRoute>
              }
            />

            {}
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </div>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
