import { Navigate } from 'react-router-dom';
import { useAuth } from '../../utils/auth';

// Guards authenticated pages: unauthenticated users are redirected to /login
// instead of rendering the page and only bouncing once an API call fails.
function ProtectedRoute({ children }) {
  const { isAuthenticated } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return children;
}

export default ProtectedRoute;
