import { auth } from '../utils/auth';
import { isDemoMode, resolveDemoRequest } from './demoData';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

const handleResponse = async (response) => {
  if (!response.ok) {
    if (response.status === 401) {
      localStorage.removeItem('tripsplit_user');
      window.location.href = '/login';
      throw new Error('Session expired. Please login again.');
    }

    const responseText = await response.text();

    let errorMessage;
    try {
      const errorArray = JSON.parse(responseText);
      errorMessage = errorArray.join(', ');
    } catch (parseError) {
      errorMessage = responseText || `HTTP ${response.status}: ${response.statusText}`;
    }

    throw new Error(errorMessage);
  }

  // Handle successful responses
  try {
    const responseText = await response.text();
    return responseText ? JSON.parse(responseText) : {};
  } catch (error) {
    return {};
  }
};

const getToken = () => {
  const user = JSON.parse(localStorage.getItem('tripsplit_user') || '{}');
  return user.token || null;
};

const makeAuthenticatedRequest = async (url, options = {}) => {
  // In demo mode, serve mock data instead of hitting the backend.
  if (isDemoMode()) {
    return resolveDemoRequest(url, options);
  }

  const token = getToken();

  // Check if token is expired before making request
  if (auth.isTokenExpired(token)) {
    localStorage.removeItem('tripsplit_user');
    window.location.href = '/login';
    throw new Error('Session expired. Please login again.');
  }

  const defaultHeaders = {
    ...(token && { 'Authorization': `Bearer ${token}` }),
    ...options.headers,
  };

  // for non receipts
  if (!(options.body instanceof FormData)) {
    defaultHeaders['Content-Type'] = 'application/json';
  }

  const defaultOptions = {
    headers: defaultHeaders,
  };

  const response = await fetch(`${API_BASE_URL}${url}`, {
    ...defaultOptions,
    ...options,
  });

  return handleResponse(response);
};

// Helper function for non-authenticated requests
const makePublicRequest = async (url, options = {}) => {
  const defaultOptions = {
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
  };

  const response = await fetch(`${API_BASE_URL}${url}`, {
    ...defaultOptions,
    ...options,
  });

  return handleResponse(response);
};

export const apiService = {
  async login(credentials) {
    try {
      console.log('Login attempt with:', { email: credentials.email });

      const authResponse = await makePublicRequest('/auth/authenticate', {
        method: 'POST',
        body: JSON.stringify({
          email: credentials.email,
          password: credentials.password
        })
      });

      console.log('Auth response:', authResponse);

      const token = authResponse.jwt_token;

      if (!token) {
        throw new Error('No token received from server');
      }

      console.log('Token received, getting user info...');

      const userResponse = await fetch(`${API_BASE_URL}/user/current`, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      if (!userResponse.ok) {
        throw new Error(`Failed to get user info: ${userResponse.status}`);
      }

      const userData = await userResponse.json();
      console.log('User data received:', userData);

      return {
        token: token,
        user: {
          id: userData.appUserId,
          username: userData.username,
          email: userData.email,
          firstName: userData.firstName,
          lastName: userData.lastName
        }
      };

    } catch (error) {
      console.error('Login error:', error);
      throw error;
    }
  },

  async register(userData) {
    return makePublicRequest('/auth/register', {
      method: 'POST',
      body: JSON.stringify(userData)
    });
  },

  // User endpoints 
  async getCurrentUser() {
    return makeAuthenticatedRequest('/user');
  },

  async updateUser(userId, userData) {
    console.log('trying to update', userId);
    console.log(userData);
    return makeAuthenticatedRequest(`/user/${userId}`, {
      method: 'PUT',
      body: JSON.stringify(userData)
    });
  },

  // Expenses endpoints 
  async getExpenses() {
    return makeAuthenticatedRequest('/expenses');
  },

  async getGroupExpenses(groupId) {
    return makeAuthenticatedRequest(`/expenses/group/${groupId}`);
  },

  async getExpense(id) {
    return makeAuthenticatedRequest(`/expenses/${id}`);
  },

  async createExpense(expense) {
    console.log("sending over", expense);
    return makeAuthenticatedRequest('/expenses', {
      method: 'POST',
      body: JSON.stringify(expense)
    });
  },

  async updateExpense(id, expense) {
    return makeAuthenticatedRequest(`/expenses/${id}`, {
      method: 'PUT',
      body: JSON.stringify(expense)
    });
  },

  async deleteExpense(id) {
    return makeAuthenticatedRequest(`/expenses/${id}`, {
      method: 'DELETE'
    });
  },

  async updateUserExpense(userExpenseData) {
    return makeAuthenticatedRequest(`/user_expenses/${userExpenseData.userId}/${userExpenseData.expenseId}`, {
      method: 'PUT',
      body: JSON.stringify(userExpenseData),
    });
  },

  // Groups endpoints
  async getGroups() {
    return makeAuthenticatedRequest('/user/groups');
  },
  async getGroupMembers(groupId) {
    return makeAuthenticatedRequest(`/groups/${groupId}/members`);
  },

  // Settle up: net balances + minimal payments, and recorded settlements
  async getSettledGroupIds() {
    return makeAuthenticatedRequest('/groups/settled');
  },
  async getSettlePlan(groupId) {
    return makeAuthenticatedRequest(`/groups/${groupId}/settle-plan`);
  },
  async getGroupActivity(groupId) {
    return makeAuthenticatedRequest(`/groups/${groupId}/activity`);
  },
  async getGroupSettlements(groupId) {
    return makeAuthenticatedRequest(`/settlements/group/${groupId}`);
  },
  async recordSettlement(data) {
    // { groupId, payeeId, amount, payerId? } — payer defaults to the current user
    return makeAuthenticatedRequest('/settlements', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },

  async removeGroupMember(groupId, userId) {
    return makeAuthenticatedRequest(`/groups/${groupId}/members/${userId}`, {
      method: 'DELETE'
    });
  },

  // admin access
  async getAllGroups() {
    return makeAuthenticatedRequest('/groups');
  },
  async getAllUsers() {
    return makeAuthenticatedRequest('/user');
  },


  async getGroup(id) {
    return makeAuthenticatedRequest(`/groups/${id}`);
  },

  async createGroup(group) {
    console.log(group);
    return makeAuthenticatedRequest('/groups', {
      method: 'POST',
      body: JSON.stringify(group)
    });
  },

  async joinGroup(data) {
    console.log("the data is", data);
    return makeAuthenticatedRequest('/groups/join', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },


  async updateGroup(id, group) {
    console.log("updating", group);
    return makeAuthenticatedRequest(`/groups/${id}`, {
      method: 'PUT',
      body: JSON.stringify(group)
    });
  },

  async deleteGroup(id) {
    return makeAuthenticatedRequest(`/groups/${id}`, {
      method: 'DELETE'
    });
  },


  // User Expenses endpoints 
  async getUser(userId) {
    return makeAuthenticatedRequest(`/user/${userId}`);
  },

  async getUserBalance(userId) {
    return makeAuthenticatedRequest(`/user-expenses/user/${userId}/balance`);
  },

  async getUserExpenses(userId) {
    return makeAuthenticatedRequest(`/user-expenses/user/${userId}`);
  },

  async getExpenseSplits(expenseId) {
    return makeAuthenticatedRequest(`/user-expenses/expense/${expenseId}`);
  },
  
  async deleteUser(id) {
    return makeAuthenticatedRequest(`/user/${id}`, {
      method: 'DELETE'
    });
  },

  // Comments endpoints 
  async getComments(expenseId) {
    return makeAuthenticatedRequest(`/comments/expense/${expenseId}`);
  },

  async addComment(comment) {
    return makeAuthenticatedRequest('/comments', {
      method: 'POST',
      body: JSON.stringify(comment)
    });
  },

  // Receipts endpoints

  async uploadExpenseReceipt(expenseId, receiptFile) {
    const formData = new FormData();
    formData.append('file', receiptFile);

    return makeAuthenticatedRequest(`/receipts/expense/${expenseId}/upload`, {
      method: 'POST',
      body: formData,
    });
  },
  // Get receipts for an expense
  async getExpenseReceipts(expenseId) {
    return makeAuthenticatedRequest(`/receipts/expense/${expenseId}`);
  },

  // Get receipt by ID
  async getReceipt(receiptId) {
    return makeAuthenticatedRequest(`/receipts/${receiptId}`);
  },

  // Delete receipt file and record
  async deleteExpenseReceipt(receiptId) {
    try {
      // Delete the file first
      await makeAuthenticatedRequest(`/receipts/${receiptId}/deleteFile`, {
        method: 'DELETE'
      });

      // Then delete the receipt record
      await makeAuthenticatedRequest(`/receipts/${receiptId}`, {
        method: 'DELETE'
      });
    } catch (error) {
      console.error('Failed to delete receipt:', error);
      throw error;
    }
  },

  // Utility methods
  isTokenValid() {
    const token = getToken();
    return !auth.isTokenExpired(token);
  },

  getAuthToken() {
    return getToken();
  }
};