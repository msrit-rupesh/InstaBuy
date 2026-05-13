import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../../services/api';
import { useAuth } from '../../context/AuthContext';

const ProductManagement = () => {
    const { user } = useAuth();
    const navigate = useNavigate();
    const [allProducts, setAllProducts] = useState([]); // Raw product list
    const [products, setProducts] = useState([]); // Displayed slice
    const [loading, setLoading] = useState(false);
    const [searchTerm, setSearchTerm] = useState('');
    
    // Pagination state
    const [page, setPage] = useState(1);
    const [pageSize, setPageSize] = useState(50);
    const [totalPages, setTotalPages] = useState(0);

    // Modal states
    const [showForm, setShowForm] = useState(false);
    const [editingProduct, setEditingProduct] = useState(null);
    const [formData, setFormData] = useState({
        name: '',
        brand: '',
        category: '',
        imageUrl: '',
        description: ''
    });

    // Alert / Success States
    const [alert, setAlert] = useState({ show: false, message: '', type: 'success' });

    // Assuming we do simple pagination/limit for management as well, or just load first 100
    // for simplicity, let's load all or first page
    const fetchProducts = async () => {
        setLoading(true);
        const term = searchTerm.trim();
        try {
            let response;
            if (term !== '') {
                 response = await api.get('/api/products/search', {
                    params: { query: term }
                 });
            } else {
                 response = await api.get('/api/products');
            }
            const data = response.data;
            const fetched = Array.isArray(data) ? data : (data.content || data.products || []);
            setAllProducts(fetched);
            setPage(1);
        } catch (error) {
            console.error("Fetch products failed", error);
            showAlert("Failed to load products", "danger");
            setAllProducts([]);
        } finally {
            setLoading(false);
        }
    };

    // Client-side pagination logic
    useEffect(() => {
        const startIndex = (page - 1) * pageSize;
        const endIndex = startIndex + pageSize;
        setProducts(allProducts.slice(startIndex, endIndex));
        setTotalPages(Math.ceil(allProducts.length / pageSize));
    }, [allProducts, page, pageSize]);

    useEffect(() => {
        const t = setTimeout(() => {
            fetchProducts();
        }, 500);
        return () => clearTimeout(t);
        // eslint-disable-next-line
    }, [searchTerm]);

    // Initial load handled by searchTerm useEffect if term is ''
    
    const handlePageSizeChange = (e) => {
        setPageSize(Number(e.target.value));
        setPage(1);
    };

    const showAlert = (message, type = 'success') => {
        setAlert({ show: true, message, type });
        setTimeout(() => setAlert({ show: false, message: '', type: 'success' }), 3000);
    };

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const openForm = async (product = null) => {
        if (!product && (user?.role === 'VENDOR' || user?.roleId === 2)) {
            try {
                const vendorRes = await api.get('/api/profile/vendor');
                const v = vendorRes.data;
                if (!v || !v.companyName || !v.phone || !v.streetAddress || !v.city || !v.state || !v.country || !v.postalCode) {
                    showAlert("Please complete your Vendor Profile before adding a product.", "danger");
                    setTimeout(() => navigate('/profile'), 3000);
                    return;
                }
            } catch (err) {
                showAlert("Please complete your Vendor Profile before adding a product.", "danger");
                setTimeout(() => navigate('/profile'), 3000);
                return;
            }
        }

        if (product) {
            setEditingProduct(product);
            setFormData({
                name: product.name || '',
                brand: product.brand || '',
                category: product.category || '',
                imageUrl: product.imageUrl || '',
                description: product.description || ''
            });
        } else {
            setEditingProduct(null);
            setFormData({
                name: '', brand: '', category: '', imageUrl: '', description: ''
            });
        }
        setShowForm(true);
    };

    const closeForm = () => {
        setShowForm(false);
        setEditingProduct(null);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            if (editingProduct) {
                // UPDATE
                await api.patch(`/api/products/${editingProduct.id}`, formData);
                showAlert("Product updated successfully", "success");
            } else {
                // CREATE
                await api.post('/api/products', formData);
                showAlert("Product created successfully", "success");
            }
            fetchProducts();
            closeForm();
        } catch (error) {
            console.error("Save product failed", error);
            showAlert("Failed to save product", "danger");
        }
    };

    const handleDelete = async (id) => {
        if (window.confirm("Are you sure you want to delete this product?")) {
            try {
                await api.delete(`/api/products/${id}`);
                showAlert("Product deleted successfully", "success");
                fetchProducts();
            } catch (error) {
                console.error("Delete product failed", error);
                showAlert("Failed to delete product", "danger");
            }
        }
    };

    return (
        <div className="container-fluid py-4 bg-light min-vh-100">
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h2 className="fw-bold m-0">Product Management</h2>
                {(user?.role === 'ADMIN' || user?.role === 'VENDOR') && (
                    <button className="btn btn-primary" onClick={() => openForm()}>
                        + Add New Product
                    </button>
                )}
            </div>

            {alert.show && (
                <div className={`alert alert-${alert.type} alert-dismissible fade show`} role="alert">
                    {alert.message}
                    <button type="button" className="btn-close" onClick={() => setAlert({ ...alert, show: false })}></button>
                </div>
            )}

            <div className="card shadow-sm border-0 mb-4">
                <div className="card-header bg-white border-bottom-0 pt-3 pb-0 d-flex justify-content-between align-items-center">
                    <input 
                        type="text" 
                        className="form-control w-25 shadow-sm" 
                        placeholder="Search by name or term..." 
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                    <div className="d-flex align-items-center gap-2">
                        <label className="text-muted small fw-semibold">Show:</label>
                        <select 
                            className="form-select form-select-sm w-auto border-0 bg-light fw-bold" 
                            value={pageSize} 
                            onChange={handlePageSizeChange}
                        >
                            <option value="25">25</option>
                            <option value="50">50</option>
                            <option value="75">75</option>
                        </select>
                    </div>
                </div>
                <div className="card-body">
                    {loading ? (
                        <div className="text-center py-4">
                            <div className="spinner-border text-primary" role="status">
                                <span className="visually-hidden">Loading...</span>
                            </div>
                        </div>
                    ) : (
                        <div className="table-responsive">
                            <table className="table table-hover align-middle">
                                <thead className="table-light">
                                    <tr>
                                        <th>ID</th>
                                        <th>Image</th>
                                        <th>Name</th>
                                        <th>Brand</th>
                                        <th>Category</th>
                                        {user?.role === 'ADMIN' && <th className="text-end">Actions</th>}
                                    </tr>
                                </thead>
                                <tbody>
                                    {products.length > 0 ? (
                                        products.map(p => (
                                            <tr key={p.id}>
                                                <td>{p.id}</td>
                                                <td>
                                                    <img 
                                                        src={p.imageUrl || 'https://via.placeholder.com/50'} 
                                                        alt={p.name} 
                                                        style={{ width: '50px', height: '50px', objectFit: 'contain' }}
                                                        onError={(e) => { e.target.src = 'https://via.placeholder.com/50' }}
                                                    />
                                                </td>
                                                <td className="fw-semibold">{p.name}</td>
                                                <td>{p.brand}</td>
                                                <td><span className="badge bg-secondary">{p.category}</span></td>
                                                {user?.role === 'ADMIN' && (
                                                    <td className="text-end">
                                                        <div className="d-flex justify-content-end gap-2">
                                                            <button className="btn btn-sm btn-outline-primary" onClick={() => openForm(p)}>
                                                                Edit
                                                            </button>
                                                            <button className="btn btn-sm btn-outline-danger" onClick={() => handleDelete(p.id)}>
                                                                Delete
                                                            </button>
                                                        </div>
                                                    </td>
                                                )}
                                            </tr>
                                        ))
                                    ) : (
                                        <tr>
                                            <td colSpan="6" className="text-center py-4 text-muted">
                                                No products found.
                                            </td>
                                        </tr>
                                    )}
                                </tbody>
                            </table>
                        </div>
                    )}
                    
                    {/* Pagination Controls */}
                    {!loading && (
                        <div className="d-flex justify-content-between align-items-center mt-3 p-2 bg-light rounded">
                            <span className="text-muted small">Total: {allProducts.length} items</span>
                            <div className="d-flex align-items-center gap-3">
                                <button 
                                    className="btn btn-outline-secondary btn-sm px-3" 
                                    onClick={() => setPage(p => Math.max(1, p - 1))}
                                    disabled={page === 1}
                                >
                                    Previous
                                </button>
                                <span className="fw-bold small">Page {page} of {totalPages || 1}</span>
                                <button 
                                    className="btn btn-outline-secondary btn-sm px-3" 
                                    onClick={() => setPage(p => p + 1)}
                                    disabled={page >= totalPages}
                                >
                                    Next
                                </button>
                            </div>
                        </div>
                    )}
                </div>
            </div>

            {/* Create / Edit Modal Form */}
            {showForm && (
                <div className="modal show d-block" tabIndex="-1" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
                    <div className="modal-dialog modal-dialog-centered">
                        <form className="modal-content shadow" onSubmit={handleSubmit}>
                            <div className="modal-header">
                                <h5 className="modal-title">{editingProduct ? 'Edit Product' : 'Add New Product'}</h5>
                                <button type="button" className="btn-close" onClick={closeForm}></button>
                            </div>
                            <div className="modal-body">
                                <div className="mb-3">
                                    <label className="form-label">Product Name *</label>
                                    <input type="text" className="form-control" name="name" required value={formData.name} onChange={handleInputChange} />
                                </div>
                                <div className="row mb-3">
                                    <div className="col">
                                        <label className="form-label">Brand *</label>
                                        <input type="text" className="form-control" name="brand" required value={formData.brand} onChange={handleInputChange} />
                                    </div>
                                    <div className="col">
                                        <label className="form-label">Category *</label>
                                        <input type="text" className="form-control" name="category" required value={formData.category} onChange={handleInputChange} />
                                    </div>
                                </div>
                                <div className="mb-3">
                                    <label className="form-label">Image URL</label>
                                    <input type="url" className="form-control" name="imageUrl" value={formData.imageUrl} onChange={handleInputChange} placeholder="https://example.com/image.jpg" />
                                </div>
                                <div className="mb-3">
                                    <label className="form-label">Description</label>
                                    <textarea className="form-control" name="description" rows="3" value={formData.description} onChange={handleInputChange}></textarea>
                                </div>
                            </div>
                            <div className="modal-footer">
                                <button type="button" className="btn btn-secondary" onClick={closeForm}>Cancel</button>
                                <button type="submit" className="btn btn-primary">{editingProduct ? 'Save Changes' : 'Create Product'}</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default ProductManagement;
