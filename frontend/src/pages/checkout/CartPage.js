import React, { useEffect, useState } from 'react';
import { useCart } from '../../context/CartContext';
import { useAuth } from '../../context/AuthContext';
import api from '../../services/api';
import { Link } from 'react-router-dom';

const CartPage = () => {
    const { user } = useAuth();
    const { cartItems, removeFromCart, updateQuantity, cartCount } = useCart();
    const [cartWithPrices, setCartWithPrices] = useState([]);
    const [loading, setLoading] = useState(true);

    // Address selection state
    const [addresses, setAddresses] = useState([]);
    const [deliveryAddressId, setDeliveryAddressId] = useState(null);
    const [billingAddressId, setBillingAddressId] = useState(null);

    useEffect(() => {
        const loadCartData = async () => {
            if (cartItems.length === 0) {
                setCartWithPrices([]);
                setLoading(false);
                return;
            }

            setLoading(true);

            // Fetch addresses alongside cart items
            try {
                const profileResponse = await api.get('/api/profile');
                if (profileResponse.data && Array.isArray(profileResponse.data.addresses)) {
                    const addrs = profileResponse.data.addresses;
                    setAddresses(addrs);
                    if (addrs.length > 0) {
                        const dId = profileResponse.data.deliveryAddressId;
                        const validD = dId && addrs.some(a => String(a.id || a.addressId) === String(dId));
                        setDeliveryAddressId(validD ? dId : (addrs[0].id || addrs[0].addressId));

                        const bId = profileResponse.data.billingAddressId;
                        const validB = bId && addrs.some(a => String(a.id || a.addressId) === String(bId));
                        setBillingAddressId(validB ? bId : (addrs[0].id || addrs[0].addressId));
                    }
                }
            } catch (err) {
                console.error("Failed to fetch user profile for addresses", err);
            }

            const updatedItems = await Promise.all(cartItems.map(async (item) => {
                try {
                    const response = await api.get(`/api/inventory/product/${item.id}`);
                    const data = response.data;
                    let bestPrice = 0;
                    let available = false;
                    let bestStockId = null;

                    if (Array.isArray(data)) {
                        const availableItems = data.filter(i => i.availability);
                        if (availableItems.length > 0) {
                            const bestDeal = availableItems.reduce((prev, curr) => {
                                const getFinalPrice = (i) => {
                                    const p = Number(i.price || 0);
                                    const d = Number(i.discount || 0);
                                    return p - (d * p / 100);
                                };
                                return getFinalPrice(curr) < getFinalPrice(prev) ? curr : prev;
                            });
                            const p = Number(bestDeal.price || 0);
                            const d = Number(bestDeal.discount || 0);
                            bestPrice = p - (d * p / 100);
                            bestStockId = bestDeal.id;
                            available = true;
                        }
                    }
                    return { ...item, currentPrice: bestPrice, available, stockId: bestStockId };
                } catch (error) {
                    console.error(`Failed to fetch price for ${item.name}`, error);
                    return { ...item, currentPrice: 0, available: false, stockId: null };
                }
            }));
            setCartWithPrices(updatedItems);
            setLoading(false);
        };

        if (cartItems.length > 0) {
            loadCartData();
        } else {
            setCartWithPrices([]);
            setLoading(false);
        }
    }, [cartItems]);

    const calculateTotal = () => {
        return cartWithPrices.reduce((total, item) => total + (item.currentPrice * item.quantity), 0);
    };

    const [checkoutLoading, setCheckoutLoading] = useState(false);

    const handleCheckout = async () => {
        try {
            setCheckoutLoading(true);

            if (!deliveryAddressId || !billingAddressId) {
                alert("Please select both a delivery and billing address to proceed.");
                setCheckoutLoading(false);
                return;
            }

            // Step 1: Update Billing and Delivery Addresses
            await api.put(`/api/profile/delivery-address/${deliveryAddressId}`);
            await api.put(`/api/profile/billing-address/${billingAddressId}`);

            // Step 2: Create an Order (Assuming a standard POST /api/orders/create endpoint)
            const orderPayload = cartWithPrices.filter(i => i.available).map(item => ({
                stockId: item.stockId || item.id, // Ensure stockId is passed since backend requires it
                quantity: item.quantity
            }));

            const orderResponse = await api.post('/api/orders/create', orderPayload);
            const orderId = orderResponse.data.id || orderResponse.data.orderId;

            if (!orderId) {
                throw new Error("Failed to generate order ID");
            }

            // Step 2.5: Wait for Order status to be PAYMENT_INITIATED
            let status = 'PENDING';
            let elapsed = 0;
            const pollInterval = 3000;
            const maxWait = 120000; // 2 minutes

            while (elapsed < maxWait) {
                try {
                    const statusRes = await api.get(`/api/orders/${orderId}`);
                    status = statusRes.data.status || statusRes.data;

                    if (status === 'PAYMENT_INITIATED') {
                        break;
                    }
                    if (status === 'CANCELLED' || status === 'FAILED') {
                        throw new Error(`Order processing failed with status: ${status}`);
                    }
                } catch (err) {
                    console.error("Error polling order status:", err);
                    if (err.message && err.message.includes("failed with status")) throw err;
                    // otherwise ignore and retry
                }

                await new Promise(r => setTimeout(r, pollInterval));
                elapsed += pollInterval;
            }

            if (status !== 'PAYMENT_INITIATED') {
                throw new Error("Order processing timed out. Please try again later.");
            }

            // Step 3: Create Checkout Session
            const checkoutPayload = { "orderId": orderId };
            const paymentResponse = await api.post('/api/payment/create-checkout-session', checkoutPayload);

            // Step 4: Redirect to Stripe URL
            if (paymentResponse.data && paymentResponse.data.url) {
                // Store orderId in session storage in case of stripe redirecting to generic /payment-failure without ID param
                sessionStorage.setItem('pendingOrderId', orderId);
                window.location.href = paymentResponse.data.url;
            } else {
                throw new Error("Missing Stripe checkout URL in response");
            }

        } catch (error) {
            console.error("Checkout process failed:", error);
            alert("Checkout failed: " + ((error.response && error.response.data) ? JSON.stringify(error.response.data) : error.message));
        } finally {
            setCheckoutLoading(false);
        }
    };

    if (loading && cartItems.length > 0) {
        return (
            <div className="container py-5 text-center">
                <div className="spinner-border text-primary" role="status">
                    <span className="visually-hidden">Loading your cart...</span>
                </div>
                <p className="mt-3 text-muted">Refreshing latest prices...</p>
            </div>
        );
    }

    if (cartItems.length === 0) {
        return (
            <div className="container py-5 text-center">
                <div className="mb-4">
                    <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" fill="#dee2e6" className="bi bi-cart-x" viewBox="0 0 16 16">
                        <path d="M7.354 5.646a.5.5 0 1 0-.708.708L7.793 7.5 6.646 8.646a.5.5 0 1 0 .708.708L8.5 8.207l1.146 1.147a.5.5 0 0 0 .708-.708L9.207 7.5l1.147-1.146a.5.5 0 0 0-.708-.708L8.5 6.793 7.354 5.646z" />
                        <path d="M.5 1H2a.5.5 0 0 1 .485.379L2.89 3H14.5a.5.5 0 0 1 .49.598l-1 5a.5.5 0 0 1-.465.401l-9.397.472L4.415 11H13a.5.5 0 0 1 0 1H4a.5.5 0 0 1-.491-.408L2.01 3.607 1.61 2H.5a.5.5 0 0 1-.5-.5zM3.102 4l.84 4.479 9.144-.459L13.89 4H3.102zM5 12a2 2 0 1 0 0 4 2 2 0 0 0 0-4zm7 0a2 2 0 1 0 0 4 2 2 0 0 0 0-4zm-7 1a1 1 0 1 1 0 2 1 1 0 0 1 0-2zm7 0a1 1 0 1 1 0 2 1 1 0 0 1 0-2z" />
                    </svg>
                </div>
                <h2 className="fw-bold">Your cart is empty</h2>
                <p className="text-muted mb-4">Looks like you haven't added anything to your cart yet.</p>
                <Link to="/products" className="btn btn-primary px-4 rounded-pill">Start Shopping</Link>
            </div>
        );
    }

    return (
        <div className="container py-4">
            <h2 className="fw-bold mb-4">Shopping Cart ({cartCount} {cartCount === 1 ? 'item' : 'items'})</h2>

            <div className="row g-4">
                <div className="col-lg-8">
                    <div className="card border-0 shadow-sm rounded-4 overflow-hidden">
                        <div className="table-responsive">
                            <table className="table table-hover align-middle mb-0">
                                <thead className="table-light">
                                    <tr>
                                        <th className="ps-4">Product</th>
                                        <th>Price</th>
                                        <th>Quantity</th>
                                        <th className="text-end pe-4">Subtotal</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {cartWithPrices.map((item) => (
                                        <tr key={item.id}>
                                            <td className="ps-4 py-3">
                                                <div className="d-flex align-items-center gap-3">
                                                    <img
                                                        src={item.imageUrl || 'https://via.placeholder.com/60'}
                                                        alt={item.name}
                                                        className="rounded border"
                                                        style={{ width: '60px', height: '60px', objectFit: 'contain' }}
                                                    />
                                                    <div>
                                                        <div className="fw-bold text-dark">{item.name}</div>
                                                        <div className="text-muted small">Brand: {item.brand}</div>
                                                        <button
                                                            className="btn btn-link btn-sm p-0 text-danger text-decoration-none mt-1"
                                                            onClick={() => removeFromCart(item.id)}
                                                        >
                                                            Remove
                                                        </button>
                                                    </div>
                                                </div>
                                            </td>
                                            <td>
                                                {item.available ? (
                                                    <div className="fw-bold text-dark">₹{item.currentPrice.toFixed(2)}</div>
                                                ) : (
                                                    <span className="badge bg-danger">Unavailable</span>
                                                )}
                                            </td>
                                            <td>
                                                <div className="d-flex align-items-center gap-2" style={{ width: '120px' }}>
                                                    <button
                                                        className="btn btn-outline-secondary btn-sm rounded-circle px-2"
                                                        onClick={() => updateQuantity(item.id, item.quantity - 1)}
                                                        disabled={item.quantity <= 1}
                                                    >-</button>
                                                    <span className="fw-bold px-1">{item.quantity}</span>
                                                    <button
                                                        className="btn btn-outline-secondary btn-sm rounded-circle px-2"
                                                        onClick={() => updateQuantity(item.id, item.quantity + 1)}
                                                    >+</button>
                                                </div>
                                            </td>
                                            <td className="text-end pe-4 fw-bold text-dark">
                                                ₹{(item.currentPrice * item.quantity).toFixed(2)}
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    </div>

                    {/* Address Selection Section (Amazon-style checkout progressively grouped) */}
                    <div className="card border-0 shadow-sm rounded-4 p-4 mt-4 mb-4 mb-lg-0">
                        <div className="d-flex justify-content-between align-items-center mb-4">
                            <h4 className="fw-bold mb-0">Delivery & Billing Information</h4>
                            {addresses.length > 0 && (
                                <Link to="/profile" className="btn btn-sm btn-outline-primary rounded-pill px-3">
                                    + Add New Address
                                </Link>
                            )}
                        </div>
                        {addresses.length === 0 ? (
                            <div className="alert alert-warning py-3 rounded-3 d-flex align-items-center">
                                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" fill="currentColor" className="bi bi-exclamation-triangle-fill me-3 flex-shrink-0" viewBox="0 0 16 16">
                                    <path d="M8.982 1.566a1.13 1.13 0 0 0-1.96 0L.165 13.233c-.457.778.091 1.767.98 1.767h13.713c.889 0 1.438-.99.98-1.767L8.982 1.566zM8 5c.535 0 .954.462.9.995l-.35 3.507a.552.552 0 0 1-1.1 0L7.1 5.995A.905.905 0 0 1 8 5zm.002 6a1 1 0 1 1 0 2 1 1 0 0 1 0-2z" />
                                </svg>
                                <div>
                                    <strong>No addresses found.</strong> Please <Link to="/profile" className="alert-link">add a saved address in your Profile</Link> to proceed with checkout.
                                </div>
                            </div>
                        ) : (
                            <div className="row g-4">
                                <div className="col-md-6 border-end-md">
                                    <label className="fw-bold mb-2">1. Delivery Address</label>
                                    <select
                                        className="form-select form-select-lg mb-3 shadow-none border-secondary"
                                        value={deliveryAddressId || ''}
                                        onChange={(e) => setDeliveryAddressId(e.target.value)}
                                    >
                                        <option value="" disabled>Select Delivery Address</option>
                                        {addresses.map(addr => (
                                            <option key={addr.id || addr.addressId} value={addr.id || addr.addressId}>
                                                {addr.fullName} ({addr.addressType})
                                            </option>
                                        ))}
                                    </select>
                                    {deliveryAddressId && (
                                        <div className="p-3 bg-light rounded-3 text-muted border">
                                            {(() => {
                                                const addr = addresses.find(a => Number(a.id || a.addressId) === Number(deliveryAddressId));
                                                return addr ? (
                                                    <>
                                                        <strong className="text-dark">{addr.fullName}</strong><span className="badge bg-secondary ms-2">{addr.addressType}</span><br />
                                                        {addr.streetAddress}<br />
                                                        {addr.city}, {addr.state} {addr.postalCode}<br />
                                                        {addr.country}<br />
                                                        <span className="mt-2 d-inline-block small text-dark"><i className="bi bi-telephone-fill me-1"></i> {addr.phone}</span>
                                                    </>
                                                ) : null;
                                            })()}
                                        </div>
                                    )}
                                </div>
                                <div className="col-md-6">
                                    <label className="fw-bold mb-2">2. Billing Address</label>
                                    <select
                                        className="form-select form-select-lg mb-3 shadow-none border-secondary"
                                        value={billingAddressId || ''}
                                        onChange={(e) => setBillingAddressId(e.target.value)}
                                    >
                                        <option value="" disabled>Select Billing Address</option>
                                        {addresses.map(addr => (
                                            <option key={addr.id || addr.addressId} value={addr.id || addr.addressId}>
                                                {addr.fullName} ({addr.addressType})
                                            </option>
                                        ))}
                                    </select>
                                    {billingAddressId && (
                                        <div className="p-3 bg-light rounded-3 text-muted border">
                                            {(() => {
                                                const addr = addresses.find(a => Number(a.id || a.addressId) === Number(billingAddressId));
                                                return addr ? (
                                                    <>
                                                        <strong className="text-dark">{addr.fullName}</strong><span className="badge bg-secondary ms-2">{addr.addressType}</span><br />
                                                        {addr.streetAddress}<br />
                                                        {addr.city}, {addr.state} {addr.postalCode}<br />
                                                        {addr.country}
                                                    </>
                                                ) : null;
                                            })()}
                                        </div>
                                    )}
                                </div>
                            </div>
                        )}
                    </div>
                </div>

                <div className="col-lg-4">
                    <div className="card border-0 shadow-sm rounded-4 p-4">
                        <h5 className="fw-bold mb-4">Order Summary</h5>
                        <div className="d-flex justify-content-between mb-2">
                            <span className="text-muted">Subtotal</span>
                            <span className="fw-semibold">₹{calculateTotal().toFixed(2)}</span>
                        </div>
                        <div className="d-flex justify-content-between mb-4">
                            <span className="text-muted">Shipping</span>
                            <span className="text-success fw-semibold">FREE</span>
                        </div>
                        <hr className="my-3" />
                        <div className="d-flex justify-content-between mb-4">
                            <h5 className="fw-bold">Total</h5>
                            <h5 className="fw-bold text-primary">₹{calculateTotal().toFixed(2)}</h5>
                        </div>
                        <button
                            className="btn btn-warning btn-lg w-100 rounded-pill fw-bold py-3 shadow-sm mb-3"
                            onClick={handleCheckout}
                            disabled={checkoutLoading || calculateTotal() === 0}
                        >
                            {checkoutLoading ? (
                                <span>
                                    <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                                    Preparing Checkout...
                                </span>
                            ) : (
                                "Proceed to Checkout"
                            )}
                        </button>
                        <p className="text-center text-muted small mb-0 px-2 mt-4 border-top pt-3">
                            By placing your order, you agree to our Terms of Service and adhere to our Privacy Policy. Prices and availability are standard but may vary based on seller and location.
                        </p>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default CartPage;
