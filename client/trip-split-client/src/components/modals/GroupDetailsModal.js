import { useState, useEffect } from 'react';
import { Modal, Button } from 'react-bootstrap';
import { FaUsers, FaUserMinus, FaCrown, FaUser, FaEnvelope, FaCalendar, FaEdit, FaTrash } from 'react-icons/fa';
import { useAuth } from '../../utils/auth';
import { apiService } from '../../services/apiService';

function GroupDetailsModal({ show, onHide, group, onEdit, onDelete }) {
  const { currentUser } = useAuth();
  const [members, setMembers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (show && group) {
      loadGroupMembers();
    }
  }, [show, group]);

  const loadGroupMembers = async () => {
    if (!group) return;

    try {
      setLoading(true);
      setError('');
      
      const groupMembers = await apiService.getGroupMembers(group.groupId);
      setMembers(groupMembers || []);
      
    } catch (err) {
      console.error('Failed to load group members:', err);
      setError('Failed to load group members');
    } finally {
      setLoading(false);
    }
  };

const handleRemoveMember = async (userId) => {
    if (!window.confirm('Are you sure you want to remove this member from the group?')) {
      return;
    }

    try {
      setLoading(true);
      console.log(group);
      console.log(userId);
      await apiService.removeGroupMember(group.groupId, userId);
      
      // Remove from local state
      setMembers(members.filter(member => member.user.appUserId !== userId));
      
    } catch (err) {
      console.error('Failed to remove member:', err);
      setError(err.message || 'Failed to remove member');
    } finally {
      setLoading(false);
    }
  };

  if (!group) return null;

  // Check if current user is admin
  const currentUserMembership = members.find(member => 
    member.user.appUserId === currentUser?.userId
  );
  const isCurrentUserAdmin = currentUserMembership?.isGroupAdmin || false;

  return (
    <Modal show={show} onHide={onHide} size="lg">
      <Modal.Header closeButton>
        <Modal.Title>
          <FaUsers className="me-2" />
          {group.name}
        </Modal.Title>
      </Modal.Header>
      
      <Modal.Body>
        {error && (
          <div className="alert alert-danger">{error}</div>
        )}

        {/* Group Info */}
        <div className="mb-4">
          <div className="row">
            <div className="col-md-8">
              <h5>{group.name}</h5>
              {group.description && (
                <p className="text-muted mb-2">{group.description}</p>
              )}
              <div className="d-flex gap-3 text-muted small">
                <span>
                  <FaCalendar className="me-1" />
                  Created {new Date(group.createdAt || Date.now()).toLocaleDateString()}
                </span>
                <span>
                  <FaUsers className="me-1" />
                  {members.length} member{members.length !== 1 ? 's' : ''}
                </span>
              </div>
            </div>
            <div className="col-md-4 text-end">
              {isCurrentUserAdmin && (
                <div className="btn-group">
                  <button
                    className="btn btn-outline-primary btn-sm"
                    onClick={() => {
                      onHide();
                      onEdit(group);
                    }}
                  >
                    <FaEdit className="me-1" /> Edit
                  </button>
                  <button
                    className="btn btn-outline-danger btn-sm"
                    onClick={() => {
                      if (window.confirm('Are you sure you want to delete this group? This action cannot be undone.')) {
                        onHide();
                        onDelete(group.groupId);
                      }
                    }}
                  >
                    <FaTrash className="me-1" /> Delete
                  </button>
                </div>
              )}
            </div>
          </div>
        </div>

        {/* Members List */}
        <div>
          <h6 className="d-flex align-items-center justify-content-between">
            Group Members
            {isCurrentUserAdmin && (
              <small className="text-muted">You can remove members as admin</small>
            )}
          </h6>
          
          {loading ? (
            <div className="text-center py-3">
              <div className="spinner-border spinner-border-sm me-2"></div>
              Loading members...
            </div>
          ) : members.length === 0 ? (
            <div className="text-center py-4">
              <FaUsers className="text-muted mb-2" size={32} />
              <p className="text-muted">No members found</p>
            </div>
          ) : (
            <div className="list-group">
              {members.map(member => {
                const isCurrentUser = member.user.appUserId === currentUser?.userId;
                const isMemberAdmin = member.isGroupAdmin;
                
                return (
                  <div key={member.user.appUserId} className="list-group-item">
                    <div className="d-flex align-items-center justify-content-between">
                      <div className="d-flex align-items-center">
                        <div className="me-3">
                          <div className="bg-primary text-white rounded-circle d-flex align-items-center justify-content-center" 
                               style={{width: '40px', height: '40px'}}>
                            <FaUser size={16} />
                          </div>
                        </div>
                        <div>
                          <div className="d-flex align-items-center">
                            <h6 className="mb-0 me-2">
                              {member.user.firstName} {member.user.lastName}
                            </h6>
                            {isMemberAdmin && (
                              <span className="badge bg-warning text-dark">
                                <FaCrown className="me-1" size={10} />
                                Admin
                              </span>
                            )}
                            {isCurrentUser && (
                              <span className="badge bg-info ms-1">You</span>
                            )}
                          </div>
                          <small className="text-muted">
                            <FaEnvelope className="me-1" size={10} />
                            {member.user.email}
                          </small>
                        </div>
                      </div>
                      
                      <div>
                        {isCurrentUserAdmin && !isCurrentUser && (
                          <button
                            className="btn btn-outline-danger btn-sm"
                            onClick={() => handleRemoveMember(member.userId)}
                            disabled={loading}
                            title="Remove member"
                          >
                            <FaUserMinus size={12} />
                          </button>
                        )}
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>

        {/* Admin Actions */}
        {isCurrentUserAdmin && (
          <div className="mt-4 p-3 bg-light rounded">
            <h6 className="text-muted">Admin Actions</h6>
            <p className="small text-muted mb-2">
              As an admin, you can edit group details, remove members, or delete the group.
            </p>
            <div className="d-flex gap-2">
              <button
                className="btn btn-primary btn-sm"
                onClick={() => {
                  onHide();
                  onEdit(group);
                }}
              >
                <FaEdit className="me-1" /> Edit Group
              </button>
              <button
                className="btn btn-danger btn-sm"
                onClick={() => {
                  if (window.confirm('Are you sure you want to delete this group? This will also delete all expenses in this group. This action cannot be undone.')) {
                    onHide();
                    onDelete(group.groupId || group.id);
                  }
                }}
              >
                <FaTrash className="me-1" /> Delete Group
              </button>
            </div>
          </div>
        )}
      </Modal.Body>
      
      <Modal.Footer>
        <Button variant="secondary" onClick={onHide}>
          Close
        </Button>
      </Modal.Footer>
    </Modal>
  );
}

export default GroupDetailsModal;