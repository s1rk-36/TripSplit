import { useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
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
import ProtectedRoute from './components/common/ProtectedRoute';
import DemoBanner from './components/common/DemoBanner';
import DemoToast from './components/common/DemoToast';
import { initReveal } from './utils/reveal';


function App() {
    useEffect(() => {
        initReveal();
    }, []);

    return (
        <AuthProvider>
        <Router>
            <Navbar />
            <DemoBanner />
            <DemoToast />
            <main className="main-content">
                <Routes>

                    <Route path="/" element={<Home />} />
                    <Route path="/login" element={<Login />} />
                    <Route path="/register" element={<Register />} />
        
                    <Route path="/dashboard" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
                    <Route path="/groups" element={<ProtectedRoute><Groups /></ProtectedRoute>} />
                    <Route path="/profile" element={<ProtectedRoute><ProfileSettings /></ProtectedRoute>} />
                    <Route path="/expenses" element={<ProtectedRoute><Expenses /></ProtectedRoute>} />
                    <Route path="/admin" element={<ProtectedRoute><AdminDashboard /></ProtectedRoute>} />

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