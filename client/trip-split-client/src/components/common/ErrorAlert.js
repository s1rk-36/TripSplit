import { FaExclamationTriangle, FaRedo } from 'react-icons/fa';

function ErrorAlert({ error, onRetry, type = 'danger' }) {
  return (
    <div className={`alert alert-${type} d-flex align-items-center`} role="alert">
      <FaExclamationTriangle className="me-2" />
      <div className="flex-grow-1">
        <strong>Error:</strong> {error}
      </div>
      {onRetry && (
        <button className="btn btn-outline-danger btn-sm ms-2" onClick={onRetry}>
          <FaRedo className="me-1" /> Retry
        </button>
      )}
    </div>
  );
}

export default ErrorAlert;