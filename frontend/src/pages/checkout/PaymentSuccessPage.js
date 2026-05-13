import React, { useEffect, useState, useRef } from 'react';
import { useSearchParams, useNavigate, Link } from 'react-router-dom';
import { useCart } from '../../context/CartContext';
import api from '../../services/api';

const PaymentSuccessPage = () => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const { clearCart } = useCart();
    
    const [status, setStatus] = useState('processing');
    const [errorMsg, setErrorMsg] = useState('');
    
    const isConfirming = useRef(false);

    useEffect(() => {
        const confirmPayment = async () => {
            const sessionId = searchParams.get('session_id');
            if (!sessionId) {
                setStatus('error');
                setErrorMsg('Invalid or missing session ID.');
                return;
            }

            // Prevent strict mode double execution issue
            if (isConfirming.current) return;
            isConfirming.current = true;

            try {
                // Call backend confirmation API
                await api.post('/api/payment/payment/confirm', { sessionId });
                
                setStatus('success');
                clearCart();
            } catch (error) {
                console.error('Failed to confirm payment:', error);
                setStatus('error');
                setErrorMsg(error.response?.data || 'An error occurred while confirming payment.');
            }
        };

        confirmPayment();
    }, [searchParams, clearCart]);

    return (
        <div className="container py-5 text-center">
            <div className="row justify-content-center">
                <div className="col-md-6 col-lg-5">
                    <div className="card shadow-sm border-0 rounded-4 p-5">
                        {status === 'processing' && (
                            <div>
                                <div className="spinner-border text-primary mb-4" role="status" style={{ width: '3rem', height: '3rem' }}>
                                    <span className="visually-hidden">Loading...</span>
                                </div>
                                <h3 className="fw-bold">Confirming Payment...</h3>
                                <p className="text-muted">Please wait while we finalize your order.</p>
                            </div>
                        )}

                        {status === 'success' && (
                            <div>
                                <div className="mb-4 text-success">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" fill="currentColor" className="bi bi-check-circle-fill" viewBox="0 0 16 16">
                                        <path d="M16 8A8 8 0 1 1 0 8a8 8 0 0 1 16 0zm-3.97-3.03a.75.75 0 0 0-1.08.022L7.477 9.417 5.384 7.323a.75.75 0 0 0-1.06 1.06L6.97 11.03a.75.75 0 0 0 1.079-.02l3.992-4.99a.75.75 0 0 0-.01-1.05z"/>
                                    </svg>
                                </div>
                                <h3 className="fw-bold text-success">Payment Successful!</h3>
                                <p className="text-muted mb-4">
                                    Thank you! Your order has been placed successfully.
                                    You can check your order history in your Profile.
                                </p>
                                <div className="d-grid gap-2">
                                    <Link to="/products" className="btn btn-primary rounded-pill py-2 fw-bold">
                                        Continue Shopping
                                    </Link>
                                    <Link to="/profile" className="btn btn-outline-secondary rounded-pill py-2 fw-bold">
                                        View Orders
                                    </Link>
                                </div>
                            </div>
                        )}

                        {status === 'error' && (
                            <div>
                                <div className="mb-4 text-danger">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" fill="currentColor" className="bi bi-exclamation-circle-fill" viewBox="0 0 16 16">
                                        <path d="M16 8A8 8 0 1 1 0 8a8 8 0 0 1 16 0zM8 4a.905.905 0 0 0-.9.995l.35 3.507a.552.552 0 0 0 1.1 0l.35-3.507A.905.905 0 0 0 8 4zm.002 6a1 1 0 1 0 0 2 1 1 0 0 0 0-2z"/>
                                    </svg>
                                </div>
                                <h3 className="fw-bold">Notice</h3>
                                <p className="text-muted mb-4">{errorMsg}</p>
                                <Link to="/" className="btn btn-primary rounded-pill py-2 fw-bold px-4">
                                    Go Home
                                </Link>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default PaymentSuccessPage;
