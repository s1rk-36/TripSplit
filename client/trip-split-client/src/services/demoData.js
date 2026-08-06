// Demo mode: lets recruiters/visitors explore the real app populated with sample
// data, without signing up or hitting the backend. When active, apiService serves
// the mock data below instead of making network calls (see apiService.js).
import { auth } from '../utils/auth';
import { disambiguateNames } from './../utils/helpers';

const DEMO_FLAG = 'tripsplit_demo';

// A non-verified JWT with a far-future expiry so client-side auth checks pass.
// It is never sent to the server (all demo requests are intercepted), so the
// missing signature is irrelevant.
const demoPayload = { sub: 'demo', authorities: 'ROLE_USER', exp: 4102444800 }; // ~year 2100
export const DEMO_TOKEN = `x.${btoa(JSON.stringify(demoPayload))}.x`;

export const demoUser = {
  userId: 1,
  firstName: 'Mock',
  lastName: 'User',
  email: 'demo@tripsplit.app',
  username: 'demo',
  token: DEMO_TOKEN,
};

const members = [
  { userId: 1, firstName: 'Mock', lastName: 'User', username: 'demo', email: 'demo@tripsplit.app' },
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
  { groupId: 1, name: 'Japan Spring Trip', description: 'Cherry blossom tour across Tokyo and Kyoto.', createdBy: 1, inviteCode: 'K7M2QP9R',
    users: [mem(members[0], true), mem(members[1]), mem(members[2]), mem(members[3])] },
  { groupId: 2, name: 'Iceland Road Adventure', description: 'Self-drive ring road trip around Iceland.', createdBy: 1, inviteCode: 'H4TN8RJ2',
    users: [mem(members[0], true), mem(members[2]), mem(members[4])] },
  { groupId: 3, name: 'Vegas Weekend', description: 'Weekend celebration with the crew.', createdBy: 4, inviteCode: 'VG9KMP3T',
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
  expense(2, 'Ryokan, Kyoto', '3 nights, traditional inn', 800, 'Lodging', '2026-06-16T09:00:00', 1, 2),
  expense(3, 'Sushi Dinner', 'Group dinner in Ginza', 240, 'Food', '2026-06-17T20:00:00', 1, 1),
  expense(4, 'Rental Car', '4x4 for the ring road', 600, 'Travel', '2026-05-02T08:00:00', 2, 3),
  expense(5, 'Blue Lagoon Tickets', 'Spa entry for the group', 180, 'Activities', '2026-05-03T14:00:00', 2, 1),
  expense(6, 'Show Tickets', 'Vegas show for everyone', 200, 'Entertainment', '2026-04-20T19:00:00', 3, 4),
];

// Net for the demo user (id 1): paid 1620 (expenses 1,3,5) − owed 870 (share of all 6) = +750.
const DEMO_BALANCE = 750.0;

// Same rule the backend applies: the username only appears where two people would
// otherwise read identically.
const demoDisplayNames = disambiguateNames(
  members.map((m) => ({ id: m.userId, firstName: m.firstName, lastName: m.lastName, username: m.username }))
);

const nameOf = (userId) => demoDisplayNames[userId] || `Member ${userId}`;

// Recorded cash payments. Vegas (group 3) is fully squared by these, so the demo
// shows a settled group with the stamp alongside two groups that still owe.
const demoSettlements = [
  { settlementId: 1, groupId: 3, payerId: 1, payeeId: 4, amount: 50, createdAt: '2026-04-22T12:00:00' },
  { settlementId: 2, groupId: 3, payerId: 2, payeeId: 4, amount: 50, createdAt: '2026-04-22T15:30:00' },
  { settlementId: 3, groupId: 3, payerId: 5, payeeId: 4, amount: 50, createdAt: '2026-04-23T09:15:00' },
].map((s) => ({ ...s, payerName: nameOf(s.payerId), payeeName: nameOf(s.payeeId) }));

// Mirrors SettleUpService on the backend: net = paid − owed, adjusted by
// settlements, then greedy largest-debtor → largest-creditor pairing.
const computeSettlePlan = (groupId) => {
  const nets = {};
  groupMemberIds[groupId]?.forEach((id) => { nets[id] = 0; });

  const groupExpenses = demoExpenses.filter((e) => e.groupId === groupId);
  groupExpenses.forEach((e) => {
    e.userExpenses.forEach((ue) => {
      nets[ue.userId] = (nets[ue.userId] || 0) + ue.amountPaid - ue.amountOwed;
    });
  });
  demoSettlements.filter((s) => s.groupId === groupId).forEach((s) => {
    nets[s.payerId] = (nets[s.payerId] || 0) + s.amount;
    nets[s.payeeId] = (nets[s.payeeId] || 0) - s.amount;
  });

  const balances = Object.entries(nets).map(([userId, net]) => ({
    userId: Number(userId),
    name: nameOf(Number(userId)),
    net: Math.round(net * 100) / 100,
  }));

  const debtors = balances.filter((b) => b.net < -0.01)
    .map((b) => ({ ...b, left: -b.net })).sort((a, b) => b.left - a.left);
  const creditors = balances.filter((b) => b.net > 0.01)
    .map((b) => ({ ...b, left: b.net })).sort((a, b) => b.left - a.left);

  const transfers = [];
  let d = 0; let c = 0;
  while (d < debtors.length && c < creditors.length) {
    const amount = Math.min(debtors[d].left, creditors[c].left);
    if (amount > 0.01) {
      transfers.push({
        fromUserId: debtors[d].userId, fromName: debtors[d].name,
        toUserId: creditors[c].userId, toName: creditors[c].name,
        amount: Math.round(amount * 100) / 100,
      });
    }
    debtors[d].left -= amount;
    creditors[c].left -= amount;
    if (debtors[d].left <= 0.01) d += 1;
    if (creditors[c].left <= 0.01) c += 1;
  }

  return {
    balances,
    transfers,
    hasExpenses: groupExpenses.length > 0,
    // Matches SettleUpService: an empty transfer list only means the pairing found
    // no more matches, so ask the balances themselves.
    settled: groupExpenses.length > 0 && balances.every((b) => Math.abs(b.net) <= 0.01),
  };
};

// Newest-first feed of expenses added and settlements recorded, like the backend.
const computeActivity = (groupId) => {
  const items = [
    ...demoExpenses.filter((e) => e.groupId === groupId).map((e) => ({
      type: 'EXPENSE',
      actorName: nameOf(e.createdBy),
      targetName: null,
      title: e.name,
      amount: e.totalCost,
      createdAt: e.createdAt,
    })),
    ...demoSettlements.filter((s) => s.groupId === groupId).map((s) => ({
      type: 'SETTLEMENT',
      actorName: s.payerName,
      targetName: s.payeeName,
      title: null,
      amount: s.amount,
      createdAt: s.createdAt,
    })),
  ];
  return items.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
};

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

// Fires a transient notice rendered by <DemoToast/>. Used to block mutating actions
// in demo mode up front (so a modal/form the demo can't submit never opens).
export const showDemoNotice = (
  message = 'This is a read-only demo. Sign up to create your own groups and expenses.'
) => {
  window.dispatchEvent(new CustomEvent('demo-notice', { detail: message }));
};

const lastSegment = (url) => Number(url.split('?')[0].split('/').filter(Boolean).pop());

// Maps an API path to mock data. Reads succeed; writes are blocked with a friendly
// message so the demo stays read-only.
export const resolveDemoRequest = (url, options = {}) => {
  const method = (options.method || 'GET').toUpperCase();
  if (method !== 'GET') {
    return Promise.reject(new Error('This is a read-only demo. Sign up to create your own groups and expenses.'));
  }

  const path = url.split('?')[0];

  if (path === '/user/groups' || path === '/groups') return Promise.resolve(demoGroups);
  if (path === '/user' || path === '/user/current') return Promise.resolve(demoUser);

  if (path.startsWith('/user_expenses/user/') && path.endsWith('/balance')) return Promise.resolve(DEMO_BALANCE);
  if (path.startsWith('/user_expenses/user/')) return Promise.resolve(demoExpenses);
  if (path.startsWith('/user_expenses/expense/')) return Promise.resolve([]);

  if (path === '/expenses') return Promise.resolve(demoExpenses);
  if (path.startsWith('/expenses/group/')) {
    const gid = lastSegment(path);
    return Promise.resolve(demoExpenses.filter((e) => e.groupId === gid));
  }
  if (path.startsWith('/expenses/')) {
    const id = lastSegment(path);
    return Promise.resolve(demoExpenses.find((e) => e.expenseId === id) || null);
  }

  // Must precede the generic /groups/ handlers below.
  if (path === '/groups/settled') {
    return Promise.resolve(
      demoGroups.map((g) => g.groupId).filter((id) => computeSettlePlan(id).settled)
    );
  }
  if (path.startsWith('/groups/') && path.endsWith('/members')) {
    const gid = Number(path.split('/')[2]);
    const g = demoGroups.find((x) => x.groupId === gid);
    return Promise.resolve(g ? g.users : []);
  }
  if (path.startsWith('/groups/') && path.endsWith('/settle-plan')) {
    return Promise.resolve(computeSettlePlan(Number(path.split('/')[2])));
  }
  if (path.startsWith('/groups/') && path.endsWith('/activity')) {
    return Promise.resolve(computeActivity(Number(path.split('/')[2])));
  }
  if (path.startsWith('/settlements/group/')) {
    const gid = lastSegment(path);
    return Promise.resolve(demoSettlements.filter((s) => s.groupId === gid));
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
