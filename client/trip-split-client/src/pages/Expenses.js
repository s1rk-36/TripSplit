import { useState, useEffect } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { FaPlus, FaReceipt, FaDollarSign, FaCalendar, FaEdit, FaTrash, FaEye, FaFilter, FaSearch, FaUsers, FaUser, FaFileImage } from 'react-icons/fa';
import Layout from '../components/layout/Layout';
import LoadingSpinner from '../components/common/LoadingSpinner';
import ErrorAlert from '../components/common/ErrorAlert';
import CreateExpenseModal from '../components/modals/CreateExpenseModal';
import EditExpenseModal from '../components/modals/EditExpenseModal';
import ExpenseDetailsModal from '../components/modals/ExpenseDetailsModal';
import { apiService } from '../services/apiService';
import { useAuth } from '../utils/auth';
import { formatCurrency } from '../utils/helpers';

function Expenses() {
  const { currentUser } = useAuth();
  const [searchParams] = useSearchParams();
  const [expenses, setExpenses] = useState([]);
  const [groups, setGroups] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  const [filterGroup, setFilterGroup] = useState(searchParams.get('group') || 'all');
  const [filterCategory, setFilterCategory] = useState('all');
  const [filterDateRange, setFilterDateRange] = useState('all');
  const [sortBy, setSortBy] = useState('date');
  const [sortOrder, setSortOrder] = useState('desc');
  
  // Modal states
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showDetailsModal, setShowDetailsModal] = useState(false);
  const [selectedExpense, setSelectedExpense] = useState(null);

  useEffect(() => {
    loadExpenses();
    loadGroups();
    loadCategories();
  }, []);

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
    } catch (err) {
      console.error('Failed to create expense:', err);
      throw new Error(err.message || 'Failed to create expense');
    }
  };

  const handleEditExpense = async (expenseId, expenseData) => {
    try {
      const updatedExpense = await apiService.updateExpense(expenseId, expenseData);
      setExpenses(expenses.map(expense => 
        expense.id === expenseId ? updatedExpense : expense
      ));
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
      setExpenses(expenses.filter(expense => expense.id !== expenseId));
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

  const getGroupName = (groupId) => {
    const group = groups.find(g => g.id === groupId || g.groupId === groupId);
    return group ? group.name : 'Unknown Group';
  };

  // Filter and sort expenses
  const filteredExpenses = expenses.filter(expense => {
    const matchesSearch = expense.name.toLowerCase().includes(searchTerm.toLowerCase());
    
    const matchesGroup = filterGroup === 'all' || expense.groupId.toString() === filterGroup;
    const matchesCategory = filterCategory === 'all' || expense.category === filterCategory;
    
    let matchesDate = true;
    if (filterDateRange !== 'all') {
      const expenseDate = new Date(expense.date);
      const now = new Date();
      
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
    
    return matchesSearch && matchesGroup && matchesCategory && matchesDate;
  }).sort((a, b) => {
    let aValue, bValue;
    
    switch (sortBy) {
      case 'date':
        aValue = new Date(a.date);
        bValue = new Date(b.date);
        break;
      case 'amount':
        aValue = a.totalCost;
        bValue = b.totalCost;
        break;
      default:
        aValue = new Date(a.date);
        bValue = new Date(b.date);
    }
    
    if (sortOrder === 'asc') {
      return aValue > bValue ? 1 : -1;
    } else {
      return aValue < bValue ? 1 : -1;
    }
  });

  const totalExpenses = filteredExpenses.reduce((sum, expense) => sum + expense.totalCost, 0);
  const userPaidTotal = filteredExpenses
    .filter(expense => expense.paidBy === currentUser?.userId)
    .reduce((sum, expense) => sum + expense.totalCost, 0);
  const userShareTotal = filteredExpenses.reduce((sum, expense) => {
    const userSplit = expense.splits?.find(split => split.userId === currentUser?.userId);
    return sum + (userSplit?.totalCost || 0);
  }, 0);

  if (loading) {
    return (
      <Layout>
        <LoadingSpinner message="Loading expenses..." />
      </Layout>
    );
  }

  return (
    <Layout>
      {/* Header */}
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2>Expenses</h2>
          <p className="text-muted">Track and manage your shared expenses</p>
        </div>
        <button 
          className="btn btn-primary"
          onClick={() => setShowCreateModal(true)}
        >
          <FaPlus className="me-1" /> Add Expense
        </button>
      </div>

      {error && <ErrorAlert error={error} onRetry={loadExpenses} />}

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
                  <option key={group.id || group.groupId} value={group.id || group.groupId}>
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
                <option value="date-desc">Newest First</option>
                <option value="date-asc">Oldest First</option>
                <option value="amount-desc">Highest Amount</option>
                <option value="amount-asc">Lowest Amount</option>
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
                  setSortBy('date');
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

      {/* Summary Cards */}
      {filteredExpenses.length > 0 && (
        <div className="row mb-4">
          <div className="col-md-3">
            <div className="card text-center">
              <div className="card-body">
                <h4 className="text-primary">{filteredExpenses.length}</h4>
                <p className="card-text small">Total Expenses</p>
              </div>
            </div>
          </div>
          <div className="col-md-3">
            <div className="card text-center">
              <div className="card-body">
                <h4 className="text-success">{formatCurrency(totalExpenses)}</h4>
                <p className="card-text small">Total Amount</p>
              </div>
            </div>
          </div>
          <div className="col-md-3">
            <div className="card text-center">
              <div className="card-body">
                <h4 className="text-info">{formatCurrency(userPaidTotal)}</h4>
                <p className="card-text small">You Paid</p>
              </div>
            </div>
          </div>
          <div className="col-md-3">
            <div className="card text-center">
              <div className="card-body">
                <h4 className="text-warning">{formatCurrency(userShareTotal)}</h4>
                <p className="card-text small">Your Share</p>
              </div>
            </div>
          </div>
        </div>
      )}

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
              onClick={() => setShowCreateModal(true)}
            >
              <FaPlus className="me-2" /> Add Your First Expense
            </button>
          )}
        </div>
      ) : (
        <div className="card">
          <div className="card-body p-0">
            <div className="table-responsive">
              <table className="table table-hover mb-0">
                <thead className="table-light">
                  <tr>
                    <th>Name</th>
                    <th>Amount</th>
                    <th>Group</th>
                    <th>Category</th>
                    <th>Paid By</th>
                    <th>Date</th>
                    <th>Your Share</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  {filteredExpenses.map(expense => {
                    const userSplit = expense.splits?.find(split => split.userId === currentUser?.userId);
                    const isPaidByUser = expense.paidBy === currentUser?.userId;
                    
                    return (
                      <tr key={expense.id} className="align-middle">
                        <td>
                          <div className="d-flex align-items-center">
                            <div>
                              <div className="fw-medium">{expense.name}</div>
                    
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
                          <span className="fw-bold">{formatCurrency(expense.totalCost)}</span>
                        </td>
                        <td>
                          <span className="badge bg-light text-dark">
                            {getGroupName(expense.groupId)}
                          </span>
                        </td>
                        <td>
                          {expense.category && (
                            <span className="badge bg-secondary">{expense.category}</span>
                          )}
                        </td>
                        <td>
                          <div className="d-flex align-items-center">
                            <FaUser className="text-muted me-1" size={12} />
                            {expense.paidByName}
                            {isPaidByUser && ' (You)'}
                          </div>
                        </td>
                        <td>
                          <small>{new Date(expense.date).toLocaleDateString()}</small>
                        </td>
                        <td>
                          <span className={userSplit?.totalCost ? 'fw-medium' : 'text-muted'}>
                            {formatCurrency(userSplit?.totalCost || 0)}
                          </span>
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
                            <ul className="dropdown-menu dropdown-menu-end">
                              <li>
                                <button 
                                  className="dropdown-item"
                                  onClick={() => handleViewDetails(expense.id)}
                                >
                                  <FaEye className="me-2" /> View Details
                                </button>
                              </li>
                              {isPaidByUser && (
                                <>
                                  <li>
                                    <button 
                                      className="dropdown-item"
                                      onClick={() => {
                                        setSelectedExpense(expense);
                                        setShowEditModal(true);
                                      }}
                                    >
                                      <FaEdit className="me-2" /> Edit
                                    </button>
                                  </li>
                                  <li><hr className="dropdown-divider" /></li>
                                  <li>
                                    <button 
                                      className="dropdown-item text-danger"
                                      onClick={() => handleDeleteExpense(expense.id)}
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

      {/* Modals */}
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