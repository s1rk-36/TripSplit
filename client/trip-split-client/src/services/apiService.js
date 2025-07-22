const API_BASE_URL = 'http://localhost:8080/api';

const handleResponse = async (response) => {
  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(`HTTP ${response.status}: ${errorText}`);
  }
  
  const contentType = response.headers.get('content-type');
  if (contentType && contentType.includes('application/json')) {
    return response.json();
  }
  return response.ok;
};

const makeRequest = async (url, options = {}) => {
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
  // Authentication
  async login(credentials) {
    return makeRequest('/auth/login', {
      method: 'POST',
      body: JSON.stringify(credentials)
    });
  },

  async register(userData) {
    return makeRequest('/auth/register', {
      method: 'POST',
      body: JSON.stringify(userData)
    });
  },

  // Expenses
  async getExpenses() {
    return makeRequest('/expenses');
  },

  async getExpensesByGroup(groupId) {
    return makeRequest(`/expenses/group/${groupId}`);
  },

  async getExpense(id) {
    return makeRequest(`/expenses/${id}`);
  },

  async createExpense(expense) {
    return makeRequest('/expenses', {
      method: 'POST',
      body: JSON.stringify(expense)
    });
  },

  async updateExpense(id, expense) {
    return makeRequest(`/expenses/${id}`, {
      method: 'PUT',
      body: JSON.stringify(expense)
    });
  },

  async deleteExpense(id) {
    return makeRequest(`/expenses/${id}`, {
      method: 'DELETE'
    });
  },

  // Groups
  async getGroups() {
    return makeRequest('/groups');
  },

  async getGroup(id) {
    return makeRequest(`/groups/${id}`);
  },

  async createGroup(group) {
    return makeRequest('/groups', {
      method: 'POST',
      body: JSON.stringify(group)
    });
  },

  // User Expenses
  async getUserBalance(userId) {
    return makeRequest(`/user-expenses/user/${userId}/balance`);
  },

  async getUserExpenses(userId) {
    return makeRequest(`/user-expenses/user/${userId}`);
  },

  async getExpenseSplits(expenseId) {
    return makeRequest(`/user-expenses/expense/${expenseId}`);
  },

  // Comments
  async getComments(expenseId) {
    return makeRequest(`/comments/expense/${expenseId}`);
  },

  async addComment(comment) {
    return makeRequest('/comments', {
      method: 'POST',
      body: JSON.stringify(comment)
    });
  },

  // Receipts
  async getReceipts(expenseId) {
    return makeRequest(`/receipts/expense/${expenseId}`);
  },

  async uploadReceipt(receipt) {
    return makeRequest('/receipts', {
      method: 'POST',
      body: JSON.stringify(receipt)
    });
  }
};