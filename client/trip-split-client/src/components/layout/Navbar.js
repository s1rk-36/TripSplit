import { Link } from 'react-router-dom';
import { FaDollarSign, FaHome, FaUsers, FaReceipt, FaSignOutAlt, FaCog } from 'react-icons/fa';
import { useAuth } from '../../utils/auth';

function Navbar() {
  const { currentUser, logout, isAuthenticated } = useAuth();

  const handleLogout = () => {
    if (window.confirm('Are you sure you want to logout?')) {
      logout();
    }
  };

  // Get user initials for profile icon
  const getUserInitials = () => {
    // if (!currentUser) return 'U';
    const firstInitial = currentUser.firstName ? currentUser.firstName.charAt(0).toUpperCase() : '';
    const lastInitial = currentUser.lastName ? currentUser.lastName.charAt(0).toUpperCase() : '';
    return firstInitial + lastInitial || currentUser.email?.charAt(0).toUpperCase();
  };

  return (
    <nav className="navbar navbar-expand-lg navbar-light bg-white shadow-sm">
      <div className="container">
        {/* Brand - always visible */}
        <Link 
          className="navbar-brand d-flex align-items-center" 
          to={isAuthenticated ? "/dashboard" : "/"}
        >
          <FaDollarSign className="me-2 text-primary" size={32} />
          <span className="fw-bold text-primary fs-3">TripSplit</span>
        </Link>
        
        <button 
          className="navbar-toggler" 
          type="button" 
          data-bs-toggle="collapse" 
          data-bs-target="#navbarNav"
          aria-controls="navbarNav"
          aria-expanded="false"
          aria-label="Toggle navigation"
        >
          <span className="navbar-toggler-icon"></span>
        </button>
        
        <div className="collapse navbar-collapse" id="navbarNav">
          {/* Authenticated Navigation */}
          {isAuthenticated ? (
            <>
              {/* Left side*/}
              <ul className="navbar-nav me-auto">
              </ul>
              
              {/* Right side - User profile */}
              <ul className="navbar-nav">
                <li className="nav-item dropdown">
                  <a 
                    className="nav-link dropdown-toggle d-flex align-items-center text-dark" 
                    href="#" 
                    role="button" 
                    data-bs-toggle="dropdown"
                    aria-expanded="false"
                    style={{ cursor: 'pointer' }}
                  >
                    {/* Profile Icon with Initials */}
                    <div 
                      className="rounded-circle bg-primary text-white d-flex align-items-center justify-content-center me-2"
                      style={{ 
                        width: '32px', 
                        height: '32px', 
                        fontSize: '14px', 
                        fontWeight: 'bold' 
                      }}
                    >
                      {getUserInitials()}
                    </div>
                    <span className="d-none d-md-inline">
                      {currentUser.firstName} {currentUser.lastName}
                    </span>
                  </a>
                  <ul className="dropdown-menu dropdown-menu-end">
                    <li>
                      <h6 className="dropdown-header">
                        <div className="fw-bold">{currentUser.firstName} {currentUser.lastName}</div>
                        <small className="text-muted">{currentUser.email}</small>
                      </h6>
                    </li>
                    <li><hr className="dropdown-divider" /></li>
                    <li>
                      <Link className="dropdown-item" to="/profile">
                        <FaCog className="me-2" /> Account Settings
                      </Link>
                    </li>
                    <li>
                      <Link className="dropdown-item" to="/dashboard">
                        <FaHome className="me-2" /> Dashboard
                      </Link>
                    </li>
                    <li><hr className="dropdown-divider" /></li>
                    <li>
                      <button 
                        className="dropdown-item text-danger" 
                        onClick={handleLogout}
                        style={{ border: 'none', background: 'none', width: '100%', textAlign: 'left' }}
                      >
                        <FaSignOutAlt className="me-2" /> Logout
                      </button>
                    </li>
                  </ul>
                </li>
              </ul>
            </>
          ) : (
            /* Unauthenticated Navigation */
            <>
              <ul className="navbar-nav me-auto">
                {/* Empty - pushes login/signup to the right */}
              </ul>
              
              {/* Right side - Login/Register buttons */}
              <div className="d-flex gap-2">
                <Link to="/login" className="btn btn-outline-primary">
                  Login
                </Link>
                <Link to="/register" className="btn btn-primary">
                  Sign Up
                </Link>
              </div>
            </>
          )}
        </div>
      </div>
    </nav>
  );
}

export default Navbar;