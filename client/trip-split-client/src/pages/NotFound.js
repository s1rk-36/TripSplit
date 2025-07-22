import { Link } from 'react-router-dom';

function NotFound() {
  return (
    <div className="text-center">
      <h2>404 - Page Not Found</h2>
      <p>The page you're looking for doesn't exist.</p>
      <Link to="/" className="btn btn-primary">Return to Home</Link>
    </div>
  );
}

export default NotFound;