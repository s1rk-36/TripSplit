import { useState, useEffect, useCallback } from 'react';
import { Modal, Button } from 'react-bootstrap';
import { FaBalanceScale, FaArrowRight, FaCheck } from 'react-icons/fa';
import { apiService } from '../../services/apiService';
import { useAuth } from '../../utils/auth';
import { isDemoMode, showDemoNotice } from '../../services/demoData';
import { formatCurrency } from '../../utils/helpers';

/**
 * The settle-up view for one group: each member's net position, the minimal list
 * of payments that squares the group, and recording those payments once they've
 * been made in the real world (cash, Venmo, ...).
 */
function SettleUpModal({ show, onHide, group, onSettlementRecorded }) {
  const { currentUser } = useAuth();
  const [plan, setPlan] = useState(null);
  const [loading, setLoading] = useState(false);
  const [recordingKey, setRecordingKey] = useState(null);
  const [error, setError] = useState('');

  const loadPlan = useCallback(async () => {
    if (!group) return;
    try {
      setLoading(true);
      setError('');
      const data = await apiService.getSettlePlan(group.groupId);
      setPlan(data);
    } catch (err) {
      setError(err.message || 'Failed to load the settle-up plan');
    } finally {
      setLoading(false);
    }
  }, [group]);

  useEffect(() => {
    if (show) loadPlan();
  }, [show, loadPlan]);

  const handleRecord = async (transfer) => {
    if (isDemoMode()) {
      showDemoNotice('This is a read-only demo — sign up to record real settlements.');
      return;
    }
    const key = `${transfer.fromUserId}-${transfer.toUserId}`;
    try {
      setRecordingKey(key);
      setError('');
      await apiService.recordSettlement({
        groupId: group.groupId,
        payerId: transfer.fromUserId,
        payeeId: transfer.toUserId,
        amount: transfer.amount,
      });
      await loadPlan();
      onSettlementRecorded?.();
    } catch (err) {
      setError(err.message || 'Failed to record the payment');
    } finally {
      setRecordingKey(null);
    }
  };

  const netColor = (net) => (net > 0.01 ? 'text-success' : net < -0.01 ? 'text-danger' : 'text-muted');
  const involvesMe = (t) =>
    t.fromUserId === currentUser?.userId || t.toUserId === currentUser?.userId;

  return (
    <Modal show={show} onHide={onHide} size="lg">
      <Modal.Header closeButton>
        <Modal.Title>
          <FaBalanceScale className="me-2" />
          Settle up · {group?.name}
        </Modal.Title>
      </Modal.Header>

      <Modal.Body>
        {error && <div className="alert alert-danger">{error}</div>}

        {loading || !plan ? (
          <div className="text-center py-4">
            <div className="spinner-border text-primary" role="status">
              <span className="visually-hidden">Loading...</span>
            </div>
          </div>
        ) : (
          <>
            {/* Member balances */}
            <div className="ts-eyebrow mb-2">Where everyone stands</div>
            <div className="ts-balance-list mb-4">
              {plan.balances.map((b) => (
                <div key={b.userId} className="ts-balance-row">
                  <span>
                    {b.name}
                    {b.userId === currentUser?.userId && <span className="text-muted"> (you)</span>}
                  </span>
                  <span className={`ts-amount ${netColor(b.net)}`}>
                    {b.net > 0.01 ? 'gets back ' : b.net < -0.01 ? 'owes ' : ''}
                    {formatCurrency(Math.abs(b.net))}
                  </span>
                </div>
              ))}
            </div>

            {/* Settled, or the minimal payment plan */}
            {plan.settled ? (
              <div className="text-center py-4">
                <span className="ts-settled-badge">Settled</span>
                <p className="ts-page-sub mt-3 mb-0">
                  Everyone is square. Add a new expense to start the tab again.
                </p>
              </div>
            ) : (
              <>
                <div className="ts-eyebrow mb-2">
                  Simplest way to square up · {plan.transfers.length} payment{plan.transfers.length === 1 ? '' : 's'}
                </div>
                <div className="ts-transfer-list">
                  {plan.transfers.map((t) => {
                    const key = `${t.fromUserId}-${t.toUserId}`;
                    return (
                      <div key={key} className="ts-transfer-row">
                        <span className="ts-transfer-who">
                          <strong>{t.fromName}</strong>
                          <FaArrowRight className="mx-2 text-muted" size={12} />
                          <strong>{t.toName}</strong>
                        </span>
                        <span className="d-flex align-items-center gap-3">
                          <span className="ts-amount">{formatCurrency(t.amount)}</span>
                          {involvesMe(t) ? (
                            <Button
                              size="sm"
                              variant="primary"
                              disabled={recordingKey === key}
                              onClick={() => handleRecord(t)}
                            >
                              {recordingKey === key ? (
                                <span className="spinner-border spinner-border-sm" />
                              ) : (
                                <>
                                  <FaCheck className="me-1" size={11} /> Mark paid
                                </>
                              )}
                            </Button>
                          ) : (
                            <span className="text-muted small">between others</span>
                          )}
                        </span>
                      </div>
                    );
                  })}
                </div>
                <p className="text-muted small mt-3 mb-0">
                  Payments happen outside TripSplit (cash, Venmo, ...). "Mark paid" records
                  one here so the ledger stays honest. You can record payments you made or
                  received.
                </p>
              </>
            )}
          </>
        )}
      </Modal.Body>

      <Modal.Footer>
        <Button variant="secondary" onClick={onHide}>Close</Button>
      </Modal.Footer>
    </Modal>
  );
}

export default SettleUpModal;
