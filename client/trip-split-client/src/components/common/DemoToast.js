import { useState, useEffect } from 'react';
import { FaLock } from 'react-icons/fa';

// Shows a brief top-center notice when a demo visitor tries a mutating action.
// Driven by the 'demo-notice' event dispatched from showDemoNotice().
function DemoToast() {
  const [message, setMessage] = useState('');

  useEffect(() => {
    let timer;
    const handler = (e) => {
      setMessage(e.detail);
      clearTimeout(timer);
      timer = setTimeout(() => setMessage(''), 4000);
    };
    window.addEventListener('demo-notice', handler);
    return () => {
      window.removeEventListener('demo-notice', handler);
      clearTimeout(timer);
    };
  }, []);

  if (!message) return null;

  return (
    <div
      className="position-fixed start-50 translate-middle-x"
      style={{ top: '1rem', zIndex: 2000, maxWidth: '90%' }}
      role="status"
    >
      <div className="ts-demo-toast">
        <FaLock className="me-2 flex-shrink-0" style={{ color: 'var(--ts-marigold)' }} />
        <span>{message}</span>
      </div>
    </div>
  );
}

export default DemoToast;
