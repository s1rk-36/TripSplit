import { useState, useEffect } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { FaPlus, FaReceipt, FaDollarSign, FaCalendar, FaEdit, FaTrash, FaEye, FaFilter, FaSearch, FaUsers, FaUser, FaFileImage, FaArrowLeft, FaBalanceScale, FaExchangeAlt } from 'react-icons/fa';
import Layout from '../components/layout/Layout';
import LoadingSpinner from '../components/common/LoadingSpinner';
import ErrorAlert from '../components/common/ErrorAlert';
import CreateExpenseModal from '../components/modals/CreateExpenseModal';
import SettleUpModal from '../components/modals/SettleUpModal';
import EditExpenseModal from '../components/modals/EditExpenseModal';
import ExpenseDetailsModal from '../components/modals/ExpenseDetailsModal';
import { apiService } from '../services/apiService';
import { useAuth } from '../utils/auth';
import { isDemoMode, showDemoNotice } from '../services/demoData';
import { formatCurrency, getBalanceColor, getBalanceText } from '../utils/helpers';

function Expenses() {
  const { currentUser } = useAuth();
  const [searchParams] = useSearchParams();
  const [expenses, setExpenses] = useState([]);
  const [groups, setGroups] = useState([]);
  const [categories, setCategories] = useState([]);
  const [userCache, setUserCache] = useState({}); // Cache for user data
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  const [filterGroup, setFilterGroup] = useState(searchParams.get('group') || 'all');
  const [filterCategory, setFilterCategory] = useState('all');
  const [filterDateRange, setFilterDateRange] = useState('all');
  const [sortBy, setSortBy] = useState('createdAt');
  const [sortOrder, setSortOrder] = useState('desc');
  
  // Modal states
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showDetailsModal, setShowDetailsModal] = useState(false);
  const [selectedExpense, setSelectedExpense] = useState(null);
  const [showSettleModal, setShowSettleModal] = useState(false);
  const [activity, setActivity] = useState([]);

  useEffect(() => {
    loadExpenses();
    loadGroups();
    loadCategories();
  }, []);

  // Activity feed exists per group, so load it only when one group is in view.
  useEffect(() => {
    if (filterGroup === 'all' || !filterGroup) {
      setActivity([]);
      return;
    }
    loadActivity(filterGroup);
  }, [filterGroup]);

  const loadActivity = async (groupId) => {
    try {
      const items = await apiService.getGroupActivity(groupId);
      setActivity(items || []);
    } catch (err) {
      console.error('Failed to load activity:', err);
      setActivity([]);
    }
  };

  const loadExpenses = async () => {
    try {
      setLoading(true);
      setError('');
      
      let expensesData;
      if (filterGroup !== 'all' && filterGroup) {
        expensesData = await apiService.getGroupExpenses(filterGroup);
      } else {
        expensesData = await apiService.getUserExpenses();
      }
      
      setExpenses(expensesData || []);
      
      if (expensesData && expensesData.length > 0) {
        await loadUserData(expensesData);
      }
      
    } catch (err) {
      console.error('Failed to load expenses:', err);
      setError(err.message || 'Failed to load expenses');
    } finally {
      setLoading(false);
    }
  };

  const loadGroups = async () => {
    try {
      const groupsData = await apiService.getGroups();
      setGroups(groupsData || []);
    } catch (err) {
      console.error('Failed to load groups:', err);
    }
  };

  const loadCategories = async () => {
    const categoriesData = [
      'FOOD',          
      'TRANSPORTATION',
      'LODGING',       
      'ACTIVITIES',    
      'SHOPPING',
      'TRAVEL_FEES',  
      'OTHER'
    ];
    setCategories(categoriesData);
  };

  const handleCreateExpense = async (expenseData) => {
    try {
      const newExpense = await apiService.createExpense(expenseData);
      setExpenses([newExpense, ...expenses]);
      setShowCreateModal(false);

      return newExpense;
    } catch (err) {
      console.error('Failed to create expense:', err);
      throw new Error(err.message || 'Failed to create expense');
    }
  };

  const handleEditExpense = async (expenseId, expenseData) => {
    try {
      const updatedExpense = await apiService.updateExpense(expenseId, expenseData);
      loadExpenses();
      setShowEditModal(false);
      setSelectedExpense(null);
    } catch (err) {
      console.error('Failed to update expense:', err);
      throw new Error(err.message || 'Failed to update expense');
    }
  };

  const handleDeleteExpense = async (expenseId) => {
    if (!window.confirm('Are you sure you want to delete this expense? This action cannot be undone.')) {
      return;
    }

    try {
      await apiService.deleteExpense(expenseId);
      setExpenses(expenses.filter(expense => expense.expenseId !== expenseId));
    } catch (err) {
      console.error('Failed to delete expense:', err);
      setError(err.message || 'Failed to delete expense');
    }
  };

  const handleViewDetails = async (expenseId) => {
    try {
      const expenseDetails = await apiService.getExpense(expenseId);
      setSelectedExpense(expenseDetails);
      setShowDetailsModal(true);
    } catch (err) {
      console.error('Failed to load expense details:', err);
      setError(err.message || 'Failed to load expense details');
    }
  };

  const loadUserData = async (expensesData) => {
    // Get all unique user IDs from expenses
    const userIds = [...new Set(expensesData.map(expense => expense.createdBy))];
    const newUserCache = { ...userCache };
    
    // Fetch user data for each unique user ID
    for (const userId of userIds) {
      if (!newUserCache[userId]) {
        try {
          const userData = await apiService.getUser(userId);
          newUserCache[userId] = `${userData.firstName} ${userData.lastName}`;
        } catch (err) {
          console.error(`Failed to load user ${userId}:`, err);
          newUserCache[userId] = 'Unknown User';
        }
      }
    }
    
    // Update the cache with all user data
    setUserCache(newUserCache);
  };

  const getUserName = (userId) => {
    return userCache[userId];
  };

  const getGroupName = (groupId) => {
    const group = groups.find(g => g.id === groupId || g.groupId === groupId);
    return group ? group.name : 'Unknown Group';
  };

  // In demo mode, block mutating actions up front with a notice instead of opening a modal.
  const demoGuard = (action) => () => {
    if (isDemoMode()) {
      showDemoNotice();
      return;
    }
    action();
  };

  // Filter and sort expenses
  const filteredExpenses = expenses.filter(expense => {
    // Handle search - check name and description
    const matchesSearch = (expense.name && expense.name.toLowerCase().includes(searchTerm.toLowerCase())) ||
                         (expense.description && expense.description.toLowerCase().includes(searchTerm.toLowerCase()));
    
    // Handle group filter
    const matchesGroup = filterGroup === 'all' || (expense.groupId && expense.groupId.toString() === filterGroup);
    
    // Handle category filter
    const matchesCategory = filterCategory === 'all' || expense.category === filterCategory;
    
    // Handle date filter
    let matchesDate = true;
    if (filterDateRange !== 'all') {
      const expenseDate = expense.createdAt ? new Date(expense.createdAt) : null;
      const now = new Date();
      
      if (expenseDate) {
        switch (filterDateRange) {
          case 'today':
            matchesDate = expenseDate.toDateString() === now.toDateString();
            break;
          case 'week':
            const weekAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
            matchesDate = expenseDate >= weekAgo;
            break;
          case 'month':
            const monthAgo = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000);
            matchesDate = expenseDate >= monthAgo;
            break;
          default:
            matchesDate = true;
        }
      }
    }
    
    return matchesSearch && matchesGroup && matchesCategory && matchesDate;
  }).sort((a, b) => {
    let aValue, bValue;
    
    switch (sortBy) {
      case 'createdAt':
        aValue = a.createdAt ? new Date(a.createdAt) : new Date(0);
        bValue = b.createdAt ? new Date(b.createdAt) : new Date(0);
        break;
      case 'totalCost':
        aValue = a.totalCost || 0;
        bValue = b.totalCost || 0;
        break;
      case 'name':
        aValue = a.name ? a.name.toLowerCase() : '';
        bValue = b.name ? b.name.toLowerCase() : '';
        break;
      default:
        aValue = a.createdAt ? new Date(a.createdAt) : new Date(0);
        bValue = b.createdAt ? new Date(b.createdAt) : new Date(0);
    }
    
    if (sortOrder === 'asc') {
      return aValue > bValue ? 1 : -1;
    } else {
      return aValue < bValue ? 1 : -1;
    }
  });

  // Calculate totals using userExpenses
  const totalExpenses = filteredExpenses.reduce((sum, expense) => sum + (expense.totalCost || 0), 0);
  const userPaidTotal = filteredExpenses.reduce((sum, expense) => {
    const userExpense = expense.userExpenses?.find(ue => ue.userId === currentUser?.userId);
    return sum + (userExpense?.amountPaid || 0);
  }, 0);
  const userOwedTotal = filteredExpenses.reduce((sum, expense) => {
    const userExpense = expense.userExpenses?.find(ue => ue.userId === currentUser?.userId);
    return sum + (userExpense?.amountOwed || 0);
  }, 0);
  // Net position: what you paid minus your share. Positive = you're owed, negative = you owe.
  const userBalance = userPaidTotal - userOwedTotal;

  if (loading) {
    return (
      <Layout>
        <LoadingSpinner message="Loading expenses..." />
      </Layout>
    );
  }

  return (
    <Layout>
      {/* Breadcrumb back to the groups view */}
      <Link to="/groups" className="ts-back-link mb-3">
        <FaArrowLeft size={11} />
        Back to groups
      </Link>

      {/* Header */}
      <div className="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-3">
        <div>
          <div className="ts-eyebrow">The ledger</div>
          <h2 className="ts-page-title">
            {filterGroup !== 'all' && groups.length > 0 ? getGroupName(Number(filterGroup)) : 'Expenses'}
          </h2>
          <p className="ts-page-sub">
            {filterGroup !== 'all'
              ? "This group's expenses, who paid, and how they split"
              : 'Every entry, who paid, and how it splits'}
          </p>
        </div>
        <div className="d-flex gap-2 flex-wrap">
          {filterGroup !== 'all' && (
            <button
              className="btn btn-outline-primary"
              onClick={() => setShowSettleModal(true)}
            >
              <FaBalanceScale className="me-1" /> Settle up
            </button>
          )}
          <button
            className="btn btn-primary"
            onClick={demoGuard(() => setShowCreateModal(true))}
          >
            <FaPlus className="me-1" /> Add Expense
          </button>
        </div>
      </div>

      {error && <ErrorAlert error={error} onRetry={loadExpenses} />}

      {/* Summary Cards */}
      {filteredExpenses.length > 0 && (
        <div className="ts-ledger-strip mb-4 ts-reveal">
          <div className="ts-stat">
            <div className="ts-eyebrow">Group total</div>
            <div className="ts-stat-value">{formatCurrency(totalExpenses)}</div>
            <p className="ts-stat-label">across {filteredExpenses.length} expense{filteredExpenses.length === 1 ? '' : 's'}</p>
          </div>
          <div className="ts-stat">
            <div className="ts-eyebrow">You paid</div>
            <div className="ts-stat-value">{formatCurrency(userPaidTotal)}</div>
            <p className="ts-stat-label">out of your own pocket</p>
          </div>
          <div className="ts-stat">
            <div className="ts-eyebrow">Your share</div>
            <div className="ts-stat-value">{formatCurrency(userOwedTotal)}</div>
            <p className="ts-stat-label">your part of the total</p>
          </div>
          <div className="ts-stat">
            <div className="ts-eyebrow">Balance</div>
            <div className={`ts-stat-value ${getBalanceColor(userBalance)}`}>{formatCurrency(Math.abs(userBalance))}</div>
            <p className="ts-stat-label">{getBalanceText(userBalance)}</p>
          </div>
        </div>
      )}

      {/* Filters */}
      <div className="card mb-4">
        <div className="card-body">
          <div className="row g-3">
            <div className="col-md-3">
              <div className="input-group">
                <span className="input-group-text">
                  <FaSearch />
                </span>
                <input
                  type="text"
                  className="form-control"
                  placeholder="Search expenses..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
              </div>
            </div>
            <div className="col-md-2">
              <select
                className="form-select"
                value={filterGroup}
                onChange={(e) => setFilterGroup(e.target.value)}
              >
                <option value="all">All Groups</option>
                {groups.map(group => (
                  <option key={group.groupId} value={group.groupId}>
                    {group.name}
                  </option>
                ))}
              </select>
            </div>
            <div className="col-md-2">
              <select
                className="form-select"
                value={filterCategory}
                onChange={(e) => setFilterCategory(e.target.value)}
              >
                <option value="all">All Categories</option>
                {categories.map(category => (
                  <option key={category} value={category}>
                    {category}
                  </option>
                ))}
              </select>
            </div>
            <div className="col-md-2">
              <select
                className="form-select"
                value={filterDateRange}
                onChange={(e) => setFilterDateRange(e.target.value)}
              >
                <option value="all">All Time</option>
                <option value="today">Today</option>
                <option value="week">This Week</option>
                <option value="month">This Month</option>
              </select>
            </div>
            <div className="col-md-2">
              <select
                className="form-select"
                value={`${sortBy}-${sortOrder}`}
                onChange={(e) => {
                  const [field, order] = e.target.value.split('-');
                  setSortBy(field);
                  setSortOrder(order);
                }}
              >
                <option value="createdAt-desc">Newest First</option>
                <option value="createdAt-asc">Oldest First</option>
                <option value="totalCost-desc">Highest Amount</option>
                <option value="totalCost-asc">Lowest Amount</option>
                <option value="name-asc">A-Z</option>
                <option value="name-desc">Z-A</option>
              </select>
            </div>
            <div className="col-md-1">
              <button
                className="btn btn-outline-secondary"
                onClick={() => {
                  setSearchTerm('');
                  setFilterGroup('all');
                  setFilterCategory('all');
                  setFilterDateRange('all');
                  setSortBy('createdAt');
                  setSortOrder('desc');
                }}
                title="Clear all filters"
              >
                <FaFilter />
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Expenses List */}
      {filteredExpenses.length === 0 ? (
        <div className="text-center py-5">
          <FaReceipt className="text-muted mb-3" size={64} />
          <h4>
            {searchTerm || filterGroup !== 'all' || filterCategory !== 'all' || filterDateRange !== 'all'
              ? 'No expenses found'
              : 'No Expenses Yet'
            }
          </h4>
          <p className="text-muted mb-4">
            {searchTerm || filterGroup !== 'all' || filterCategory !== 'all' || filterDateRange !== 'all'
              ? 'Try adjusting your search or filters'
              : 'Add your first expense to start tracking shared costs'
            }
          </p>
          {(!searchTerm && filterGroup === 'all' && filterCategory === 'all' && filterDateRange === 'all') && (
            <button
              className="btn btn-primary btn-lg"
              onClick={demoGuard(() => setShowCreateModal(true))}
            >
              <FaPlus className="me-2" /> Add Your First Expense
            </button>
          )}
        </div>
      ) : (
        <div className="card ts-reveal ts-reveal-d1">
          <div className="card-body p-0">
            <div className="table-responsive">
              <table className="table table-hover mb-0">
                <thead className="table-light">
                  <tr>
                    <th>Name</th>
                    <th>Amount</th>
                    <th>Group</th>
                    <th>Category</th>
                    <th>Created By</th>
                    <th>Date</th>
                    <th>Your Share</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  {filteredExpenses.map(expense => {
                    const userExpense = expense.userExpenses?.find(ue => ue.userId === currentUser?.userId);
                    const isCreatedByUser = expense.createdBy === currentUser?.userId;
                    
                    return (
                      <tr key={expense.expenseId} className="align-middle">
                        <td>
                          <div className="d-flex align-items-center">
                            <div>
                              <div className="fw-medium">{expense.name}</div>
                              {expense.description && (
                                <small className="text-muted">{expense.description}</small>
                              )}
                              {expense.hasReceipt && (
                                <div className="mt-1">
                                  <span className="badge bg-info">
                                    <FaFileImage className="me-1" />
                                    Receipt
                                  </span>
                                </div>
                              )}
                            </div>
                          </div>
                        </td>
                        <td>
                          <span className="ts-amount">{formatCurrency(expense.totalCost)}</span>
                        </td>
                        <td>
                          <span className="badge bg-light text-dark">
                            {getGroupName(expense.groupId)}
                          </span>
                        </td>
                        <td>
                          {expense.category && (
                            <span className="ts-stamp">{expense.category}</span>
                          )}
                        </td>
                        <td>
                          <div className="d-flex align-items-center">
                            <FaUser className="text-muted me-1" size={12} />
                            {getUserName(expense.createdBy)}
                            {isCreatedByUser && ' (You)'}
                          </div>
                        </td>
                        <td>
                          <small className="ts-mono">{new Date(expense.createdAt).toLocaleDateString()}</small>
                        </td>
                        <td>
                          <span className={`ts-amount ${userExpense?.amountOwed ? '' : 'text-muted'}`}>
                            {formatCurrency(userExpense?.amountOwed || 0)}
                          </span>
                          {userExpense?.amountPaid > 0 && (
                            <div>
                              <small className="text-success ts-mono">
                                Paid: {formatCurrency(userExpense.amountPaid)}
                              </small>
                            </div>
                          )}
                        </td>
                        <td>
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
                                  onClick={() => handleViewDetails(expense.expenseId)}
                                >
                                  <FaEye className="me-2" /> View Details
                                </button>
                              </li>
                              {isCreatedByUser && (
                                <>
                                  <li>
                                    <button
                                      className="dropdown-item"
                                      onClick={demoGuard(() => {
                                        setSelectedExpense(expense);
                                        setShowEditModal(true);
                                      })}
                                    >
                                      <FaEdit className="me-2" /> Edit
                                    </button>
                                  </li>
                                  <li><hr className="dropdown-divider" /></li>
                                  <li>
                                    <button
                                      className="dropdown-item text-danger"
                                      onClick={demoGuard(() => handleDeleteExpense(expense.expenseId))}
                                    >
                                      <FaTrash className="me-2" /> Delete
                                    </button>
                                  </li>
                                </>
                              )}
                            </ul>
                          </div>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* Activity feed: the group's recent history, ledger-style */}
      {filterGroup !== 'all' && activity.length > 0 && (
        <div className="card mt-4 ts-reveal">
          <div className="card-header d-flex align-items-center gap-2">
            <FaExchangeAlt size={13} className="text-muted" />
            Activity
          </div>
          <div className="card-body p-0">
            <ul className="ts-activity">
              {activity.map((item, i) => (
                <li key={i} className="ts-activity-row">
                  <span className="ts-activity-date ts-mono">
                    {new Date(item.createdAt).toLocaleDateString()}
                  </span>
                  <span className="ts-activity-text">
                    {item.type === 'SETTLEMENT' ? (
                      <>
                        <strong>{item.actorName}</strong> paid <strong>{item.targetName}</strong>
                        <span className="ts-stamp ms-2">settled</span>
                      </>
                    ) : (
                      <>
                        <strong>{item.actorName}</strong> added {item.title}
                      </>
                    )}
                  </span>
                  <span className={`ts-amount ${item.type === 'SETTLEMENT' ? 'text-success' : ''}`}>
                    {formatCurrency(item.amount)}
                  </span>
                </li>
              ))}
            </ul>
          </div>
        </div>
      )}

      {/* Modals */}
      <SettleUpModal
        show={showSettleModal}
        onHide={() => setShowSettleModal(false)}
        group={groups.find(g => g.groupId === Number(filterGroup)) || null}
        onSettlementRecorded={() => loadActivity(filterGroup)}
      />

      <CreateExpenseModal
        show={showCreateModal}
        onHide={() => setShowCreateModal(false)}
        onSubmit={handleCreateExpense}
        groups={groups}
        categories={categories}
        preSelectedGroup={filterGroup !== 'all' ? filterGroup : null}
      />

      <EditExpenseModal
        show={showEditModal}
        onHide={() => {
          setShowEditModal(false);
          setSelectedExpense(null);
        }}
        expense={selectedExpense}
        onSubmit={handleEditExpense}
        groups={groups}
        categories={categories}
      />

      <ExpenseDetailsModal
        show={showDetailsModal}
        onHide={() => {
          setShowDetailsModal(false);
          setSelectedExpense(null);
          loadExpenses();
        }}
        expense={selectedExpense}
        groups={groups}
        onEdit={(expense) => {
          setShowDetailsModal(false);
          setSelectedExpense(expense);
          setShowEditModal(true);
        }}
        onDelete={handleDeleteExpense}
      />
    </Layout>
  );
}

export default Expenses;