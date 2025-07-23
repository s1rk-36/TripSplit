import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { FaPlus, FaUsers, FaDollarSign, FaCalendar, FaEdit, FaTrash, FaEye, FaCopy, FaShare, FaSearch } from 'react-icons/fa';
import Layout from '../components/layout/Layout';
import LoadingSpinner from '../components/common/LoadingSpinner';
import ErrorAlert from '../components/common/ErrorAlert';
import CreateGroupModal from '../components/modals/CreateGroupModal';
import EditGroupModal from '../components/modals/EditGroupModal';
import GroupDetailsModal from '../components/modals/GroupDetailsModal';
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
        console.log(groupData);
      const newGroup = await apiService.createGroup(groupData);
      setGroups([...groups, newGroup]);
      setShowCreateModal(false);
    } catch (err) {
      console.error('Failed to create group:', err);
      throw new Error(err.message || 'Failed to create group');
    }
  };

  const handleEditGroup = async (groupId, groupData) => {
    try {
      const updatedGroup = await apiService.updateGroup(groupId, groupData);
      setGroups(groups.map(group => 
        group.id === groupId ? updatedGroup : group
      ));
      setShowEditModal(false);
      setSelectedGroup(null);
    } catch (err) {
      console.error('Failed to update group:', err);
      throw new Error(err.message || 'Failed to update group');
    }
  };

  const handleDeleteGroup = async (groupId) => {
    if (!window.confirm('Are you sure you want to delete this group? This action cannot be undone.')) {
      return;
    }

    try {
      await apiService.deleteGroup(groupId);
      setGroups(groups.filter(group => group.id !== groupId));
    } catch (err) {
      console.error('Failed to delete group:', err);
      setError(err.message || 'Failed to delete group');
    }
  };

  const handleViewDetails = async (groupId) => {
    try {
      const groupDetails = await apiService.getGroup(groupId);
      setSelectedGroup(groupDetails);
      setShowDetailsModal(true);
    } catch (err) {
      console.error('Failed to load group details:', err);
      setError(err.message || 'Failed to load group details');
    }
  };

  const handleCopyInviteCode = (inviteCode) => {
    navigator.clipboard.writeText(inviteCode);
    // You could add a toast notification here
    alert('Invite code copied to clipboard!');
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

  const totalExpenses = groups.reduce((sum, group) => sum + (group.totalExpenses || 0), 0);
  const totalBalance = groups.reduce((sum, group) => sum + (group.userBalance || 0), 0);

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
        <button 
          className="btn btn-primary"
          onClick={() => setShowCreateModal(true)}
        >
          <FaPlus className="me-1" /> Create Group
        </button>
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
        <div className="col-md-4">
          <select
            className="form-select"
            value={filterStatus}
            onChange={(e) => setFilterStatus(e.target.value)}
          >
            <option value="all">All Groups</option>
            <option value="active">Active Groups</option>
            <option value="settled">Settled Groups</option>
          </select>
        </div>
      </div>

      {/* Quick Stats */}
      {groups.length > 0 && (
        <div className="row mb-4">
          <div className="col-md-3">
            <div className="card text-center">
              <div className="card-body">
                <h4 className="text-primary">{groups.length}</h4>
                <p className="card-text small">Total Groups</p>
              </div>
            </div>
          </div>
          <div className="col-md-3">
            <div className="card text-center">
              <div className="card-body">
                <h4 className="text-success">{formatCurrency(totalExpenses)}</h4>
                <p className="card-text small">Total Expenses</p>
              </div>
            </div>
          </div>
          <div className="col-md-3">
            <div className="card text-center">
              <div className="card-body">
                <h4 className={totalBalance >= 0 ? 'text-success' : 'text-danger'}>
                  {formatCurrency(Math.abs(totalBalance))}
                </h4>
                <p className="card-text small">
                  {totalBalance >= 0 ? 'You are owed' : 'You owe'}
                </p>
              </div>
            </div>
          </div>
          <div className="col-md-3">
            <div className="card text-center">
              <div className="card-body">
                <h4 className="text-info">
                  {groups.reduce((sum, group) => sum + (group.memberCount || 0), 0)}
                </h4>
                <p className="card-text small">Total Members</p>
              </div>
            </div>
          </div>
        </div>
      )}

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
              : 'Create your first group to start splitting expenses with friends'
            }
          </p>
          {(!searchTerm && filterStatus === 'all') && (
            <button 
              className="btn btn-primary btn-lg"
              onClick={() => setShowCreateModal(true)}
            >
              <FaPlus className="me-2" /> Create Your First Group
            </button>
          )}
        </div>
      ) : (
        <div className="row">
          {filteredGroups.map(group => (
            <div key={group.id} className="col-md-6 col-lg-4 mb-4">
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
                          <button 
                            className="dropdown-item"
                            onClick={() => handleViewDetails(group.id)}
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
                      <span className={`badge ${group.isActive ? 'bg-success' : 'bg-secondary'}`}>
                        {group.isActive ? 'Active' : 'Settled'}
                      </span>
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
                    <div className="d-flex align-items-center">
                      <FaCalendar className="text-muted me-2" size={16} />
                      <span className="small">
                        Created {new Date(group.createdAt).toLocaleDateString()}
                      </span>
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
                    <button 
                      className="btn btn-primary btn-sm"
                      onClick={() => handleViewDetails(group.id)}
                    >
                      View Details
                    </button>
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