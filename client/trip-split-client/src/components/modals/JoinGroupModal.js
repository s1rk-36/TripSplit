import { useState } from 'react';
import { Modal, Button } from 'react-bootstrap';
import { FaUserPlus, FaKey, FaUsers } from 'react-icons/fa';

function JoinGroupModal({ show, onHide, onSubmit }) {
  const [inviteCode, setInviteCode] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Validate invite code
    if (!inviteCode.trim()) {
      setError('Invite code is required');
      return;
    }

    try {
      setLoading(true);
      setError('');
      
      await onSubmit(inviteCode.trim().toUpperCase());
      
      // Reset form
      setInviteCode('');
    } catch (err) {
      setError(err.message || 'Failed to join group');
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    setInviteCode('');
    setError('');
    onHide();
  };

  const handleInputChange = (e) => {
    // Convert to uppercase and remove spaces
    const value = e.target.value.toUpperCase().replace(/\s/g, '');
    setInviteCode(value);
  };

  return (
    <Modal show={show} onHide={handleClose} size="md">
      <Modal.Header closeButton>
        <Modal.Title>
          <FaUserPlus className="me-2" />
          Join a Group
        </Modal.Title>
      </Modal.Header>
      
      <form onSubmit={handleSubmit}>
        <Modal.Body>
          {error && (
            <div className="alert alert-danger">{error}</div>
          )}

          <div className="text-center mb-4">
            <FaUsers className="text-primary mb-3" size={48} />
            <h5>Enter Invite Code</h5>
            <p className="text-muted">
              Ask a group member for the invite code to join their expense group
            </p>
          </div>

          <div className="mb-3">
            <label className="form-label">
              <FaKey className="me-2" />
              Invite Code *
            </label>
            <input
              type="text"
              className="form-control form-control-lg text-center"
              value={inviteCode}
              onChange={handleInputChange}
              maxLength={20}
              style={{ 
                fontSize: '1.2rem', 
                fontWeight: 'bold', 
                letterSpacing: '2px',
                textTransform: 'uppercase'
              }}
              required
              autoFocus
            />
          </div>

        </Modal.Body>
        
        <Modal.Footer>
          <Button variant="secondary" onClick={handleClose}>
            Cancel
          </Button>
          <Button 
            variant="primary" 
            type="submit"
            disabled={loading || !inviteCode.trim()}
          >
            {loading ? (
              <>
                <span className="spinner-border spinner-border-sm me-2"></span>
                Joining...
              </>
            ) : (
              <>
                <FaUserPlus className="me-1" />
                Join Group
              </>
            )}
          </Button>
        </Modal.Footer>
      </form>
    </Modal>
  );
}

export default JoinGroupModal;