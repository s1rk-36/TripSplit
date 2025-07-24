import { useState, useEffect } from 'react';
import { Modal, Button, Tab, Tabs } from 'react-bootstrap';
import { FaReceipt, FaEdit, FaTrash, FaComment, FaFileImage, FaDownload, FaUsers, FaDollarSign, FaCalendar, FaTag, FaUser, FaCopy, FaPaperPlane } from 'react-icons/fa';
import { apiService } from '../../services/apiService';
import { useAuth } from '../../utils/auth';
import { formatCurrency } from '../../utils/helpers';

function ExpenseDetailsModal({ show, onHide, expense, groups, onEdit, onDelete }) {
  const { currentUser } = useAuth();
  const [activeTab, setActiveTab] = useState('details');
  const [comments, setComments] = useState([]);
  const [receipts, setReceipts] = useState([]);
  const [newComment, setNewComment] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (show && expense) {
      loadExpenseData();
      setActiveTab('details'); // Reset to details tab when opening
    }
  }, [show, expense]);

  const loadExpenseData = async () => {
    if (!expense) return;

    try {
      setLoading(true);
      setError('');
      
      // temp data
      const commentsData = expense.comments || [];
      const receiptsData = expense.receipts || (expense.hasReceipt ? [{ id: 1, filename: 'receipt.jpg', url: '#' }] : []);
      
      setComments(commentsData);
      setReceipts(receiptsData);
      
    } catch (err) {
      console.error('Failed to load expense data:', err);
      setError(err.message || 'Failed to load expense details');
    } finally {
      setLoading(false);
    }
  };

  const handleAddComment = async (e) => {
    e.preventDefault();
    if (!newComment.trim()) return;

    try {
      // This would be an API call: await apiService.addComment(expense.id, newComment)
      const comment = {
        id: Date.now(),
        text: newComment.trim(),
        author: `${currentUser?.firstName} ${currentUser?.lastName}`,
        authorId: currentUser?.userId,
        createdAt: new Date().toISOString()
      };
      
      setComments([...comments, comment]);
      setNewComment('');
    } catch (err) {
      console.error('Failed to add comment:', err);
      setError(err.message || 'Failed to add comment');
    }
  };

  const handleDownloadReceipt = async (receiptId, filename) => {
    try {
      // This would download the receipt file
      // Implementation depends on your backend API
      console.log('Downloading receipt:', receiptId, filename);
      // await apiService.downloadReceipt(receiptId);
    } catch (err) {
      console.error('Failed to download receipt:', err);
      setError(err.message || 'Failed to download receipt');
    }
  };

  const handleCopyExpenseId = () => {
    navigator.clipboard.writeText(expense?.id?.toString() || '');
    alert('Expense ID copied to clipboard!');
  };

  if (!expense) return null;

  const group = groups?.find(g => (g.id || g.groupId) === expense.groupId);
  const userSplit = expense.splits?.find(split => split.userId === currentUser?.userId);
  const isPaidByUser = expense.paidBy === currentUser?.userId;
  const totalSplitAmount = expense.splits?.reduce((sum, split) => sum + (split.amount || 0), 0) || 0;

  return (
    <Modal show={show} onHide={onHide} size="xl">
      <Modal.Header closeButton>
        <Modal.Title>
          <FaReceipt className="me-2" />
          {expense.description}
        </Modal.Title>
      </Modal.Header>
      
      <Modal.Body>
        {error && (
          <div className="alert alert-danger">{error}</div>
        )}

        {/* Expense Header Info */}
        <div className="row mb-4">
          <div className="col-md-8">
            <div className="d-flex gap-4 mb-3">
              <div>
                <h4 className="text-primary mb-0">{formatCurrency(expense.amount)}</h4>
                <small className="text-muted">Total Amount</small>
              </div>
              <div>
                <h5 className="mb-0">{formatCurrency(userSplit?.amount || 0)}</h5>
                <small className="text-muted">Your Share</small>
              </div>
              <div>
                <h5 className={`mb-0 ${isPaidByUser ? 'text-success' : 'text-muted'}`}>
                  {isPaidByUser ? formatCurrency(expense.amount) : '$0.00'}
                </h5>
                <small className="text-muted">You Paid</small>
              </div>
            </div>
            
            <div className="d-flex gap-3 flex-wrap mb-3">
              <span className="d-flex align-items-center text-muted">
                <FaUsers className="me-1" />
                {group?.name || 'Unknown Group'}
              </span>
              <span className="d-flex align-items-center text-muted">
                <FaCalendar className="me-1" />
                {new Date(expense.date).toLocaleDateString()}
              </span>
              {expense.category && (
                <span className="d-flex align-items-center text-muted">
                  <FaTag className="me-1" />
                  {expense.category}
                </span>
              )}
              <span className="d-flex align-items-center text-muted">
                <FaUser className="me-1" />
                Paid by {expense.paidByName || 'Unknown'}{isPaidByUser ? ' (You)' : ''}
              </span>
            </div>

            {expense.notes && (
              <div className="mb-3">
                <strong>Notes:</strong>
                <p className="text-muted mb-0 mt-1">{expense.notes}</p>
              </div>
            )}
          </div>
          
          <div className="col-md-4 text-end">
            <div className="btn-group mb-3">
              <button
                className="btn btn-outline-secondary btn-sm"
                onClick={handleCopyExpenseId}
                title="Copy Expense ID"
              >
                <FaCopy className="me-1" /> Copy ID
              </button>
              {isPaidByUser && (
                <>
                  <button
                    className="btn btn-outline-primary btn-sm"
                    onClick={() => {
                      onHide();
                      onEdit(expense);
                    }}
                  >
                    <FaEdit className="me-1" /> Edit
                  </button>
                  <button
                    className="btn btn-outline-danger btn-sm"
                    onClick={() => {
                      if (window.confirm('Are you sure you want to delete this expense?')) {
                        onHide();
                        onDelete(expense.id);
                      }
                    }}
                  >
                    <FaTrash className="me-1" /> Delete
                  </button>
                </>
              )}
            </div>
            
            <div className="d-flex gap-2 justify-content-end flex-wrap">
              {receipts.length > 0 && (
                <span className="badge bg-info">
                  <FaFileImage className="me-1" />
                  {receipts.length} Receipt{receipts.length > 1 ? 's' : ''}
                </span>
              )}
              {comments.length > 0 && (
                <span className="badge bg-secondary">
                  <FaComment className="me-1" />
                  {comments.length} Comment{comments.length > 1 ? 's' : ''}
                </span>
              )}
              <span className="badge bg-primary">
                {expense.splitType || 'Equal'} Split
              </span>
            </div>
          </div>
        </div>

        {/* Tabs */}
        <Tabs activeKey={activeTab} onSelect={(tab) => setActiveTab(tab)} className="mb-3">
          {/* Split Details Tab */}
          <Tab eventKey="details" title="Split Details">
            <div className="row">
              <div className="col-md-8">
                <h6>How this expense was split:</h6>
                <div className="table-responsive">
                  <table className="table table-sm">
                    <thead>
                      <tr>
                        <th>Person</th>
                        <th>Amount</th>
                        <th>Percentage</th>
                        <th>Status</th>
                      </tr>
                    </thead>
                    <tbody>
                      {expense.splits?.map(split => (
                        <tr key={split.userId}>
                          <td>
                            <div className="d-flex align-items-center">
                              <FaUser className="text-muted me-2" size={12} />
                              {split.name || split.userName || 'Unknown User'}
                              {split.userId === currentUser?.userId && ' (You)'}
                              {split.userId === expense.paidBy && ' 💳'}
                            </div>
                          </td>
                          <td>
                            <span className="fw-medium">{formatCurrency(split.amount)}</span>
                          </td>
                          <td>
                            <span className="text-muted">
                              {expense.amount > 0 ? ((split.amount / expense.amount) * 100).toFixed(1) : 0}%
                            </span>
                          </td>
                          <td>
                            {split.userId === expense.paidBy ? (
                              <span className="badge bg-success">Paid</span>
                            ) : split.settled ? (
                              <span className="badge bg-info">Settled</span>
                            ) : (
                              <span className="badge bg-warning text-dark">Owes</span>
                            )}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                    <tfoot>
                      <tr className="table-light">
                        <th>Total</th>
                        <th>{formatCurrency(totalSplitAmount)}</th>
                        <th>100.0%</th>
                        <th></th>
                      </tr>
                    </tfoot>
                  </table>
                </div>
              </div>
              
              <div className="col-md-4">
                <h6>Summary</h6>
                <div className="card">
                  <div className="card-body">
                    <div className="mb-2">
                      <small className="text-muted">Expense ID</small>
                      <div className="font-monospace">{expense.id}</div>
                    </div>
                    <div className="mb-2">
                      <small className="text-muted">Created</small>
                      <div>{expense.createdAt ? new Date(expense.createdAt).toLocaleDateString() : 'Unknown'}</div>
                    </div>
                    <div className="mb-2">
                      <small className="text-muted">Split Method</small>
                      <div>
                        <span className="badge bg-primary">
                          {expense.splitType === 'equal' && 'Equal Split'}
                          {expense.splitType === 'percentage' && 'Percentage Split'}
                          {expense.splitType === 'amount' && 'Custom Amounts'}
                          {expense.splitType === 'shares' && 'Share-based'}
                          {!expense.splitType && 'Equal Split'}
                        </span>
                      </div>
                    </div>
                    <div className="mb-2">
                      <small className="text-muted">People Involved</small>
                      <div>{expense.splits?.length || 0} members</div>
                    </div>
                    {userSplit && (
                      <div className="mt-3 p-2 bg-light rounded">
                        <small className="text-muted">Your Balance</small>
                        <div className={`fw-bold ${isPaidByUser ? 'text-success' : 'text-danger'}`}>
                          {isPaidByUser 
                            ? `+${formatCurrency(expense.amount - userSplit.amount)}` 
                            : `-${formatCurrency(userSplit.amount)}`
                          }
                        </div>
                        <small className="text-muted">
                          {isPaidByUser ? 'You are owed' : 'You owe'}
                        </small>
                      </div>
                    )}
                  </div>
                </div>
              </div>
            </div>
          </Tab>

          {/* Receipts Tab */}
          <Tab eventKey="receipts" title={`Receipts (${receipts.length})`}>
            <h6>Receipt Images</h6>
            {receipts.length === 0 ? (
              <div className="text-center py-4">
                <FaFileImage className="text-muted mb-3" size={48} />
                <h5>No Receipts</h5>
                <p className="text-muted">No receipt images were uploaded for this expense.</p>
              </div>
            ) : (
              <div className="row">
                {receipts.map(receipt => (
                  <div key={receipt.id} className="col-md-4 mb-3">
                    <div className="card">
                      <div className="card-img-top bg-light d-flex align-items-center justify-content-center" style={{height: '200px'}}>
                        <FaFileImage size={48} className="text-muted" />
                      </div>
                      <div className="card-body">
                        <h6 className="card-title">{receipt.filename}</h6>
                        <p className="card-text">
                          <small className="text-muted">
                            Uploaded {receipt.uploadedAt ? new Date(receipt.uploadedAt).toLocaleDateString() : 'recently'}
                          </small>
                        </p>
                        <button
                          className="btn btn-outline-primary btn-sm"
                          onClick={() => handleDownloadReceipt(receipt.id, receipt.filename)}
                        >
                          <FaDownload className="me-1" /> Download
                        </button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </Tab>

          {/* Comments Tab */}
          <Tab eventKey="comments" title={`Comments (${comments.length})`}>
            <div className="mb-4">
              <h6>Discussion</h6>
              
              {/* Add Comment Form */}
              <form onSubmit={handleAddComment} className="mb-4">
                <div className="input-group">
                  <input
                    type="text"
                    className="form-control"
                    placeholder="Add a comment..."
                    value={newComment}
                    onChange={(e) => setNewComment(e.target.value)}
                  />
                  <button 
                    type="submit" 
                    className="btn btn-outline-primary"
                    disabled={!newComment.trim()}
                  >
                    <FaPaperPlane />
                  </button>
                </div>
              </form>

              {/* Comments List */}
              {comments.length === 0 ? (
                <div className="text-center py-4">
                  <FaComment className="text-muted mb-3" size={48} />
                  <h5>No Comments</h5>
                  <p className="text-muted">Be the first to comment on this expense.</p>
                </div>
              ) : (
                <div className="comments-list">
                  {comments.map(comment => (
                    <div key={comment.id} className="card mb-3">
                      <div className="card-body">
                        <div className="d-flex justify-content-between align-items-start mb-2">
                          <div className="d-flex align-items-center">
                            <div className="bg-primary text-white rounded-circle d-flex align-items-center justify-content-center me-2" style={{width: '32px', height: '32px'}}>
                              <FaUser size={14} />
                            </div>
                            <div>
                              <strong>{comment.author}</strong>
                              {comment.authorId === currentUser?.userId && (
                                <span className="badge bg-light text-dark ms-2">You</span>
                              )}
                            </div>
                          </div>
                          <small className="text-muted">
                            {new Date(comment.createdAt).toLocaleDateString()}
                          </small>
                        </div>
                        <p className="mb-0">{comment.text}</p>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </Tab>
        </Tabs>
      </Modal.Body>
      
      <Modal.Footer>
        <Button variant="secondary" onClick={onHide}>
          Close
        </Button>
        {isPaidByUser && (
          <Button 
            variant="primary" 
            onClick={() => {
              onHide();
              onEdit(expense);
            }}
          >
            <FaEdit className="me-1" /> Edit Expense
          </Button>
        )}
      </Modal.Footer>
    </Modal>
  );
}

export default ExpenseDetailsModal;
