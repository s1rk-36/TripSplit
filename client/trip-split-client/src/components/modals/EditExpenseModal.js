import { useState, useEffect } from 'react';
import { Modal, Button } from 'react-bootstrap';
import { FaEdit, FaTrash, FaDownload, FaFileImage } from 'react-icons/fa';
import { useAuth } from '../../utils/auth';
import { apiService } from '../../services/apiService';

function EditExpenseModal({ show, onHide, expense, onSubmit, groups, categories }) {
  const { currentUser } = useAuth();
  const [formData, setFormData] = useState({
    name: '',
    totalCost: '',
    groupId: '',
    category: '',
    description: '',
    date: '',
    receipt: null
  });
  
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [receipts, setReceipts] = useState([]);
  const [receiptLoading, setReceiptLoading] = useState(false);

  useEffect(() => {
    if (expense && show) {
      // Populate form with expense data
      setFormData({
        name: expense.name || '',
        totalCost: expense.totalCost?.toString() || '',
        groupId: expense.groupId?.toString() || '',
        category: expense.category || '',
        description: expense.description || '',
        date: expense.createdAt ? expense.createdAt.split('T')[0] : '',
        receipt: null // Don't pre-populate file input
      });

      // Load existing receipts if available
      if (expense.hasReceipt || expense.expenseId) {
        loadReceipts();
      } else {
        setReceipts([]);
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

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleFileChange = (e) => {
    const file = e.target.files[0];
    setFormData(prev => ({ ...prev, receipt: file }));
  };

  const handleDeleteReceipt = async (receiptId) => {
    if (!window.confirm('Are you sure you want to delete this receipt?')) {
      return;
    }

    try {
      setReceiptLoading(true);
      await apiService.deleteExpenseReceipt(receiptId);
      await loadReceipts(); // Reload receipts after deletion
    } catch (err) {
      console.error('Failed to delete receipt:', err);
      setError('Failed to delete receipt');
    } finally {
      setReceiptLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Validate form
    if (!formData.name.trim()) {
      setError('Name is required');
      return;
    }
    if (!formData.category) {
      setError('Please select a category');
      return;
    }

    try {
      setLoading(true);
      setError('');
      
      const expenseData = {
        expenseId: expense.expenseId,
        name: formData.name.trim(),
        totalCost: expense.totalCost, 
        groupId: expense.groupId,
        category: formData.category,
        description: formData.description.trim() || null,
        createdBy: expense.createdBy,
        createdAt: formData.date ? `${formData.date}T00:00:00` : expense.createdAt,
        userExpenses: expense.userExpenses
      };
      
      await onSubmit(expense.expenseId, expenseData);
      
      // Handle receipt upload if new file selected
      if (formData.receipt) {
        try {
          await apiService.uploadExpenseReceipt(expense.expenseId, formData.receipt);
          // Reload receipts after upload
          await loadReceipts();
        } catch (receiptError) {
          console.error('Failed to upload receipt:', receiptError);
          setError('Expense updated but receipt upload failed');
        }
      }
      
    } catch (err) {
      setError(err.message || 'Failed to update expense');
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    setFormData({
      name: '',
      totalCost: '',
      groupId: '',
      category: '',
      description: '',
      date: '',
      receipt: null
    });
    setReceipts([]);
    setError('');
    onHide();
  };

  if (!expense) return null;

  return (
    <Modal show={show} onHide={handleClose} size="lg">
      <Modal.Header closeButton>
        <Modal.Title>
          <FaEdit className="me-2" />
          Edit Expense
        </Modal.Title>
      </Modal.Header>
      
      <form onSubmit={handleSubmit}>
        <Modal.Body>
          {error && (
            <div className="alert alert-danger">{error}</div>
          )}

          <div className="row">
            <div className="col-md-12">
              {/* Basic Info */}
              <div className="mb-3">
                <label className="form-label">Name *</label>
                <input
                  type="text"
                  className="form-control"
                  name="name"
                  value={formData.name}
                  onChange={handleChange}
                  placeholder="e.g. Dinner at Tokyo Restaurant"
                  required
                />
              </div>

              <div className="row">
                <div className="col-md-6">
                  <div className="mb-3">
                    <label className="form-label">Total Cost (Read Only)</label>
                    <div className="input-group">
                      <span className="input-group-text">$</span>
                      <input
                        type="text"
                        className="form-control"
                        value={expense?.totalCost?.toFixed(2) || '0.00'}
                        disabled
                        style={{ backgroundColor: '#f8f9fa' }}
                      />
                    </div>
                    <small className="text-muted">Total cost cannot be changed after creation</small>
                  </div>
                </div>
                <div className="col-md-6">
                  <div className="mb-3">
                    <label className="form-label">Date</label>
                    <input
                      type="date"
                      className="form-control"
                      name="date"
                      value={formData.date}
                      onChange={handleChange}
                    />
                  </div>
                </div>
              </div>

              <div className="row">
                <div className="col-md-6">
                  <div className="mb-3">
                    <label className="form-label">Group (Read Only)</label>
                    <input
                      type="text"
                      className="form-control"
                      value={groups?.find(g => (g.id || g.groupId) === expense?.groupId)?.name || 'Unknown Group'}
                      disabled
                      style={{ backgroundColor: '#f8f9fa' }}
                    />
                    <small className="text-muted">Group cannot be changed after creation</small>
                  </div>
                </div>
                <div className="col-md-6">
                  <div className="mb-3">
                    <label className="form-label">Category *</label>
                    <select
                      className="form-select"
                      name="category"
                      value={formData.category}
                      onChange={handleChange}
                      required
                    >
                      <option value="">Select a category</option>
                      {categories.map(category => (
                        <option key={category} value={category}>
                          {category}
                        </option>
                      ))}
                    </select>
                  </div>
                </div>
              </div>

              <div className="mb-3">
                <label className="form-label">Description</label>
                <textarea
                  className="form-control"
                  name="description"
                  value={formData.description}
                  onChange={handleChange}
                  rows="3"
                  placeholder="Additional details about the expense (optional)"
                />
              </div>

              <div className="mb-3">
                <label className="form-label">Receipts</label>
                
                {/* Show existing receipts */}
                {receipts.length > 0 && (
                  <div className="mb-3">
                    <h6>Current Receipts</h6>
                    {receipts.map(receipt => (
                      <div key={receipt.receiptId} className="p-3 border rounded mb-2">
                        <div className="d-flex justify-content-between align-items-center">
                          <div className="d-flex align-items-center">
                            <FaFileImage className="text-info me-2" />
                            <div>
                              <span className="text-success">Receipt #{receipt.receiptId}</span>
                              <br />
                              <small className="text-muted">
                                Uploaded: {new Date(receipt.uploadedAt).toLocaleDateString()}
                              </small>
                            </div>
                          </div>
                          <div className="btn-group">
                            {receipt.imageUrl && (
                              <a
                                href={receipt.imageUrl}
                                target="_blank"
                                rel="noopener noreferrer"
                                className="btn btn-outline-primary btn-sm"
                                disabled={receiptLoading}
                              >
                                <FaDownload className="me-1" /> View
                              </a>
                            )}
                            <button
                              type="button"
                              className="btn btn-outline-danger btn-sm"
                              onClick={() => handleDeleteReceipt(receipt.receiptId)}
                              disabled={receiptLoading}
                            >
                              <FaTrash className="me-1" /> Delete
                            </button>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
                
                {receiptLoading && (
                  <div className="mb-2">
                    <small className="text-muted">
                      <div className="spinner-border spinner-border-sm me-2"></div>
                      Loading receipts...
                    </small>
                  </div>
                )}
                
                {/* Upload new receipt */}
                <div>
                  <label className="form-label">Add New Receipt</label>
                  <input
                    type="file"
                    className="form-control"
                    accept="image/*"
                    onChange={handleFileChange}
                  />
                  <small className="text-muted">
                    Upload a new receipt image (optional)
                  </small>
                  {formData.receipt && (
                    <div className="mt-2 p-2 border rounded bg-light">
                      <small className="text-success d-block mb-2">
                        ✓ New file selected: {formData.receipt.name}
                      </small>
                      <img
                        src={URL.createObjectURL(formData.receipt)}
                        alt="Receipt preview"
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
              </div>
            </div>
          </div>
        </Modal.Body>
        
        <Modal.Footer>
          <Button variant="secondary" onClick={handleClose}>
            Cancel
          </Button>
          <Button 
            variant="primary" 
            type="submit"
            disabled={loading}
          >
            {loading ? (
              <>
                <span className="spinner-border spinner-border-sm me-2"></span>
                Updating...
              </>
            ) : (
              'Update Expense'
            )}
          </Button>
        </Modal.Footer>
      </form>
    </Modal>
  );
}

export default EditExpenseModal;