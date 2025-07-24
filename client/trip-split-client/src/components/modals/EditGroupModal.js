import { useState, useEffect } from 'react';
import { Modal, Button } from 'react-bootstrap';
import { FaUsers, FaPlus, FaTimes, FaEdit } from 'react-icons/fa';

function EditGroupModal({ show, onHide, group, onSubmit }) {
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    isActive: true
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (group) {
      setFormData({
        name: group.name || '',
        description: group.description || '',
        isActive: group.isActive !== undefined ? group.isActive : true
      });
    }
  }, [group]);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({ 
      ...prev, 
      [name]: type === 'checkbox' ? checked : value 
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Validate form
    if (!formData.name.trim()) {
      setError('Group name is required');
      return;
    }

    try {
      setLoading(true);
      setError('');
      
      await onSubmit(group.id, {
        name: formData.name.trim(),
        description: formData.description.trim(),
        isActive: formData.isActive
      });
      
    } catch (err) {
      setError(err.message || 'Failed to update group');
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    setError('');
    onHide();
  };

  return (
    <Modal show={show} onHide={handleClose} size="lg">
      <Modal.Header closeButton>
        <Modal.Title>
          <FaEdit className="me-2" />
          Edit Group
        </Modal.Title>
      </Modal.Header>
      
      <form onSubmit={handleSubmit}>
        <Modal.Body>
          {error && (
            <div className="alert alert-danger">{error}</div>
          )}

          <div className="mb-3">
            <label className="form-label">Group Name *</label>
            <input
              type="text"
              className="form-control"
              name="name"
              value={formData.name}
              onChange={handleChange}
              placeholder="e.g. Japan Trip 2024"
              required
            />
          </div>

          <div className="mb-3">
            <label className="form-label">Description</label>
            <textarea
              className="form-control"
              name="description"
              value={formData.description}
              onChange={handleChange}
              rows="3"
              placeholder="Brief description of the group"
            />
          </div>

          <div className="mb-3">
            <div className="form-check">
              <input
                type="checkbox"
                className="form-check-input"
                id="isActive"
                name="isActive"
                checked={formData.isActive}
                onChange={handleChange}
              />
              <label className="form-check-label" htmlFor="isActive">
                Active Group
              </label>
              <small className="form-text text-muted d-block">
                Inactive groups are considered settled and won't appear in active filters
              </small>
            </div>
          </div>

          {group && (
            <div className="alert alert-info">
              <h6>Group Information</h6>
              <div className="row">
                <div className="col-md-6">
                  <small className="text-muted">
                    <strong>Created:</strong> {new Date(group.createdAt).toLocaleDateString()}
                  </small>
                </div>
                <div className="col-md-6">
                  <small className="text-muted">
                    <strong>Invite Code:</strong> {group.inviteCode}
                  </small>
                </div>
                <div className="col-md-6">
                  <small className="text-muted">
                    <strong>Members:</strong> {group.memberCount || group.members?.length || 0}
                  </small>
                </div>
                <div className="col-md-6">
                  <small className="text-muted">
                    <strong>Total Expenses:</strong> ${(group.totalExpenses || 0).toFixed(2)}
                  </small>
                </div>
              </div>
            </div>
          )}

          <div className="alert alert-warning">
            <strong>Note:</strong> You can manage group members from the group details page. 
            Only the group creator can add or remove members.
          </div>
        </Modal.Body>
        
        <Modal.Footer>
          <Button variant="secondary" onClick={handleClose}>
            Cancel
          </Button>
          <Button 
            variant="primary" 
            type="submit"
            disabled={loading}
          >
            {loading ? (
              <>
                <span className="spinner-border spinner-border-sm me-2"></span>
                Updating...
              </>
            ) : (
              'Update Group'
            )}
          </Button>
        </Modal.Footer>
      </form>
    </Modal>
  );
}

export default EditGroupModal;