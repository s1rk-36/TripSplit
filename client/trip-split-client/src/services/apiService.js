import { auth } from '../utils/auth';
import { isDemoMode, resolveDemoRequest } from './demoData';

// REACT_APP_API_URL should be the API root, e.g. https://host/api. Deployment also
// defines a HEALTH_URL ending in /api/health for the keep-alive ping, and pasting
// that one here turns every call into /api/health/user/groups and 404s the whole
// app. Recover from that rather than shipping a site that only fails at runtime.
const normalizeBaseUrl = (raw) => {
  let url = (raw || '').trim().replace(/\/+$/, '');
  if (!url) {
    return 'http://localhost:8080/api';
  }
  if (url.endsWith('/health')) {
    url = url.slice(0, -'/health'.length);
    console.warn(
      'REACT_APP_API_URL ended in /health — that is the keep-alive URL, not the API root. ' +
      `Using ${url} instead. Set it to the API root to silence this.`
    );
  }
  // The API is a separate service, so the base must carry a scheme and host. A bare
  // path posts to whatever is serving the frontend, which answers "Cannot POST
  // /api/...". Nothing can be inferred here, so say plainly what to fix.
  if (!/^https?:\/\//i.test(url)) {
    console.error(
      `REACT_APP_API_URL is "${url}", which has no scheme or host, so requests go to ` +
      'the site serving the frontend instead of the API. Set it to the full API root, ' +
      'e.g. https://your-api.onrender.com/api'
    );
  }
  return url;
};

const API_BASE_URL = normalizeBaseUrl(process.env.REACT_APP_API_URL);

// A missing id used to interpolate straight into the path, so the app requested
// /user-expenses/user/undefined and showed the server's 404 instead of naming the
// real problem. Fail here, where the caller is still in the stack trace.
const requireId = (id, caller) => {
  if (id === undefined || id === null || id === '') {
    throw new Error(`${caller} called without an id — the current user may not be loaded yet.`);
  }
  return id;
};

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
      // Nothing about sign-in is logged: the response carries the bearer token, and
      // now the account alongside it, neither of which belongs in the console.
      const authResponse = await makePublicRequest('/auth/authenticate', {
        method: 'POST',
        body: JSON.stringify({
          email: credentials.email,
          password: credentials.password
        })
      });

      const token = authResponse.jwt_token;

      if (!token) {
        throw new Error('No token received from server');
      }

      // Sign-in returns the account alongside the token, so the extra /user/current
      // round trip is only needed against a backend that predates that. Each round
      // trip is a noticeable fraction of a second in production.
      let userData = authResponse.user;

      if (!userData) {
        const userResponse = await fetch(`${API_BASE_URL}/user/current`, {
          method: 'GET',
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        if (!userResponse.ok) {
          throw new Error(`Failed to get user info: ${userResponse.status}`);
        }

        userData = await userResponse.json();
      }

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
    return makeAuthenticatedRequest(`/user/${requireId(userId, 'getUser')}`);
  },

  async getUserBalance(userId) {
    return makeAuthenticatedRequest(
      `/user_expenses/user/${requireId(userId, 'getUserBalance')}/balance`);
  },

  async getUserExpenses(userId) {
    return makeAuthenticatedRequest(
      `/user_expenses/user/${requireId(userId, 'getUserExpenses')}`);
  },

  async getExpenseSplits(expenseId) {
    return makeAuthenticatedRequest(`/user_expenses/expense/${expenseId}`);
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