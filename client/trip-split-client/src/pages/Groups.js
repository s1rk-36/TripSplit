import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { FaPlus, FaUsers, FaCalendar, FaUser, FaEye, FaEdit, FaTrash, FaSearch, FaFilter, FaCrown, FaUserPlus } from 'react-icons/fa';
import Layout from '../components/layout/Layout';
import LoadingSpinner from '../components/common/LoadingSpinner';
import ErrorAlert from '../components/common/ErrorAlert';
import CreateGroupModal from '../components/modals/CreateGroupModal';
import EditGroupModal from '../components/modals/EditGroupModal';
import GroupDetailsModal from '../components/modals/GroupDetailsModal';
import { apiService } from '../services/apiService';
import { useAuth } from '../utils/auth';
import JoinGroupModal from '../components/modals/JoinGroupModal';

function Groups() {
  const { currentUser } = useAuth();
  const [groups, setGroups] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  const [filterType, setFilterType] = useState('all');
  const [sortBy, setSortBy] = useState('name');
  const [sortOrder, setSortOrder] = useState('asc');
  
  // Modal states
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showDetailsModal, setShowDetailsModal] = useState(false);
  const [selectedGroup, setSelectedGroup] = useState(null);
  const [showJoinModal, setShowJoinModal] = useState(false);

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
      setGroups([newGroup, ...groups]);
      setShowCreateModal(false);
    } catch (err) {
      console.error('Failed to create group:', err);
      throw new Error(err.message || 'Failed to create group');
    }
  };

  const handleJoinGroup = async (groupId) => {
    try {
      const joinedGroup = await apiService.joinGroup({ groupId });
      setGroups([...groups, joinedGroup]);
      setShowJoinModal(false);
    } catch (err) {
      console.error('Failed to join group:', err);
      throw new Error(err.message || 'Failed to join group');
    }
  };

  const handleEditGroup = async (groupId, updatedGroup) => {
    try {
      console.log(updatedGroup);
      const result = await apiService.updateGroup(groupId, updatedGroup);
      
      loadGroups();
      setShowEditModal(false);
      setSelectedGroup(null);
    } catch (err) {
      console.error('Failed to update group:', err);
      setError(err.message || 'Failed to update group');
    }
  };

  const handleDeleteGroup = async (groupId) => {
    if (!window.confirm('Are you sure you want to delete this group? This will also delete all expenses in this group. This action cannot be undone.')) {
      return;
    }

    try {
      await apiService.deleteGroup(groupId);
      setGroups(groups.filter(group => (group.groupId || group.id) !== groupId));
    } catch (err) {
      console.error('Failed to delete group:', err);
      setError(err.message || 'Failed to delete group');
    }
  };

  // Filter and sort groups
  const filteredGroups = groups.filter(group => {
    const matchesSearch = group.name && group.name.toLowerCase().includes(searchTerm.toLowerCase());
    
    let matchesFilter = true;
    if (filterType === 'admin') {
      const currentUserMembership = group.users?.find(userGroup => 
        userGroup.user.appUserId === currentUser?.userId
      );
      matchesFilter = currentUserMembership?.isGroupAdmin || false;
    } else if (filterType === 'member') {
      const currentUserMembership = group.users?.find(userGroup => 
        userGroup.user.appUserId === currentUser?.userId
      );
      matchesFilter = currentUserMembership && !currentUserMembership.isGroupAdmin;
    }
    
    return matchesSearch && matchesFilter;
  }).sort((a, b) => {
    let aValue, bValue;
    
    switch (sortBy) {
      case 'name':
        aValue = a.name ? a.name.toLowerCase() : '';
        bValue = b.name ? b.name.toLowerCase() : '';
        break;
      case 'members':
        aValue = a.users?.length || 0;
        bValue = b.users?.length || 0;
        break;
      case 'date':
        aValue = a.createdAt ? new Date(a.createdAt) : new Date(0);
        bValue = b.createdAt ? new Date(b.createdAt) : new Date(0);
        break;
      default:
        aValue = a.name ? a.name.toLowerCase() : '';
        bValue = b.name ? b.name.toLowerCase() : '';
    }
    
    if (sortOrder === 'asc') {
      return aValue > bValue ? 1 : -1;
    } else {
      return aValue < bValue ? 1 : -1;
    }
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
          <h2>Groups</h2>
          <p className="text-muted">Manage your expense sharing groups</p>
        </div>
                    <div className="btn-group">
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

      {error && <ErrorAlert error={error} onRetry={loadGroups} />}

      {/* Filters */}
      <div className="card mb-4">
        <div className="card-body">
          <div className="row g-3">
            <div className="col-md-4">
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
            <div className="col-md-2">
              <select
                className="form-select"
                value={filterType}
                onChange={(e) => setFilterType(e.target.value)}
              >
                <option value="all">All Groups</option>
                <option value="admin">Admin Of</option>
                <option value="member">Member Of</option>
              </select>
            </div>
            <div className="col-md-3">
              <select
                className="form-select"
                value={`${sortBy}-${sortOrder}`}
                onChange={(e) => {
                  const [field, order] = e.target.value.split('-');
                  setSortBy(field);
                  setSortOrder(order);
                }}
              >
                <option value="name-asc">Name (A-Z)</option>
                <option value="name-desc">Name (Z-A)</option>
                <option value="members-desc">Most Members</option>
                <option value="members-asc">Fewest Members</option>
              </select>
            </div>
            <div className="col-md-3">
              <button
                className="btn btn-outline-secondary w-100"
                onClick={() => {
                  setSearchTerm('');
                  setFilterType('all');
                  setSortBy('name');
                  setSortOrder('asc');
                }}
              >
                <FaFilter className="me-1" /> Clear Filters
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Groups List */}
      {filteredGroups.length === 0 ? (
        <div className="text-center py-5">
          <FaUsers className="text-muted mb-3" size={64} />
          <h4>
            {searchTerm || filterType !== 'all' 
              ? 'No groups found' 
              : 'No Groups Yet'
            }
          </h4>
          <p className="text-muted mb-4">
            {searchTerm || filterType !== 'all'
              ? 'Try adjusting your search or filters'
              : 'Create your first group to start sharing expenses'
            }
          </p>
          {(!searchTerm && filterType === 'all') && (

 
          <button 
            className="btn btn-primary"
            onClick={() => setShowCreateModal(true)}
          >
            <FaPlus className="me-1" /> Create Group
          </button>

          )}
        </div>
      ) : (
        <div className="row">
          {filteredGroups.map(group => {
            // Check if current user is admin
            const currentUserMembership = group.users?.find(userGroup => 
              userGroup.user.appUserId === currentUser?.userId
            );
            const isCurrentUserAdmin = currentUserMembership?.isGroupAdmin || false;

            return (
              <div key={group.groupId || group.id} className="col-md-6 col-lg-4 mb-4">
                <div className="card h-100">
                  <div className="card-body">
                    <div className="d-flex justify-content-between align-items-start mb-2">
                      <h5 className="card-title mb-0">{group.name}</h5>
                      <div className="d-flex gap-1">
                        {/* Admin indicator */}
                        {isCurrentUserAdmin && (
                          <span className="badge bg-warning text-dark" title="You are an admin">
                            <FaCrown size={10} className="me-1" />
                          </span>
                        )}
                        
                        {/* Member count badge */}
                        <span className="badge bg-light text-dark">
                          <FaUsers className="me-1" size={10} />
                          {group.users?.length || 0}
                        </span>
                      </div>
                    </div>
                    
                    {group.description && (
                      <p className="card-text text-muted small mb-3">{group.description}</p>
                    )}
                    
                  </div>
                  
                  <div className="card-footer d-flex justify-content-between align-items-center">
                    <Link 
                      to={`/expenses?group=${group.groupId || group.id}`}
                      className="btn btn-outline-primary btn-sm"
                    >
                      <FaEye className="me-1" /> View Expenses
                    </Link>
                    
                    <div className="btn-group">
                      <button
                        className="btn btn-outline-info btn-sm"
                        onClick={() => {
                          setSelectedGroup(group);
                          setShowDetailsModal(true);
                        }}
                      >
                        <FaUsers className="me-1" /> Details
                      </button>
                      
                      {/* Only show Edit/Delete for admins */}
                      {isCurrentUserAdmin && (
                        <>
                          <button
                            className="btn btn-outline-warning btn-sm"
                            onClick={() => {
                              setSelectedGroup(group);
                              setShowEditModal(true);
                            }}
                          >
                            <FaEdit className="me-1" /> Edit
                          </button>
                          <button
                            className="btn btn-outline-danger btn-sm"
                            onClick={() => handleDeleteGroup(group.groupId || group.id)}
                          >
                            <FaTrash className="me-1" /> Delete
                          </button>
                        </>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            );
          })}
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