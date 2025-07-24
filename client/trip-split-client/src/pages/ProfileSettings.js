import { useState, useEffect } from 'react';
import { FaUser, FaEnvelope, FaLock, FaSave, FaEye, FaEyeSlash, FaEdit, FaCheck, FaTimes } from 'react-icons/fa';
import Layout from '../components/layout/Layout';
import LoadingSpinner from '../components/common/LoadingSpinner';
import ErrorAlert from '../components/common/ErrorAlert';
import { apiService } from '../services/apiService';
import { useAuth } from '../utils/auth';
import { validatePassword } from '../utils/auth';

function ProfileSettings() {
  const { currentUser } = useAuth();
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  
  // Profile form state
  const [profileData, setProfileData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    username: ''
  });
  
  // Password form state
  const [passwordData, setPasswordData] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  });
  
  // UI state
  const [showPasswords, setShowPasswords] = useState({
    current: false,
    new: false,
    confirm: false
  });
  const [activeTab, setActiveTab] = useState('profile');
  const [passwordValidation, setPasswordValidation] = useState({
    length: false,
    number: false,
    letter: false
  });

  useEffect(() => {
    if (currentUser) {
      // Initialize with current user data
      setProfileData({
        firstName: currentUser.firstName || '',
        lastName: currentUser.lastName || '',
        email: currentUser.email || '',
        username: currentUser.username || ''
      });
    }
  }, [currentUser]);

  useEffect(() => {
    // Validate password as user types
    if (passwordData.newPassword) {
      setPasswordValidation(validatePassword(passwordData.newPassword));
    }
  }, [passwordData.newPassword]);

  const handleProfileChange = (e) => {
    console.log(e.target);
    const { name, value } = e.target;
    setProfileData(prev => ({ ...prev, [name]: value }));
  };

  const handlePasswordChange = (e) => {
    const { name, value } = e.target;
    setPasswordData(prev => ({ ...prev, [name]: value }));
  };

  const handleProfileSubmit = async (e) => {
    e.preventDefault();
    
    try {
      setSaving(true);
      setError('');
      setSuccess('');
      
      await apiService.updateUserProfile(profileData);
      setSuccess('Profile updated successfully!');
      
    } catch (err) {
      console.error('Failed to update profile:', err);
      setError(err.message || 'Failed to update profile');
    } finally {
      setSaving(false);
    }
  };

  const handlePasswordSubmit = async (e) => {
    e.preventDefault();
    
    // Validate passwords
    if (passwordData.newPassword !== passwordData.confirmPassword) {
      setError('New passwords do not match');
      return;
    }
    
    if (!passwordValidation.length || !passwordValidation.number || !passwordValidation.letter) {
      setError('Password does not meet requirements');
      return;
    }
    
    try {
      setSaving(true);
      setError('');
      setSuccess('');
      
      await apiService.updateUser({
        newPassword: passwordData.newPassword
      });
      
      setSuccess('Password updated successfully!');
      setPasswordData({
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
      });
      
    } catch (err) {
      console.error('Failed to update password:', err);
      setError(err.message || 'Failed to update password');
    } finally {
      setSaving(false);
    }
  };

  const togglePasswordVisibility = (field) => {
    setShowPasswords(prev => ({
      ...prev,
      [field]: !prev[field]
    }));
  };

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
          {/* Header */}
          <div className="d-flex align-items-center mb-4">
            <div className="bg-primary text-white rounded-circle d-flex align-items-center justify-content-center me-3" style={{width: '48px', height: '48px'}}>
              <FaUser size={24} />
            </div>
            <div>
              <h2 className="mb-0">Profile Settings</h2>
              <p className="text-muted mb-0">Manage your account information and security</p>
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

          {/* Tabs */}
          <ul className="nav nav-tabs mb-4">
            <li className="nav-item">
              <button 
                className={`nav-link ${activeTab === 'profile' ? 'active' : ''}`}
                onClick={() => setActiveTab('profile')}
              >
                <FaUser className="me-1" /> Profile Information
              </button>
            </li>
            <li className="nav-item nav-dark">
              <button 
                className={`nav-link ${activeTab === 'password' ? 'active' : ''}`}
                onClick={() => setActiveTab('password')}
              >
                <FaLock className="me-1" /> Password & Security
              </button>
            </li>
          </ul>

          {/* Profile Tab */}
          {activeTab === 'profile' && (
            <div className="card">
              <div className="card-header">
                <h5 className="mb-0">
                  <FaEdit className="me-2" />
                  Personal Information
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
                    <label className="form-label">Email Address *</label>
                    <input
                      type="email"
                      className="form-control"
                      name="email"
                      value={profileData.email}
                      onChange={handleProfileChange}
                      required
                    />
                  </div>

                  <div className="mb-3">
                    <label className="form-label">Username *</label>
                    <input
                      type="text"
                      className="form-control"
                      name="username"
                      value={profileData.username}
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

          {/* Password Tab */}
          {activeTab === 'password' && (
            <div className="card">
              <div className="card-header">
                <h5 className="mb-0">
                  <FaLock className="me-2" />
                  Change Password
                </h5>
              </div>
              <div className="card-body">
                <form onSubmit={handlePasswordSubmit}>
                  <div className="mb-3">
                    <label className="form-label">Current Password *</label>
                    <div className="input-group">
                      <input
                        type={showPasswords.current ? "text" : "password"}
                        className="form-control"
                        name="currentPassword"
                        value={passwordData.currentPassword}
                        onChange={handlePasswordChange}
                        required
                      />
                      <button
                        type="button"
                        className="btn btn-outline-secondary"
                        onClick={() => togglePasswordVisibility('current')}
                      >
                        {showPasswords.current ? <FaEyeSlash /> : <FaEye />}
                      </button>
                    </div>
                  </div>

                  <div className="mb-3">
                    <label className="form-label">New Password *</label>
                    <div className="input-group">
                      <input
                        type={showPasswords.new ? "text" : "password"}
                        className="form-control"
                        name="newPassword"
                        value={passwordData.newPassword}
                        onChange={handlePasswordChange}
                        required
                      />
                      <button
                        type="button"
                        className="btn btn-outline-secondary"
                        onClick={() => togglePasswordVisibility('new')}
                      >
                        {showPasswords.new ? <FaEyeSlash /> : <FaEye />}
                      </button>
                    </div>
                    
                    {/* Password Requirements */}
                    {passwordData.newPassword && (
                      <div className="mt-2">
                        <small className="text-muted d-block mb-1">Password must contain:</small>
                        <div className="d-flex flex-wrap gap-2">
                          <span className={`badge ${passwordValidation.length ? 'bg-success' : 'bg-danger'}`}>
                            {passwordValidation.length ? <FaCheck /> : <FaTimes />} 8+ characters
                          </span>
                          <span className={`badge ${passwordValidation.number ? 'bg-success' : 'bg-danger'}`}>
                            {passwordValidation.number ? <FaCheck /> : <FaTimes />} Number
                          </span>
                          <span className={`badge ${passwordValidation.letter ? 'bg-success' : 'bg-danger'}`}>
                            {passwordValidation.letter ? <FaCheck /> : <FaTimes />} Letter
                          </span>
                        </div>
                      </div>
                    )}
                  </div>

                  <div className="mb-3">
                    <label className="form-label">Confirm New Password *</label>
                    <div className="input-group">
                      <input
                        type={showPasswords.confirm ? "text" : "password"}
                        className={`form-control ${
                          passwordData.confirmPassword && passwordData.newPassword !== passwordData.confirmPassword 
                            ? 'is-invalid' 
                            : passwordData.confirmPassword && passwordData.newPassword === passwordData.confirmPassword
                            ? 'is-valid'
                            : ''
                        }`}
                        name="confirmPassword"
                        value={passwordData.confirmPassword}
                        onChange={handlePasswordChange}
                        required
                      />
                      <button
                        type="button"
                        className="btn btn-outline-secondary"
                        onClick={() => togglePasswordVisibility('confirm')}
                      >
                        {showPasswords.confirm ? <FaEyeSlash /> : <FaEye />}
                      </button>
                      {passwordData.confirmPassword && passwordData.newPassword !== passwordData.confirmPassword && (
                        <div className="invalid-feedback">
                          Passwords do not match
                        </div>
                      )}
                    </div>
                  </div>

                  <div className="d-grid">
                    <button 
                      type="submit" 
                      className="btn btn-primary"
                      disabled={saving || !passwordValidation.length || !passwordValidation.number || !passwordValidation.letter}
                    >
                      {saving ? (
                        <>
                          <span className="spinner-border spinner-border-sm me-2"></span>
                          Updating...
                        </>
                      ) : (
                        <>
                          <FaLock className="me-1" />
                          Update Password
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