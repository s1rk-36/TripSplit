import { useState, useEffect } from 'react';
import { FaUsers, FaUserShield, FaTrash, FaEdit, FaCrown, FaUserMinus, FaEye, FaUserPlus } from 'react-icons/fa';
import Layout from '../components/layout/Layout';
import LoadingSpinner from '../components/common/LoadingSpinner';
import ErrorAlert from '../components/common/ErrorAlert';
import EditGroupModal from '../components/modals/EditGroupModal';
import { apiService } from '../services/apiService';
import { useAuth } from '../utils/auth';

function AdminDashboard() {
  const { currentUser } = useAuth();
  const [loading, setLoading] = useState(false);
  const [users, setUsers] = useState([]);
  const [groups, setGroups] = useState([]);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [activeTab, setActiveTab] = useState('users');
  const [showEditModal, setShowEditModal] = useState(false);
  const [selectedGroup, setSelectedGroup] = useState(null);

  useEffect(() => {
    // Check if user has admin credentials (email check)
    if (currentUser?.email !== 'admin@example.com') {
      console.log('Access denied: Not admin credentials');
      window.location.href = '/groups';
      return;
    }
    
    loadData();
  }, [currentUser]);

  const loadData = async () => {
    setLoading(true);
    try {
      const [usersData, groupsData] = await Promise.all([
        apiService.getAllUsers('/admin/user'),
        apiService.getAllGroups('/admin/groups')
      ]);
      setUsers(usersData);
      setGroups(groupsData);
    } catch (err) {
      console.error('Failed to load admin data:', err);
      setError(err.message || 'Failed to load admin data');
    } finally {
      setLoading(false);
    }
  };

  const deleteUser = async (userId) => {
    if (!window.confirm('Are you sure you want to delete this user? This cannot be undone.')) return;
    
    try {
      await apiService.deleteUser(userId);
      setSuccess('User deleted successfully');
      loadData();
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      setError(err.message || 'Failed to delete user');
    }
  };

  const deleteGroup = async (groupId) => {
    if (!window.confirm('Are you sure you want to delete this group? This cannot be undone.')) return;
    
    try {
      await apiService.deleteGroup(groupId);
      setSuccess('Group deleted successfully');
      loadData();
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      setError(err.message || 'Failed to delete group');
    }
  };

  const handleEditGroup = async (groupId, updatedGroup) => {
    try {
      console.log('Updating group:', updatedGroup);
      const result = await apiService.updateGroup(groupId, updatedGroup);
      
      setSuccess('Group updated successfully');
      loadData();
      setShowEditModal(false);
      setSelectedGroup(null);
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      console.error('Failed to update group:', err);
      setError(err.message || 'Failed to update group');
    }
  };

  const isUserAdmin = (user) => {
    return user.roles && user.roles.includes('ADMIN');
  };

  const getAdminCount = () => {
    return users.filter(user => isUserAdmin(user)).length;
  };

  // Show loading while checking credentials
  if (!currentUser) {
    return (
      <Layout>
        <LoadingSpinner message="Checking permissions..." />
      </Layout>
    );
  }

  if (loading) {
    return (
      <Layout>
        <LoadingSpinner message="Loading admin dashboard..." />
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="container-fluid">
        {/* Header */}
        <div className="d-flex align-items-center mb-4">
          <div className="bg-danger text-white rounded-circle d-flex align-items-center justify-content-center me-3" style={{width: '48px', height: '48px'}}>
            <FaUserShield size={24} />
          </div>
          <div>
            <h2 className="mb-0">Admin Dashboard</h2>
          </div>
        </div>

        {error && <ErrorAlert error={error} onDismiss={() => setError('')} />}
        {success && (
          <div className="alert alert-success alert-dismissible fade show">
            {success}
            <button type="button" className="btn-close" onClick={() => setSuccess('')}></button>
          </div>
        )}

        {/* Stats Cards */}
        <div className="row mb-4">
          <div className="col-md-3">
            <div className="card text-white bg-primary">
              <div className="card-body">
                <div className="d-flex align-items-center">
                  <FaUsers size={32} className="me-3" />
                  <div>
                    <h5 className="card-title mb-0">Total Users</h5>
                    <h3 className="mb-0">{users.length}</h3>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div className="col-md-3">
            <div className="card text-white bg-success">
              <div className="card-body">
                <div className="d-flex align-items-center">
                  <FaUsers size={32} className="me-3" />
                  <div>
                    <h5 className="card-title mb-0">Total Groups</h5>
                    <h3 className="mb-0">{groups.length}</h3>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div className="col-md-3">
            <div className="card text-white bg-info">
              <div className="card-body">
                <div className="d-flex align-items-center">
                  <FaUserPlus size={32} className="me-3" />
                  <div>
                    <h5 className="card-title mb-0">Regular Users</h5>
                    <h3 className="mb-0">{users.length - getAdminCount()}</h3>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Tabs */}
        <ul className="nav nav-tabs mb-4">
          <li className="nav-item">
            <button 
              className={`nav-link text-black ${activeTab === 'users' ? 'active' : ''}`}
              onClick={() => setActiveTab('users')}
            >
              <FaUsers className="me-1" /> Users Management
            </button>
          </li>
          <li className="nav-item">
            <button 
              className={`nav-link text-black ${activeTab === 'groups' ? 'active' : ''}`}
              onClick={() => setActiveTab('groups')}
            >
              <FaUsers className="me-1" /> Groups Management
            </button>
          </li>
        </ul>

        {/* Users Tab */}
        {activeTab === 'users' && (
          <div className="card">
            <div className="card-header">
              <h5 className="mb-0">Users Management</h5>
            </div>
            <div className="card-body">
              <div className="table-responsive">
                <table className="table table-hover">
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Name</th>
                      <th>Email</th>
                      <th>Username</th>
                      <th>Roles</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {users.map(user => (
                      <tr key={user.appUserId}>
                        <td>{user.appUserId}</td>
                        <td>{user.firstName} {user.lastName}</td>
                        <td>
                          {user.email}
                          {user.email === 'admin@example.com' && (
                            <span className="badge bg-danger ms-2">MODERATOR</span>
                          )}
                        </td>
                        <td>{user.username}</td>
                        <td>
                          <div className="d-flex gap-1">
                            {user.roles?.map(role => (
                              <span 
                                key={role} 
                                className={`badge ${role === 'ADMIN' ? 'bg-danger' : 'bg-secondary'}`}
                              >
                                {role === 'ADMIN' && <FaCrown className="me-1" size={10} />}
                                {role}
                              </span>
                            ))}
                          </div>
                        </td>
  
                        <td>
                          <div className="btn-group btn-group-sm">
                            <button 
                              className="btn btn-outline-danger"
                              onClick={() => deleteUser(user.appUserId)}
                              title="Delete User"
                              hidden={user.email === 'admin@example.com'} // Can't delete super admin
                            >
                              <FaTrash />
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        )}

        {/* Groups Tab */}
        {activeTab === 'groups' && (
          <div className="card">
            <div className="card-header">
              <h5 className="mb-0">Groups Management</h5>
            </div>
            <div className="card-body">
              <div className="table-responsive">
                <table className="table table-hover">
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Name</th>
                      <th>Description</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {groups.map(group => (
                      <tr key={group.groupId || group.id}>
                        <td>{group.groupId || group.id}</td>
                        <td>{group.name}</td>
                        <td>{group.description || 'No description'}</td>
                        <td>
                          <div className="btn-group btn-group-sm">
                            <button 
                              className="btn btn-outline-primary"
                              onClick={() => {
                                setSelectedGroup(group);
                                setShowEditModal(true);
                              }}
                              title="Edit Group"
                            >
                              <FaEdit />
                            </button>
                            <button 
                              className="btn btn-outline-danger"
                              onClick={() => deleteGroup(group.groupId || group.id)}
                              title="Delete Group"
                            >
                              <FaTrash />
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Edit Group Modal */}
      <EditGroupModal
        show={showEditModal}
        onHide={() => {
          setShowEditModal(false);
          setSelectedGroup(null);
        }}
        group={selectedGroup}
        onSubmit={handleEditGroup}
      />
    </Layout>
  );
}

export default AdminDashboard;