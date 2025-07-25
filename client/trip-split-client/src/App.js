import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Home from './pages/Home';
import Navbar from './components/layout/Navbar';
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import Expenses from './pages/Expenses';
import Groups from './pages/Groups';
import NotFound from './pages/NotFound';
import ProfileSettings from './pages/ProfileSettings';
import { AuthProvider } from './utils/auth';
import AdminDashboard from './pages/AdminDashboard';


function App() {
    return (
        <AuthProvider>
        <Router>
            <Navbar />
            <main className="main-content">
                <Routes>

                    <Route path="/" element={<Home />} />
                    <Route path="/login" element={<Login />} />
                    <Route path="/register" element={<Register />} />
        
                    <Route path="/dashboard" element={<Dashboard />} />
                    <Route path="/groups" element={<Groups />} />
                    <Route path="/profile" element={<ProfileSettings />} />
                    <Route path="/expenses" element={<Expenses />} />
                    <Route path="/admin" element={<AdminDashboard />} />

                    {/*
                    <Route path="/expenses/:id" element={<ExpenseDetails />} />
                    <Route path="/groups/:groupId/expenses" element={<GroupExpenses />} />
                    <Route path="/profile" element={<Profile />} />*/}
                    <Route path="*" element={<NotFound />} />
                </Routes>
            </main>
        </Router>
        </AuthProvider>
    );
}

export default App;