import { FaExclamationTriangle, FaRedo, FaTimes } from 'react-icons/fa';

function ErrorAlert({ error, onRetry, onDismiss, type = 'danger' }) {
  
  // Handle different error types
  let displayError = error;
  
  if (typeof error === 'object') {
    if (error instanceof Error && error.message) {
      displayError = error.message;
      console.log('Using error.message:', displayError);
    } else if (error.message) {
      displayError = error.message;
      console.log('Using object.message:', displayError);
    } else if (Array.isArray(error)) {
      displayError = error.join(', ');
      console.log('Using joined array:', displayError);
    } else {
      displayError = JSON.stringify(error);
      console.log('Using stringified object:', displayError);
    }
  } else if (typeof error === 'string') {
    displayError = error;
    console.log('Using string directly:', displayError);
  }

  console.log('Final displayError:', displayError);

  return (
    <div className={`alert alert-${type} alert-dismissible d-flex align-items-start`} role="alert">
      <FaExclamationTriangle className="me-2 mt-1 flex-shrink-0" />
      <div className="flex-grow-1">
        <strong>Error:</strong> 
        <div className="mt-1">
          {displayError}
        </div>
      </div>
      <div className="d-flex gap-2 ms-2">
        {onRetry && (
          <button className="btn btn-outline-danger btn-sm" onClick={onRetry}>
            <FaRedo className="me-1" /> Retry
          </button>
        )}
        {onDismiss && (
          <button 
            type="button" 
            className="btn-close btn-close-white" 
            onClick={onDismiss}
            aria-label="Close"
          ></button>
        )}
      </div>
    </div>
  );
}

export default ErrorAlert;