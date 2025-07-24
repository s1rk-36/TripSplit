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
  const [recentActivities] = useState([
    { title: 'New expense added', description: 'Flight Tickets - $1,200.00', time: '2 hours ago' },
    { title: 'Payment received', description: 'Bob paid $400 for hotel', time: '5 hours ago' },
    { title: 'Comment added', description: 'Carol commented on Taxi Fare', time: '1 day ago' },
    { title: 'Receipt uploaded', description: 'Dinner bill receipt uploaded', time: '2 days ago' }
  ]);

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
      console.error('Failed to load dashboard data:', err);
      // Fallback to mock data
      setStats({
        userBalance: 382.00,
        totalExpenses: 10,
        activeGroups: 5
      });
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

          {/* Recent Activity */}
          <div className="card">
            <div className="card-header">
              <h5 className="mb-0">Recent Activity</h5>
            </div>
            <div className="card-body">
              {recentActivities.map((activity, index) => (
                <div key={index} className="d-flex justify-content-between align-items-center py-2 border-bottom">
                  <div>
                    <strong>{activity.title}</strong>
                    <div className="text-muted small">{activity.description}</div>
                  </div>
                  <small className="text-muted">{activity.time}</small>
                </div>
              ))}
            </div>
          </div>
        </>
      )}
    </Layout>
  );
}

export default Dashboard;