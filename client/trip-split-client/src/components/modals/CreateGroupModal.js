import { useState } from 'react';
import { Modal, Button } from 'react-bootstrap';
import { FaUsers, FaPlus, FaTimes } from 'react-icons/fa';

function CreateGroupModal({ show, onHide, onSubmit, currentUser }) {
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    members: [{ email: '', name: '' }]
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleMemberChange = (index, field, value) => {
    const updatedMembers = [...formData.members];
    updatedMembers[index][field] = value;
    setFormData(prev => ({ ...prev, members: updatedMembers }));
  };

  const addMember = () => {
    setFormData(prev => ({
      ...prev,
      members: [...prev.members, { email: '', name: '' }]
    }));
  };

  const removeMember = (index) => {
    if (formData.members.length > 1) {
      const updatedMembers = formData.members.filter((_, i) => i !== index);
      setFormData(prev => ({ ...prev, members: updatedMembers }));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Validate form
    if (!formData.name.trim()) {
      setError('Group name is required');
      return;
    }

    // Filter out empty members
    const validMembers = formData.members.filter(member => 
      member.email.trim() && member.name.trim()
    );

    try {
      setLoading(true);
      setError('');
      
      await onSubmit({
        name: formData.name.trim(),
        description: formData.description.trim(),
        members: validMembers,
        createdBy: currentUser.userId
      });
      
      // Reset form
      setFormData({
        name: '',
        description: '',
        members: [{ email: '', name: '' }]
      });
    } catch (err) {
      setError(err.message || 'Failed to create group');
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    setFormData({
      name: '',
      description: '',
      members: [{ email: '', name: '' }]
    });
    setError('');
    onHide();
  };

  return (
    <Modal show={show} onHide={handleClose} size="lg">
      <Modal.Header closeButton>
        <Modal.Title>
          <FaUsers className="me-2" />
          Create New Group
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

          <div className="mb-4">
            <label className="form-label">Description</label>
            <textarea
              className="form-control"
              name="description"
              value={formData.description}
              onChange={handleChange}
              rows="2"
              placeholder="Brief description of the group (optional)"
            />
          </div>

          <div className="mb-3">
            <label className="form-label">Invite Members</label>
            <small className="text-muted d-block mb-2">
              Add people you want to share expenses with (optional - you can invite them later)
            </small>
            
            {formData.members.map((member, index) => (
              <div key={index} className="row mb-2">
                <div className="col-md-5">
                  <input
                    type="text"
                    className="form-control"
                    placeholder="Full Name"
                    value={member.name}
                    onChange={(e) => handleMemberChange(index, 'name', e.target.value)}
                  />
                </div>
                <div className="col-md-5">
                  <input
                    type="email"
                    className="form-control"
                    placeholder="Email Address"
                    value={member.email}
                    onChange={(e) => handleMemberChange(index, 'email', e.target.value)}
                  />
                </div>
                <div className="col-md-2">
                  <button
                    type="button"
                    className="btn btn-outline-danger btn-sm w-100"
                    onClick={() => removeMember(index)}
                    disabled={formData.members.length === 1}
                  >
                    <FaTimes />
                  </button>
                </div>
              </div>
            ))}
            
            <button
              type="button"
              className="btn btn-outline-primary btn-sm"
              onClick={addMember}
            >
              <FaPlus className="me-1" /> Add Another Member
            </button>
          </div>

          <div className="alert alert-info">
            <strong>Tip:</strong> You can always invite more members later using the group's invite code.
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
                Creating...
              </>
            ) : (
              'Create Group'
            )}
          </Button>
        </Modal.Footer>
      </form>
    </Modal>
  );
}

export default CreateGroupModal;