// Demo mode: lets recruiters/visitors explore the real app populated with sample
// data, without signing up or hitting the backend. When active, apiService serves
// the mock data below instead of making network calls (see apiService.js).
import { auth } from '../utils/auth';

const DEMO_FLAG = 'tripsplit_demo';

// A non-verified JWT with a far-future expiry so client-side auth checks pass.
// It is never sent to the server (all demo requests are intercepted), so the
// missing signature is irrelevant.
const demoPayload = { sub: 'demo', authorities: 'ROLE_USER', exp: 4102444800 }; // ~year 2100
export const DEMO_TOKEN = `x.${btoa(JSON.stringify(demoPayload))}.x`;

export const demoUser = {
  userId: 1,
  firstName: 'Alex',
  lastName: 'Rivera',
  email: 'demo@tripsplit.app',
  username: 'demo',
  token: DEMO_TOKEN,
};

const members = [
  { userId: 1, firstName: 'Alex', lastName: 'Rivera', username: 'demo', email: 'demo@tripsplit.app' },
  { userId: 2, firstName: 'Sam', lastName: 'Chen', username: 'samc', email: 'sam@example.com' },
  { userId: 3, firstName: 'Jordan', lastName: 'Patel', username: 'jpatel', email: 'jordan@example.com' },
  { userId: 4, firstName: 'Taylor', lastName: 'Kim', username: 'tkim', email: 'taylor@example.com' },
  { userId: 5, firstName: 'Morgan', lastName: 'Diaz', username: 'mdiaz', email: 'morgan@example.com' },
];

// The API returns group members as membership objects: { user: {...}, isGroupAdmin }.
const mem = (u, isGroupAdmin = false) => ({
  user: {
    appUserId: u.userId,
    firstName: u.firstName,
    lastName: u.lastName,
    email: u.email,
    username: u.username,
  },
  isGroupAdmin,
});

const demoGroups = [
  { groupId: 1, name: 'Japan Spring Trip', description: 'Cherry blossom tour across Tokyo and Kyoto.', createdBy: 1,
    users: [mem(members[0], true), mem(members[1]), mem(members[2]), mem(members[3])] },
  { groupId: 2, name: 'Iceland Road Adventure', description: 'Self-drive ring road trip around Iceland.', createdBy: 1,
    users: [mem(members[0], true), mem(members[2]), mem(members[4])] },
  { groupId: 3, name: 'Vegas Weekend', description: 'Weekend celebration with the crew.', createdBy: 4,
    users: [mem(members[0]), mem(members[1]), mem(members[3], true), mem(members[4])] },
];

// Member userIds per group, used to build even splits.
const groupMemberIds = { 1: [1, 2, 3, 4], 2: [1, 3, 5], 3: [1, 2, 4, 5] };

// Splits an expense evenly: the payer paid the full amount, everyone owes an equal share.
const split = (total, groupId, payerId) => {
  const ids = groupMemberIds[groupId];
  const share = Math.round((total / ids.length) * 100) / 100;
  return ids.map((userId) => ({ userId, amountPaid: userId === payerId ? total : 0, amountOwed: share }));
};

const expense = (expenseId, name, description, totalCost, category, createdAt, groupId, createdBy) => ({
  expenseId, name, description, totalCost, category, createdAt, groupId, createdBy,
  userExpenses: split(totalCost, groupId, createdBy),
});

const demoExpenses = [
  expense(1, 'Flight Tickets', 'Round-trip flights to Tokyo', 1200, 'Travel', '2026-06-15T10:00:00', 1, 1),
  expense(2, 'Ryokan — Kyoto', '3 nights, traditional inn', 800, 'Lodging', '2026-06-16T09:00:00', 1, 2),
  expense(3, 'Sushi Dinner', 'Group dinner in Ginza', 240, 'Food', '2026-06-17T20:00:00', 1, 1),
  expense(4, 'Rental Car', '4x4 for the ring road', 600, 'Travel', '2026-05-02T08:00:00', 2, 3),
  expense(5, 'Blue Lagoon Tickets', 'Spa entry for the group', 180, 'Activities', '2026-05-03T14:00:00', 2, 1),
  expense(6, 'Show Tickets', 'Vegas show for everyone', 200, 'Entertainment', '2026-04-20T19:00:00', 3, 4),
];

// Net for the demo user (id 1): paid 1620 (expenses 1,3,5) − owed 870 (share of all 6) = +750.
const DEMO_BALANCE = 750.0;

// True only when the demo flag is set AND the active session is the demo user.
// The token check prevents a leftover flag from hijacking a real login with mock data.
export const isDemoMode = () => {
  if (localStorage.getItem(DEMO_FLAG) !== 'true') return false;
  const user = auth.getCurrentUser();
  return !!user && user.token === DEMO_TOKEN;
};

export const startDemo = () => {
  localStorage.setItem(DEMO_FLAG, 'true');
  auth.setCurrentUser(demoUser);
};

export const endDemo = () => {
  localStorage.removeItem(DEMO_FLAG);
  auth.clearCurrentUser();
};

const lastSegment = (url) => Number(url.split('?')[0].split('/').filter(Boolean).pop());

// Maps an API path to mock data. Reads succeed; writes are blocked with a friendly
// message so the demo stays read-only.
export const resolveDemoRequest = (url, options = {}) => {
  const method = (options.method || 'GET').toUpperCase();
  if (method !== 'GET') {
    return Promise.reject(new Error('This is a read-only demo — sign up to create your own groups and expenses.'));
  }

  const path = url.split('?')[0];

  if (path === '/user/groups' || path === '/groups') return Promise.resolve(demoGroups);
  if (path === '/user' || path === '/user/current') return Promise.resolve(demoUser);

  if (path.startsWith('/user-expenses/user/') && path.endsWith('/balance')) return Promise.resolve(DEMO_BALANCE);
  if (path.startsWith('/user-expenses/user/')) return Promise.resolve(demoExpenses);
  if (path.startsWith('/user-expenses/expense/')) return Promise.resolve([]);

  if (path === '/expenses') return Promise.resolve(demoExpenses);
  if (path.startsWith('/expenses/group/')) {
    const gid = lastSegment(path);
    return Promise.resolve(demoExpenses.filter((e) => e.groupId === gid));
  }
  if (path.startsWith('/expenses/')) {
    const id = lastSegment(path);
    return Promise.resolve(demoExpenses.find((e) => e.expenseId === id) || null);
  }

  if (path.startsWith('/groups/') && path.endsWith('/members')) {
    const gid = Number(path.split('/')[2]);
    const g = demoGroups.find((x) => x.groupId === gid);
    return Promise.resolve(g ? g.users : []);
  }
  if (path.startsWith('/groups/')) {
    const id = lastSegment(path);
    return Promise.resolve(demoGroups.find((g) => g.groupId === id) || null);
  }

  if (path.startsWith('/comments/expense/')) return Promise.resolve([]);

  if (path.startsWith('/user/')) {
    const id = lastSegment(path);
    return Promise.resolve(members.find((m) => m.userId === id) || demoUser);
  }

  // Safe default for any unmapped read.
  return Promise.resolve([]);
};
