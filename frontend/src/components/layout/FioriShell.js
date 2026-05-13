import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { APP_CONFIG } from '../../config';
import Navbar from './Navbar';

const FioriShell = ({ children }) => {
    const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
    const { user, logout } = useAuth();
    const navigate = useNavigate();

    const toggleMobileMenu = () => {
        setIsMobileMenuOpen(!isMobileMenuOpen);
    };

    const closeMobileMenu = () => {
        setIsMobileMenuOpen(false);
    };

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    // Helper to get initials
    const getInitials = (name) => {
        if (!name) return 'U';
        return name.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();
    };

    return (
        <div className="d-flex flex-column min-vh-100 bg-light">
            <nav className="navbar navbar-expand-lg navbar-light bg-white sticky-top shadow-sm">
                <div className="container-fluid">
                    <div className="d-flex align-items-center">
                        <button
                            className="btn btn-link text-dark text-decoration-none me-3 p-0 d-lg-none"
                            onClick={toggleMobileMenu}
                            aria-label="Toggle Navigation"
                        >
                            <span className="navbar-toggler-icon"></span>
                        </button>

                        <a className="navbar-brand fw-bold text-dark" href="#" onClick={(e) => { e.preventDefault(); navigate('/'); }}>
                            {APP_CONFIG.COMPANY_NAME}
                        </a>
                    </div>

                    <Navbar isOpen={isMobileMenuOpen} onClose={closeMobileMenu} />
                </div>
            </nav>

            <main className="flex-grow-1">
                {children}
            </main>

            <footer className="footer mt-auto py-3 bg-white border-top text-center text-muted small">
                <div className="container">
                    {APP_CONFIG.COPYRIGHT_TEXT}
                </div>
            </footer>
        </div>
    );
};

export default FioriShell;
