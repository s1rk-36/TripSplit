export const auth = {
  getCurrentUser() {
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user) : null;
  },

  setCurrentUser(user) {
    localStorage.setItem('user', JSON.stringify(user));
  },

  clearCurrentUser() {
    localStorage.removeItem('user');
  },

  isAuthenticated() {
    return this.getCurrentUser() !== null;
  },

  getToken() {
    const user = this.getCurrentUser();
    return user?.token || null;
  },

  logout() {
    this.clearCurrentUser();
    window.location.href = '/';
  }
};

export const useAuth = () => {
  const currentUser = auth.getCurrentUser();
  const isAuthenticated = auth.isAuthenticated();

  return {
    currentUser,
    isAuthenticated,
    login: auth.setCurrentUser,
    logout: auth.logout
  };
};