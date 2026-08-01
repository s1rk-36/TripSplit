import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { FaDollarSign, FaReceipt, FaUsers, FaPlus } from 'react-icons/fa';
import Layout from '../components/layout/Layout';
import { formatCurrency } from '../utils/helpers';
import { apiService } from '../services/apiService';
import { useAuth } from '../utils/auth';

function Dashboard() {
  const { currentUser } = useAuth();
  const [stats, setStats] = useState({
    userBalance: 0,
    totalExpenses: 0,
    activeGroups: 0
  });
  const [statsError, setStatsError] = useState('');

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    try {
      
      // Load user balance, expenses count, and groups count
      const [balance, expenses, groups] = await Promise.all([
        apiService.getUserBalance(currentUser.userId),
        apiService.getExpenses(),
        apiService.getGroups()
      ]);
      
      setStats({
        userBalance: balance,
        totalExpenses: expenses.length,
        activeGroups: groups.length
      });
    } catch (err) {
      // This used to substitute invented figures (a $382.00 balance, 10 expenses,
      // 5 groups) whenever a call failed, so a brand-new account with nothing in it
      // was shown someone else's numbers. Report the failure instead.
      console.error('Failed to load dashboard data:', err);
      setStatsError(err.message || 'Could not load your dashboard.');
    }
  };

  return (
    <Layout>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2>Dashboard</h2>
          <p className="text-muted">Overview of your expenses and balances</p>
        </div>
        <Link to="/expenses/add" className="btn btn-primary">
          <FaPlus className="me-1" /> Add Expense
        </Link>
      </div>

      {statsError && (
        <div className="alert alert-danger">{statsError}</div>
      )}

      {(
        <>
          {/* Stats Cards */}
          <div className="row mb-4">
            <div className="col-md-4">
              <div className="card text-center">
                <div className="card-body">
                  <FaDollarSign className="text-primary mb-2" size={40} />
                  <h3 className={stats.userBalance >= 0 ? 'text-success' : 'text-danger'}>
                    {formatCurrency(Math.abs(stats.userBalance))}
                  </h3>
                  <p className="card-text">
                    {stats.userBalance >= 0 ? 'You are owed' : 'You owe'}
                  </p>
                </div>
              </div>
            </div>
            <div className="col-md-4">
              <div className="card text-center">
                <div className="card-body">
                  <FaReceipt className="text-info mb-2" size={40} />
                  <h3>{stats.totalExpenses}</h3>
                  <p className="card-text">Total Expenses</p>
                </div>
              </div>
            </div>
            <div className="col-md-4">
              <div className="card text-center">
                <div className="card-body">
                  <FaUsers className="text-warning mb-2" size={40} />
                  <h3>{stats.activeGroups}</h3>
                  <p className="card-text">Active Groups</p>
                </div>
              </div>
            </div>
          </div>

          {/* Activity is per group and derived from that group's ledger, so it lives
              on the group's expenses view. This card used to render four invented
              rows ("Bob paid $400 for hotel") that were the same for every account. */}
          <div className="card">
            <div className="card-header">
              <h5 className="mb-0">Recent Activity</h5>
            </div>
            <div className="card-body text-muted">
              Activity is tracked per group.{' '}
              <Link to="/groups">Open a group</Link> to see what has been added and settled.
            </div>
          </div>
        </>
      )}
    </Layout>
  );
}

export default Dashboard;