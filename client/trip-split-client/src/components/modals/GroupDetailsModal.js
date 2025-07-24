import { useState, useEffect } from 'react';
import { Modal, Button, Tab, Tabs } from 'react-bootstrap';
import { FaUsers, FaReceipt, FaDollarSign, FaCopy, FaEdit, FaTrash, FaPlus, FaEye, FaCalendar } from 'react-icons/fa';
import { apiService } from '../../services/apiService';
import { useAuth } from '../../utils/auth';
import { formatCurrency } from '../../utils/helpers';

function GroupDetailsModal({ show, onHide, group, onEdit, onDelete }) {
  const { currentUser } = useAuth();
  const [activeTab, setActiveTab] = useState('overview');
  const [expenses, setExpenses] = useState([]);
  const [balances, setBalances] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (show && group) {
      loadGroupDetails();
    }
  }, [show, group]);

  const loadGroupDetails = async () => {
    if (!group) return;

    try {
      setLoading(true);
      setError('');
      
      // Load group expenses and balances
      const [expensesData, balancesData] = await Promise.all([
        apiService.getExpensesByGroup(group.id),
        apiService.getGroupBalances ? apiService.getGroupBalances(group.id) : Promise.resolve([])
      ]);
      
      setExpenses(expensesData || []);
      setBalances(balancesData || []);
      
    } catch (err) {
      console.error('Failed to load group details:', err);
      setError(err.message || 'Failed to load group details');
    } finally {
      setLoading(false);
    }
  };

  const handleCopyInviteCode = () => {
    if (group?.inviteCode) {
      navigator.clipboard.writeText(group.inviteCode);
      alert('Invite code copied to clipboard!');
    }
  };

  const handleAddExpense = () => {
    // Close this modal and trigger add expense for this group
    onHide();
    // You could emit an event or call a parent function to open CreateExpenseModal
    // with the group pre-selected
  };

  const calculateGroupStats = () => {
    const totalExpenses = expenses.reduce((sum, expense) => sum + expense.amount, 0);
    const userExpenses = expenses.filter(expense => expense.paidBy === currentUser?.id);
    const userPaid = userExpenses.reduce((sum, expense) => sum + expense.amount, 0);
    const userShare = expenses.reduce((sum, expense) => {
      const userSplit = expense.splits?.find(split => split.userId === currentUser?.id);
      return sum + (userSplit?.amount || 0);
    }, 0);
    const userBalance = userPaid - userShare;

    return { totalExpenses, userPaid, userShare, userBalance };
  };

  const getSettlementSuggestions = () => {
    const settlements = [];
    const positiveBalances = balances.filter(b => b.balance > 0);
    const negativeBalances = balances.filter(b => b.balance < 0);

    negativeBalances.forEach(debtor => {
      positiveBalances.forEach(creditor => {
        if (Math.abs(debtor.balance) > 0.01 && creditor.balance > 0.01) {
          const amount = Math.min(Math.abs(debtor.balance), creditor.balance);
          settlements.push({
            from: debtor.name,
            to: creditor.name,
            amount: amount
          });
          debtor.balance += amount;
          creditor.balance -= amount;
        }
      });
    });

    return settlements;
  };

  if (!group) return null;

  const stats = calculateGroupStats();
  const settlements = getSettlementSuggestions();

  return (
    <Modal show={show} onHide={onHide} size="xl">
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

        {/* Group Header Info */}
        <div className="row mb-4">
          <div className="col-md-8">
            <p className="text-muted mb-2">{group.description}</p>
            <div className="d-flex gap-3">
              <small className="text-muted">
                <FaUsers className="me-1" />
                {group.memberCount || group.members?.length || 0} members
              </small>
              <span className={`badge ${group.isActive ? 'bg-success' : 'bg-secondary'}`}>
                {group.isActive ? 'Active' : 'Settled'}
              </span>
            </div>
          </div>
          <div className="col-md-4 text-end">
            <div className="btn-group">
              <button
                className="btn btn-outline-primary btn-sm"
                onClick={handleCopyInviteCode}
              >
                <FaCopy className="me-1" /> Copy Invite Code
              </button>
              <button
                className="btn btn-outline-secondary btn-sm"
                onClick={() => onEdit(group)}
              >
                <FaEdit className="me-1" /> Edit
              </button>
              <button
                className="btn btn-outline-danger btn-sm"
                onClick={() => {
                  onHide();
                  onDelete(group.id);
                }}
              >
                <FaTrash className="me-1" /> Delete
              </button>
            </div>
          </div>
        </div>

        {/* Quick Stats */}
        <div className="row mb-4">
          <div className="col-md-3">
            <div className="card text-center">
              <div className="card-body py-3">
                <h5 className="text-primary mb-1">{formatCurrency(stats.totalExpenses)}</h5>
                <small className="text-muted">Total Expenses</small>
              </div>
            </div>
          </div>
          <div className="col-md-3">
            <div className="card text-center">
              <div className="card-body py-3">
                <h5 className="text-info mb-1">{formatCurrency(stats.userPaid)}</h5>
                <small className="text-muted">You Paid</small>
              </div>
            </div>
          </div>
          <div className="col-md-3">
            <div className="card text-center">
              <div className="card-body py-3">
                <h5 className="text-warning mb-1">{formatCurrency(stats.userShare)}</h5>
                <small className="text-muted">Your Share</small>
              </div>
            </div>
          </div>
          <div className="col-md-3">
            <div className="card text-center">
              <div className="card-body py-3">
                <h5 className={`mb-1 ${stats.userBalance >= 0 ? 'text-success' : 'text-danger'}`}>
                  {formatCurrency(Math.abs(stats.userBalance))}
                </h5>
                <small className="text-muted">
                  {stats.userBalance >= 0 ? 'You are owed' : 'You owe'}
                </small>
              </div>
            </div>
          </div>
        </div>

        {/* Tabs */}
        <Tabs activeKey={activeTab} onSelect={(tab) => setActiveTab(tab)} className="mb-3">
          {/* Overview Tab */}
          <Tab eventKey="overview" title="Overview">
            <div className="row">
              <div className="col-md-6">
                <h6>Recent Expenses</h6>
                {loading ? (
                  <div className="text-center py-3">
                    <div className="spinner-border spinner-border-sm"></div>
                  </div>
                ) : expenses.length === 0 ? (
                  <div className="text-center py-4">
                    <FaReceipt className="text-muted mb-2" size={32} />
                    <p className="text-muted">No expenses yet</p>
                    <button className="btn btn-primary btn-sm" onClick={handleAddExpense}>
                      <FaPlus className="me-1" /> Add First Expense
                    </button>
                  </div>
                ) : (
                  <div className="list-group">
                    {expenses.slice(0, 5).map(expense => (
                      <div key={expense.id} className="list-group-item">
                        <div className="d-flex justify-content-between align-items-start">
                          <div>
                            <h6 className="mb-1">{expense.description}</h6>
                            <small className="text-muted">
                              Paid by {expense.paidByName} on {new Date(expense.date).toLocaleDateString()}
                            </small>
                          </div>
                          <span className="badge bg-primary">{formatCurrency(expense.amount)}</span>
                        </div>
                      </div>
                    ))}
                    {expenses.length > 5 && (
                      <div className="text-center mt-2">
                        <button 
                          className="btn btn-link btn-sm"
                          onClick={() => setActiveTab('expenses')}
                        >
                          View all {expenses.length} expenses
                        </button>
                      </div>
                    )}
                  </div>
                )}
              </div>
              
              <div className="col-md-6">
                <h6>Settlement Suggestions</h6>
                {settlements.length === 0 ? (
                  <div className="text-center py-4">
                    <div className="text-success">
                      <FaDollarSign size={32} className="mb-2" />
                      <p className="mb-0">All settled up!</p>
                    </div>
                  </div>
                ) : (
                  <div className="list-group">
                    {settlements.map((settlement, index) => (
                      <div key={index} className="list-group-item">
                        <div className="d-flex justify-content-between align-items-center">
                          <div>
                            <strong>{settlement.from}</strong> owes <strong>{settlement.to}</strong>
                          </div>
                          <span className="badge bg-warning text-dark">
                            {formatCurrency(settlement.amount)}
                          </span>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          </Tab>

          {/* Expenses Tab */}
          <Tab eventKey="expenses" title={`Expenses (${expenses.length})`}>
            <div className="d-flex justify-content-between align-items-center mb-3">
              <h6>All Expenses</h6>
              <button className="btn btn-primary btn-sm" onClick={handleAddExpense}>
                <FaPlus className="me-1" /> Add Expense
              </button>
            </div>
            
            {loading ? (
              <div className="text-center py-4">
                <div className="spinner-border"></div>
              </div>
            ) : expenses.length === 0 ? (
              <div className="text-center py-5">
                <FaReceipt className="text-muted mb-3" size={48} />
                <h5>No expenses yet</h5>
                <p className="text-muted">Start by adding your first expense to this group</p>
                <button className="btn btn-primary" onClick={handleAddExpense}>
                  <FaPlus className="me-2" /> Add First Expense
                </button>
              </div>
            ) : (
              <div className="table-responsive">
                <table className="table table-hover">
                  <thead>
                    <tr>
                      <th>Description</th>
                      <th>Amount</th>
                      <th>Paid By</th>
                      <th>Date</th>
                      <th>Your Share</th>
                    </tr>
                  </thead>
                  <tbody>
                    {expenses.map(expense => {
                      const userSplit = expense.splits?.find(split => split.userId === currentUser?.id);
                      return (
                        <tr key={expense.id}>
                          <td>
                            <div>
                              <div className="fw-medium">{expense.description}</div>
                              {expense.category && (
                                <span className="badge bg-secondary">{expense.category}</span>
                              )}
                            </div>
                          </td>
                          <td>{formatCurrency(expense.amount)}</td>
                          <td>
                            {expense.paidByName}
                            {expense.paidBy === currentUser?.id && ' (You)'}
                          </td>
                          <td>{new Date(expense.date).toLocaleDateString()}</td>
                          <td>{formatCurrency(userSplit?.amount || 0)}</td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            )}
          </Tab>

          {/* Members Tab */}
          <Tab eventKey="members" title={`Members (${group.members?.length || group.memberCount || 0})`}>
            <div className="d-flex justify-content-between align-items-center mb-3">
              <h6>Group Members</h6>
              <div>
                <button className="btn btn-outline-primary btn-sm me-2" onClick={handleCopyInviteCode}>
                  <FaCopy className="me-1" /> Invite Code: {group.inviteCode}
                </button>
              </div>
            </div>
            
            <div className="row">
              {group.members?.map(member => {
                const memberBalance = balances.find(b => b.userId === member.id)?.balance || 0;
                return (
                  <div key={member.id} className="col-md-6 mb-3">
                    <div className="card">
                      <div className="card-body">
                        <div className="d-flex justify-content-between align-items-start">
                          <div>
                            <h6 className="mb-1">
                              {member.name}
                              {member.id === currentUser?.id && ' (You)'}
                            </h6>
                            <small className="text-muted">{member.email}</small>
                          </div>
                          <div className="text-end">
                            <div className={`fw-bold ${memberBalance >= 0 ? 'text-success' : 'text-danger'}`}>
                              {formatCurrency(Math.abs(memberBalance))}
                            </div>
                            <small className="text-muted">
                              {memberBalance >= 0 ? 'is owed' : 'owes'}
                            </small>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          </Tab>

          {/* Balances Tab */}
          <Tab eventKey="balances" title="Balances">
            <h6>Current Balances</h6>
            <div className="row">
              {balances.length === 0 ? (
                <div className="col-12 text-center py-4">
                  <p className="text-muted">No balance information available</p>
                </div>
              ) : (
                balances.map(balance => (
                  <div key={balance.userId} className="col-md-6 mb-3">
                    <div className="card">
                      <div className="card-body">
                        <div className="d-flex justify-content-between align-items-center">
                          <div>
                            <h6 className="mb-1">
                              {balance.name}
                              {balance.userId === currentUser?.id && ' (You)'}
                            </h6>
                          </div>
                          <div className="text-end">
                            <div className={`h5 mb-0 ${balance.balance >= 0 ? 'text-success' : 'text-danger'}`}>
                              {formatCurrency(Math.abs(balance.balance))}
                            </div>
                            <small className="text-muted">
                              {balance.balance >= 0 ? 'is owed' : 'owes'}
                            </small>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                ))
              )}
            </div>
            
            {settlements.length > 0 && (
              <>
                <hr />
                <h6>Suggested Settlements</h6>
                <div className="list-group">
                  {settlements.map((settlement, index) => (
                    <div key={index} className="list-group-item">
                      <div className="d-flex justify-content-between align-items-center">
                        <div>
                          <strong>{settlement.from}</strong> should pay <strong>{settlement.to}</strong>
                        </div>
                        <span className="badge bg-warning text-dark fs-6">
                          {formatCurrency(settlement.amount)}
                        </span>
                      </div>
                    </div>
                  ))}
                </div>
              </>
            )}
          </Tab>
        </Tabs>
      </Modal.Body>
      
      <Modal.Footer>
        <Button variant="secondary" onClick={onHide}>
          Close
        </Button>
        <Button variant="primary" onClick={handleAddExpense}>
          <FaPlus className="me-1" /> Add Expense
        </Button>
      </Modal.Footer>
    </Modal>
  );
}

export default GroupDetailsModal;