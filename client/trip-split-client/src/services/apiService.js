const API_BASE_URL = 'http://localhost:8080/api';

const handleResponse = async (response) => {
  if (!response.ok) {
    if (response.status === 401) {
      // Token expired or invalid - redirect to login
      localStorage.removeItem('tripsplit_user');
      window.location.href = '/login';
      throw new Error('Session expired. Please login again.');
    }
    
    const errorText = await response.text();
    throw new Error(`HTTP ${response.status}: ${errorText}`);
  }
  
  // Handle empty responses (like DELETE operations)
  const contentType = response.headers.get('content-type');
  if (contentType && contentType.includes('application/json')) {
    return response.json();
  }
  return response.ok;
};

const getToken = () => {
  const user = JSON.parse(localStorage.getItem('tripsplit_user') || '{}');
  return user.token || null;
};

const isTokenExpired = (token) => {
  if (!token) return true;
  
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    const now = Math.floor(Date.now() / 1000);
    return payload.exp < now;
  } catch (e) {
    return true;
  }
};

// Helper function for making authenticated requests
const makeAuthenticatedRequest = async (url, options = {}) => {
  const token = getToken();
  
  // Check if token is expired before making request
  if (isTokenExpired(token)) {
    localStorage.removeItem('tripsplit_user');
    window.location.href = '/login';
    throw new Error('Session expired. Please login again.');
  }
  
  const defaultOptions = {
    headers: {
      'Content-Type': 'application/json',
      ...(token && { 'Authorization': `Bearer ${token}` }),
      ...options.headers,
    },
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
      const authResponse = await makePublicRequest('/auth/authenticate', {
        method: 'POST',
        body: JSON.stringify({
          username: credentials.email,
          password: credentials.password
        })
      });
      
      const token = authResponse.jwt_token || authResponse.token;
      
      if (!token) {
        throw new Error('Invalid response: missing token');
      }
      
      // get user information using the token
      const userResponse = await fetch(`${API_BASE_URL}/user`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        }
      });
      
      if (!userResponse.ok) {
        throw new Error(`Failed to get user info: ${userResponse.status}`);
      }
      
      const userData = await userResponse.json();
      
      return {
        token: token,
        user: {
          id: userData.appUserId || userData.id,
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

  // User endpoints (require authentication)
  async getCurrentUser() {
    return makeAuthenticatedRequest('/user');
  },

  async updateUser(userData) {
    return makeAuthenticatedRequest('/user', {
      method: 'PUT',
      body: JSON.stringify(userData)
    });
  },

  // Expenses endpoints (require authentication)
  async getExpenses() {
    return makeAuthenticatedRequest('/expenses');
  },

  async getExpensesByGroup(groupId) {
    return makeAuthenticatedRequest(`/expenses/group/${groupId}`);
  },

  async getExpense(id) {
    return makeAuthenticatedRequest(`/expenses/${id}`);
  },

  async createExpense(expense) {
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

  // Groups endpoints (require authentication)
  async getGroups() {
    return makeAuthenticatedRequest('/groups');
  },

  async getGroup(id) {
    return makeAuthenticatedRequest(`/groups/${id}`);
  },

  async createGroup(group) {
    return makeAuthenticatedRequest('/groups', {
      method: 'POST',
      body: JSON.stringify(group)
    });
  },

  async updateGroup(id, group) {
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

  // User Expenses endpoints (require authentication)
  async getUserBalance(userId) {
    return makeAuthenticatedRequest(`/user-expenses/user/${userId}/balance`);
  },

  async getUserExpenses(userId) {
    return makeAuthenticatedRequest(`/user-expenses/user/${userId}`);
  },

  async getExpenseSplits(expenseId) {
    return makeAuthenticatedRequest(`/user-expenses/expense/${expenseId}`);
  },

  // Comments endpoints (require authentication)
  async getComments(expenseId) {
    return makeAuthenticatedRequest(`/comments/expense/${expenseId}`);
  },

  async addComment(comment) {
    return makeAuthenticatedRequest('/comments', {
      method: 'POST',
      body: JSON.stringify(comment)
    });
  },

  // Receipts endpoints (require authentication)
  async getReceipts(expenseId) {
    return makeAuthenticatedRequest(`/receipts/expense/${expenseId}`);
  },

  async uploadReceipt(receipt) {
    return makeAuthenticatedRequest('/receipts', {
      method: 'POST',
      body: JSON.stringify(receipt)
    });
  },

  // Utility methods
  isTokenValid() {
    const token = getToken();
    return !isTokenExpired(token);
  },

  getAuthToken() {
    return getToken();
  }
};