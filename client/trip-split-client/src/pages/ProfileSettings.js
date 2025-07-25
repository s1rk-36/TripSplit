import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { FaUser, FaEnvelope, FaLock, FaSave, FaEye, FaEyeSlash, FaEdit, FaCheck, FaTimes } from 'react-icons/fa';
import Layout from '../components/layout/Layout';
import LoadingSpinner from '../components/common/LoadingSpinner';
import ErrorAlert from '../components/common/ErrorAlert';
import { apiService } from '../services/apiService';
import { useAuth, auth } from '../utils/auth'; 

function ProfileSettings() {
  const { currentUser } = useAuth();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  
  const [profileData, setProfileData] = useState({
    appUserId: 0,
    firstName: '',
    lastName: '',
    email: '',
    username: ''
  });
  
  const [activeTab, setActiveTab] = useState('profile');

  useEffect(() => {
    const freshUser = auth.getCurrentUser();
    if (freshUser) {
      setProfileData({
        appUserId: freshUser.userId,
        firstName: freshUser.firstName || '',
        lastName: freshUser.lastName || '',
        email: freshUser.email || '',
        username: freshUser.username || ''
      });
    }
  }, [currentUser]);

  const handleProfileChange = (e) => {
    const { name, value } = e.target;
    setProfileData(prev => ({ ...prev, [name]: value }));
  };

  const handleProfileSubmit = async (e) => {
    e.preventDefault();
    
    try {
      setSaving(true);
      setError('');
      setSuccess('');
      
      console.log('Updating profile:', profileData);
      
      await apiService.updateUser(currentUser.userId, profileData);
      const currentStoredUser = JSON.parse(localStorage.getItem('tripsplit_user') || '{}');
      const updatedUser = {
        ...currentStoredUser,
        firstName: profileData.firstName,
        lastName: profileData.lastName,
        email: profileData.email,
      };
      localStorage.setItem('tripsplit_user', JSON.stringify(updatedUser));

      setSuccess('Profile updated successfully!');
      
      navigate('/groups');

      window.location.reload();
      
    } catch (err) {
      console.error('Failed to update profile:', err);
      setError(err.message || 'Failed to update profile');
    } finally {
      setSaving(false);
    }
  };

  const displayUser = auth.getCurrentUser() || currentUser;

  if (loading) {
    return (
      <Layout>
        <LoadingSpinner message="Loading profile..." />
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="row justify-content-center">
        <div className="col-lg-8">
          {/* Header - Using fresh data */}
          <div className="d-flex align-items-center mb-4">
            <div className="bg-primary text-white rounded-circle d-flex align-items-center justify-content-center me-3" style={{width: '48px', height: '48px'}}>
              <FaUser size={24} />
            </div>
            <div>
              <h2 className="mb-0">Profile Settings</h2>
              <p className="text-muted mb-0">
                Welcome, {displayUser?.firstName || 'User'} {displayUser?.lastName || ''}
              </p>
            </div>
          </div>

          {error && <ErrorAlert error={error} />}
          {success && (
            <div className="alert alert-success alert-dismissible fade show">
              <FaCheck className="me-2" />
              {success}
              <button 
                type="button" 
                className="btn-close" 
                onClick={() => setSuccess('')}
              ></button>
            </div>
          )}

          {/* Profile Tab */}
          {activeTab === 'profile' && (
            <div className="card">
              <div className="card-header">
                <h5 className="mb-0">
                  <FaEdit className="me-2" />
                  Profile Information
                </h5>
              </div>
              <div className="card-body">
                <form onSubmit={handleProfileSubmit}>
                  <div className="row">
                    <div className="col-md-6">
                      <div className="mb-3">
                        <label className="form-label">First Name *</label>
                        <input
                          type="text"
                          className="form-control"
                          name="firstName"
                          value={profileData.firstName}
                          onChange={handleProfileChange}
                          required
                        />
                      </div>
                    </div>
                    <div className="col-md-6">
                      <div className="mb-3">
                        <label className="form-label">Last Name *</label>
                        <input
                          type="text"
                          className="form-control"
                          name="lastName"
                          value={profileData.lastName}
                          onChange={handleProfileChange}
                          required
                        />
                      </div>
                    </div>
                  </div>

                  <div className="mb-3">
                    <label className="form-label">Email *</label>
                    <input
                      type="email"
                      className="form-control"
                      name="email"
                      value={profileData.email}
                      onChange={handleProfileChange}
                      required
                    />
                  </div>

                  <div className="d-grid">
                    <button 
                      type="submit" 
                      className="btn btn-primary"
                      disabled={saving}
                    >
                      {saving ? (
                        <>
                          <span className="spinner-border spinner-border-sm me-2"></span>
                          Saving...
                        </>
                      ) : (
                        <>
                          <FaSave className="me-1" />
                          Save Changes
                        </>
                      )}
                    </button>
                  </div>
                </form>
              </div>
            </div>
          )}
        </div>
      </div>
    </Layout>
  );
}

export default ProfileSettings;