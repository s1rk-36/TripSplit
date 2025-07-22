import { Link } from 'react-router-dom';
import { FaDollarSign, FaUsers, FaReceipt, FaCalculator, FaArrowRight, FaCheck } from 'react-icons/fa';

function Home() {
  return (
    <div>
      {/* Main Section */}
      <section className="bg-primary text-white py-5">
        <div className="container">
          <div className="row align-items-center min-vh-75">
            <div className="col-lg-6">
              <h1 className="display-4 fw-bold mb-4">
                Split Travel Expenses with Friends
              </h1>
              <p className="lead mb-4">
                Track shared expenses, split bills fairly, and settle up easily. 
                Perfect for group trips, roommates, and shared adventures.
              </p>
              <div className="d-flex gap-3 flex-wrap">
                <Link to="/register" className="btn btn-light btn-lg px-4">
                  Get Started Free <FaArrowRight className="ms-2" />
                </Link>
                <Link to="/login" className="btn btn-outline-light btn-lg px-4">
                  Sign In
                </Link>
              </div>
            </div>
            <div className="col-lg-6 text-center">
              {/* App Screenshots */}
              <div className="position-relative">
                <div className="row g-3">
                  <div className="col-6">
                    <div className="bg-white rounded-3 p-3 shadow-lg">
                      <img 
                        src=""
                        alt="TripSplit Dashboard"
                        className="img-fluid rounded-2"
                        style={{ maxHeight: '400px', objectFit: 'cover' }}
                      />
                      <div className="mt-2">
                        <small className="text-muted fw-bold">Dashboard View</small>
                      </div>
                    </div>
                  </div>
                  <div className="col-6">
                    <div className="bg-white rounded-3 p-3 shadow-lg">
                      <img 
                        src=""
                        alt="Expense Split View"
                        className="img-fluid rounded-2"
                        style={{ maxHeight: '400px', objectFit: 'cover' }}
                      />
                      <div className="mt-2">
                        <small className="text-muted fw-bold">Expense Splitting</small>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="py-5">
        <div className="container">
          <div className="row text-center mb-5">
            <div className="col">
              <h2 className="display-5 fw-bold mb-3">Why Choose TripSplit?</h2>
              <p className="lead text-muted">
                Make group expense management simple and stress-free
              </p>
            </div>
          </div>
          
          <div className="row g-4">
            <div className="col-md-4">
              <div className="card h-100 border-0 shadow-sm">
                <div className="card-body text-center p-4">
                  <FaUsers className="text-primary mb-3" size={48} />
                  <h5 className="card-title">Group Management</h5>
                  <p className="card-text">
                    Create groups for trips, events, or shared living. 
                    Add friends and track expenses together.
                  </p>
                </div>
              </div>
            </div>
            
            <div className="col-md-4">
              <div className="card h-100 border-0 shadow-sm">
                <div className="card-body text-center p-4">
                  <FaReceipt className="text-success mb-3" size={48} />
                  <h5 className="card-title">Smart Expense Tracking</h5>
                  <p className="card-text">
                    Add expenses, upload receipts, and split costs fairly. 
                    Track who paid what and who owes whom.
                  </p>
                </div>
              </div>
            </div>
            
            <div className="col-md-4">
              <div className="card h-100 border-0 shadow-sm">
                <div className="card-body text-center p-4">
                  <FaCalculator className="text-warning mb-3" size={48} />
                  <h5 className="card-title">Automatic Calculations</h5>
                  <p className="card-text">
                    No more manual math! We calculate who owes what 
                    and provide clear settlement recommendations.
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* How It Works Section */}
      <section className="py-5 bg-light">
        <div className="container">
          <div className="row text-center mb-5">
            <div className="col">
              <h2 className="display-5 fw-bold mb-3">How It Works</h2>
              <p className="lead text-muted">Get started in just 3 simple steps</p>
            </div>
          </div>
          
          <div className="row g-4">
            <div className="col-md-4 text-center">
              <div className="bg-primary text-white rounded-circle d-inline-flex align-items-center justify-content-center mb-3" style={{width: '60px', height: '60px'}}>
                <span className="fw-bold fs-4">1</span>
              </div>
              <h5>Create a Group</h5>
              <p className="text-muted">
                Start by creating a group for your trip or shared expenses. 
                Invite friends via group code.
              </p>
            </div>
            
            <div className="col-md-4 text-center">
              <div className="bg-success text-white rounded-circle d-inline-flex align-items-center justify-content-center mb-3" style={{width: '60px', height: '60px'}}>
                <span className="fw-bold fs-4">2</span>
              </div>
              <h5>Add Expenses</h5>
              <p className="text-muted">
                Record expenses as they happen. Add receipts, 
                choose who to split with, and let us do the math.
              </p>
            </div>
            
            <div className="col-md-4 text-center">
              <div className="bg-warning text-white rounded-circle d-inline-flex align-items-center justify-content-center mb-3" style={{width: '60px', height: '60px'}}>
                <span className="fw-bold fs-4">3</span>
              </div>
              <h5>Settle Up</h5>
              <p className="text-muted">
                See who owes what at a glance.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Benefits Section */}
      <section className="py-5">
        <div className="container">
          <div className="row align-items-center">
            <div className="col-lg-6">
              <h2 className="display-5 fw-bold mb-4">Perfect for Every Occasion</h2>
              <div className="mb-3">
                <FaCheck className="text-success me-2" />
                <strong>Group Travel:</strong> Vacations, weekend trips, business travel
              </div>
              <div className="mb-3">
                <FaCheck className="text-success me-2" />
                <strong>Shared Living:</strong> Roommate expenses, utilities, groceries
              </div>
              <div className="mb-3">
                <FaCheck className="text-success me-2" />
                <strong>Events:</strong> Parties, dinners, group activities
              </div>
              <div className="mt-4">
                <Link to="/register" className="btn btn-primary btn-lg">
                  Start Splitting Expenses
                </Link>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="py-4 bg-dark text-white-50">
        <div className="container">
          <div className="row">
            <div className="col-md-6">
              <div className="d-flex align-items-center mb-2">
                <FaDollarSign className="me-2" />
                <strong className="text-white">TripSplit</strong>
              </div>
              <p className="small">Kristian, Guillermo, Louie</p>
            </div>
            <div className="col-md-6 text-md-end">
              <div className="d-flex justify-content-md-end gap-3">
                <Link to="/login" className="text-white-50 text-decoration-none">Login</Link>
                <Link to="/register" className="text-white-50 text-decoration-none">Sign Up</Link>
              </div>
            </div>
          </div>
        </div>
      </footer>
    </div>
  );
}

export default Home;