import { useState, useEffect } from 'react';
import { Modal, Button } from 'react-bootstrap';
import { FaReceipt, FaEquals, FaDollarSign } from 'react-icons/fa';
import { useAuth } from '../../utils/auth';
import { apiService } from '../../services/apiService';

function CreateExpenseModal({ show, onHide, onSubmit, groups, categories, preSelectedGroup }) {
  const { currentUser } = useAuth();
  const [formData, setFormData] = useState({
    name: '',
    totalCost: '',
    groupId: preSelectedGroup || '',
    category: '',
    description: '',
    date: new Date().toISOString().split('T')[0],
    receipt: null
  });
  
  const [groupMembers, setGroupMembers] = useState([]);
  const [userExpenses, setUserExpenses] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (formData.groupId) {
      loadGroupMembers(formData.groupId);
    }
  }, [formData.groupId]);

  useEffect(() => {
    if (groupMembers.length > 0) {
      initializeUserExpenses();
    }
  }, [groupMembers, formData.totalCost]);

  useEffect(() => {
    if (preSelectedGroup) {
      setFormData(prev => ({ ...prev, groupId: preSelectedGroup }));
    }
  }, [preSelectedGroup]);

  const loadGroupMembers = async (groupId) => {
    try {
      const userGroups = await apiService.getGroupMembers(groupId);
      const members = userGroups.map(userGroup => ({
        id: userGroup.user.appUserId,
        name: `${userGroup.user.firstName} ${userGroup.user.lastName}`,
        email: userGroup.user.email,
        isAdmin: userGroup.admin
      }));
      
      setGroupMembers(members);
    } catch (err) {
      console.error('Failed to load group members:', err);
    }
  };

  const initializeUserExpenses = () => {
    if (groupMembers.length === 0) return;

    const totalCost = parseFloat(formData.totalCost) || 0;
    const equalShare = totalCost / groupMembers.length;

    // Default: current user pays, everyone owes equal share
    const newUserExpenses = groupMembers.map(member => ({
      userId: member.id,
      name: member.name,
      amountOwed: equalShare,
      amountPaid: member.id === currentUser?.userId ? totalCost : 0,
      included: true
    }));

    setUserExpenses(newUserExpenses);
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleFileChange = (e) => {
    const file = e.target.files[0];
    setFormData(prev => ({ ...prev, receipt: file }));
  };

  const handleAmountOwedChange = (userId, value) => {
    const updatedUserExpenses = userExpenses.map(userExpense => {
      if (userExpense.userId === userId) {
        return { ...userExpense, amountOwed: parseFloat(value) || 0 };
      }
      return userExpense;
    });
    setUserExpenses(updatedUserExpenses);
  };

  const handlePayerChange = (newPayerId) => {
    const totalCost = parseFloat(formData.totalCost) || 0;
    const updatedUserExpenses = userExpenses.map(userExpense => ({
      ...userExpense,
      amountPaid: userExpense.userId === parseInt(newPayerId) ? totalCost : 0
    }));
    setUserExpenses(updatedUserExpenses);
  };

  const toggleMemberInclusion = (userId) => {
    const updatedUserExpenses = userExpenses.map(userExpense => {
      if (userExpense.userId === userId) {
        return { 
          ...userExpense, 
          included: !userExpense.included,
          amountOwed: !userExpense.included ? userExpense.amountOwed : 0
        };
      }
      return userExpense;
    });
    setUserExpenses(updatedUserExpenses);
    
    // Recalculate equal splits for included members
    const includedMembers = updatedUserExpenses.filter(ue => ue.included);
    if (includedMembers.length > 0) {
      const totalCost = parseFloat(formData.totalCost) || 0;
      const equalShare = totalCost / includedMembers.length;
      
      setUserExpenses(updatedUserExpenses.map(ue => ({
        ...ue,
        amountOwed: ue.included ? equalShare : 0
      })));
    }
  };

  const validateUserExpenses = () => {
    const includedUserExpenses = userExpenses.filter(ue => ue.included);
    const totalOwed = includedUserExpenses.reduce((sum, ue) => sum + (ue.amountOwed || 0), 0);
    const totalPaid = userExpenses.reduce((sum, ue) => sum + (ue.amountPaid || 0), 0);
    const expenseAmount = parseFloat(formData.totalCost) || 0;
    
    return Math.abs(totalOwed - expenseAmount) < 0.01 && Math.abs(totalPaid - expenseAmount) < 0.01;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Validate form
    if (!formData.name.trim()) {
      setError('Name is required');
      return;
    }
    if (!formData.totalCost || parseFloat(formData.totalCost) <= 0) {
      setError('Total cost must be greater than 0');
      return;
    }
    if (!formData.groupId) {
      setError('Please select a group');
      return;
    }
    if (!formData.category) {
      setError('Please select a category');
      return;
    }
    if (!validateUserExpenses()) {
      setError('Split amounts do not match the total expense amount or payment amount is incorrect');
      return;
    }

    // Check if we have current user
    if (!currentUser || !currentUser.userId) {
      setError('User not authenticated');
      return;
    }

    try {
      setLoading(true);
      setError('');
      
      const expenseData = {
        name: formData.name.trim(),
        totalCost: parseFloat(formData.totalCost),
        groupId: parseInt(formData.groupId),
        category: formData.category,
        description: formData.description.trim() || null,
        createdBy: currentUser.userId,
        createdAt: formData.date, 
        // Send userExpenses for the user_expense table
        userExpenses: userExpenses
          .filter(ue => ue.included)
          .map(ue => ({
            userId: ue.userId,
            amountOwed: ue.amountOwed,
            // Preserve who actually paid. This was hardcoded to 0, which discarded
            // the payment and left every balance showing "owes" with nobody credited.
            amountPaid: ue.amountPaid || 0
          }))
      };

      const createdExpense = await onSubmit(expenseData);
      console.log(createdExpense);
      // Upload receipt if provided
      if (formData.receipt) {
        console.log("provided receipt", formData.receipt);
        try {
          console.log("sending receipt",formData);
          await apiService.uploadExpenseReceipt(createdExpense.expenseId, formData.receipt);
        } catch (receiptError) {
          console.error('Failed to upload receipt:', receiptError);
          setError('Expense created but receipt upload failed. You can add it later.');
          return;
        }
      }

      
      // Reset form
      setFormData({
        name: '',
        totalCost: '',
        groupId: preSelectedGroup || '',
        category: '',
        description: '',
        date: new Date().toISOString().split('T')[0],
        receipt: null
      });
      setUserExpenses([]);
    } catch (err) {
      setError(err.message || 'Failed to create expense');
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    setFormData({
      name: '',
      totalCost: '',
      groupId: preSelectedGroup || '',
      category: '',
      description: '',
      date: new Date().toISOString().split('T')[0],
      receipt: null
    });
    setUserExpenses([]);
    setError('');
    onHide();
  };

  const totalOwed = userExpenses.reduce((sum, ue) => sum + (ue.included ? ue.amountOwed || 0 : 0), 0);
  const totalPaid = userExpenses.reduce((sum, ue) => sum + (ue.amountPaid || 0), 0);
  const expenseAmount = parseFloat(formData.totalCost) || 0;
  const currentPayer = userExpenses.find(ue => ue.amountPaid > 0);

  return (
    <Modal show={show} onHide={handleClose} size="xl">
      <Modal.Header closeButton>
        <Modal.Title>
          <FaReceipt className="me-2" />
          Add New Expense
        </Modal.Title>
      </Modal.Header>
      
      <form onSubmit={handleSubmit}>
        <Modal.Body>
          {error && (
            <div className="alert alert-danger">{error}</div>
          )}

          <div className="row">
            <div className="col-md-6">
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
                    <label className="form-label">Total Cost *</label>
                    <div className="input-group">
                      <span className="input-group-text">$</span>
                      <input
                        type="number"
                        className="form-control"
                        name="totalCost"
                        value={formData.totalCost}
                        onChange={handleChange}
                        placeholder="0.00"
                        step="0.01"
                        min="0"
                        required
                      />
                    </div>
                  </div>
                </div>
                <div className="col-md-6">
                  <div className="mb-3">
                    <label className="form-label">Date *</label>
                    <input
                      type="date"
                      className="form-control"
                      name="date"
                      value={formData.date}
                      onChange={handleChange}
                      required
                    />
                  </div>
                </div>
              </div>

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

              <div className="mb-3">
                <label className="form-label">Group *</label>
                <select
                  className="form-select"
                  name="groupId"
                  value={formData.groupId}
                  onChange={handleChange}
                  required
                >
                  <option value="">Select a group</option>
                  {groups.map(group => (
                    <option key={group.id || group.groupId} value={group.id || group.groupId}>
                      {group.name}
                    </option>
                  ))}
                </select>
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
                <label className="form-label">Receipt</label>
                <input
                  type="file"
                  className="form-control"
                  accept="image/*"
                  onChange={handleFileChange}
                />
                <small className="text-muted">Upload a photo of the receipt (optional)</small>
                {formData.receipt && (
                  <div className="mt-2 p-2 border rounded bg-light">
                    <small className="text-success d-block mb-2">
                      ✓ Selected: {formData.receipt.name}
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

            <div className="col-md-6">
              {/* Split Configuration */}
              <div className="mb-3">
                <label className="form-label">Split Among Members</label>
                <small className="text-muted d-block">Choose who owes what amount</small>
              </div>

              {/* User Expenses Details */}
              {groupMembers.length > 0 && (
                <div className="mb-3">
                  <label className="form-label">Who Owes What</label>
                  <div className="border rounded p-3">
                    <div className="row mb-2 fw-bold">
                      <div className="col-1"></div>
                      <div className="col-4">Member</div>
                      <div className="col-3">Amount Owed</div>
                      <div className="col-2">Paid</div>
                      <div className="col-2">%</div>
                    </div>
                    <hr className="my-2" />
                    
                    {userExpenses.map(userExpense => (
                      <div key={userExpense.userId} className="row align-items-center mb-2">
                        <div className="col-1">
                          <input
                            type="checkbox"
                            className="form-check-input"
                            checked={userExpense.included}
                            onChange={() => toggleMemberInclusion(userExpense.userId)}
                          />
                        </div>
                        <div className="col-4">
                          <span className={userExpense.included ? '' : 'text-muted'}>
                            {userExpense.name}
                            {userExpense.userId === currentUser?.userId && ' (You)'}
                          </span>
                        </div>
                        <div className="col-3">
                          <div className="input-group input-group-sm">
                            <span className="input-group-text">$</span>
                            <input
                              type="number"
                              className="form-control"
                              value={userExpense.amountOwed || ''}
                              onChange={(e) => handleAmountOwedChange(userExpense.userId, e.target.value)}
                              disabled={!userExpense.included}
                              min="0"
                              step="0.01"
                              placeholder="0.00"
                            />
                          </div>
                        </div>
                        <div className="col-2 text-center">
                          {userExpense.amountPaid > 0 ? (
                            <span className="badge bg-success">
                              ${userExpense.amountPaid.toFixed(2)}
                            </span>
                          ) : (
                            <span className="text-muted">$0.00</span>
                          )}
                        </div>
                        <div className="col-2 text-end">
                          <small className="text-muted">
                            {userExpense.included && expenseAmount > 0 
                              ? `${((userExpense.amountOwed / expenseAmount) * 100).toFixed(1)}%`
                              : '0%'
                            }
                          </small>
                        </div>
                      </div>
                    ))}
                    
                    <hr />
                    <div className="row">
                      <div className="col-5">
                        <strong>Total Owed:</strong>
                      </div>
                      <div className="col-3 text-end">
                        <strong className={Math.abs(totalOwed - expenseAmount) < 0.01 ? 'text-success' : 'text-danger'}>
                          ${totalOwed.toFixed(2)}
                        </strong>
                      </div>
                      <div className="col-2 text-end">
                        <strong className={Math.abs(totalPaid - expenseAmount) < 0.01 ? 'text-success' : 'text-danger'}>
                          ${totalPaid.toFixed(2)}
                        </strong>
                      </div>
                      <div className="col-2"></div>
                    </div>
                    <div className="row">
                      <div className="col-5">
                        <small className="text-muted">Expense Total:</small>
                      </div>
                      <div className="col-3 text-end">
                        <small className="text-muted">${expenseAmount.toFixed(2)}</small>
                      </div>
                      <div className="col-2 text-end">
                        <small className="text-muted">${expenseAmount.toFixed(2)}</small>
                      </div>
                      <div className="col-2"></div>
                    </div>
                    
                    {(Math.abs(totalOwed - expenseAmount) >= 0.01 || Math.abs(totalPaid - expenseAmount) >= 0.01) && (
                      <div className="row mt-2">
                        <div className="col-12">
                          <small className="text-danger">
                            {Math.abs(totalOwed - expenseAmount) >= 0.01 && 
                              `Owed difference: ${Math.abs(totalOwed - expenseAmount).toFixed(2)}`
                            }
                            {Math.abs(totalOwed - expenseAmount) >= 0.01 && Math.abs(totalPaid - expenseAmount) >= 0.01 && ' | '}
                            {Math.abs(totalPaid - expenseAmount) >= 0.01 && 
                              `Paid difference: ${Math.abs(totalPaid - expenseAmount).toFixed(2)}`
                            }
                          </small>
                        </div>
                      </div>
                    )}
                    
                    <div className="mt-2">
                      <button
                        type="button"
                        className="btn btn-outline-primary btn-sm"
                        onClick={() => {
                          const includedMembers = userExpenses.filter(ue => ue.included);
                          if (includedMembers.length > 0) {
                            const equalShare = expenseAmount / includedMembers.length;
                            setUserExpenses(userExpenses.map(ue => ({
                              ...ue,
                              amountOwed: ue.included ? equalShare : 0
                            })));
                          }
                        }}
                      >
                        <FaEquals className="me-1" /> Split Equally
                      </button>
                    </div>
                  </div>
                </div>
              )}
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
            disabled={loading || !validateUserExpenses()}
          >
            {loading ? (
              <>
                <span className="spinner-border spinner-border-sm me-2"></span>
                Adding...
              </>
            ) : (
              'Add Expense'
            )}
          </Button>
        </Modal.Footer>
      </form>
    </Modal>
  );
}

export default CreateExpenseModal;