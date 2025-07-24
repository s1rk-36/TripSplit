import { useState, useEffect } from 'react';
import { Modal, Button } from 'react-bootstrap';
import { FaReceipt, FaCalendar, FaUsers, FaUser, FaDollarSign, FaEdit, FaTrash, FaFileImage, FaDownload, FaCreditCard, FaCheck } from 'react-icons/fa';
import { formatCurrency } from '../../utils/helpers';
import { useAuth } from '../../utils/auth';
import { apiService } from '../../services/apiService';

function ExpenseDetailsModal({ show, onHide, expense, groups, onEdit, onDelete, onPayExpense }) {
  const { currentUser } = useAuth();
  const [userExpenseDetails, setUserExpenseDetails] = useState([]);
  const [loading, setLoading] = useState(false);
  const [receipts, setReceipts] = useState([]);
  const [receiptLoading, setReceiptLoading] = useState(false);
  const [paymentLoading, setPaymentLoading] = useState(false);
  const [paymentAmounts, setPaymentAmounts] = useState({});

  useEffect(() => {
    if (expense && show) {
      loadUserExpenseDetails();
      if (expense.hasReceipt || expense.expenseId) {
        loadReceipts();
      }
    }
  }, [expense, show]);

  const loadReceipts = async () => {
    try {
      setReceiptLoading(true);
      const receiptsData = await apiService.getExpenseReceipts(expense.expenseId);
      setReceipts(receiptsData || []);
    } catch (err) {
      console.error('Failed to load receipts:', err);
      setReceipts([]);
    } finally {
      setReceiptLoading(false);
    }
  };

  const loadUserExpenseDetails = async () => {
    if (!expense || !expense.userExpenses) return;

    try {
      setLoading(true);
      const detailsWithNames = [];

      // Load user details for each userExpense
      for (const userExpense of expense.userExpenses) {
        try {
          const userData = await apiService.getUser(userExpense.userId);
          detailsWithNames.push({
            ...userExpense,
            userName: `${userData.firstName} ${userData.lastName}`,
            userEmail: userData.email
          });
        } catch (err) {
          console.error(`Failed to load user ${userExpense.userId}:`, err);
          detailsWithNames.push({
            ...userExpense,
            userName: 'Unknown User',
            userEmail: ''
          });
        }
      }

      setUserExpenseDetails(detailsWithNames);
    } catch (err) {
      console.error('Failed to load user expense details:', err);
    } finally {
      setLoading(false);
    }
  };

  const getGroupName = (groupId) => {
    const group = groups?.find(g => (g.id || g.groupId) === groupId);
    return group ? group.name : 'Unknown Group';
  };

  const formatCategory = (category) => {
    return category ? category.replace('_', ' ') : 'Uncategorized';
  };

  const handleEdit = () => {
    onHide();
    onEdit(expense);
  };

  const handleDelete = () => {
    if (window.confirm('Are you sure you want to delete this expense? This action cannot be undone.')) {
      onHide();
      onDelete(expense.expenseId);
    }
  };

  const handlePayExpense = async (amount) => {
    try {
      setPaymentLoading(true);
      
      const currentUserExpense = userExpenseDetails.find(ue => ue.userId === currentUser?.userId);
      if (!currentUserExpense) {
        throw new Error('User expense record not found');
      }
      
      // Update the user_expense record with new amountPaid
      const updatedUserExpense = {
        userId: currentUser.userId,
        expenseId: expense.expenseId,
        amountOwed: currentUserExpense.amountOwed,
        amountPaid: (currentUserExpense.amountPaid || 0) + amount
      };
      
      await apiService.updateUserExpense(updatedUserExpense);
      
      // Update local state to reflect payment
      setUserExpenseDetails(prevDetails => 
        prevDetails.map(ue => {
          if (ue.userId === currentUser.userId) {
            return { ...ue, amountPaid: (ue.amountPaid || 0) + amount };
          }
          return ue;
        })
      );
      
      // Call parent callback if provided
      if (onPayExpense) {
        onPayExpense(expense, updatedUserExpense);
      }
      
      // Clear payment amounts
      setPaymentAmounts({});
      
    } catch (err) {
      console.error('Failed to process payment:', err);
      alert('Failed to process payment: ' + (err.message || 'Unknown error'));
    } finally {
      setPaymentLoading(false);
    }
  };

  const handleQuickPay = async () => {
    const currentUserExpense = userExpenseDetails.find(ue => ue.userId === currentUser?.userId);
    if (!currentUserExpense || currentUserExpense.amountOwed <= 0) return;
    
    const remainingBalance = currentUserExpense.amountOwed - (currentUserExpense.amountPaid || 0);
    if (remainingBalance <= 0) return;
    
    await handlePayExpense(remainingBalance);
  };

  if (!expense) return null;

  const isCreatedByUser = expense.createdBy === currentUser?.userId;
  const currentUserExpense = userExpenseDetails.find(ue => ue.userId === currentUser?.userId);
  const totalOwed = userExpenseDetails.reduce((sum, ue) => sum + (ue.amountOwed || 0), 0);
  const totalPaid = userExpenseDetails.reduce((sum, ue) => sum + (ue.amountPaid || 0), 0);
  const payer = userExpenseDetails.find(ue => ue.amountPaid > 0);

  return (
    <Modal show={show} onHide={onHide} size="lg">
      <Modal.Header closeButton>
        <Modal.Title>
          <FaReceipt className="me-2" />
          Expense Details
        </Modal.Title>
      </Modal.Header>
      
      <Modal.Body>
        <div className="row">
          <div className="col-md-8">
            {/* Basic Info */}
            <div className="mb-4">
              <h4 className="mb-2">{expense.name}</h4>
              {expense.description && (
                <p className="text-muted mb-3">{expense.description}</p>
              )}
              
              <div className="row g-3">
                <div className="col-sm-6">
                  <div className="d-flex align-items-center">
                    <FaDollarSign className="text-success me-2" />
                    <div>
                      <div className="fw-bold fs-5 text-success">
                        {formatCurrency(expense.totalCost)}
                      </div>
                      <small className="text-muted">Total Cost</small>
                    </div>
                  </div>
                </div>
                
                <div className="col-sm-6">
                  <div className="d-flex align-items-center">
                    <FaCalendar className="text-primary me-2" />
                    <div>
                      <div className="fw-medium">
                        {new Date(expense.createdAt).toLocaleDateString('en-US', {
                          weekday: 'long',
                          year: 'numeric',
                          month: 'long',
                          day: 'numeric'
                        })}
                      </div>
                      <small className="text-muted">Date</small>
                    </div>
                  </div>
                </div>
                
                <div className="col-sm-6">
                  <div className="d-flex align-items-center">
                    <FaUsers className="text-info me-2" />
                    <div>
                      <div className="fw-medium">{getGroupName(expense.groupId)}</div>
                      <small className="text-muted">Group</small>
                    </div>
                  </div>
                </div>
                
                <div className="col-sm-6">
                  <div className="d-flex align-items-center">
                    <div className="bg-secondary text-white rounded-circle d-flex align-items-center justify-content-center me-2" 
                         style={{width: '20px', height: '20px', fontSize: '10px'}}>
                      #
                    </div>
                    <div>
                      <div className="fw-medium">{formatCategory(expense.category)}</div>
                      <small className="text-muted">Category</small>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            {/* Receipts */}
            {(expense.hasReceipt || receipts.length > 0) && (
              <div className="mb-4">
                <h6>Receipts</h6>
                {receiptLoading ? (
                  <div className="border rounded p-3">
                    <div className="text-center">
                      <div className="spinner-border spinner-border-sm me-2"></div>
                      Loading receipts...
                    </div>
                  </div>
                ) : receipts.length > 0 ? (
                  <div className="border rounded p-3">
                    {receipts.map(receipt => (
                      <div key={receipt.receiptId} className="mb-3 last:mb-0">
                        <div className="d-flex align-items-center justify-content-between mb-2">
                          <div className="d-flex align-items-center">
                            <FaFileImage className="text-info me-2" size={20} />
                            <div>
                              <span>Receipt #{receipt.receiptId}</span>
                              <br />
                              <small className="text-muted">
                                Uploaded: {new Date(receipt.uploadedAt).toLocaleDateString()}
                              </small>
                            </div>
                          </div>
                          <div className="btn-group">
                            {receipt.imageUrl && (
                              <>
                                <a
                                  href={receipt.imageUrl}
                                  target="_blank"
                                  rel="noopener noreferrer"
                                  className="btn btn-outline-primary btn-sm"
                                >
                                  <FaDownload className="me-1" /> View
                                </a>
                              </>
                            )}
                          </div>
                        </div>
                        {/* Optional: Show thumbnail */}
                        {receipt.imageUrl && (
                          <div className="mt-2">
                            <img
                              src={receipt.imageUrl}
                              alt={`Receipt #${receipt.receiptId}`}
                              style={{
                                maxWidth: '100%',
                                maxHeight: '200px',
                                objectFit: 'contain',
                                border: '1px solid #dee2e6',
                                borderRadius: '4px'
                              }}
                              onError={(e) => {
                                e.target.style.display = 'none';
                              }}
                            />
                          </div>
                        )}
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="border rounded p-3 text-muted">
                    No receipts could be loaded
                  </div>
                )}
              </div>
            )}
          </div>

          <div className="col-md-4">
            {/* Your Share Summary */}
            {currentUserExpense && (
              <div className="card mb-3">
                <div className="card-header">
                  <h6 className="mb-0">Your Share</h6>
                </div>
                <div className="card-body">
                  <div className="d-flex justify-content-between mb-2">
                    <span>You owe:</span>
                    <span className="fw-bold text-warning">
                      {formatCurrency(currentUserExpense.amountOwed || 0)}
                    </span>
                  </div>
                  <div className="d-flex justify-content-between mb-2">
                    <span>You paid:</span>
                    <span className="fw-bold text-success">
                      {formatCurrency(currentUserExpense.amountPaid || 0)}
                    </span>
                  </div>
                  <hr />
                  <div className="d-flex justify-content-between mb-2">
                    <span>Net balance:</span>
                    <span className={`fw-bold ${(currentUserExpense.amountPaid - currentUserExpense.amountOwed) >= 0 ? 'text-success' : 'text-danger'}`}>
                      {formatCurrency(Math.abs((currentUserExpense.amountPaid || 0) - (currentUserExpense.amountOwed || 0)))}
                    </span>
                  </div>
                  <small className="text-muted">
                    {(currentUserExpense.amountPaid - currentUserExpense.amountOwed) >= 0 
                      ? 'You are owed money' 
                      : 'You owe money'
                    }
                  </small>
                  
                  {/* Quick Pay Button */}
                  {(currentUserExpense.amountOwed > currentUserExpense.amountPaid) && (
                    <div className="mt-3">
                      <button
                        className="btn btn-success w-100"
                        onClick={handleQuickPay}
                        disabled={paymentLoading}
                      >
                        {paymentLoading ? (
                          <>
                            <span className="spinner-border spinner-border-sm me-2"></span>
                            Processing...
                          </>
                        ) : (
                          <>
                            <FaCreditCard className="me-2" />
                            Pay {formatCurrency(currentUserExpense.amountOwed - (currentUserExpense.amountPaid || 0))}
                          </>
                        )}
                      </button>
                      <small className="text-muted d-block mt-1">
                        Pay your remaining balance
                      </small>
                    </div>
                  )}
                  
                  {/* Fully Paid */}
                  {(currentUserExpense.amountPaid >= currentUserExpense.amountOwed) && (
                    <div className="mt-3 text-center">
                      <div className="badge bg-success p-2">
                        <FaCheck className="me-1" />
                        Fully Paid
                      </div>
                    </div>
                  )}
                </div>
              </div>
            )}
          </div>
        </div>

        {/* Split Details */}
        <div className="mt-4">
          <h6>Split Details</h6>
          {loading ? (
            <div className="text-center py-3">
              <div className="spinner-border spinner-border-sm me-2"></div>
              Loading member details...
            </div>
          ) : userExpenseDetails.length > 0 ? (
            <div className="table-responsive">
              <table className="table table-sm">
                <thead className="table-light">
                  <tr>
                    <th>Member</th>
                    <th className="text-end">Amount Owed</th>
                    <th className="text-end">Amount Paid</th>
                    <th className="text-end">Balance</th>
                    <th className="text-end">Share % / Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {userExpenseDetails.map(userExpense => {
                    const balance = (userExpense.amountPaid || 0) - (userExpense.amountOwed || 0);
                    const sharePercentage = expense.totalCost > 0 ? ((userExpense.amountOwed || 0) / expense.totalCost * 100) : 0;
                    
                    return (
                      <tr key={userExpense.userId}>
                        <td>
                          <div className="d-flex align-items-center">
                            <div className="bg-primary text-white rounded-circle d-flex align-items-center justify-content-center me-2" 
                                 style={{width: '24px', height: '24px', fontSize: '10px'}}>
                              <FaUser />
                            </div>
                            <div>
                              <div className="fw-medium">{userExpense.userName}</div>
                              {userExpense.userId === currentUser?.userId && (
                                <small className="text-muted">(You)</small>
                              )}
                            </div>
                          </div>
                        </td>
                        <td className="text-end">
                          <span className="text-warning fw-medium">
                            {formatCurrency(userExpense.amountOwed || 0)}
                          </span>
                        </td>
                        <td className="text-end">
                          <span className="text-success fw-medium">
                            {formatCurrency(userExpense.amountPaid || 0)}
                          </span>
                          {userExpense.amountPaid > 0 && (
                            <div>
                              <small className="badge bg-success">Payer</small>
                            </div>
                          )}
                        </td>
                        <td className="text-end">
                          <span className={`fw-medium ${balance >= 0 ? 'text-success' : 'text-danger'}`}>
                            {formatCurrency(Math.abs(balance))}
                          </span>
                          <div>
                            <small className="text-muted">
                              {balance >= 0 ? 'owed' : 'owes'}
                            </small>
                          </div>
                        </td>
                        <td className="text-end">
                          <div>
                            <span className="text-muted">
                              {sharePercentage.toFixed(1)}%
                            </span>
                            
                            {/* Paid Status */}
                            {userExpense.userId === currentUser?.userId && balance >= 0 && (
                              <div className="mt-1">
                                <small className="badge bg-success">Settled</small>
                              </div>
                            )}
                          </div>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
                <tfoot className="table-light">
                  <tr>
                    <th>Totals:</th>
                    <th className="text-end text-warning">
                      {formatCurrency(totalOwed)}
                    </th>
                    <th className="text-end text-success">
                      {formatCurrency(totalPaid)}
                    </th>
                    <th className="text-end">
                      <span className={Math.abs(totalPaid - totalOwed) < 0.01 ? 'text-success' : 'text-danger'}>
                        {formatCurrency(Math.abs(totalPaid - totalOwed))}
                      </span>
                    </th>
                    <th className="text-end">100.0%</th>
                  </tr>
                </tfoot>
              </table>
            </div>
          ) : (
            <div className="text-center py-3 text-muted">
              <FaUsers size={32} className="mb-2" />
              <p>No split details available</p>
            </div>
          )}
        </div>
      </Modal.Body>
      
      <Modal.Footer>
        <Button variant="secondary" onClick={onHide}>
          Close
        </Button>
        {isCreatedByUser && (
          <Button variant="primary" onClick={handleEdit}>
            <FaEdit className="me-1" /> Edit
          </Button>
        )}
      </Modal.Footer>
    </Modal>
  );
}

export default ExpenseDetailsModal;