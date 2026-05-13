import React, { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import api from '../../services/api';

const SalesReport = () => {
    const { user } = useAuth();
    const [startDate, setStartDate] = useState('');
    const [endDate, setEndDate] = useState('');
    const [sales, setSales] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const handleSubmit = async (e) => {
        e.preventDefault();
        
        if (!startDate || !endDate) {
            setError('Please select both start and end dates');
            return;
        }

        try {
            setLoading(true);
            setError(null);
            
            const endObj = new Date(endDate);
            endObj.setDate(endObj.getDate() + 1);

            const res = await api.get('/api/sales', {
                params: {
                    vendorId: user?.id,
                    startDate: startDate,
                    endDate: endObj.toISOString().split('T')[0]
                }
            });

            // Sort newest first
            const sorted = (res.data || []).sort((a,b) => new Date(b.createdAt) - new Date(a.createdAt));
            setSales(sorted);
        } catch (err) {
            console.error("Sales report error:", err);
            setError(err.response?.data?.message || err.message || 'Failed to fetch sales report');
        } finally {
            setLoading(false);
        }
    };

    const formatCurrency = (amount) => {
        return new Intl.NumberFormat('en-IN', {
            style: 'currency',
            currency: 'INR'
        }).format(amount || 0);
    };

    if (user?.role !== 'VENDOR') {
        return <div className="container mt-4 alert alert-warning">Only vendors can view this page.</div>;
    }

    return (
        <div className="container py-4">
            <h2 className="mb-4 fw-bold">Sales Report</h2>
            
            <div className="card shadow-sm border-0 mb-4">
                <div className="card-body">
                    <form onSubmit={handleSubmit} className="row g-3 align-items-end">
                        <div className="col-md-4">
                            <label className="form-label fw-semibold">Start Date</label>
                            <input 
                                type="date" 
                                className="form-control bg-light" 
                                value={startDate} 
                                onChange={(e) => setStartDate(e.target.value)}
                                max={endDate || undefined}
                            />
                        </div>
                        <div className="col-md-4">
                            <label className="form-label fw-semibold">End Date</label>
                            <input 
                                type="date" 
                                className="form-control bg-light" 
                                value={endDate} 
                                onChange={(e) => setEndDate(e.target.value)}
                                min={startDate || undefined}
                            />
                        </div>
                        <div className="col-md-4">
                            <button type="submit" className="btn btn-primary w-100 py-2 d-flex align-items-center justify-content-center gap-2" disabled={loading}>
                                {loading ? (
                                    <><span className="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Fetching...</>
                                ) : (
                                    <><i className="bi bi-search"></i> Get Report</>
                                )}
                            </button>
                        </div>
                    </form>
                    {error && <div className="mt-3 text-danger small"><i className="bi bi-exclamation-triangle-fill me-1"></i>{error}</div>}
                </div>
            </div>

            <div className="card shadow-sm border-0">
                <div className="card-header bg-white py-3 border-0">
                    <h5 className="mb-0 fw-semibold">Resulting Orders</h5>
                </div>
                <div className="table-responsive">
                    <table className="table table-hover align-middle mb-0">
                        <thead className="table-light">
                            <tr>
                                <th>Order ID</th>
                                <th>Product ID</th>
                                <th>Quantity</th>
                                <th>Unit Price</th>
                                <th>Discount</th>
                                <th>Final Price</th>
                                <th>Date</th>
                            </tr>
                        </thead>
                        <tbody>
                            {sales.length > 0 ? (
                                sales.map((sale) => (
                                    <tr key={sale.id}>
                                        <td>#{sale.orderId}</td>
                                        <td><span className="badge bg-secondary text-truncate" style={{maxWidth: '120px'}} title={sale.productId}>{sale.productId}</span></td>
                                        <td>{sale.quantity}</td>
                                        <td>{formatCurrency(sale.unitPrice)}</td>
                                        <td className="text-danger">-{formatCurrency(sale.discountAmount)}</td>
                                        <td className="fw-semibold text-success">{formatCurrency(sale.finalPrice)}</td>
                                        <td className="text-muted small">
                                            {new Date(sale.createdAt).toLocaleDateString('en-IN', {
                                                day: '2-digit', month: 'short', year: 'numeric',
                                                hour: '2-digit', minute: '2-digit'
                                            })}
                                        </td>
                                    </tr>
                                ))
                            ) : (
                                <tr>
                                    <td colSpan="7" className="text-center py-5 text-muted">
                                        <i className="bi bi-inbox fs-2 text-black-50 d-block mb-3"></i>
                                        {loading ? "Loading..." : "No sales entries found for the selected date range."}
                                    </td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
};

export default SalesReport;
