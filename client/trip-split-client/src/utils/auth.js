export const auth = {
  getCurrentUser() {
    const user = localStorage.getItem('tripsplit_user');
    if (!user) return null;
    
    try {
      const userData = JSON.parse(user);
      
      if (!userData.token) {
        this.clearCurrentUser();
        return null;
      }
      
      if (this.isTokenExpired(userData.token)) {
        this.clearCurrentUser();
        return null;
      }
      
      return userData;
    } catch (e) {
      this.clearCurrentUser();
      return null;
    }
  },

  setCurrentUser(user) {
    if (!user.token) {
      console.error('User object must contain a token');
      return;
    }
    
    localStorage.setItem('tripsplit_user', JSON.stringify(user));
  },

  clearCurrentUser() {
    localStorage.removeItem('tripsplit_user');
  },

  isAuthenticated() {
    return this.getCurrentUser() !== null;
  },

  getToken() {
    const user = this.getCurrentUser();
    return user?.token || null;
  },

  isTokenExpired(token) {
    if (!token) return true;
    
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const now = Math.floor(Date.now() / 1000);
      return payload.exp < now;
    } catch (e) {
      return true;
    }
  },

  getTokenPayload(token = null) {
    const tokenToUse = token || this.getToken();
    if (!tokenToUse) return null;
    
    try {
      return JSON.parse(atob(tokenToUse.split('.')[1]));
    } catch (e) {
      return null;
    }
  },

  getUserRoles() {
    const payload = this.getTokenPayload();
    return payload?.authorities?.split(',') || [];
  },

  hasRole(role) {
    const roles = this.getUserRoles();
    return roles.includes(role);
  },

  logout() {
    this.clearCurrentUser();
    window.location.href = '/login';
  },
};

export const useAuth = () => {
  const currentUser = auth.getCurrentUser();
  const isAuthenticated = auth.isAuthenticated();

  return {
    currentUser,
    isAuthenticated,
    login: auth.setCurrentUser,
    logout: auth.logout,
    getToken: auth.getToken,
    hasRole: auth.hasRole,
    isTokenExpired: auth.isTokenExpired
  };
};

export const validatePassword = (password) => {
  return {
    length: password.length >= 8,
    number: /\d/.test(password),
    letter: /[a-zA-Z]/.test(password)
  };
};

export const withAuth = (Component) => {
  return function AuthenticatedComponent(props) {
    const { isAuthenticated } = useAuth();
    
    if (!isAuthenticated) {
      window.location.href = '/login';
      return null;
    }
    
    return <Component {...props} />;
  };
};