import React from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import Greeting from './Greeting';
import { useCart } from '../../context/CartContext';

const Navbar = ({ isOpen, onClose }) => {
    const { user, logout } = useAuth();
    const { cartCount } = useCart();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/login');
        onClose(); // Close sidebar on logout
    };

    if (!user) return null;

    // Admin and Vendor see management links
    const showManagementLinks = user.role === 'ADMIN' || user.role === 'VENDOR';

    return (
        <>
            {/* Desktop Navbar (Visible on Large Screens) */}
            <div className="collapse navbar-collapse d-none d-lg-block">
                <ul className="navbar-nav me-auto mb-2 mb-lg-0 gap-3">
                    {showManagementLinks && (
                        <li className="nav-item dropdown">
                            <a className="nav-link dropdown-toggle fw-semibold" href="#" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                                Catalog
                            </a>
                            <ul className="dropdown-menu shadow-sm border-0">
                                <li>
                                    <Link className="dropdown-item" to="/products/manage">Manage Catalog</Link>
                                </li>
                            </ul>
                        </li>
                    )}
                    {user.role === 'VENDOR' && (
                        <>
                            <li className="nav-item dropdown">
                                <a className="nav-link dropdown-toggle fw-semibold" href="#" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                                    Inventory
                                </a>
                                <ul className="dropdown-menu shadow-sm border-0">
                                    <li>
                                        <Link className="dropdown-item" to="/inventory?view=add">Add Items</Link>
                                    </li>
                                    <li>
                                        <Link className="dropdown-item" to="/inventory?view=list">My Inventory</Link>
                                    </li>
                                </ul>
                            </li>
                            <li className="nav-item dropdown">
                                <a className="nav-link dropdown-toggle fw-semibold" href="#" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                                    Reports
                                </a>
                                <ul className="dropdown-menu shadow-sm border-0">
                                    <li>
                                        <Link className="dropdown-item" to="/">Dashboard</Link>
                                    </li>
                                    <li>
                                        <Link className="dropdown-item" to="/sales-report">Sales Report</Link>
                                    </li>
                                </ul>
                            </li>
                        </>
                    )}
                </ul>
                
                <div className="d-flex align-items-center gap-3">
                    {user.role === 'CUSTOMER' && (
                        <Link to="/cart" className="btn btn-link link-dark position-relative p-2">
                            <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" fill="currentColor" className="bi bi-cart3" viewBox="0 0 16 16">
                                <path d="M0 1.5A.5.5 0 0 1 .5 1H2a.5.5 0 0 1 .485.379L2.89 3H14.5a.5.5 0 0 1 .49.598l-1 5a.5.5 0 0 1-.465.401l-9.397.472L4.415 11H13a.5.5 0 0 1 0 1H4a.5.5 0 0 1-.491-.408L2.01 3.607 1.61 2H.5a.5.5 0 0 1-.5-.5zM3.102 4l.84 4.479 9.144-.459L13.89 4H3.102zM5 12a2 2 0 1 0 0 4 2 2 0 0 0 0-4zm7 0a2 2 0 1 0 0 4 2 2 0 0 0 0-4zm-7 1a1 1 0 1 1 0 2 1 1 0 0 1 0-2zm7 0a1 1 0 1 1 0 2 1 1 0 0 1 0-2z" />
                            </svg>
                            {cartCount > 0 && (
                                <span className="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger" style={{ fontSize: '0.6rem' }}>
                                    {cartCount}
                                </span>
                            )}
                        </Link>
                    )}
                    <Greeting name={user.name} />
                    <Link to="/profile" className="btn btn-outline-primary btn-sm">
                        Profile
                    </Link>
                    <button
                        className="btn btn-outline-dark btn-sm"
                        onClick={handleLogout}
                    >
                        Logout
                    </button>
                </div>
            </div>

            {/* Mobile Offcanvas Sidebar (Visible on Small Screens) */}
            <div className={`offcanvas offcanvas-start ${isOpen ? 'show' : ''} d-lg-none`} tabIndex="-1" style={{ visibility: isOpen ? 'visible' : 'hidden' }}>
                <div className="offcanvas-header bg-light border-bottom">
                    <h5 className="offcanvas-title">Menu</h5>
                    <button type="button" className="btn-close text-reset" onClick={onClose} aria-label="Close"></button>
                </div>
                <div className="offcanvas-body p-0">
                    <ul className="list-group list-group-flush">
                        <li className="list-group-item bg-light-subtle">
                            <Link to="/cart" className="text-decoration-none text-dark d-flex align-items-center justify-content-between py-2" onClick={onClose}>
                                <div className="d-flex align-items-center gap-2">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" fill="currentColor" viewBox="0 0 16 16">
                                        <path d="M0 1.5A.5.5 0 0 1 .5 1H2a.5.5 0 0 1 .485.379L2.89 3H14.5a.5.5 0 0 1 .49.598l-1 5a.5.5 0 0 1-.465.401l-9.397.472L4.415 11H13a.5.5 0 0 1 0 1H4a.5.5 0 0 1-.491-.408L2.01 3.607 1.61 2H.5a.5.5 0 0 1-.5-.5zM3.102 4l.84 4.479 9.144-.459L13.89 4H3.102zM5 12a2 2 0 1 0 0 4 2 2 0 0 0 0-4zm7 0a2 2 0 1 0 0 4 2 2 0 0 0 0-4zm-7 1a1 1 0 1 1 0 2 1 1 0 0 1 0-2zm7 0a1 1 0 1 1 0 2 1 1 0 0 1 0-2z" />
                                    </svg>
                                    <span>My Cart</span>
                                </div>
                                {cartCount > 0 && <span className="badge rounded-pill bg-danger">{cartCount}</span>}
                            </Link>
                        </li>
                        <li className="list-group-item bg-light-subtle">
                            <Link to="/profile" className="text-decoration-none text-dark d-flex align-items-center py-2" onClick={onClose}>
                                <div className="d-flex align-items-center gap-2">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" fill="currentColor" viewBox="0 0 16 16">
                                        <path d="M11 6a3 3 0 1 1-6 0 3 3 0 0 1 6 0z"/>
                                        <path fillRule="evenodd" d="M0 8a8 8 0 1 1 16 0A8 8 0 0 1 0 8zm8-7a7 7 0 0 0-5.468 11.37C3.242 11.226 4.805 10 8 10s4.757 1.225 5.468 2.37A7 7 0 0 0 8 1z"/>
                                    </svg>
                                    <span>My Profile</span>
                                </div>
                            </Link>
                        </li>
                        {showManagementLinks ? (
                            <>
                                <li className="list-group-item">
                                    <Link to="/products/manage" className="text-decoration-none text-dark d-block py-2" onClick={onClose}>
                                        Manage Catalog
                                    </Link>
                                </li>
                                {user.role === 'VENDOR' && (
                                    <>
                                        <li className="list-group-item">
                                            <Link to="/inventory?view=add" className="text-decoration-none text-dark d-block py-2" onClick={onClose}>
                                                Inventory: Add Items
                                            </Link>
                                        </li>
                                        <li className="list-group-item">
                                            <Link to="/inventory?view=list" className="text-decoration-none text-dark d-block py-2" onClick={onClose}>
                                                Inventory: My Inventory
                                            </Link>
                                        </li>
                                        <li className="list-group-item">
                                            <Link to="/" className="text-decoration-none text-dark d-block py-2" onClick={onClose}>
                                                Dashboard
                                            </Link>
                                        </li>
                                        <li className="list-group-item">
                                            <Link to="/sales-report" className="text-decoration-none text-dark d-block py-2" onClick={onClose}>
                                                Sales Report
                                            </Link>
                                        </li>
                                    </>
                                )}
                            </>
                        ) : (
                            <div className="p-4 text-center text-muted">
                                No additional menus available.
                            </div>
                        )}
                    </ul>
                </div>
                <div className="offcanvas-footer border-top p-3 bg-light">
                    <div className="mb-3 text-center">
                        <Greeting name={user.name} />
                    </div>
                    <button className="btn btn-outline-danger w-100" onClick={handleLogout}>
                        Logout
                    </button>
                </div>
            </div>
            {/* Backdrop */}
            {isOpen && <div className="offcanvas-backdrop fade show d-lg-none" onClick={onClose}></div>}
        </>
    );
};

export default Navbar;
