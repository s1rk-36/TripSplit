import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import Layout from '../components/layout/Layout';
import LoadingSpinner from '../components/common/LoadingSpinner';
import ErrorAlert from '../components/common/ErrorAlert';
import CreateGroupModal from '../components/modals/CreateGroupModal';
import EditGroupModal from '../components/modals/EditGroupModal';
import GroupDetailsModal from '../components/modals/GroupDetailsModal';
import JoinGroupModal from '../components/modals/JoinGroupModal';
import { FaPlus, FaUsers, FaDollarSign, FaEdit, FaTrash, FaEye, FaCopy, FaShare, FaSearch, FaUserPlus, FaReceipt } from 'react-icons/fa';
import { apiService } from '../services/apiService';
import { useAuth } from '../utils/auth';
import { formatCurrency } from '../utils/helpers';

function Groups() {
  const { currentUser } = useAuth();
  const [groups, setGroups] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  const [filterStatus, setFilterStatus] = useState('all');

  // Modal states
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showJoinModal, setShowJoinModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showDetailsModal, setShowDetailsModal] = useState(false);
  const [selectedGroup, setSelectedGroup] = useState(null);

  useEffect(() => {
    loadGroups();
  }, []);

  const loadGroups = async () => {
    try {
      setLoading(true);
      setError('');

      const groupsData = await apiService.getGroups();
      setGroups(groupsData || []);

    } catch (err) {
      console.error('Failed to load groups:', err);
      setError(err.message || 'Failed to load groups');
    } finally {
      setLoading(false);
    }
  };

  const handleCreateGroup = async (groupData) => {
    try {
      const newGroup = await apiService.createGroup(groupData);
      setGroups([...groups, newGroup]);
      setShowCreateModal(false);
    } catch (err) {
      console.error('Failed to create group:', err);
      throw new Error(err.message || 'Failed to create group');
    }
  };

  const handleJoinGroup = async (groupId) => {
    try {
      const joinedGroup = await apiService.joinGroup({groupId});
      setGroups([...groups, joinedGroup]);
      setShowJoinModal(false);
    } catch (err) {
      console.error('Failed to join group:', err);
      throw new Error(err.message || 'Failed to join group');
    }
  };

  const handleEditGroup = async (groupId, groupData) => {
    try {
      const updatedGroup = await apiService.updateGroup(groupId, groupData);
      setGroups(groups.map(group => 
        group.groupId === groupId ? updatedGroup : group
      ));
      setShowEditModal(false);
      setSelectedGroup(null);
    } catch (err) {
      console.error('Failed to update group:', err);
      throw new Error(err.message || 'Failed to update group');
    }
  };

  const handleDeleteGroup = async (groupId) => {
    if (!window.confirm('Are you sure you want to delete this group? This action cannot be undone and will remove all expenses and data associated with this group.')) {
      return;
    }

    try {
      await apiService.deleteGroup(groupId);
      setGroups(groups.filter(group => group.groupId !== groupId));
    } catch (err) {
      console.error('Failed to delete group:', err);
      setError(err.message || 'Failed to delete group');
    }
  };

  const handleLeaveGroup = async (groupId) => {
    if (!window.confirm('Are you sure you want to leave this group? You will lose access to all group expenses and data.')) {
      return;
    }

    try {
      await apiService.leaveGroup(groupId);
      setGroups(groups.filter(group => group.groupId !== groupId));
    } catch (err) {
      console.error('Failed to leave group:', err);
      setError(err.message || 'Failed to leave group');
    }
  };

  const handleViewDetails = async (groupId) => {
    try {
      console.log("the id is", groupId);
      const groupDetails = await apiService.getGroup(groupId);
      setSelectedGroup(groupDetails);
      setShowDetailsModal(true);
    } catch (err) {
      console.error('Failed to load group details:', err);
      setError(err.message || 'Failed to load group details');
    }
  };

  const handleCopyInviteCode = (groupId) => {
    navigator.clipboard.writeText(groupId.toString());
    alert('Group ID copied to clipboard!');
  };

  // Filter groups based on search and status
  const filteredGroups = groups.filter(group => {
    const matchesSearch = group.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      group.description?.toLowerCase().includes(searchTerm.toLowerCase());

    if (filterStatus === 'all') return matchesSearch;
    if (filterStatus === 'active') return matchesSearch && group.isActive;
    if (filterStatus === 'settled') return matchesSearch && !group.isActive;

    return matchesSearch;
  });


  if (loading) {
    return (
      <Layout>
        <LoadingSpinner message="Loading groups..." />
      </Layout>
    );
  }

  return (
    <Layout>
      {/* Header */}
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2>My Groups</h2>
          <p className="text-muted">Manage your expense groups and track shared costs</p>
        </div>
      </div>

      {error && <ErrorAlert error={error} onRetry={loadGroups} />}

      {/* Search and Filters */}
      <div className="row mb-4">
        <div className="col-md-8">
          <div className="input-group">
            <span className="input-group-text">
              <FaSearch />
            </span>
            <input
              type="text"
              className="form-control"
              placeholder="Search groups..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
        </div>
        
        <div className="btn-group col-md-4">
          <button 
            className="btn btn-outline-primary"
            onClick={() => setShowJoinModal(true)}
          >
            <FaUserPlus className="me-1" /> Join Group
          </button>
          <button
            className="btn btn-primary"
            onClick={() => setShowCreateModal(true)}
          >
            <FaPlus className="me-1" /> Create Group
          </button>
        </div>

      </div>

      {/* Groups List */}
      {filteredGroups.length === 0 ? (
        <div className="text-center py-5">
          <FaUsers className="text-muted mb-3" size={64} />
          <h4>
            {searchTerm || filterStatus !== 'all' ? 'No groups found' : 'No Groups Yet'}
          </h4>
          <p className="text-muted mb-4">
            {searchTerm || filterStatus !== 'all'
              ? 'Try adjusting your search or filters'
              : 'Create your first group or join an existing one to start splitting expenses'
            }
          </p>
          {(!searchTerm && filterStatus === 'all') && (
            <div className="d-flex gap-2 justify-content-center">
              <button
                className="btn btn-outline-primary btn-lg"
                onClick={() => setShowJoinModal(true)}
              >
                <FaUserPlus className="me-2" /> Join a Group
              </button>
              <button
                className="btn btn-primary btn-lg"
                onClick={() => setShowCreateModal(true)}
              >
                <FaPlus className="me-2" /> Create Your First Group
              </button>
            </div>
          )}
        </div>
      ) : (
        <div className="row">
          {filteredGroups.map(group => (
            <div key={group.groupId} className="col-md-6 col-lg-4 mb-4">
              <div className="card h-100 shadow-sm">
                <div className="card-body">
                  <div className="d-flex justify-content-between align-items-start mb-3">
                    <h5 className="card-title">{group.name}</h5>
                    <div className="dropdown">
                      <button
                        className="btn btn-sm btn-outline-secondary"
                        type="button"
                        data-bs-toggle="dropdown"
                        aria-expanded="false"
                      >
                        ⋮
                      </button>
                      <ul className="dropdown-menu">
                        <li>
                          <Link
                            className="dropdown-item"
                            to={`/expenses?group=${group.groupId}`}
                          >
                            <FaReceipt className="me-2" /> View Expenses
                          </Link>
                        </li>
                        <li>
                          <button
                            className="dropdown-item"
                            onClick={() => handleViewDetails(group.groupId)}
                          >
                            <FaEye className="me-2" /> View Details
                          </button>
                        </li>
                        <li>
                          <button
                            className="dropdown-item"
                            onClick={() => {
                              setSelectedGroup(group);
                              setShowEditModal(true);
                            }}
                          >
                            <FaEdit className="me-2" /> Edit
                          </button>
                        </li>
                        <li>
                          <button
                            className="dropdown-item"
                            onClick={() => handleCopyInviteCode(group.inviteCode)}
                          >
                            <FaCopy className="me-2" /> Copy Invite Code
                          </button>
                        </li>
                        <li><hr className="dropdown-divider" /></li>
                        <li>
                          <button
                            className="dropdown-item text-danger"
                            onClick={() => handleDeleteGroup(group.id)}
                          >
                            <FaTrash className="me-2" /> Delete
                          </button>
                        </li>
                      </ul>
                    </div>
                  </div>

                  <p className="card-text text-muted small mb-3">
                    {group.description}
                  </p>

                  {/* Group Stats */}
                  <div className="mb-3">
                    <div className="d-flex align-items-center justify-content-between mb-2">
                      <div className="d-flex align-items-center">
                        <FaUsers className="text-muted me-2" size={16} />
                        <span className="small">{group.memberCount || 0} members</span>
                      </div>
                    </div>
                    <div className="d-flex align-items-center justify-content-between mb-2">
                      <div className="d-flex align-items-center">
                        <FaDollarSign className="text-muted me-2" size={16} />
                        <span className="small">{formatCurrency(group.totalExpenses || 0)} total</span>
                      </div>
                      {group.userBalance !== undefined && (
                        <span className={`small ${group.userBalance >= 0 ? 'text-success' : 'text-danger'}`}>
                          {group.userBalance >= 0 ? 'Owed: ' : 'Owes: '}
                          {formatCurrency(Math.abs(group.userBalance))}
                        </span>
                      )}
                    </div>
                  </div>
                  
                  {/* Recent Activity */}
                  {group.recentActivity && (
                    <div className="mb-3">
                      <small className="text-muted">Recent:</small>
                      <div className="small text-truncate">
                        {group.recentActivity}
                      </div>
                    </div>
                  )}
                </div>

                <div className="card-footer bg-transparent">
                  <div className="d-grid gap-2">
                    <Link
                      to={`/expenses?group=${group.groupId}`}
                      className="btn btn-primary btn-sm"
                    >
                      View Expenses
                    </Link>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Modals */}
      <CreateGroupModal
        show={showCreateModal}
        onHide={() => setShowCreateModal(false)}
        onSubmit={handleCreateGroup}
        currentUser={currentUser}
      />

      <JoinGroupModal
        show={showJoinModal}
        onHide={() => setShowJoinModal(false)}
        onSubmit={handleJoinGroup}
      />

      <EditGroupModal
        show={showEditModal}
        onHide={() => {
          setShowEditModal(false);
          setSelectedGroup(null);
        }}
        group={selectedGroup}
        onSubmit={handleEditGroup}
      />

      <GroupDetailsModal
        show={showDetailsModal}
        onHide={() => {
          setShowDetailsModal(false);
          setSelectedGroup(null);
        }}
        group={selectedGroup}
        onEdit={(group) => {
          setShowDetailsModal(false);
          setSelectedGroup(group);
          setShowEditModal(true);
        }}
        onDelete={handleDeleteGroup}
      />
    </Layout>
  );
}

export default Groups;