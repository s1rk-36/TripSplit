import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../utils/auth';
import { isDemoMode, endDemo } from '../../services/demoData';

// Thin banner shown across all pages while in demo mode. Subscribing to auth
// context makes it re-render when the demo session starts/ends.
function DemoBanner() {
  const navigate = useNavigate();
  useAuth();

  if (!isDemoMode()) return null;

  const exit = () => {
    endDemo();
    navigate('/');
  };

  return (
    <div className="ts-demo-banner text-center py-2 px-3 small">
      <strong>Demo mode:</strong> exploring TripSplit with sample data. Changes aren&apos;t saved.
      <button onClick={exit} className="btn btn-sm btn-dark ms-2 py-0 px-2">Exit demo</button>
    </div>
  );
}

export default DemoBanner;
