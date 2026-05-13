import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import api from '../../services/api';

const PaymentFailedPage = () => {
    const { user } = useAuth();
    const { id } = useParams();
    const navigate = useNavigate();
    const [status, setStatus] = useState('processing'); // 'processing', 'success', 'error'
    const [errorMsg, setErrorMsg] = useState('');

    useEffect(() => {
        const markOrderAsFailed = async () => {
            // Determine orderId from route params OR session storage (Stripe default cancel)
            const orderId = id || sessionStorage.getItem('pendingOrderId');

            if (!orderId) {
                setStatus('error');
                setErrorMsg('Invalid order ID.');
                return;
            }

            try {
                // Determine order info to pass if required by the API
                // The API expects @RequestBody OrderDTO which requires userId
                let currentUserId = user?.id || user?.userId;
                if (!currentUserId) {
                    try {
                        const token = localStorage.getItem('accessToken');
                        if (token) {
                            const payload = JSON.parse(atob(token.split('.')[1]));
                            currentUserId = payload.id || payload.userId;
                        }
                    } catch (e) {
                        console.error("Failed to decode token", e);
                    }
                }

                // The backend API expects OrderRequestDTO with userId
                const orderData = { userId: currentUserId }; 
                
                await api.post(`/api/payment/${orderId}/payment/fail`, orderData);
                setStatus('success');
                
                // Clear the session storage pending order
                sessionStorage.removeItem('pendingOrderId');
            } catch (error) {
                console.error('Failed to update order status:', error);
                setStatus('error');
                setErrorMsg(error.response?.data || 'An error occurred while communicating with the server.');
            }
        };

        markOrderAsFailed();
    }, [id]);

    return (
        <div className="container py-5 text-center">
            <div className="row justify-content-center">
                <div className="col-md-6 col-lg-5">
                    <div className="card shadow-sm border-0 rounded-4 p-5">
                        {status === 'processing' && (
                            <div>
                                <div className="spinner-border text-danger mb-4" role="status" style={{ width: '3rem', height: '3rem' }}>
                                    <span className="visually-hidden">Loading...</span>
                                </div>
                                <h3 className="fw-bold">Cancelling Payment...</h3>
                                <p className="text-muted">Please wait while we update your order.</p>
                            </div>
                        )}

                        {status === 'success' && (
                            <div>
                                <div className="mb-4 text-danger">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" fill="currentColor" className="bi bi-x-circle" viewBox="0 0 16 16">
                                        <path d="M8 15A7 7 0 1 1 8 1a7 7 0 0 1 0 14zm0 1A8 8 0 1 0 8 0a8 8 0 0 0 0 16z"/>
                                        <path d="M4.646 4.646a.5.5 0 0 1 .708 0L8 7.293l2.646-2.647a.5.5 0 0 1 .708.708L8.707 8l2.647 2.646a.5.5 0 0 1-.708.708L8 8.707l-2.646 2.647a.5.5 0 0 1-.708-.708L7.293 8 4.646 5.354a.5.5 0 0 1 0-.708z"/>
                                    </svg>
                                </div>
                                <h3 className="fw-bold text-danger">Payment Failed</h3>
                                <p className="text-muted mb-4">
                                    Your payment process was cancelled or failed. Your order #{id || sessionStorage.getItem('pendingOrderId')} has been marked as failed.
                                    You can review your cart or try again later.
                                </p>
                                <div className="d-grid gap-2">
                                    <Link to="/cart" className="btn btn-primary rounded-pill py-2 fw-bold">
                                        Return to Cart
                                    </Link>
                                    <Link to="/" className="btn btn-outline-secondary rounded-pill py-2 fw-bold">
                                        Continue Shopping
                                    </Link>
                                </div>
                            </div>
                        )}

                        {status === 'error' && (
                            <div>
                                <div className="mb-4 text-warning">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" fill="currentColor" className="bi bi-exclamation-triangle" viewBox="0 0 16 16">
                                        <path d="M7.938 2.016A.13.13 0 0 1 8.002 2a.13.13 0 0 1 .063.016.146.146 0 0 1 .054.057l6.857 11.667c.036.06.035.124.002.183a.163.163 0 0 1-.054.06.116.116 0 0 1-.066.017H1.146a.115.115 0 0 1-.066-.017.163.163 0 0 1-.054-.06.176.176 0 0 1 .002-.183L7.884 2.073a.147.147 0 0 1 .054-.057zm1.044-.45a1.13 1.13 0 0 0-1.96 0L.165 13.233c-.457.778.091 1.767.98 1.767h13.713c.889 0 1.438-.99.98-1.767L8.982 1.566z"/>
                                        <path d="M7.002 12a1 1 0 1 1 2 0 1 1 0 0 1-2 0zM7.1 5.995a.905.905 0 1 1 1.8 0l-.35 3.507a.552.552 0 0 1-1.1 0L7.1 5.995z"/>
                                    </svg>
                                </div>
                                <h3 className="fw-bold">Notice</h3>
                                <p className="text-muted mb-4">{errorMsg}</p>
                                <Link to="/cart" className="btn btn-primary rounded-pill py-2 fw-bold px-4">
                                    Go to Cart
                                </Link>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default PaymentFailedPage;
