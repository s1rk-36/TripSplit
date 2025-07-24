function LoadingSpinner({ message = 'Loading...', size = 'normal' }) {
  const spinnerClass = size === 'small' ? 'spinner-border-sm' : '';
  
  return (
    <div className="d-flex justify-content-center align-items-center py-5">
      <div className="text-center">
        <div className={`spinner-border text-primary ${spinnerClass}`} role="status">
          <span className="visually-hidden">Loading...</span>
        </div>
        <div className="mt-2">{message}</div>
      </div>
    </div>
  );
}

export default LoadingSpinner;