import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { FaPlus, FaUsers, FaCalendar, FaUser, FaEye, FaEdit, FaTrash, FaSearch, FaFilter, FaCrown, FaUserPlus, FaCopy, FaCheck } from 'react-icons/fa';
import Layout from '../components/layout/Layout';
import LoadingSpinner from '../components/common/LoadingSpinner';
import ErrorAlert from '../components/common/ErrorAlert';
import CreateGroupModal from '../components/modals/CreateGroupModal';
import EditGroupModal from '../components/modals/EditGroupModal';
import GroupDetailsModal from '../components/modals/GroupDetailsModal';
import { apiService } from '../services/apiService';
import { useAuth } from '../utils/auth';
import { isDemoMode, showDemoNotice } from '../services/demoData';
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
  const [copiedGroupId, setCopiedGroupId] = useState(null);
  const [settledMap, setSettledMap] = useState({});
  
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
      loadSettledStates(groupsData || []);
    } catch (err) {
      console.error('Failed to load groups:', err);
      setError(err.message || 'Failed to load groups');
    } finally {
      setLoading(false);
    }
  };

  // A group whose ledger nets to zero gets the SETTLED stamp on its card.
  // Fetched after the cards render so a slow plan never blocks the page.
  const loadSettledStates = async (groupsData) => {
    const results = await Promise.allSettled(
      groupsData.map((g) => apiService.getSettlePlan(g.groupId))
    );
    const map = {};
    results.forEach((r, i) => {
      if (r.status === 'fulfilled' && r.value?.settled) {
        map[groupsData[i].groupId] = true;
      }
    });
    setSettledMap(map);
  };

  const copyInviteCode = async (group) => {
    const code = group.inviteCode || '';
    try {
      await navigator.clipboard.writeText(code);
      setCopiedGroupId(group.groupId);
      // Reset the copied state after 2 seconds
      setTimeout(() => setCopiedGroupId(null), 2000);
    } catch (err) {
      console.error('Failed to copy invite code:', err);
      // Fallback for browsers that don't support clipboard API
      const textArea = document.createElement('textarea');
      textArea.value = code;
      document.body.appendChild(textArea);
      textArea.focus();
      textArea.select();
      try {
        document.execCommand('copy');
        setCopiedGroupId(group.groupId);
        setTimeout(() => setCopiedGroupId(null), 2000);
      } catch (fallbackErr) {
        console.error('Fallback copy failed:', fallbackErr);
      }
      document.body.removeChild(textArea);
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

  const handleJoinGroup = async (inviteCode) => {
    try {
      const joinedGroup = await apiService.joinGroup({ inviteCode });
      setGroups([...groups, joinedGroup]);
      setShowJoinModal(false);
    } catch (err) {
      console.error('Failed to join group:', err);
      throw new Error(err.message || 'Failed to join group');
    }
  };

  const handleEditGroup = async (groupId, updatedGroup) => {
    try {
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
      setGroups(groups.filter(group => group.groupId !== groupId));
    } catch (err) {
      console.error('Failed to delete group:', err);
      setError(err.message || 'Failed to delete group');
    }
  };

  // In demo mode, block mutating actions up front with a notice instead of opening a modal.
  const demoGuard = (action) => () => {
    if (isDemoMode()) {
      showDemoNotice();
      return;
    }
    action();
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
      <div className="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-3">
        <div>
          <div className="ts-eyebrow">Your ledgers</div>
          <h2 className="ts-page-title">Groups</h2>
          <p className="ts-page-sub">Every trip keeps its own tab</p>
        </div>
        <div className="btn-group">
          <button
            className="btn btn-outline-primary"
            onClick={demoGuard(() => setShowJoinModal(true))}
          >
            <FaUserPlus className="me-1" /> Join Group
          </button>
          <button
            className="btn btn-primary"
            onClick={demoGuard(() => setShowCreateModal(true))}
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
          {filteredGroups.map((group, index) => {
            // Check if current user is admin
            const currentUserMembership = group.users?.find(userGroup =>
              userGroup.user.appUserId === currentUser?.userId
            );
            const isCurrentUserAdmin = currentUserMembership?.isGroupAdmin || false;

            return (
              <div key={group.groupId} className={`col-md-6 col-lg-4 mb-4 ts-reveal ts-reveal-d${index % 3}`}>
                <div className={`card h-100 ts-group-card ts-tab-${index % 4}`}>
                  {settledMap[group.groupId] && (
                    <span className="ts-card-stamp" title="All balances are square">Settled</span>
                  )}
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
                      <p className="card-text text-muted small mb-0">{group.description}</p>
                    )}

                    {/* Signature element: invite code as a perforated ticket stub */}
                    <div className="ts-stub">
                      <div>
                        <span className="ts-eyebrow">Invite code</span>
                        <span className="ts-stub-code">{group.inviteCode || '····'}</span>
                      </div>
                      <button
                        className={`btn btn-sm ${copiedGroupId === group.groupId ? 'btn-success' : 'btn-outline-secondary'}`}
                        onClick={() => copyInviteCode(group)}
                        title={copiedGroupId === group.groupId ? 'Copied!' : 'Copy invite code'}
                      >
                        {copiedGroupId === group.groupId ? <FaCheck size={10} /> : <FaCopy size={10} />}
                        <span className="ms-1">{copiedGroupId === group.groupId ? 'Copied' : 'Copy'}</span>
                      </button>
                    </div>

                  </div>
                  
                  <div className="card-footer d-flex justify-content-between align-items-center">
                    <Link
                      to={`/expenses?group=${group.groupId}`}
                      className="btn btn-primary btn-sm"
                    >
                      <FaEye className="me-1" /> View expenses
                    </Link>

                    <div className="d-flex gap-1">
                      <button
                        className="btn btn-outline-secondary btn-sm"
                        onClick={() => {
                          setSelectedGroup(group);
                          setShowDetailsModal(true);
                        }}
                        title="Group details"
                      >
                        <FaUsers />
                      </button>

                      {/* Only show Edit/Delete for admins */}
                      {isCurrentUserAdmin && (
                        <>
                          <button
                            className="btn btn-outline-secondary btn-sm"
                            onClick={demoGuard(() => {
                              setSelectedGroup(group);
                              setShowEditModal(true);
                            })}
                            title="Edit group"
                          >
                            <FaEdit />
                          </button>
                          <button
                            className="btn btn-outline-danger btn-sm"
                            onClick={demoGuard(() => handleDeleteGroup(group.groupId))}
                            title="Delete group"
                          >
                            <FaTrash />
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