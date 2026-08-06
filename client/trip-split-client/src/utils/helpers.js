/**
 * Display names for a set of people, with the username appended only where two of
 * them share the same first and last name — "Sam Chen (samc2)". Mirrors the rule
 * SettleUpService applies server-side, so a member reads the same everywhere.
 *
 * Takes [{ id, firstName, lastName, username }] and returns { [id]: displayName }.
 */
export const disambiguateNames = (people = []) => {
  const fullName = (p) => `${p.firstName || ''} ${p.lastName || ''}`.trim();

  const timesSeen = {};
  people.forEach((p) => {
    const name = fullName(p);
    timesSeen[name] = (timesSeen[name] || 0) + 1;
  });

  const names = {};
  people.forEach((p) => {
    const name = fullName(p);
    names[p.id] = timesSeen[name] > 1 && p.username ? `${name} (${p.username})` : name;
  });
  return names;
};

/**
 * Splits an amount into `count` shares that sum back to it exactly.
 *
 * Dividing in floats and sending the result meant each share was rounded
 * independently by the decimal(10,2) columns: $100 among 6 stored 16.67 six times,
 * which is $100.02. The extra cents became debt with no matching creditor, so the
 * group could never reach zero. Work in whole cents and hand the leftover cents to
 * the first few members instead — 4 owe $16.67 and 2 owe $16.66.
 */
export const splitEvenly = (total, count) => {
  if (!count || count < 1) return [];

  const totalCents = Math.round((Number(total) || 0) * 100);
  const base = Math.trunc(totalCents / count);
  let remainder = totalCents - base * count;
  const step = remainder < 0 ? -1 : 1; // keeps refunds (negative totals) exact too

  return Array.from({ length: count }, () => {
    let cents = base;
    if (remainder !== 0) {
      cents += step;
      remainder -= step;
    }
    return cents / 100;
  });
};

export const formatCurrency = (amount) => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD'
  }).format(amount);
};

export const formatDate = (dateString) => {
  return new Date(dateString).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  });
};

export const formatDateTime = (dateString) => {
  return new Date(dateString).toLocaleString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
};

export const validateEmail = (email) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
};

export const validatePassword = (password) => {
  return {
    length: password.length >= 8,
    number: /\d/.test(password),
    letter: /[a-zA-Z]/.test(password)
  };
};

export const calculateBalance = (userExpenses) => {
  const totalOwed = userExpenses.reduce((sum, expense) => sum + expense.amountOwed, 0);
  const totalPaid = userExpenses.reduce((sum, expense) => sum + expense.amountPaid, 0);
  return totalPaid - totalOwed;
};

export const getBalanceColor = (balance) => {
  return balance >= 0 ? 'text-success' : 'text-danger';
};

export const getBalanceText = (balance) => {
  return balance >= 0 ? 'You are owed' : 'You owe';
};

export const truncateText = (text, maxLength = 100) => {
  if (!text || text.length <= maxLength) return text;
  return text.substring(0, maxLength) + '...';
};

// Mock data for development (remove when backend is ready)
export const getMockExpenses = () => {
  return [
    {
      expenseId: 1,
      name: 'Flight Tickets',
      totalCost: 1200.00,
      category: 'Travel',
      description: 'Round trip flights to Tokyo',
      createdAt: '2025-03-10',
      createdBy: { userId: 1, firstName: 'Alice', lastName: 'Johnson' },
      groupId: 1
    },
    {
      expenseId: 2,
      name: 'Hotel Accommodation',
      totalCost: 800.50,
      category: 'Lodging',
      description: '5 nights stay at Tokyo hotel',
      createdAt: '2025-03-11',
      createdBy: { userId: 2, firstName: 'Bob', lastName: 'Smith' },
      groupId: 1
    }
  ];
};

export const getMockGroups = () => {
  return [
    { groupId: 1, name: 'Japan Spring Trip', description: 'Cherry blossom tour', memberCount: 3 },
    { groupId: 2, name: 'NYC Business Conference', description: 'Tech conference travel', memberCount: 3 }
  ];
};