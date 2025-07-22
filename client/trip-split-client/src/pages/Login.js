import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { FaDollarSign, FaEye, FaEyeSlash, FaEnvelope, FaLock } from 'react-icons/fa';
// import { apiService } from '../services/apiService';
// import { auth } from '../utils/auth';

function Login() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    email: '',
    password: ''
  });
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    try {
      setError('');
      
    //   const response = await apiService.login(formData);
    //   auth.setCurrentUser(response);
      navigate('/dashboard');
    } catch (err) {
      setError(err.message || 'Login failed');
      
      // Fallback for development
      if (formData.email === 'custom@example.com' && formData.password === 'password') {
        const mockUser = {
          userId: 1,
          firstName: 'John',
          lastName: 'Doe',
          email: 'custom@example.com'
        };
        // auth.setCurrentUser(mockUser);
        navigate('/dashboard');
      }
    }
  };

  return (
    <div className="min-vh-100 d-flex align-items-center bg-light">
      <div className="container">
        <div className="row justify-content-center">
          <div className="col-md-6 col-lg-4">
            <div className="card shadow-lg border-0">
              <div className="card-body p-5">
                <div className="text-center mb-4">
                  <h4 className="fw-bold text-dark">Welcome Back</h4>
                  <p className="text-muted">Sign in to your account</p>
                </div>

                {error && (
                  <div className="alert alert-danger">{error}</div>
                )}

                <form onSubmit={handleSubmit}>
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
                        placeholder="Enter your password"
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
                  </div>

                  <button
                    type="submit"
                    className="btn btn-primary w-100 py-2 mb-3"
                  >
                    Sign In
                  </button>
                </form>
              </div>
            </div>

            <div className="text-center mt-3">
              <span className="text-muted">Don't have an account? </span>
              <Link to="/register" className="text-decoration-none fw-bold">Sign up</Link>
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

export default Login;