import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Home from './pages/Home';
import Navbar from './components/layout/Navbar';
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import Expenses from './pages/Expenses';
// import ExpenseAdd from './pages/ExpenseAdd';
// import ExpenseDetails from './pages/ExpenseDetails';
import Groups from './pages/Groups';
// import GroupAdd from './pages/GroupAdd';
// import GroupExpenses from './pages/GroupExpenses';
// import Profile from './pages/Profile';
import NotFound from './pages/NotFound';


function App() {
    return (
        <Router>
            <Navbar />
            <main className="main-content">
                <Routes>

                    <Route path="/" element={<Home />} />
                    <Route path="/login" element={<Login />} />
                    <Route path="/register" element={<Register />} />
        
                    <Route path="/dashboard" element={<Dashboard />} />
                    <Route path="/groups" element={<Groups />} />
                    <Route path="/expenses" element={<Expenses />} />
                    {/* <Route path="/expenses/add" element={<ExpenseAdd />} />
                    <Route path="/expenses/:id" element={<ExpenseDetails />} />
                    <Route path="/groups/:groupId/expenses" element={<GroupExpenses />} />
                    <Route path="/profile" element={<Profile />} />*/}
                    <Route path="*" element={<NotFound />} />
                </Routes>
            </main>
        </Router>
    );
}

export default App;