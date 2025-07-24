import { FaExclamationTriangle, FaRedo, FaCheck } from 'react-icons/fa';

function ErrorAlert({ error, onRetry, type = 'danger' }) {
  let messages = [];

  if (!error) {
    messages = [];
  } else if (typeof error === 'string') {
    messages = error.split(';').map(msg => msg.trim()).filter(msg => msg.length);
  } else if (typeof error === 'object' && error.message) {
    messages = error.message.split(';').map(msg => msg.trim()).filter(msg => msg.length);
  }
  return (
    <div className={`alert alert-${type} d-flex align-items-start`} role="alert">
      <FaExclamationTriangle className="me-2 mt-1" />
      <div className="flex-grow-1">
        <strong>Error{messages.length > 1 ? 's' : ''}:</strong>
        <ul className="mb-0 mt-1">
          {messages.map((msg, idx) => (
            <li key={idx}>{msg}</li>
          ))}
        </ul>
      </div>
      {onRetry && (
        <button className="btn btn-outline-danger btn-sm ms-3" onClick={onRetry}>
          <FaRedo className="me-1" /> Retry
        </button>
      )}
    </div>
  );
}

export default ErrorAlert;
