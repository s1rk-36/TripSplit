import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { FaEye, FaEyeSlash, FaEnvelope, FaLock, FaUser } from 'react-icons/fa';
import { apiService } from '../services/apiService';
import { auth } from '../utils/auth';

function Register() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    username: '',
    password: '',
    confirmPassword: ''
  });
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [error, setError] = useState('');

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    try {
      setError('');
      
      if (formData.password !== formData.confirmPassword) {
        setError('Passwords do not match');
        return;
      }
      
      // All other validation happens on backend
      const response = await apiService.register({
        firstName: formData.firstName,
        lastName: formData.lastName,
        email: formData.email,
        username: formData.username || formData.email.split('@')[0], // Default username from email
        password: formData.password
      });
      
      // Expected response: { token, user }
      if (!response.token) {
        throw new Error('Invalid response format: missing token');
      }
      
      // Store user data with JWT token
      const userData = {
        userId: response.user.id,
        firstName: response.user.firstName,
        lastName: response.user.lastName,
        email: response.user.email,
        username: response.user.username,
        token: response.token
      };
      
      auth.setCurrentUser(userData);
      navigate('/groups');
      
    } catch (err) {
      console.error('Registration error:', err);
      
      // Handle different error formats
      if (err.response && err.response.data) {
        // Backend returned validation errors array
        if (Array.isArray(err.response.data)) {
          setError(err.response.data.join('. '));
        } else {
          setError(err.response.data.message || 'Registration failed');
        }
      } else if (err.message) {
        setError(err.message);
      } else {
        setError('Registration failed. Please try again.');
      }
      
    }
  };

  return (
    <div className="min-vh-100 d-flex align-items-center bg-light py-5">
      <div className="container">
        <div className="row justify-content-center">
          <div className="col-md-8 col-lg-6">
            <div className="card shadow-lg border-0">
              <div className="card-body p-5">
                <div className="text-center mb-4">
                  <h4 className="fw-bold text-dark">Create Account</h4>
                  <p className="text-muted">Join thousands of users splitting expenses</p>
                </div>

                {error && (
                  <div className="alert alert-danger" role="alert">
                    {error}
                  </div>
                )}

                <form onSubmit={handleSubmit}>
                  <div className="row">
                    <div className="col-md-6 mb-3">
                      <label className="form-label">First Name *</label>
                      <div className="input-group">
                        <span className="input-group-text">
                          <FaUser className="text-muted" />
                        </span>
                        <input
                          type="text"
                          className="form-control"
                          name="firstName"
                          value={formData.firstName}
                          onChange={handleChange}
                          placeholder="First name"
                          required
                          autoComplete="given-name"
                        />
                      </div>
                    </div>

                    <div className="col-md-6 mb-3">
                      <label className="form-label">Last Name *</label>
                      <input
                        type="text"
                        className="form-control"
                        name="lastName"
                        value={formData.lastName}
                        onChange={handleChange}
                        placeholder="Last name"
                        required
                        autoComplete="family-name"
                      />
                    </div>
                  </div>

                  <div className="mb-3">
                    <label className="form-label">Email Address *</label>
                    <div className="input-group">
                      <span className="input-group-text">
                        <FaEnvelope className="text-muted" />
                      </span>
                      <input
                        type="email"
                        className="form-control"
                        name="email"
                        value={formData.email}
                        onChange={handleChange}
                        placeholder="Enter your email"
                        required
                        autoComplete="email"
                      />
                    </div>
                  </div>

                  <div className="mb-3">
                    <label className="form-label">Username *</label>
                    <div className="input-group">
                      <span className="input-group-text">@</span>
                      <input
                        type="text"
                        className="form-control"
                        name="username"
                        value={formData.username}
                        onChange={handleChange}
                        placeholder="Choose a username"
                        required
                        autoComplete="username"
                      />
                    </div>                  </div>

                  <div className="mb-3">
                    <label className="form-label">Password *</label>
                    <div className="input-group">
                      <span className="input-group-text">
                        <FaLock className="text-muted" />
                      </span>
                      <input
                        type={showPassword ? 'text' : 'password'}
                        className="form-control"
                        name="password"
                        value={formData.password}
                        onChange={handleChange}
                        placeholder="Create a password"
                        required
                        autoComplete="new-password"
                      />
                      <button
                        type="button"
                        className="btn btn-outline-secondary"
                        onClick={() => setShowPassword(!showPassword)}
                        tabIndex={-1}
                      >
                        {showPassword ? <FaEyeSlash /> : <FaEye />}
                      </button>
                    </div>
                    
                    {/* Static Password Requirements */}
                    <div className="mt-2">
                      <small className="text-muted">Password must include:</small>
                      <ul className="mt-1 mb-0 small text-muted">
                        <li>At least 8 characters long</li>
                        <li>At least one number</li>
                        <li>At least one letter</li>
                        <li>At least one special character</li>
                      </ul>
                    </div>
                  </div>

                  <div className="mb-3">
                    <label className="form-label">Confirm Password *</label>
                    <div className="input-group">
                      <span className="input-group-text">
                        <FaLock className="text-muted" />
                      </span>
                      <input
                        type={showConfirmPassword ? 'text' : 'password'}
                        className="form-control"
                        name="confirmPassword"
                        value={formData.confirmPassword}
                        onChange={handleChange}
                        placeholder="Confirm your password"
                        required
                        autoComplete="new-password"
                      />
                      <button
                        type="button"
                        className="btn btn-outline-secondary"
                        onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                        tabIndex={-1}
                      >
                        {showConfirmPassword ? <FaEyeSlash /> : <FaEye />}
                      </button>
                    </div>
                    {formData.confirmPassword && formData.password !== formData.confirmPassword && (
                      <small className="text-danger">Passwords do not match</small>
                    )}
                  </div>

                  <button
                    type="submit"
                    className="btn btn-primary w-100 py-2 mb-3"
                  >
                    Create Account
                  </button>
                </form>
              </div>
            </div>

            <div className="text-center mt-3">
              <span className="text-muted">Already have an account? </span>
              <Link to="/login" className="text-decoration-none fw-bold">Sign in</Link>
            </div>

            <div className="text-center mt-2">
              <Link to="/" className="text-muted text-decoration-none small">← Back to Home</Link>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Register;