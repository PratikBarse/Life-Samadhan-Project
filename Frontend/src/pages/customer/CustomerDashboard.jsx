import React, { useState, useEffect } from 'react';
import { getServiceCategories } from '../../api/auth';
import { getLocations, getActiveLocations, searchProviders, createServiceRequest, getMyAssignments, cancelServiceRequest, submitRating, getMyProfile, updateMyProfile } from '../../api/customer';
import { createRazorpayOrder, verifyRazorpayPayment } from '../../api/payment';
import Modal from '../../components/common/Modal';
import Alert from '../../components/common/Alert';
import notificationService from '../../services/notificationService';

import './CustomerDashboard.css';

const CountdownTimer = ({ startTime, durationMinutes = 30 }) => {
    const [timeLeft, setTimeLeft] = useState('');

    useEffect(() => {
        const calculateTime = () => {
            if (!startTime) return false;
            
            
            let start = new Date(startTime).getTime();
            
            
            
            
            
            if (typeof startTime === 'string' && !startTime.endsWith('Z')) {
                 
                 start = new Date(startTime + 'Z').getTime();
            }

            const now = new Date().getTime();
            
            
            
            
            
            let diff = (start + durationMinutes * 60 * 1000) - now;
            
            
            
            if (diff > 18000000) { 
                 diff = diff - 19800000; 
            }

            if (diff <= 0) {
                setTimeLeft('Timed out');
                return false;
            } else {
                const mins = Math.floor(diff / (1000 * 60));
                const secs = Math.floor((diff % (1000 * 60)) / 1000);
                setTimeLeft(`${mins}m ${secs}s left`);
                return true;
            }
        };

        calculateTime();
        const timer = setInterval(() => {
            if (!calculateTime()) clearInterval(timer);
        }, 1000);

        return () => clearInterval(timer);
    }, [startTime, durationMinutes]);

    return (
        <span style={{ 
            color: timeLeft === 'Timed out' ? '#ef4444' : '#f59e0b', 
            fontWeight: '600',
            fontSize: '0.85rem',
            background: timeLeft === 'Timed out' ? '#fee2e2' : '#fff7ed',
            padding: '2px 8px',
            borderRadius: '12px',
            marginLeft: '8px',
            border: '1px solid currentColor'
        }}>
            {timeLeft}
        </span>
    );
};

const CustomerDashboard = () => {
    
  const [activeTab, setActiveTab] = useState('book'); 
  
  
  const [categories, setCategories] = useState([]);
  const [locations, setLocations] = useState([]); 
  const [providers, setProviders] = useState([]);
  const [myAssignments, setMyAssignments] = useState([]);
  
  
  const [profile, setProfile] = useState({
      userId: 0,
      name: '',
      mobile: '',
      email: '',
      address: '',
      locationId: 0
  });

  
  const [selectedCategory, setSelectedCategory] = useState('');
  const [selectedCity, setSelectedCity] = useState(''); 
  const [searchLoading, setSearchLoading] = useState(false);

  
  const [showBookingModal, setShowBookingModal] = useState(false);
  const [bookingProvider, setBookingProvider] = useState(null);
  const [bookingData, setBookingData] = useState({
    serviceAddress: '',
    scheduledDate: '',
    scheduledTime: ''
  });

  
  const [showRatingModal, setShowRatingModal] = useState(false);
  const [ratingRequest, setRatingRequest] = useState(null);
  const [ratingData, setRatingData] = useState({ stars: 5, comment: '' });

  
  const [alert, setAlert] = useState(null);
  const [refreshTrigger, setRefreshTrigger] = useState(0);
  const [bookingLoading, setBookingLoading] = useState(false);

  
  useEffect(() => {
    const loadInit = async () => {
      try {
        const [cats, locs, profData] = await Promise.all([
          getServiceCategories(),
          getLocations(), 
          getMyProfile()
        ]);
        setCategories(cats.filter(c => c.status === 'ACTIVE'));
        
        setLocations(locs);

        
        const { user, profile: prof, location: loc } = profData;
        console.log("DEBUG: Fetched Profile Data:", profData);
        setProfile({
            userId: user.id,
            name: user.name,
            mobile: user.mobile,
            email: user.email,
            address: prof?.address || '',
            locationId: prof?.locationId || 0
        });

        
        const finalLocId = prof?.locationId || 0;
        if (finalLocId > 0 && locs.length > 0) {
            const userLoc = locs.find(l => l.id == finalLocId);
            if (userLoc) {
                setSelectedCity(userLoc.district);
            }
        } else if (loc?.district) {
             setSelectedCity(loc.district);
        }

      } catch (err) {
        console.error(err);
      }
    };
    loadInit();
  }, []);

  
  
  
  useEffect(() => {
      
      
      
      if (activeTab === 'book' && selectedCategory && selectedCity) {
          handleSearch();
      }
  }, [selectedCategory, selectedCity]); 

  
  useEffect(() => {
    if (activeTab === 'requests') {
      loadAssignments();
    }
  }, [activeTab, refreshTrigger]);

  useEffect(() => {
    const handleNotification = (msg) => {
      console.log("Customer Real-time Update:", msg);
      setAlert({ type: 'info', message: typeof msg === 'string' ? msg : 'Your request status updated!' });
      setRefreshTrigger(p => p + 1);
    };

    notificationService.onReceiveNotification(handleNotification);
  }, []);

  const loadAssignments = async () => {
    try {
      const data = await getMyAssignments();
      setMyAssignments(data);
    } catch (err) {
      console.error(err);
    }
  };

  
  const handleSearch = async () => {
    if (!selectedCategory || !selectedCity) {
      
      return; 
    }
    setSearchLoading(true);
    setAlert(null);
    try {
      const data = await searchProviders(selectedCategory, selectedCity);
      setProviders(data);
      if (data.length === 0) {
        
        console.log("No providers found");
      }
    } catch (err) {
      console.error("Search failed", err);
    } finally {
      setSearchLoading(false);
    }
  };

  
  const openBookingModal = (provider) => {
    setBookingProvider(provider);
    setBookingData({
      serviceAddress: profile.address || '', 
      scheduledDate: '',
      scheduledTime: ''
    });
    setShowBookingModal(true);
  };

  const handleBookService = async () => {
    if (!bookingData.serviceAddress || !bookingData.scheduledDate || !bookingData.scheduledTime) {
      window.alert('Please fill all fields');
      return;
    }

    setBookingLoading(true);
    try {
      const dateTime = new Date(`${bookingData.scheduledDate}T${bookingData.scheduledTime}`);
      
      const requestPayload = {
        serviceCategoryId: parseInt(selectedCategory, 10), 
        locationId: profile.locationId > 0 ? profile.locationId : 1,
        providerId: bookingProvider.providerId,
        serviceAddress: bookingData.serviceAddress,
        scheduledDate: dateTime.toISOString(),
        paymentStatus: 'PENDING',
        amount: parseFloat(bookingProvider.hourlyRate) || 0
      };

      await createServiceRequest(requestPayload);
      
      setShowBookingModal(false);
      setAlert({ type: 'success', message: 'Service booked successfully! Check your email for the OTP.' });
      
      setActiveTab('requests');
      setRefreshTrigger(prev => prev + 1);
      setBookingLoading(false);
    } catch (err) {
      setBookingLoading(false);
      console.error("Detailed Booking Error:", err.response?.data || err);
      const msg = err.response?.data?.message || (typeof err.response?.data === 'string' ? err.response.data : err.message) || 'Booking failed';
      setAlert({ type: 'error', message: msg });
    }
  };

  
  const handleCancel = async (assignmentId) => {
    if (!window.confirm('Are you sure you want to cancel?')) return;
    try {
      await cancelServiceRequest(assignmentId);
      setAlert({ type: 'success', message: 'Request cancelled' });
      setRefreshTrigger(prev => prev + 1);
    } catch (err) {
      setAlert({ type: 'error', message: 'Cancellation failed' });
    }
  };

  
  const openRatingModal = (request) => {
    setRatingRequest(request);
    setRatingData({ stars: 5, comment: '' });
    setShowRatingModal(true);
  };

  const handleSubmitRating = async () => {
    try {
      await submitRating(ratingRequest.id, ratingData);
      setShowRatingModal(false);
      setAlert({ type: 'success', message: 'Rating submitted!' });
      setRefreshTrigger(prev => prev + 1);
    } catch (err) {
      setAlert({ type: 'error', message: 'Rating failed' });
    }
  };
  
  
  const handlePayment = async (assignmentId) => {
    try {
      setAlert({ type: 'info', message: 'Initializing Payment...' });
      const orderData = await createRazorpayOrder(assignmentId);
      
      const options = {
        key: orderData.keyId,
        amount: orderData.amount * 100, 
        currency: "INR",
        name: "Life Samadhan",
        description: "Service Payment",
        order_id: orderData.orderId,
        handler: async (response) => {
          try {
            const verifyData = {
              razorpayOrderId: response.razorpay_order_id,
              razorpayPaymentId: response.razorpay_payment_id,
              razorpaySignature: response.razorpay_signature
            };
            await verifyRazorpayPayment(verifyData);
            setAlert({ type: 'success', message: 'Payment Successful!' });
            setRefreshTrigger(prev => prev + 1);
          } catch (err) {
            setAlert({ type: 'error', message: 'Payment verification failed' });
          }
        },
        prefill: {
          name: profile.name,
          email: profile.email,
          contact: profile.mobile
        },
        theme: {
          color: "#2563eb"
        }
      };

      const rzp = new window.Razorpay(options);
      rzp.open();
    } catch (err) {
      setAlert({ type: 'error', message: err.response?.data?.error || 'Payment initialization failed' });
    }
  };

  
  const handleProfileUpdate = async (e) => {
      e.preventDefault();
      try {
          const payload = {
              name: profile.name,
              mobile: profile.mobile,
              address: profile.address,
              locationId: profile.locationId
          };
          await updateMyProfile(payload);
          setAlert({ type: 'success', message: 'Profile updated successfully' });
          
          
          const userLoc = locations.find(l => l.id == profile.locationId);
          if (userLoc) setSelectedCity(userLoc.district);

      } catch (err) {
          setAlert({ type: 'error', message: 'Failed to update profile' });
      }
  };

  return (
    <div className="dashboard-container">
      <div className="dashboard-header">
        <h1>Customer Dashboard</h1>
        <p>Welcome, {profile.name || 'User'}</p>
        <div className="tabs">
          <button 
            className={`tab-btn ${activeTab === 'book' ? 'active' : ''}`}
            onClick={() => setActiveTab('book')}
          >
            Book Service
          </button>
          <button 
            className={`tab-btn ${activeTab === 'requests' ? 'active' : ''}`}
            onClick={() => setActiveTab('requests')}
          >
            My Requests
          </button>
          <button 
            className={`tab-btn ${activeTab === 'profile' ? 'active' : ''}`}
            onClick={() => setActiveTab('profile')}
          >
            Profile
          </button>
        </div>
      </div>

      <div className="container mt-4">
        {alert && <Alert type={alert.type} message={alert.message} onClose={() => setAlert(null)} />}

        {activeTab === 'book' && (
          <div className="booking-section">
             {}
            {(!profile.address || profile.locationId <= 0) && (
                <div className="info-box mb-4" style={{borderColor: '#f59e0b', background: '#fffbeb', color: '#92400e'}}>
                    ⚠️ Please complete your profile (Address & Location) to improve service matching. 
                    <button className="btn-link" onClick={() => setActiveTab('profile')}>Edit Profile</button>
                </div>
            )}

            <div className="search-filters card">
              <h3>Find a Service Provider</h3>
              <div className="form-row">
                <div className="form-group">
                  <label>Category</label>
                  <select 
                    className="form-select"
                    value={selectedCategory}
                    onChange={(e) => setSelectedCategory(e.target.value)}
                  >
                    <option value="">Select Category</option>
                    {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                  </select>
                </div>
                <div className="form-group">
                  <label>City</label>
                  <select 
                    className="form-select"
                    value={selectedCity}
                    onChange={(e) => setSelectedCity(e.target.value)}
                  >
                    <option value="">Select City</option>
                    {}
                    {[...new Set(locations.map(l => l.district))].map(city => (
                        <option key={city} value={city}>{city}</option>
                    ))}
                  </select>
                </div>
                {}
                <div className="form-group" style={{ display: 'flex', alignItems: 'flex-end' }}>
                  <button className="btn btn-primary" onClick={handleSearch} disabled={searchLoading}>
                    {searchLoading ? 'Searching...' : 'Search'}
                  </button>
                </div>
              </div>
            </div>

            <div className="providers-list mt-4">
              {providers.length > 0 ? (
                  providers.map(p => (
                    <div key={p.providerId} className="provider-card card">
                      <div className="provider-info">
                        <h4>{p.name}</h4>
                        <p className="subtitle">{p.providerType} • {p.city}</p>
                        <div className="rating">
                          <span style={{ fontSize: '1.1rem' }}>★ {p.rating.toFixed(1)}</span>
                          <span style={{ color: '#6b7280', fontSize: '0.85rem', fontWeight: 'normal', marginLeft: '5px' }}>
                            ({p.reviewCount} {p.reviewCount === 1 ? 'review' : 'reviews'})
                          </span>
                        </div>
                        <p className="price">₹{p.hourlyRate}/hr</p>
                      </div>
                      <button className="btn btn-secondary" onClick={() => openBookingModal(p)}>
                        Book Now
                      </button>
                    </div>
                  ))
              ) : (
                  selectedCategory ? <p className="text-center mt-4">No providers found for this category/city.</p> : 
                  <p className="text-center mt-4">Select a category to see providers (City auto-selected from profile).</p>
              )}
            </div>
          </div>
        )}

        {activeTab === 'requests' && (
          <div className="requests-section">
            {myAssignments.length === 0 ? (
              <p className="text-center">No service requests found.</p>
            ) : (
              myAssignments.map((item) => (
                <div key={item.assignment.id} className="request-card card">
                  <div className="request-header">
                    <span className={`status-badge ${item.assignment.status.toLowerCase()}`}>
                      {item.assignment.status}
                    </span>
                    {item.assignment.status === 'ASSIGNED' && (
                        <CountdownTimer startTime={item.assignment.assignedAt} />
                    )}
                    {item.assignment.status === 'ACCEPTED' && (
                        <CountdownTimer startTime={item.assignment.acceptedAt} />
                    )}
                    <span className="date">
                      {new Date(item.assignment.assignedAt).toLocaleDateString()}
                    </span>
                  </div>
                  <div className="request-body">
                    <h4>{item.service?.name || 'Service'}</h4>
                    <p><strong>Provider:</strong> {item.provider?.userName || 'Assigned Provider'}</p>
                    <p><strong>Address:</strong> {item.request?.serviceAddress}</p>
                    {['ASSIGNED', 'ACCEPTED'].includes(item.assignment.status) && (
                      !item.assignment.otp || ['****', 'PROTECTED'].includes(item.assignment.otp) || (item.request?.otp || item.request?.OTP) === 'PROTECTED' ? (
                        <div className="otp-box info" style={{background: '#eff6ff', border: '1px dashed #3b82f6', color: '#1e40af', padding: '8px', borderRadius: '4px', marginTop: '8px', textAlign: 'center'}}>
                          OTP sent to your Registered Email
                        </div>
                      ) : (
                        <div className="otp-box">
                          OTP: <strong>{item.assignment.otp}</strong>
                        </div>
                      )
                    )}
                  </div>
                  <div className="request-actions">
                    {item.assignment.status === 'ASSIGNED' && (
                      <button 
                        className="btn btn-danger btn-sm"
                        onClick={() => handleCancel(item.assignment.id)}
                      >
                        Cancel
                      </button>
                    )}
                    {item.assignment.status === 'COMPLETED' && (
                      <div className="payment-rating-actions" style={{display: 'flex', gap: '10px', marginTop: '10px'}}>
                        {}
                        <div className="rating-container">
                          {item.rating ? (
                            <div className="existing-rating" style={{background: '#f8fafc', padding: '8px', borderRadius: '4px', border: '1px solid #e2e8f0'}}>
                              <div style={{color: '#f59e0b', fontWeight: 'bold'}}>Your Rating: {Array(item.rating.stars).fill('★').join('')}</div>
                              {item.rating.comment && <p style={{fontSize: '0.85rem', color: '#64748b', margin: '4px 0'}}>"{item.rating.comment}"</p>}
                            </div>
                          ) : (
                            <button 
                              className="btn btn-outline btn-sm"
                              onClick={() => openRatingModal(item.request)}
                            >
                              Rate Service
                            </button>
                          )}
                        </div>

                        {}
                        <div className="payment-container">
                          {item.request?.paymentStatus === 'PENDING' ? (
                            <button 
                              className="btn btn-primary btn-sm"
                              onClick={() => handlePayment(item.assignment.id)}
                            >
                              Pay Now (₹{item.request?.amount})
                            </button>
                          ) : (
                            <span className="status-badge paid">PAID</span>
                          )}
                        </div>
                      </div>
                    )}
                  </div>
                </div>
              ))
            )}
          </div>
        )}

        {activeTab === 'profile' && (
            <div className="profile-section card p-6">
                <h3>My Profile</h3>
                <form onSubmit={handleProfileUpdate}>
                    <div className="form-row">
                        <div className="form-group">
                            <label>Full Name</label>
                            <input 
                                type="text" className="form-input" 
                                value={profile.name}
                                onChange={(e) => setProfile({...profile, name: e.target.value})}
                            />
                        </div>
                        <div className="form-group">
                            <label>Mobile</label>
                            <input 
                                type="text" className="form-input" 
                                value={profile.mobile}
                                onChange={(e) => setProfile({...profile, mobile: e.target.value})}
                            />
                        </div>
                    </div>
                    
                    <div className="form-group">
                        <label>Email (Read-only)</label>
                        <input type="text" className="form-input" value={profile.email} disabled />
                    </div>

                    <div className="form-group">
                        <label>Full Address</label>
                        <textarea 
                            className="form-input" rows="3"
                            value={profile.address}
                            onChange={(e) => setProfile({...profile, address: e.target.value})}
                        />
                    </div>

                    <div className="form-group">
                        <label>Available Service Area (City)</label>
                        <select 
                            className="form-select"
                            value={profile.locationId}
                            onChange={(e) => setProfile({...profile, locationId: parseInt(e.target.value)})}
                        >
                            <option value="0">Select Service Area</option>
                            {locations.map(l => (
                                <option key={l.id} value={l.id}>{l.district} ({l.pincode})</option>
                            ))}
                        </select>
                         <p className="form-help">Select an area to see providers available in your location.</p>
                    </div>

                    <button type="submit" className="btn btn-primary">Update Profile</button>
                </form>
            </div>
        )}

      </div>

      <Modal 
        isOpen={showBookingModal} 
        onClose={() => setShowBookingModal(false)}
        title={`Book ${bookingProvider?.name}`}
      >
        {bookingProvider && (
          <div style={{ marginBottom: '20px', padding: '10px', background: '#f8fafc', borderRadius: '8px', border: '1px solid #e2e8f0' }}>
            <p style={{ margin: 0, fontSize: '0.9rem', color: '#64748b' }}>
              <strong>Provider Rating:</strong> 
              <span style={{ color: '#f59e0b', marginLeft: '5px' }}>★ {bookingProvider.rating.toFixed(1)}</span> 
              ({bookingProvider.reviewCount} reviews)
            </p>
            <p style={{ margin: 0, fontSize: '0.9rem', color: '#64748b' }}>
              <strong>Rate:</strong> ₹{bookingProvider.hourlyRate}/hr
            </p>
          </div>
        )}
        <div className="form-group">
          <label>Service Address</label>
          <textarea 
            className="form-input"
            value={bookingData.serviceAddress}
            onChange={(e) => setBookingData({...bookingData, serviceAddress: e.target.value})}
            placeholder="Enter full address"
          />
        </div>
        <div className="form-row">
          <div className="form-group">
            <label>Date</label>
            <input 
              type="date"
              className="form-input"
              value={bookingData.scheduledDate}
              onChange={(e) => setBookingData({...bookingData, scheduledDate: e.target.value})}
            />
          </div>
          <div className="form-group">
            <label>Time</label>
            <input 
              type="time"
              className="form-input"
              value={bookingData.scheduledTime}
              onChange={(e) => setBookingData({...bookingData, scheduledTime: e.target.value})}
            />
          </div>
        </div>
        <div className="modal-actions">
           <button 
             className="btn btn-primary w-full" 
             onClick={handleBookService}
             disabled={bookingLoading}
           >
             {bookingLoading ? 'Processing...' : `Confirm Booking (₹${bookingProvider?.hourlyRate})`}
           </button>
        </div>
      </Modal>

      {}
      <Modal
        isOpen={showRatingModal}
        onClose={() => setShowRatingModal(false)}
        title="Rate Service"
      >
        <div className="rating-stars text-center mb-4" style={{ fontSize: '2rem', cursor: 'pointer' }}>
          {[1, 2, 3, 4, 5].map(star => (
            <span 
              key={star} 
              onClick={() => setRatingData({...ratingData, stars: star})}
              style={{ color: star <= ratingData.stars ? '#ffc107' : '#ddd' }}
            >
              ★
            </span>
          ))}
        </div>
        <div className="form-group">
          <label>Comments</label>
          <textarea 
            className="form-input"
            value={ratingData.comment}
            onChange={(e) => setRatingData({...ratingData, comment: e.target.value})}
            placeholder="Share your experience..."
          />
        </div>
        <button className="btn btn-primary w-full" onClick={handleSubmitRating}>
          Submit Review
        </button>
      </Modal>
    </div>
  );
};

export default CustomerDashboard;
