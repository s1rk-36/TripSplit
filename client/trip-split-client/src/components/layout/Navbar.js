import { Link } from 'react-router-dom';
import { FaDollarSign } from 'react-icons/fa';
// temp need to add user auth
function Navbar() {

  return (
          <nav className="navbar navbar-expand-lg navbar-light bg-white shadow-sm">
        <div className="container">
          <Link className="navbar-brand d-flex align-items-center" to="/">
            <FaDollarSign className="me-2 text-primary" size={32} />
            <span className="fw-bold text-primary fs-3">TripSplit</span>
          </Link>
          
          <div className="d-flex gap-2">
            <Link to="/login" className="btn btn-outline-primary">Login</Link>
            <Link to="/register" className="btn btn-primary">Sign Up</Link>
          </div>
        </div>
      </nav>
  );
}

export default Navbar;