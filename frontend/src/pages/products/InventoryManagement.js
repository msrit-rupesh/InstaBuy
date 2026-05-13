import React, { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import api from '../../services/api';
import { useAuth } from '../../context/AuthContext';

const InventoryManagement = () => {
    const { user } = useAuth();
    const location = useLocation();
    const navigate = useNavigate();
    const queryParams = new URLSearchParams(location.search);
    const initialView = queryParams.get('view') || 'list';

    const [view, setView] = useState(initialView); // 'add' or 'list'
    const [allProducts, setAllProducts] = useState([]);
    const [products, setProducts] = useState([]); // Slice of products
    const [allInventory, setAllInventory] = useState([]);
    const [inventory, setInventory] = useState([]); // Slice of inventory
    const [loading, setLoading] = useState(false);
    const [actionLoading, setActionLoading] = useState(false);
    const [error, setError] = useState(null);
    const [searchTerm, setSearchTerm] = useState('');

    // Pagination state
    const [page, setPage] = useState(1);
    const [pageSize, setPageSize] = useState(10);
    const [totalPages, setTotalPages] = useState(0);

    // Modal state
    const [showModal, setShowModal] = useState(false);
    const [modalType, setModalType] = useState('add'); // 'add' or 'edit'
    const [selectedItem, setSelectedItem] = useState(null);
    const [formData, setFormData] = useState({
        productId: '',
        price: '',
        discount: '0',
        quantity: ''
    });

    useEffect(() => {
        const currentView = queryParams.get('view') || 'list';
        setView(currentView);
        setPage(1); // Reset page on view change
        setError(null); // Reset error state on view switch
        
        // Always ensure products are fetched for name lookup
        if (allProducts.length === 0) fetchProducts();
        
        if (currentView === 'list') {
            fetchInventory();
        }
    }, [location.search]);

    // Handle slicing for pagination
    useEffect(() => {
        const data = view === 'add' ? allProducts : allInventory;
        const startIndex = (page - 1) * pageSize;
        const endIndex = startIndex + pageSize;
        
        if (view === 'add') {
            setProducts(data.slice(startIndex, endIndex));
        } else {
            setInventory(data.slice(startIndex, endIndex));
        }
        
        setTotalPages(Math.ceil(data.length / pageSize));
    }, [allProducts, allInventory, page, pageSize, view]);

    const fetchProducts = async () => {
        setLoading(true);
        setError(null);
        try {
            const term = searchTerm.trim();
            let response;
            if (term !== '') {
                response = await api.get('/api/products/search', {
                    params: { query: term }
                });
            } else {
                response = await api.get('/api/products');
            }
            const data = response.data;
            const fetched = Array.isArray(data) ? data : (data.content || []);
            setAllProducts(fetched);
        } catch (err) {
            if (!err.response || err.response.status === 500) {
                setError("Failed to fetch products");
            }
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const handleSearchSubmit = (e) => {
        e.preventDefault();
        fetchProducts();
    };

    const fetchInventory = async () => {
        setLoading(true);
        setError(null);
        try {
            const response = await api.get('/api/inventory/vendor');
            const fetched = response.data || [];
            setAllInventory(fetched);
            setPage(1);
        } catch (err) {
            if (!err.response || err.response.status === 500) {
                setError("Failed to fetch your inventory");
            }
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const handleOpenAddModal = async (product) => {
        if (user?.role === 'VENDOR' || user?.roleId === 2) {
            try {
                const vendorRes = await api.get('/api/profile/vendor');
                const v = vendorRes.data;
                if (!v || !v.companyName || !v.phone || !v.streetAddress || !v.city || !v.state || !v.country || !v.postalCode) {
                    setError("Please complete your Vendor Profile before adding items to your inventory.");
                    setTimeout(() => navigate('/profile'), 3000);
                    return;
                }
            } catch (err) {
                setError("Please complete your Vendor Profile before adding items to your inventory.");
                setTimeout(() => navigate('/profile'), 3000);
                return;
            }
        }

        setSelectedItem(product);
        setModalType('add');
        setFormData({
            productId: product.id,
            price: '',
            discount: '0',
            quantity: ''
        });
        setShowModal(true);
    };

    const handleOpenEditModal = (item) => {
        setSelectedItem(item);
        setModalType('edit');
        setFormData({
            productId: item.productId,
            price: item.price,
            discount: item.discount,
            quantity: item.quantity || 1 // Assuming quantity might be missing in some responses initially
        });
        setShowModal(true);
    };

    const handleCloseModal = () => {
        setShowModal(false);
        setSelectedItem(null);
        setFormData({ productId: '', price: '', discount: '0', quantity: '' });
    };

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setActionLoading(true);
        try {
            if (modalType === 'add') {
                await api.post('/api/inventory', formData);
                alert("Product added to inventory successfully!");
            } else {
                // Assuming PUT /api/inventory/{id} or similar for edit
                // If the user didn't specify the edit endpoint, I'll use a generic one or assume /api/inventory
                await api.patch(`/api/inventory/${selectedItem.id}`, formData);
                alert("Inventory updated successfully!");
            }
            handleCloseModal();
            if (view === 'list') fetchInventory(); else navigate('/inventory?view=list');
        } catch (err) {
            console.error(err);
            alert(err.response?.data?.message || "Failed to save inventory item");
        } finally {
            setActionLoading(false);
        }
    };

    const handleDelete = async (id) => {
        if (!window.confirm("Are you sure you want to remove this item from your inventory?")) return;
        
        setActionLoading(true);
        try {
            await api.delete(`/api/inventory/${id}`);
            alert("Item removed from inventory");
            fetchInventory();
        } catch (err) {
            console.error(err);
            alert("Failed to delete item");
        } finally {
            setActionLoading(false);
        }
    };

    return (
        <div className="container py-4">
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h2 className="fw-bold">{view === 'add' ? 'Add Products to Inventory' : 'My Inventory'}</h2>
                <div className="d-flex gap-2">
                    <button 
                        className={`btn rounded-pill px-4 ${view === 'list' ? 'btn-primary shadow' : 'btn-outline-primary'}`}
                        onClick={() => navigate('/inventory?view=list')}
                    >
                        My Inventory
                    </button>
                    <button 
                        className={`btn rounded-pill px-4 ${view === 'add' ? 'btn-primary shadow' : 'btn-outline-primary'}`}
                        onClick={() => navigate('/inventory?view=add')}
                    >
                        Add New Items
                    </button>
                </div>
            </div>

            {error && (
                <div className="alert alert-danger shadow-sm border-0 mb-4" role="alert">
                    <i className="bi bi-exclamation-octagon-fill me-2"></i>
                    {error}
                </div>
            )}

            {loading ? (
                <div className="text-center py-5">
                    <div className="spinner-border text-primary" role="status">
                        <span className="visually-hidden">Loading...</span>
                    </div>
                </div>
            ) : view === 'add' ? (
                <>
                    <div className="mb-4">
                        <form onSubmit={handleSearchSubmit} className="input-group shadow-sm rounded-pill overflow-hidden" style={{ maxWidth: '400px' }}>
                            <input 
                                type="text" 
                                className="form-control border-0 px-4" 
                                placeholder="Search products by name or brand..." 
                                value={searchTerm}
                                onChange={(e) => setSearchTerm(e.target.value)}
                            />
                            <button className="btn btn-primary px-4 fw-semibold border-0" type="submit">
                                Search
                            </button>
                        </form>
                    </div>

                    <div className="table-responsive bg-white rounded shadow-sm">
                    <table className="table table-hover align-middle mb-0">
                        <thead className="table-light text-muted small text-uppercase">
                            <tr>
                                <th className="ps-4">Product</th>
                                <th>Brand</th>
                                <th>Category</th>
                                <th className="text-end pe-4">Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            {products.map(product => (
                                <tr key={product.id}>
                                    <td className="ps-4 py-3">
                                        <div className="d-flex align-items-center">
                                            <div className="flex-shrink-0" style={{ width: '40px', height: '40px' }}>
                                                <img src={product.imageUrl || 'https://via.placeholder.com/40'} className="img-fluid rounded" alt="" />
                                            </div>
                                            <div className="ms-3">
                                                <div className="fw-bold text-dark">{product.name}</div>
                                                <div className="small text-muted">ID: {product.id}</div>
                                            </div>
                                        </div>
                                    </td>
                                    <td>{product.brand}</td>
                                    <td><span className="badge bg-light text-dark border">{product.category}</span></td>
                                    <td className="text-end pe-4">
                                        <button 
                                            className="btn btn-sm btn-outline-success rounded-pill px-3"
                                            onClick={() => handleOpenAddModal(product)}
                                        >
                                            Add to Inventory
                                        </button>
                                    </td>
                                </tr>
                            ))}
                            {products.length === 0 && (
                                <tr>
                                    <td colSpan="4" className="text-center py-5 text-muted">No products found to add.</td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>

                {/* Pagination Controls */}
                {!loading && allProducts.length > 0 && (
                    <div className="d-flex justify-content-between align-items-center mt-4 p-3 bg-white rounded shadow-sm">
                        <div className="d-flex align-items-center gap-3">
                            <span className="text-muted small fw-semibold">Show:</span>
                            <select 
                                className="form-select form-select-sm w-auto border-0 bg-light fw-bold" 
                                value={pageSize} 
                                onChange={(e) => { setPageSize(Number(e.target.value)); setPage(1); }}
                            >
                                <option value="10">10</option>
                                <option value="25">25</option>
                                <option value="50">50</option>
                            </select>
                            <span className="text-muted small">Total: {allProducts.length} items</span>
                        </div>
                        <div className="d-flex align-items-center gap-4">
                            <button 
                                className="btn btn-outline-secondary btn-sm px-3 rounded-pill" 
                                onClick={() => setPage(p => Math.max(1, p - 1))}
                                disabled={page === 1}
                            >
                                Previous
                            </button>
                            <span className="fw-bold small">Page {page} of {totalPages || 1}</span>
                            <button 
                                className="btn btn-outline-secondary btn-sm px-3 rounded-pill" 
                                onClick={() => setPage(p => p + 1)}
                                disabled={page >= totalPages}
                            >
                                Next
                            </button>
                        </div>
                    </div>
                )}
                </>
            ) : (
                <>
                <div className="table-responsive bg-white rounded shadow-sm">
                    <table className="table table-hover align-middle mb-0">
                        <thead className="table-light text-muted small text-uppercase">
                            <tr>
                                <th className="ps-4">Product Name</th>
                                <th>Product ID</th>
                                <th>Price</th>
                                <th>Discount</th>
                                <th>Stock</th>
                                <th className="text-end pe-4">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {inventory.map(item => (
                                <tr key={item.id}>
                                    <td className="ps-4 py-3 fw-bold text-dark">
                                        {allProducts.find(p => p.id === item.productId)?.name || 'Loading name...'}
                                    </td>
                                    <td className="py-3 text-muted small">{item.productId}</td>
                                    <td className="fw-bold text-dark">₹{Number(item.price || 0).toFixed(2)}</td>
                                    <td>
                                        <span className="badge bg-danger-subtle text-danger border border-danger-subtle rounded-pill px-2">
                                            {item.discount}% OFF
                                        </span>
                                    </td>
                                    <td>
                                        <span className={`fw-semibold ${item.quantity > 5 ? 'text-success' : 'text-warning'}`}>
                                            {item.quantity} available
                                        </span>
                                    </td>
                                    <td className="text-end pe-4">
                                        <div className="d-flex justify-content-end gap-2">
                                            <button 
                                                className="btn btn-sm btn-outline-primary"
                                                onClick={() => handleOpenEditModal(item)}
                                            >
                                                Edit
                                            </button>
                                            <button 
                                                className="btn btn-sm btn-outline-danger"
                                                onClick={() => handleDelete(item.id)}
                                            >
                                                Delete
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            ))}
                            {inventory.length === 0 && (
                                <tr>
                                    <td colSpan="6" className="text-center py-5 text-muted">
                                        Your inventory is empty. 
                                        <button className="btn btn-link p-0 ms-1 align-baseline" onClick={() => navigate('/inventory?view=add')}>Add items</button>
                                    </td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>

                {/* Pagination Controls */}
                {!loading && allInventory.length > 0 && (
                    <div className="d-flex justify-content-between align-items-center mt-4 p-3 bg-white rounded shadow-sm">
                        <div className="d-flex align-items-center gap-3">
                            <span className="text-muted small fw-semibold">Show:</span>
                            <select 
                                className="form-select form-select-sm w-auto border-0 bg-light fw-bold" 
                                value={pageSize} 
                                onChange={(e) => { setPageSize(Number(e.target.value)); setPage(1); }}
                            >
                                <option value="10">10</option>
                                <option value="25">25</option>
                                <option value="50">50</option>
                            </select>
                            <span className="text-muted small">Total: {allInventory.length} items</span>
                        </div>
                        <div className="d-flex align-items-center gap-4">
                            <button 
                                className="btn btn-outline-secondary btn-sm px-3 rounded-pill" 
                                onClick={() => setPage(p => Math.max(1, p - 1))}
                                disabled={page === 1}
                            >
                                Previous
                            </button>
                            <span className="fw-bold small">Page {page} of {totalPages || 1}</span>
                            <button 
                                className="btn btn-outline-secondary btn-sm px-3 rounded-pill" 
                                onClick={() => setPage(p => p + 1)}
                                disabled={page >= totalPages}
                            >
                                Next
                            </button>
                        </div>
                    </div>
                )}
                </>
            )}

            {/* Add/Edit Modal */}
            {showModal && (
                <>
                    <div className="modal show d-block" tabIndex="-1" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
                        <div className="modal-dialog modal-dialog-centered">
                            <div className="modal-content border-0 shadow-lg">
                                <div className="modal-header">
                                    <h5 className="modal-title fw-bold">
                                        {modalType === 'add' ? 'Add to Inventory' : 'Edit Inventory Item'}
                                    </h5>
                                    <button type="button" className="btn-close" onClick={handleCloseModal}></button>
                                </div>
                                <form onSubmit={handleSubmit}>
                                    <div className="modal-body p-4">
                                        <div className="mb-3">
                                            <label className="form-label text-muted small fw-bold">PRODUCT ID</label>
                                            <input type="text" className="form-control bg-light" value={formData.productId} readOnly />
                                        </div>
                                        <div className="row g-3">
                                            <div className="col-md-6">
                                                <label className="form-label text-muted small fw-bold">PRICE (₹)</label>
                                                <input 
                                                    type="number" 
                                                    name="price"
                                                    className="form-control" 
                                                    placeholder="0.00" 
                                                    step="0.01" 
                                                    min="1"
                                                    required
                                                    value={formData.price}
                                                    onChange={handleInputChange}
                                                />
                                            </div>
                                            <div className="col-md-6">
                                                <label className="form-label text-muted small fw-bold">DISCOUNT (%)</label>
                                                <input 
                                                    type="number" 
                                                    name="discount"
                                                    className="form-control" 
                                                    placeholder="0" 
                                                    min="0"
                                                    max="100"
                                                    required
                                                    value={formData.discount}
                                                    onChange={handleInputChange}
                                                />
                                            </div>
                                        </div>
                                        <div className="mt-3">
                                            <label className="form-label text-muted small fw-bold">QUANTITY</label>
                                            <input 
                                                type="number" 
                                                name="quantity"
                                                className="form-control" 
                                                placeholder="Enter stock quantity" 
                                                min="1"
                                                required
                                                value={formData.quantity}
                                                onChange={handleInputChange}
                                            />
                                        </div>
                                    </div>
                                    <div className="modal-footer bg-light">
                                        <button type="button" className="btn btn-secondary" onClick={handleCloseModal}>Cancel</button>
                                        <button type="submit" className="btn btn-primary px-4" disabled={actionLoading}>
                                            {actionLoading ? (
                                                <span className="spinner-border spinner-border-sm me-2"></span>
                                            ) : null}
                                            {modalType === 'add' ? 'Add Item' : 'Update Item'}
                                        </button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </div>
                </>
            )}
        </div>
    );
};

export default InventoryManagement;
