import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { FaDollarSign, FaEye, FaEyeSlash, FaEnvelope, FaLock, FaUser, FaCheck } from 'react-icons/fa';
// import { apiService } from '../services/apiService';
// import { auth, validatePassword } from '../utils/auth';

function Register() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    confirmPassword: ''
  });
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [error, setError] = useState('');
  const [passwordStrength, setPasswordStrength] = useState({
    length: false,
    number: false,
    letter: false
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    
    if (name === 'password') {
    //   setPasswordStrength(validatePassword(value));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    try {
      setError('');
      
      if (formData.password !== formData.confirmPassword) {
        setError('Passwords do not match');
        return;
      }
      
      if (!passwordStrength.length || !passwordStrength.number || !passwordStrength.letter) {
        setError('Password does not meet requirements');
        return;
      }
      
    //   const response = await apiService.register(formData);
    //   auth.setCurrentUser(response);
      navigate('/dashboard');
    } catch (err) {
      setError(err.message || 'Registration failed');
      
      // Fallback for development
      const mockUser = {
        userId: Date.now(),
        firstName: formData.firstName,
        lastName: formData.lastName,
        email: formData.email
      };
    //   auth.setCurrentUser(mockUser);
      navigate('/dashboard');
    }
  };

  const isPasswordValid = passwordStrength.length && passwordStrength.number && passwordStrength.letter;

  return (
    <div className="min-vh-100 d-flex align-items-center bg-light py-5">
      <div className="container">
        <div className="row justify-content-center">
          <div className="col-md-8 col-lg-6">
            <div className="card shadow-lg border-0">
              <div className="card-body p-5">
                <div className="text-center mb-4">
                  <h4 className="fw-bold text-dark">Create Account</h4>
                </div>

                {error && (
                  <div className="alert alert-danger">{error}</div>
                )}

                <form onSubmit={handleSubmit}>
                  <div className="row">
                    <div className="col-md-6 mb-3">
                      <label className="form-label">First Name</label>
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
                        />
                      </div>
                    </div>

                    <div className="col-md-6 mb-3">
                      <label className="form-label">Last Name</label>
                      <input
                        type="text"
                        className="form-control"
                        name="lastName"
                        value={formData.lastName}
                        onChange={handleChange}
                        placeholder="Last name"
                        required
                      />
                    </div>
                  </div>

                  <div className="mb-3">
                    <label className="form-label">Email Address</label>
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
                      />
                    </div>
                  </div>

                  <div className="mb-3">
                    <label className="form-label">Password</label>
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
                      />
                      <button
                        type="button"
                        className="btn btn-outline-secondary"
                        onClick={() => setShowPassword(!showPassword)}
                      >
                        {showPassword ? <FaEyeSlash /> : <FaEye />}
                      </button>
                    </div>
                    
                    <div className="mt-2">
                      <small className="text-muted">Password must contain:</small>
                      <div className="mt-1">
                        <small className={passwordStrength.length ? 'text-success' : 'text-muted'}>
                          <FaCheck className={passwordStrength.length ? 'text-success' : 'text-muted'} size={12} />
                          <span className="ms-1">At least 8 characters</span>
                        </small>
                      </div>
                      <div>
                        <small className={passwordStrength.number ? 'text-success' : 'text-muted'}>
                          <FaCheck className={passwordStrength.number ? 'text-success' : 'text-muted'} size={12} />
                          <span className="ms-1">At least one number</span>
                        </small>
                      </div>
                      <div>
                        <small className={passwordStrength.letter ? 'text-success' : 'text-muted'}>
                          <FaCheck className={passwordStrength.letter ? 'text-success' : 'text-muted'} size={12} />
                          <span className="ms-1">At least one letter</span>
                        </small>
                      </div>
                    </div>
                  </div>

                  <div className="mb-3">
                    <label className="form-label">Confirm Password</label>
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
                      />
                      <button
                        type="button"
                        className="btn btn-outline-secondary"
                        onClick={() => setShowConfirmPassword(!showConfirmPassword)}
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