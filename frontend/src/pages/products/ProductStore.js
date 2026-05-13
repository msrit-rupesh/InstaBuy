import React, { useState, useEffect } from 'react';
import api from '../../services/api';
import { useCart } from '../../context/CartContext';

const ProductStore = () => {
    const { addToCart } = useCart();
    const [allProducts, setAllProducts] = useState([]); // Store all fetched products for client-side paging
    const [products, setProducts] = useState([]); // Currently displayed products (sliced)
    const [loading, setLoading] = useState(false);

    // Pagination state
    const [page, setPage] = useState(1);
    const [pageSize, setPageSize] = useState(50);
    const [totalPages, setTotalPages] = useState(0);

    // Modal state
    const [selectedProduct, setSelectedProduct] = useState(null);
    const [inventoryDetails, setInventoryDetails] = useState(null);
    const [inventoryLoading, setInventoryLoading] = useState(false);
    const [inventoryError, setInventoryError] = useState(null);

    // Search state
    const [searchTerm, setSearchTerm] = useState('');

    const fetchProducts = async () => {
        setLoading(true);
        const term = searchTerm.trim();
        try {
            let response;
            if (term !== '') {
                // Search API: GET /api/products/search?query=value
                response = await api.get('/api/products/search', {
                    params: { query: term }
                });
            } else {
                // List API: GET /api/products
                response = await api.get('/api/products');
            }

            const data = response.data;
            const fetchedProducts = Array.isArray(data)
                ? data
                : (data.content || data.products || []);

            setAllProducts(fetchedProducts);
            setTotalPages(Math.ceil(fetchedProducts.length / pageSize));
            setPage(1); // Reset to first page on search or load

        } catch (error) {
            console.error("Failed to fetch products", error);
            setAllProducts([]);
            setTotalPages(0);
        } finally {
            setLoading(false);
        }
    };

    // Client-side pagination effect: slice allProducts whenever page/pageSize changes
    useEffect(() => {
        const startIndex = (page - 1) * pageSize;
        const endIndex = startIndex + pageSize;
        setProducts(allProducts.slice(startIndex, endIndex));
        setTotalPages(Math.ceil(allProducts.length / pageSize));
    }, [allProducts, page, pageSize]);

    // Initial load
    useEffect(() => {
        fetchProducts();
        // eslint-disable-next-line
    }, []);

    // Handle page size change
    const handlePageSizeChange = (e) => {
        const newSize = Number(e.target.value);
        setPageSize(newSize);
        setPage(1);
    };

    // If the user clears the search term manually, we can reload.
    useEffect(() => {
        if (searchTerm.trim() === '') {
            fetchProducts();
        }
        // eslint-disable-next-line
    }, [searchTerm]);

    // Fetch inventory details when a product is selected
    useEffect(() => {
        const fetchInventory = async () => {
            if (!selectedProduct) {
                setInventoryDetails(null);
                setInventoryError(null);
                return;
            }

            setInventoryLoading(true);
            setInventoryError(null);
            try {
                // GET /api/inventory/product/{productId}
                const response = await api.get(`/api/inventory/product/${selectedProduct.id}`);
                const data = response.data;

                if (Array.isArray(data)) {
                    // Filter by availability and select the lowest calculated price
                    const availableItems = data.filter(item => item.availability);
                    
                    if (availableItems.length > 0) {
                        // Find item with lowest (price - discount_amount)
                        const bestDeal = availableItems.reduce((prev, curr) => {
                            const getFinalPrice = (item) => {
                                const p = Number(item.price || 0);
                                const d = Number(item.discount || 0);
                                return p - (d * p / 100);
                            };
                            return getFinalPrice(curr) < getFinalPrice(prev) ? curr : prev;
                        });
                        setInventoryDetails(bestDeal);
                    } else {
                        setInventoryDetails(null);
                        setInventoryError("stock is unavailable");
                    }
                } else if (data && typeof data === 'object') {
                    // Fallback for single object response
                    setInventoryDetails(data);
                } else {
                    setInventoryDetails(null);
                }
            } catch (error) {
                console.error("Failed to fetch inventory details", error);
                if (error.response && error.response.status === 400) {
                    setInventoryError("stock is unavailable");
                } else {
                    setInventoryError("Failed to load inventory details");
                }
            } finally {
                setInventoryLoading(false);
            }
        };

        fetchInventory();
    }, [selectedProduct]);

    const handleAddToCart = () => {
        if (!selectedProduct) return;
        addToCart(selectedProduct);
        alert(`${selectedProduct.name} added to cart!`);
        setSelectedProduct(null);
    };

    const handleSearchSubmit = (e) => {
        e.preventDefault();
        fetchProducts();
    };

    const handlePrevious = () => {
        if (page > 1) setPage(page - 1);
    };

    const handleNext = () => {
        if (page < totalPages) setPage(page + 1);
    };

    return (
        <div className="container-fluid py-4 bg-light min-vh-100">
            <div className="row mb-4 align-items-center">
                <div className="col-md-6">
                    <h2 className="fw-bold mb-0">Discover Products</h2>
                </div>
                <div className="col-md-6 d-flex justify-content-md-end gap-3 mt-3 mt-md-0">
                    <form onSubmit={handleSearchSubmit} className="input-group w-100 w-md-25 shadow-sm rounded-pill overflow-hidden">
                        <input
                            type="text"
                            className="form-control border-0 px-4"
                            placeholder="Search products..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            style={{ boxShadow: 'none' }}
                        />
                        <button className="btn btn-primary px-4 fw-semibold border-0" type="submit">
                            Search
                        </button>
                    </form>
                </div>
            </div>

            {loading && products.length === 0 ? (
                <div className="text-center py-5">
                    <div className="spinner-border text-primary" role="status">
                        <span className="visually-hidden">Loading...</span>
                    </div>
                </div>
            ) : (
                <>
                    <div className="row row-cols-1 row-cols-sm-2 row-cols-md-3 row-cols-lg-4 row-cols-xl-5 g-4">
                        {products.map(product => (
                            <div className="col" key={product.id}>
                                <div className="card h-100 shadow-sm border product-card bg-white" style={{ transition: 'box-shadow 0.2s', cursor: 'pointer', borderRadius: '8px' }} onClick={() => setSelectedProduct(product)}>
                                    <div className="p-3 text-center d-flex align-items-center justify-content-center" style={{ height: '220px', borderBottom: '1px solid #eee' }}>
                                        <img
                                            src={product.imageUrl || 'https://via.placeholder.com/300x300?text=No+Image'}
                                            className="img-fluid"
                                            style={{ maxHeight: '100%', maxWidth: '100%', objectFit: 'contain' }}
                                            alt={product.name}
                                            onError={(e) => { e.target.src = 'https://via.placeholder.com/300x300?text=No+Image' }}
                                        />
                                    </div>
                                    <div className="card-body d-flex flex-column p-3">
                                        <h6 className="card-title fw-bold text-dark text-truncate mb-1" title={product.name} style={{ fontSize: '15px' }}>{product.name}</h6>
                                        <div className="text-secondary small mb-1">{product.brand}</div>
                                        <div className="mt-auto pt-2">
                                            <button
                                                className="btn w-100 fw-semibold text-dark rounded-pill py-1"
                                                onClick={(e) => { e.stopPropagation(); setSelectedProduct(product); }}
                                                style={{ fontSize: '13px', backgroundColor: '#ffd814', border: '1px solid #fcd200', boxShadow: '0 2px 5px rgba(213,217,217,.5)' }}
                                            >
                                                View Details
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>

                    {products.length === 0 && !loading && (
                        <div className="text-center py-5 text-muted">
                            <h4>No products found</h4>
                        </div>
                    )}

                    {/* Pagination Controls */}
                    <div className="d-flex flex-column flex-md-row justify-content-between align-items-center mt-5 gap-3 bg-white p-3 rounded shadow-sm">
                        <div className="d-flex align-items-center gap-2">
                            <label className="text-muted small fw-semibold">Items per page:</label>
                            <select
                                className="form-select form-select-sm w-auto border-0 bg-light fw-bold"
                                value={pageSize}
                                onChange={handlePageSizeChange}
                                style={{ cursor: 'pointer' }}
                            >
                                <option value="25">25</option>
                                <option value="50">50</option>
                                <option value="75">75</option>
                            </select>
                        </div>

                        <div className="d-flex align-items-center gap-3">
                            <button
                                className="btn btn-outline-secondary px-4 fw-semibold border-0 bg-light shadow-sm"
                                onClick={handlePrevious}
                                disabled={page === 1 || loading}
                            >
                                Previous
                            </button>
                            <div className="bg-primary text-white px-3 py-1 rounded-pill fw-bold shadow-sm">
                                {page} of {totalPages || 1}
                            </div>
                            <button
                                className="btn btn-outline-secondary px-4 fw-semibold border-0 bg-light shadow-sm"
                                onClick={handleNext}
                                disabled={page >= totalPages || loading}
                            >
                                Next
                            </button>
                        </div>

                        <div className="text-muted small fw-semibold">
                            Total {allProducts.length} products
                        </div>
                    </div>
                </>
            )}

            {/* Product Detail Modal */}
            {selectedProduct && (
                <div className="modal show d-block" tabIndex="-1" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
                    <div className="modal-dialog modal-dialog-centered modal-lg">
                        <div className="modal-content border-0 shadow">
                            <div className="modal-header border-bottom-0 pb-0 position-relative z-index-1" style={{ zIndex: 10 }}>
                                <button type="button" className="btn-close" onClick={() => setSelectedProduct(null)} style={{ position: 'absolute', top: '15px', right: '15px', zIndex: 20 }}></button>
                            </div>
                            <div className="modal-body p-4 pt-0">
                                <div className="row g-4">
                                    <div className="col-md-5 text-center">
                                        <img
                                            src={selectedProduct.imageUrl || 'https://via.placeholder.com/400x400?text=No+Image'}
                                            className="img-fluid rounded border"
                                            alt={selectedProduct.name}
                                        />
                                    </div>
                                    <div className="col-md-7 d-flex flex-column">
                                        <h3 className="fw-bold mb-1">{selectedProduct.name}</h3>
                                        <div className="mb-3 text-muted">By {selectedProduct.brand} | Category: {selectedProduct.category}</div>

                                        {inventoryLoading ? (
                                            <div className="d-flex align-items-center gap-2 mb-3 text-primary">
                                                <div className="spinner-border spinner-border-sm" role="status"></div>
                                                <span>Fetching latest price...</span>
                                            </div>
                                        ) : inventoryError ? (
                                            <div className="alert alert-danger py-2 px-3 mb-3 border-0 shadow-sm" style={{ backgroundColor: '#fff8f8', color: '#c30000', fontSize: '14px' }}>
                                                ⚠️ {inventoryError}
                                            </div>
                                        ) : inventoryDetails ? (
                                            <div className="mb-4 bg-light p-3 rounded-3 border border-opacity-10 border-dark">
                                                <div className="d-flex align-items-baseline gap-2 mb-2">
                                                    <span className="text-muted text-decoration-line-through small">₹{Number(inventoryDetails.price || 0).toFixed(2)}</span>
                                                    <span className="badge bg-danger rounded-pill">-{Number(inventoryDetails.discount || 0)}% OFF</span>
                                                </div>
                                                <div className="d-flex align-items-center gap-2">
                                                    <h2 className="fw-bold mb-0 text-success">
                                                        ₹{(Number(inventoryDetails.price || 0) - (Number(inventoryDetails.discount || 0) * Number(inventoryDetails.price || 0) / 100)).toFixed(2)}
                                                    </h2>
                                                    <span className="text-secondary small">Inclusive of all taxes</span>
                                                </div>
                                                <div className="mt-2 small">
                                                    {inventoryDetails.availability ? (
                                                        <span className="text-success fw-semibold">✅ In Stock</span>
                                                    ) : (
                                                        <span className="text-danger fw-semibold">❌ Currently Unavailable</span>
                                                    )}
                                                </div>
                                            </div>
                                        ) : null}

                                        <p className="mb-4" style={{ whiteSpace: 'pre-wrap' }}>
                                            {selectedProduct.description || 'No description available for this product.'}
                                        </p>
                                        <div className="mt-auto">
                                            <button
                                                className="btn btn-warning rounded-pill px-4 py-2 fw-semibold w-100"
                                                disabled={inventoryLoading || !!inventoryError || (inventoryDetails && !inventoryDetails.availability)}
                                                onClick={handleAddToCart}
                                            >
                                                Add to Cart
                                            </button>
                                            <div className="text-center mt-2 small text-muted">
                                                Secure transaction
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            )}

        </div>
    );
};

export default ProductStore;
