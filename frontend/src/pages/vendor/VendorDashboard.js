import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import api from '../../services/api';
import { Link } from 'react-router-dom';

const getMonthStartEnd = () => {
    const date = new Date();
    return {
        startDate: new Date(date.getFullYear(), date.getMonth(), 1)
            .toISOString().split('T')[0],
        endDate: new Date(date.getFullYear(), date.getMonth() + 1, 0)
            .toISOString().split('T')[0]
    };
};

const getRecentTransactionsDateRange = () => {
    const end = new Date();
    end.setDate(end.getDate() + 1); // Add 1 day to correctly capture today
    
    const start = new Date();
    start.setMonth(start.getMonth() - 6); // Look back 6 months for safety
    start.setDate(1);
    
    return { 
        startDate: start.toISOString().split('T')[0], 
        endDate: end.toISOString().split('T')[0] 
    };
};

const VendorDashboard = () => {
    const { user } = useAuth();
    const [totalReport, setTotalReport] = useState(null);
    const [monthReport, setMonthReport] = useState(null);
    const [recentSales, setRecentSales] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        const fetchDashboardData = async () => {
            try {
                setLoading(true);
                
                // Fetch Total Report
                const totalRes = await api.get('/api/sales/total-report');
                setTotalReport(totalRes.data);

                // Fetch Monthly Report
                const monthDates = getMonthStartEnd();
                const monthRes = await api.get('/api/sales/report', { 
                    params: { vendorId: user?.id, ...monthDates } 
                });
                setMonthReport(monthRes.data);

                // Fetch Sales for recent transactions
                const recentDates = getRecentTransactionsDateRange();
                const salesRes = await api.get('/api/sales', { 
                    params: { vendorId: user?.id, ...recentDates } 
                });
                
                const sales = salesRes.data || [];
                // Sort by date newest first and take top 10
                const sorted = [...sales].sort((a,b) => new Date(b.createdAt) - new Date(a.createdAt));
                setRecentSales(sorted.slice(0, 10));
                
            } catch (err) {
                console.error("Dashboard error:", err);
                setError(err.response?.data?.message || err.message || 'Failed to fetch dashboard data');
            } finally {
                setLoading(false);
            }
        };

        if (user && user.role === 'VENDOR') {
            fetchDashboardData();
        }
    }, [user]);

    if (loading) return <div className="container mt-4"><div className="spinner-border text-primary" role="status"><span className="visually-hidden">Loading...</span></div></div>;
    if (error) return <div className="container mt-4 alert alert-danger">{error}</div>;

    const formatCurrency = (amount) => {
        return new Intl.NumberFormat('en-IN', {
            style: 'currency',
            currency: 'INR'
        }).format(amount || 0);
    };

    return (
        <div className="container mt-4">
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h2 className="fw-bold mb-0">Vendor Dashboard</h2>
                <Link to="/sales-report" className="btn btn-outline-primary btn-sm">
                    <i className="bi bi-funnel me-1"></i> Full Sales Report
                </Link>
            </div>
            
            <div className="row g-4 mb-4">
                <div className="col-md-6 col-lg-4">
                    <div className="card shadow-sm border-0 h-100 bg-primary text-white">
                        <div className="card-body">
                            <h6 className="card-subtitle mb-2 text-white-50">Total Revenue</h6>
                            <h3 className="card-title mb-0">{formatCurrency(totalReport?.totalRevenue)}</h3>
                            <div className="mt-3 small">
                                <div><i className="bi bi-box-seam me-2"></i>{totalReport?.totalOrders || 0} Orders</div>
                                <div><i className="bi bi-stack me-2"></i>{totalReport?.totalQuantity || 0} Items Sold</div>
                            </div>
                        </div>
                    </div>
                </div>

                <div className="col-md-6 col-lg-4">
                    <div className="card shadow-sm border-0 h-100 bg-success text-white">
                        <div className="card-body">
                            <h6 className="card-subtitle mb-2 text-white-50">This Month Sales</h6>
                            <h3 className="card-title mb-0">{formatCurrency(monthReport?.totalRevenue)}</h3>
                            <div className="mt-3 small">
                                <div><i className="bi bi-box-seam me-2"></i>{monthReport?.totalOrders || 0} Orders</div>
                                <div><i className="bi bi-stack me-2"></i>{monthReport?.totalQuantity || 0} Items Sold</div>
                            </div>
                        </div>
                    </div>
                </div>
                
                <div className="col-md-6 col-lg-4">
                    <div className="card shadow-sm border-0 h-100 bg-info text-white">
                        <div className="card-body">
                            <h6 className="card-subtitle mb-2 text-white-50">Total Discount Given</h6>
                            <h3 className="card-title mb-0">{formatCurrency(totalReport?.totalDiscount)}</h3>
                        </div>
                    </div>
                </div>
            </div>

            <div className="row g-4 mb-4">
                <div className="col-12">
                    <div className="card shadow-sm border-0">
                        <div className="card-header bg-white border-bottom-0 pt-4 pb-2">
                            <h5 className="card-title fw-semibold mb-0">Recent Transactions</h5>
                        </div>
                        <div className="card-body p-0">
                            <div className="table-responsive">
                                <table className="table table-hover align-middle mb-0">
                                    <thead className="table-light">
                                        <tr>
                                            <th className="ps-4">Order ID</th>
                                            <th>Product ID</th>
                                            <th>Quantity</th>
                                            <th>Final Price</th>
                                            <th>Date</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {recentSales.map((sale) => (
                                            <tr key={sale.id}>
                                                <td className="ps-4 fw-semibold text-primary">#{sale.orderId}</td>
                                                <td><span className="badge bg-secondary text-truncate" style={{maxWidth: '120px'}} title={sale.productId}>{sale.productId}</span></td>
                                                <td>{sale.quantity}</td>
                                                <td className="fw-semibold text-success">{formatCurrency(sale.finalPrice)}</td>
                                                <td className="text-muted small">
                                                    {new Date(sale.createdAt).toLocaleDateString('en-IN', {
                                                        day: '2-digit', month: 'short', year: 'numeric',
                                                        hour: '2-digit', minute: '2-digit'
                                                    })}
                                                </td>
                                            </tr>
                                        ))}
                                        {recentSales.length === 0 && (
                                            <tr>
                                                <td colSpan="5" className="text-center py-5 text-muted">
                                                    No recent transactions found.
                                                </td>
                                            </tr>
                                        )}
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default VendorDashboard;
