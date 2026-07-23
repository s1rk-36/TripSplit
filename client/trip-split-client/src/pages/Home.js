import { Link, useNavigate } from 'react-router-dom';
import { FaUsers, FaReceipt, FaBalanceScale } from 'react-icons/fa';
import { startDemo } from '../services/demoData';


function Home() {
  const navigate = useNavigate();

  const handleDemo = () => {
    startDemo();
    navigate('/groups');
  };

  return (
    <div>
      {/* Hero */}
      <section className="ts-hero py-5">
        <div className="container py-lg-4">
          <div className="row align-items-center min-vh-75">
            <div className="col-lg-6 ts-rise">
              <div className="ts-eyebrow ts-eyebrow--paper mb-3">
                Shared travel ledger
              </div>
              <h1 className="display-4 mb-4">
                One trip. One tab.<br /><span className="ts-underline">No spreadsheets.</span>
              </h1>
              <p className="ts-hero-lead mb-4">
                TripSplit keeps a running ledger for your group: who paid,
                who owes, and exactly how to settle up when the trip ends.
              </p>
              <div className="d-flex gap-3 flex-wrap align-items-center">
                <button onClick={handleDemo} className="btn btn-warning btn-lg px-4">
                  Try the live demo
                </button>
                <Link to="/register" className="btn btn-outline-paper btn-lg px-4">
                  Create free account
                </Link>
              </div>
              <p className="ts-hero-note mt-3 mb-0">
                Already keeping a tab? <Link to="/login">Sign in</Link>
              </p>
            </div>

            {/* Signature: boarding-pass sample split */}
            <div className="col-lg-6 mt-5 mt-lg-0 ts-rise ts-rise-delay-1">
              <div className="ts-ticket" aria-hidden="true">
                <div className="ts-ticket-head">
                  <span className="ts-eyebrow ts-eyebrow--paper">TripSplit · Group ledger</span>
                  <span className="ts-mono small">NO. 0042</span>
                </div>
                <div className="ts-ticket-body position-relative">
                  <div className="ts-ticket-row">
                    <strong>Japan Spring Trip</strong>
                    <span className="ts-mono small text-muted">4 travelers</span>
                  </div>
                  <div className="ts-ticket-row">
                    <span>Flight tickets · Sam paid</span>
                    <span className="ts-amount">$1,200.00</span>
                  </div>
                  <div className="ts-ticket-row">
                    <span>Ryokan, Kyoto · Jordan paid</span>
                    <span className="ts-amount">$800.00</span>
                  </div>
                  <div className="ts-ticket-row">
                    <span>Sushi dinner, Ginza · you paid</span>
                    <span className="ts-amount">$240.00</span>
                  </div>
                  <div className="ts-ticket-row ts-ticket-total">
                    <span>Your share</span>
                    <span className="ts-amount">$560.00</span>
                  </div>
                  <span className="ts-settled-stamp">Settled</span>
                </div>
                <div className="ts-ticket-stub">
                  <div>
                    <span className="ts-eyebrow">Invite code</span>
                    <div className="ts-stub-code">K7M2·QP9R</div>
                  </div>
                  <span className="ts-mono small text-muted">Share with your group</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* What it does */}
      <section className="py-5">
        <div className="container py-4">
          <div className="text-center mb-5 ts-reveal">
            <h2 className="ts-section-title display-6">Everything on one tab</h2>
            <hr className="ts-rule" />
            <p className="ts-page-sub">
              The whole trip's money, kept straight without anyone playing accountant.
            </p>
          </div>

          <div className="row g-4">
            <div className="col-md-4 ts-reveal">
              <div className="ts-feature">
                <FaUsers className="ts-feature-icon" size={28} />
                <h5>Groups</h5>
                <p>
                  Open a group for the trip and hand friends an invite code.
                  No emails to chase, no accounts to link.
                </p>
              </div>
            </div>

            <div className="col-md-4 ts-reveal ts-reveal-d1">
              <div className="ts-feature">
                <FaReceipt className="ts-feature-icon" size={28} />
                <h5>Receipts</h5>
                <p>
                  Log expenses as they happen and attach the receipt photo.
                  Every entry shows who paid and how it splits.
                </p>
              </div>
            </div>

            <div className="col-md-4 ts-reveal ts-reveal-d2">
              <div className="ts-feature">
                <FaBalanceScale className="ts-feature-icon" size={28} />
                <h5>Settling</h5>
                <p>
                  The ledger nets it all out: what you paid, what your share
                  is, and the one number that squares you with the group.
                </p>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* How it works: a real sequence, so numbered steps earn their place */}
      <section className="py-5" style={{ background: 'var(--ts-paper-raised)', borderTop: '1px solid var(--ts-line)', borderBottom: '1px solid var(--ts-line)' }}>
        <div className="container py-4">
          <div className="text-center mb-5 ts-reveal">
            <h2 className="ts-section-title display-6">From takeoff to settled</h2>
            <hr className="ts-rule" />
          </div>

          <div className="row g-4 text-center">
            <div className="col-md-4 ts-reveal">
              <span className="ts-step-num mb-3">01</span>
              <h5>Open a group</h5>
              <p className="ts-page-sub">
                Name the trip, then share the invite code with everyone coming along.
              </p>
            </div>

            <div className="col-md-4 ts-reveal ts-reveal-d1">
              <span className="ts-step-num mb-3">02</span>
              <h5>Log expenses</h5>
              <p className="ts-page-sub">
                Add costs as they happen: dinner, the rental car, the ryokan.
                Attach receipts, pick who's in on the split.
              </p>
            </div>

            <div className="col-md-4 ts-reveal ts-reveal-d2">
              <span className="ts-step-num mb-3">03</span>
              <h5>Settle up</h5>
              <p className="ts-page-sub">
                At the end, everyone sees one clear balance. Pay it, stamp it settled.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Closing CTA */}
      <section className="py-5">
        <div className="container py-3">
          <div className="ts-cta-band text-center p-5 ts-reveal">
            <div className="ts-eyebrow ts-eyebrow--paper mb-2">Boarding now</div>
            <h2 className="display-6 mb-3">Keep the trip. Lose the math.</h2>
            <div className="d-flex gap-3 justify-content-center flex-wrap">
              <Link to="/register" className="btn btn-warning btn-lg px-4">
                Start your first group
              </Link>
              <button onClick={handleDemo} className="btn btn-outline-paper btn-lg px-4">
                Try the demo first
              </button>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}

export default Home;
