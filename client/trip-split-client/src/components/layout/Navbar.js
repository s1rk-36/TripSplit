import { useState, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import {FaUser, FaCog, FaSignOutAlt, FaSignInAlt, FaUserPlus, FaUserShield } from 'react-icons/fa';
import { useAuth, auth } from '../../utils/auth'
import { isDemoMode } from '../../services/demoData'

function Navbar() {
  const { currentUser, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [displayUser, setDisplayUser] = useState(null);
  const [forceUpdate, setForceUpdate] = useState(0);

  useEffect(() => {
    const freshUser = auth.getCurrentUser();
    setDisplayUser(freshUser);
  }, [currentUser, forceUpdate]);

  useEffect(() => {
    const handleStorageChange = () => {
      const freshUser = auth.getCurrentUser();
      setDisplayUser(freshUser);
    };

    window.addEventListener('storage', handleStorageChange);
    
    window.addEventListener('userDataUpdated', handleStorageChange);

    return () => {
      window.removeEventListener('storage', handleStorageChange);
      window.removeEventListener('userDataUpdated', handleStorageChange);
    };
  }, []);

  const handleLogout = () => {
    localStorage.removeItem('tripsplit_user');
    navigate('/login');
    window.location.reload();
  };

  const isActive = (path) => {
    return location.pathname === path ? 'active' : '';
  };

  const userToDisplay = displayUser || currentUser;
  const isAdmin = userToDisplay?.email === 'admin@example.com';
  // In demo mode, show the logged-out nav (Sign In / Sign Up) so visitors can convert,
  // even though a demo session is active behind the scenes.
  const showAccountNav = userToDisplay && !isDemoMode();

  return (
    <nav className="navbar navbar-expand-lg navbar-dark bg-primary shadow-sm">
      <div className="container">
        <Link className="navbar-brand fw-bold" to={userToDisplay ? (isAdmin ? "/admin" : "/groups") : "/"}>
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
          {showAccountNav ? (
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
                    <div className={`text-white rounded-circle d-flex align-items-center justify-content-center me-2 ${isAdmin ? 'bg-danger' : 'text-primary'}`} style={{width: '32px', height: '32px'}}>
                      {isAdmin ? <FaUserShield size={16} /> : <FaUser size={16} />}
                    </div>
                    <span className="d-none d-md-inline">
                      {isAdmin ? 'Admin' : `${userToDisplay?.firstName} ${userToDisplay?.lastName}`}
                    </span>
                  </a>
                  <ul className="dropdown-menu dropdown-menu-end shadow" aria-labelledby="profileDropdown">
                    <li>
                      <div className="dropdown-header">
                        <div className="fw-bold">
                          {isAdmin ? 'Administrator' : `${userToDisplay?.firstName} ${userToDisplay?.lastName}`}
                        </div>
                      </div>
                    </li>
                    <li><hr className="dropdown-divider" /></li>
                    
                    {/* Only show profile settings for non-admin users */}
                    {!isAdmin && (
                      <>
                        <li>
                          <Link className="dropdown-item" to="/profile">
                            <FaCog className="me-2" />
                            Account Settings
                          </Link>
                        </li>
                        <li><hr className="dropdown-divider" /></li>
                      </>
                    )}
                    
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