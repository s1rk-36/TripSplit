import { Link, useNavigate, useLocation } from 'react-router-dom';
import {FaUser, FaCog, FaSignOutAlt, FaSignInAlt, FaUserPlus } from 'react-icons/fa';
import { useAuth } from '../../utils/auth';

function Navbar() {
  const { currentUser, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
  localStorage.removeItem('tripsplit_user');
  
  navigate('/login');
  
  window.location.reload();
  };

  const isActive = (path) => {
    return location.pathname === path ? 'active' : '';
  };

  return (
    <nav className="navbar navbar-expand-lg navbar-dark bg-primary shadow-sm">
      <div className="container">
        <Link className="navbar-brand fw-bold" to={currentUser ? "/groups" : "/"}>
          TripSplit
        </Link>
        
        <button 
          className="navbar-toggler" 
          type="button" 
          data-bs-toggle="collapse" 
          data-bs-target="#navbarNav"
        >
          <span className="navbar-toggler-icon"></span>
        </button>
        
        <div className="collapse navbar-collapse" id="navbarNav">
          {currentUser ? (
            // Authenticated user navigation
            <>
              <ul className="navbar-nav me-auto">
              </ul>

              <ul className="navbar-nav">
                {/* Profile Dropdown */}
                <li className="nav-item dropdown">
                  <a 
                    className="nav-link dropdown-toggle d-flex align-items-center" 
                    href="#" 
                    id="profileDropdown" 
                    role="button" 
                    data-bs-toggle="dropdown" 
                    aria-expanded="false"
                  >
                    <div className="bg-white text-primary rounded-circle d-flex align-items-center justify-content-center me-2" style={{width: '32px', height: '32px'}}>
                      <FaUser size={16} />
                    </div>
                    <span className="d-none d-md-inline">
                      {currentUser?.firstName} {currentUser?.lastName}
                    </span>
                  </a>
                  <ul className="dropdown-menu dropdown-menu-end shadow" aria-labelledby="profileDropdown">
                    <li>
                      <div className="dropdown-header">
                        <div className="fw-bold">{currentUser?.firstName} {currentUser?.lastName}</div>
                        <small className="text-muted">{currentUser?.email}</small>
                      </div>
                    </li>
                    <li><hr className="dropdown-divider" /></li>
                    <li>
                      <Link className="dropdown-item" to="/profile">
                        <FaCog className="me-2" />
                        Account Settings
                      </Link>
                    </li>
                    <li><hr className="dropdown-divider" /></li>
                    <li>
                      <button 
                        className="dropdown-item text-danger" 
                        onClick={handleLogout}
                      >
                        <FaSignOutAlt className="me-2" />
                        Sign Out
                      </button>
                    </li>
                  </ul>
                </li>
              </ul>
            </>
          ) : (
            // Non-authenticated user navigation
            <>
              <ul className="navbar-nav me-auto">
              </ul>
              
              <ul className="navbar-nav">
                <li className="nav-item">
                  <Link className={`nav-link ${isActive('/login')}`} to="/login">
                    <FaSignInAlt className="me-1" />
                    Sign In
                  </Link>
                </li>
                <li className="nav-item">
                  <Link 
                    className={`btn btn-outline-light ms-2 ${isActive('/register')}`} 
                    to="/register"
                  >
                    <FaUserPlus className="me-1" />
                    Sign Up
                  </Link>
                </li>
              </ul>
            </>
          )}
        </div>
      </div>
    </nav>
  );
}

export default Navbar;