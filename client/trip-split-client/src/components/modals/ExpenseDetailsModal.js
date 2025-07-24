import { useState, useEffect } from 'react';
import { Modal, Button } from 'react-bootstrap';
import { FaReceipt, FaCamera, FaEquals, FaPercentage, FaDollarSign, FaUsers, FaEdit } from 'react-icons/fa';
import { useAuth } from '../../utils/auth';

function EditExpenseModal({ show, onHide, expense, onSubmit, groups, categories }) {
  const { currentUser } = useAuth();
  const [formData, setFormData] = useState({
    description: '',
    amount: '',
    groupId: '',
    category: '',
    date: '',
    paidBy: '',
    splitType: 'equal',
    notes: '',
    receipt: null
  });
  const [groupMembers, setGroupMembers] = useState([]);
  const [splits, setSplits] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (expense && show) {
      // Populate form with expense data
      setFormData({
        description: expense.description || '',
        amount: expense.amount?.toString() || '',
        groupId: expense.groupId?.toString() || '',
        category: expense.category || '',
        date: expense.date || '',
        paidBy: expense.paidBy?.toString() || '',
        splitType: expense.splitType || 'equal',
        notes: expense.notes || '',
        receipt: null // Don't pre-populate file input
      });

      // Set existing splits
      if (expense.splits) {
        setSplits(expense.splits.map(split => ({
          userId: split.userId,
          name: split.name || split.userName || 'Unknown User',
          amount: split.amount || 0,
          percentage: expense.amount > 0 ? ((split.amount / expense.amount) * 100) : 0,
          shares: split.shares || 1,
          included: true
        })));
      }

      // Load group members
      if (expense.groupId) {
        loadGroupMembers(expense.groupId);
      }
    }
  }, [expense, show]);

  useEffect(() => {
    if (groupMembers.length > 0 && formData.splitType && formData.amount) {
      recalculateSplits();
    }
  }, [groupMembers, formData.splitType, formData.amount]);

  const loadGroupMembers = async (groupId) => {
    try {
      const group = groups.find(g => (g.id || g.groupId).toString() === groupId.toString());
      if (group && group.members) {
        setGroupMembers(group.members);
      } else if (group) {
        // If no members array, create a basic member list
        setGroupMembers([
          { id: currentUser?.userId, name: `${currentUser?.firstName} ${currentUser?.lastName}` }
        ]);
      }
    } catch (err) {
      console.error('Failed to load group members:', err);
    }
  };

  const recalculateSplits = () => {
    if (groupMembers.length === 0) return;

    const amount = parseFloat(formData.amount) || 0;
    
    // If we have existing splits, preserve them but recalculate if needed
    if (splits.length > 0) {
      if (formData.splitType === 'equal') {
        const includedMembers = splits.filter(split => split.included);
        const equalShare = includedMembers.length > 0 ? amount / includedMembers.length : 0;
        
        setSplits(splits.map(split => ({
          ...split,
          amount: split.included ? equalShare : 0,
          percentage: split.included ? (100 / includedMembers.length) : 0
        })));
      }
    } else {
      // Initialize splits if none exist
      initializeSplits();
    }
  };

  const initializeSplits = () => {
    if (groupMembers.length === 0) return;

    const amount = parseFloat(formData.amount) || 0;
    const equalShare = amount / groupMembers.length;

    const newSplits = groupMembers.map(member => ({
      userId: member.id,
      name: member.name,
      amount: formData.splitType === 'equal' ? equalShare : 0,
      percentage: formData.splitType === 'equal' ? (100 / groupMembers.length) : 0,
      shares: formData.splitType === 'equal' ? 1 : 0,
      included: true
    }));

    setSplits(newSplits);
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleFileChange = (e) => {
    const file = e.target.files[0];
    setFormData(prev => ({ ...prev, receipt: file }));
  };

  const handleSplitChange = (userId, field, value) => {
    const updatedSplits = splits.map(split => {
      if (split.userId === userId) {
        return { ...split, [field]: value };
      }
      return split;
    });

    // Recalculate based on split type
    if (formData.splitType === 'percentage') {
      const totalPercentage = updatedSplits.reduce((sum, split) => sum + (split.percentage || 0), 0);
      if (totalPercentage <= 100) {
        const amount = parseFloat(formData.amount) || 0;
        updatedSplits.forEach(split => {
          split.amount = (amount * (split.percentage || 0)) / 100;
        });
      }
    } else if (formData.splitType === 'shares') {
      const totalShares = updatedSplits.reduce((sum, split) => sum + (split.shares || 0), 0);
      if (totalShares > 0) {
        const amount = parseFloat(formData.amount) || 0;
        updatedSplits.forEach(split => {
          split.amount = (amount * (split.shares || 0)) / totalShares;
          split.percentage = ((split.shares || 0) / totalShares) * 100;
        });
      }
    }

    setSplits(updatedSplits);
  };

  const handleSplitTypeChange = (newSplitType) => {
    setFormData(prev => ({ ...prev, splitType: newSplitType }));
    
    // Recalculate splits based on new type
    if (newSplitType === 'equal') {
      recalculateSplits();
    }
  };

  const toggleMemberInclusion = (userId) => {
    const updatedSplits = splits.map(split => {
      if (split.userId === userId) {
        return { ...split, included: !split.included };
      }
      return split;
    });
    setSplits(updatedSplits);
    
    // Recalculate splits for included members only
    const includedMembers = updatedSplits.filter(split => split.included);
    if (includedMembers.length > 0 && formData.splitType === 'equal') {
      const amount = parseFloat(formData.amount) || 0;
      const equalShare = amount / includedMembers.length;
      
      setSplits(updatedSplits.map(split => ({
        ...split,
        amount: split.included ? equalShare : 0,
        percentage: split.included ? (100 / includedMembers.length) : 0
      })));
    }
  };

  const validateSplits = () => {
    const includedSplits = splits.filter(split => split.included);
    const totalSplitAmount = includedSplits.reduce((sum, split) => sum + (split.amount || 0), 0);
    const expenseAmount = parseFloat(formData.amount) || 0;
    
    return Math.abs(totalSplitAmount - expenseAmount) < 0.01;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Validate form
    if (!formData.description.trim()) {
      setError('Description is required');
      return;
    }
    if (!formData.amount || parseFloat(formData.amount) <= 0) {
      setError('Amount must be greater than 0');
      return;
    }
    if (!formData.groupId) {
      setError('Please select a group');
      return;
    }
    if (!validateSplits()) {
      setError('Split amounts do not match the total expense amount');
      return;
    }

    try {
      setLoading(true);
      setError('');
      
      const expenseData = {
        ...formData,
        amount: parseFloat(formData.amount),
        groupId: parseInt(formData.groupId),
        paidBy: parseInt(formData.paidBy),
        splits: splits.filter(split => split.included && split.amount > 0)
      };
      
      await onSubmit(expense.id, expenseData);
      
    } catch (err) {
      setError(err.message || 'Failed to update expense');
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    setFormData({
      description: '',
      amount: '',
      groupId: '',
      category: '',
      date: '',
      paidBy: '',
      splitType: 'equal',
      notes: '',
      receipt: null
    });
    setSplits([]);
    setError('');
    onHide();
  };

  if (!expense) return null;

  const totalSplitAmount = splits.reduce((sum, split) => sum + (split.included ? split.amount || 0 : 0), 0);
  const expenseAmount = parseFloat(formData.amount) || 0;

  return (
    <Modal show={show} onHide={handleClose} size="xl">
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
            <div className="col-md-6">
              {/* Basic Info */}
              <div className="mb-3">
                <label className="form-label">Description *</label>
                <input
                  type="text"
                  className="form-control"
                  name="description"
                  value={formData.description}
                  onChange={handleChange}
                  placeholder="e.g. Dinner at Tokyo Restaurant"
                  required
                />
              </div>

              <div className="row">
                <div className="col-md-6">
                  <div className="mb-3">
                    <label className="form-label">Amount *</label>
                    <div className="input-group">
                      <span className="input-group-text">$</span>
                      <input
                        type="number"
                        className="form-control"
                        name="amount"
                        value={formData.amount}
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
                <label className="form-label">Category</label>
                <select
                  className="form-select"
                  name="category"
                  value={formData.category}
                  onChange={handleChange}
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
                <label className="form-label">Paid By</label>
                <select
                  className="form-select"
                  name="paidBy"
                  value={formData.paidBy}
                  onChange={handleChange}
                >
                  {groupMembers.map(member => (
                    <option key={member.id} value={member.id}>
                      {member.name} {member.id === currentUser?.userId ? '(You)' : ''}
                    </option>
                  ))}
                </select>
              </div>

              <div className="mb-3">
                <label className="form-label">Receipt</label>
                <input
                  type="file"
                  className="form-control"
                  accept="image/*"
                  onChange={handleFileChange}
                />
                <small className="text-muted">Upload a new receipt image (optional)</small>
              </div>

              <div className="mb-3">
                <label className="form-label">Notes</label>
                <textarea
                  className="form-control"
                  name="notes"
                  value={formData.notes}
                  onChange={handleChange}
                  rows="2"
                  placeholder="Additional notes (optional)"
                />
              </div>
            </div>

            <div className="col-md-6">
              {/* Split Configuration */}
              <div className="mb-3">
                <label className="form-label">Split Method</label>
                <div className="btn-group w-100" role="group">
                  <input
                    type="radio"
                    className="btn-check"
                    name="splitType"
                    id="edit-split-equal"
                    checked={formData.splitType === 'equal'}
                    onChange={() => handleSplitTypeChange('equal')}
                  />
                  <label className="btn btn-outline-primary" htmlFor="edit-split-equal">
                    <FaEquals className="me-1" /> Equal
                  </label>

                  <input
                    type="radio"
                    className="btn-check"
                    name="splitType"
                    id="edit-split-percentage"
                    checked={formData.splitType === 'percentage'}
                    onChange={() => handleSplitTypeChange('percentage')}
                  />
                  <label className="btn btn-outline-primary" htmlFor="edit-split-percentage">
                    <FaPercentage className="me-1" /> %
                  </label>

                  <input
                    type="radio"
                    className="btn-check"
                    name="splitType"
                    id="edit-split-amount"
                    checked={formData.splitType === 'amount'}
                    onChange={() => handleSplitTypeChange('amount')}
                  />
                  <label className="btn btn-outline-primary" htmlFor="edit-split-amount">
                    <FaDollarSign className="me-1" /> Amount
                  </label>

                  <input
                    type="radio"
                    className="btn-check"
                    name="splitType"
                    id="edit-split-shares"
                    checked={formData.splitType === 'shares'}
                    onChange={() => handleSplitTypeChange('shares')}
                  />
                  <label className="btn btn-outline-primary" htmlFor="edit-split-shares">
                    <FaUsers className="me-1" /> Shares
                  </label>
                </div>
              </div>

              {/* Split Details */}
              {splits.length > 0 && (
                <div className="mb-3">
                  <label className="form-label">Split Details</label>
                  <div className="border rounded p-3">
                    {splits.map(split => (
                      <div key={split.userId} className="row align-items-center mb-2">
                        <div className="col-1">
                          <input
                            type="checkbox"
                            className="form-check-input"
                            checked={split.included}
                            onChange={() => toggleMemberInclusion(split.userId)}
                          />
                        </div>
                        <div className="col-4">
                          <span className={split.included ? '' : 'text-muted'}>
                            {split.name}
                          </span>
                        </div>
                        <div className="col-3">
                          {formData.splitType === 'percentage' ? (
                            <div className="input-group input-group-sm">
                              <input
                                type="number"
                                className="form-control"
                                value={split.percentage || ''}
                                onChange={(e) => handleSplitChange(split.userId, 'percentage', parseFloat(e.target.value) || 0)}
                                disabled={!split.included}
                                min="0"
                                max="100"
                              />
                              <span className="input-group-text">%</span>
                            </div>
                          ) : formData.splitType === 'shares' ? (
                            <input
                              type="number"
                              className="form-control form-control-sm"
                              value={split.shares || ''}
                              onChange={(e) => handleSplitChange(split.userId, 'shares', parseInt(e.target.value) || 0)}
                              disabled={!split.included}
                              min="0"
                              placeholder="Shares"
                            />
                          ) : formData.splitType === 'amount' ? (
                            <div className="input-group input-group-sm">
                              <span className="input-group-text">$</span>
                              <input
                                type="number"
                                className="form-control"
                                value={split.amount || ''}
                                onChange={(e) => handleSplitChange(split.userId, 'amount', parseFloat(e.target.value) || 0)}
                                disabled={!split.included}
                                min="0"
                                step="0.01"
                              />
                            </div>
                          ) : (
                            <span className="text-muted">
                              ${(split.amount || 0).toFixed(2)}
                            </span>
                          )}
                        </div>
                        <div className="col-4 text-end">
                          <small className="text-muted">
                            ${(split.amount || 0).toFixed(2)}
                          </small>
                        </div>
                      </div>
                    ))}
                    
                    <hr />
                    <div className="row">
                      <div className="col-8">
                        <strong>Total:</strong>
                      </div>
                      <div className="col-4 text-end">
                        <strong className={Math.abs(totalSplitAmount - expenseAmount) < 0.01 ? 'text-success' : 'text-danger'}>
                          ${totalSplitAmount.toFixed(2)}
                        </strong>
                      </div>
                    </div>
                    <div className="row">
                      <div className="col-8">
                        <small className="text-muted">Expense Amount:</small>
                      </div>
                      <div className="col-4 text-end">
                        <small className="text-muted">${expenseAmount.toFixed(2)}</small>
                      </div>
                    </div>
                    {Math.abs(totalSplitAmount - expenseAmount) >= 0.01 && (
                      <div className="row">
                        <div className="col-12">
                          <small className="text-danger">
                            Difference: ${Math.abs(totalSplitAmount - expenseAmount).toFixed(2)}
                          </small>
                        </div>
                      </div>
                    )}
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
            disabled={loading || !validateSplits()}
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