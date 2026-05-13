import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import api from '../../services/api';

const ProfilePage = () => {
    const { user } = useAuth();
    const [activeTab, setActiveTab] = useState((user?.role === 'VENDOR' || user?.roleId === 2) ? 'vendor' : 'profile');
    
    // Profile State
    const [profile, setProfile] = useState({
        firstName: '',
        lastName: '',
        phone: ''
    });

    // Vendor Profile State
    const [vendorProfile, setVendorProfile] = useState({
        companyName: '',
        phone: '',
        streetAddress: '',
        city: '',
        state: '',
        country: '',
        postalCode: ''
    });

    // Orders State
    const [orders, setOrders] = useState([]);
    
    // Address State
    const [addresses, setAddresses] = useState([]);
    const [showAddressForm, setShowAddressForm] = useState(false);
    const [editingAddressId, setEditingAddressId] = useState(null);
    const emptyAddressForm = {
        fullName: '',
        phone: '',
        streetAddress: '',
        city: '',
        state: '',
        country: '',
        postalCode: '',
        addressType: 'HOME'
    };
    const [addressForm, setAddressForm] = useState(emptyAddressForm);

    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    useEffect(() => {
        fetchProfileData();
        fetchOrdersData();
    }, []);

    const fetchOrdersData = async () => {
        try {
            const response = await api.get('/api/orders/get-all-orders');
            if (response.data) {
                setOrders(Array.isArray(response.data) ? response.data : []);
            }
        } catch (err) {
            console.error('Error fetching orders:', err);
        }
    };

    const fetchProfileData = async () => {
        try {
            setLoading(true);

            if (user?.role === 'VENDOR' || user?.roleId === 2) {
                try {
                    const vendorRes = await api.get('/api/profile/vendor');
                    if (vendorRes.data) {
                        setVendorProfile({
                            companyName: vendorRes.data.companyName || '',
                            phone: vendorRes.data.phone || '',
                            streetAddress: vendorRes.data.streetAddress || '',
                            city: vendorRes.data.city || '',
                            state: vendorRes.data.state || '',
                            country: vendorRes.data.country || '',
                            postalCode: vendorRes.data.postalCode || ''
                        });
                    }
                } catch (vErr) {
                    console.log("Vendor profile not fetchable yet");
                }
            }

            if (user?.role !== 'VENDOR' && user?.roleId !== 2) {
                try {
                    const response = await api.get('/api/profile');
                    if (response.data) {
                        setProfile({
                            firstName: response.data.firstName || '',
                            lastName: response.data.lastName || '',
                            phone: response.data.phone || ''
                        });
                        
                        if (response.data.addresses) {
                            setAddresses(response.data.addresses);
                        }
                    }
                } catch (pErr) {
                    console.error('Error fetching standard profile/addresses:', pErr);
                }
            }
        } finally {
            setLoading(false);
        }
    };

    const handleProfileChange = (e) => {
        setProfile({ ...profile, [e.target.name]: e.target.value });
    };

    const handleVendorProfileChange = (e) => {
        setVendorProfile({ ...vendorProfile, [e.target.name]: e.target.value });
    };

    const handleAddressChange = (e) => {
        setAddressForm({ ...addressForm, [e.target.name]: e.target.value });
    };

    const saveProfile = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');

        // Phone Validation (Indian Mobile Number)
        const phoneRegex = /^[6-9]\d{9}$/;
        if (!phoneRegex.test(profile.phone)) {
            setError('Phone number must be a valid 10-digit Indian mobile number');
            return;
        }

        try {
            setLoading(true);
            await api.post('/api/profile', profile); // Assuming POST creates/updates profile
            setSuccess('Profile updated successfully!');
        } catch (err) {
            setError(err.response?.data || 'Failed to update profile');
        } finally {
            setLoading(false);
        }
    };

    const saveVendorProfile = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');

        const phoneRegex = /^[6-9]\d{9}$/;
        if (!phoneRegex.test(vendorProfile.phone)) {
            setError('Phone number must be a valid 10-digit Indian mobile number');
            return;
        }

        try {
            setLoading(true);
            await api.post('/api/profile/vendor', vendorProfile); 
            setSuccess('Vendor Profile updated successfully!');
        } catch (err) {
            setError(err.response?.data?.message || err.response?.data || 'Failed to update vendor profile');
        } finally {
            setLoading(false);
        }
    };

    const resetAddressForm = () => {
        setShowAddressForm(false);
        setEditingAddressId(null);
        setAddressForm(emptyAddressForm);
    };

    const handleEditAddress = (addr) => {
        setEditingAddressId(addr.id || addr.addressId);
        setAddressForm({
            fullName: addr.fullName,
            phone: addr.phone,
            streetAddress: addr.streetAddress,
            city: addr.city,
            state: addr.state,
            country: addr.country,
            postalCode: addr.postalCode,
            addressType: addr.addressType || 'HOME'
        });
        setShowAddressForm(true);
    };

    const handleDeleteAddress = async (id) => {
        if (!window.confirm("Are you sure you want to delete this address?")) return;
        
        try {
            setLoading(true);
            await api.delete(`/api/profile/address/${id}`);
            setSuccess('Address deleted successfully!');
            fetchProfileData();
        } catch (err) {
            setError(err.response?.data || 'Failed to delete address');
            console.error('Delete address error:', err);
        } finally {
            setLoading(false);
        }
    };

    const saveAddress = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');

        const phoneRegex = /^[6-9]\d{9}$/;
        if (!phoneRegex.test(addressForm.phone)) {
            setError('Phone number must be a valid 10-digit Indian mobile number');
            return;
        }

        try {
            setLoading(true);
            if (editingAddressId) {
                await api.put(`/api/profile/address/${editingAddressId}`, addressForm);
                setSuccess('Address updated successfully!');
            } else {
                await api.post('/api/profile/addresses', addressForm);
                setSuccess('Address added successfully!');
            }
            resetAddressForm();
            fetchProfileData(); // Refresh addresses
        } catch (err) {
            setError(err.response?.data || (editingAddressId ? 'Failed to update address' : 'Failed to add address'));
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="container py-5">
            <div className="row justify-content-center">
                <div className="col-lg-8">
                    <h2 className="fw-bold mb-4">My Profile</h2>

                    {error && <div className="alert alert-danger">{error}</div>}
                    {success && <div className="alert alert-success">{success}</div>}

                    <div className="card shadow-sm border-0 rounded-4 overflow-hidden">
                        <div className="card-header bg-white border-bottom-0 pt-4 pb-0">
                            <ul className="nav nav-tabs card-header-tabs">
                                {(user?.role !== 'VENDOR' && user?.roleId !== 2) && (
                                    <li className="nav-item">
                                        <button 
                                            className={`nav-link ${activeTab === 'profile' ? 'active fw-bold' : 'text-muted'}`}
                                            onClick={() => setActiveTab('profile')}
                                        >
                                            Personal Information
                                        </button>
                                    </li>
                                )}
                                {(user?.role === 'VENDOR' || user?.roleId === 2) && (
                                    <li className="nav-item">
                                        <button 
                                            className={`nav-link ${activeTab === 'vendor' ? 'active fw-bold' : 'text-muted'}`}
                                            onClick={() => setActiveTab('vendor')}
                                        >
                                            Vendor Information
                                        </button>
                                    </li>
                                )}
                                {(user?.role !== 'VENDOR' && user?.roleId !== 2) && (
                                    <>
                                        <li className="nav-item">
                                            <button 
                                                className={`nav-link ${activeTab === 'address' ? 'active fw-bold' : 'text-muted'}`}
                                                onClick={() => setActiveTab('address')}
                                            >
                                                Addresses
                                            </button>
                                        </li>
                                        <li className="nav-item">
                                            <button 
                                                className={`nav-link ${activeTab === 'orders' ? 'active fw-bold' : 'text-muted'}`}
                                                onClick={() => setActiveTab('orders')}
                                            >
                                                Order History
                                            </button>
                                        </li>
                                    </>
                                )}
                            </ul>
                        </div>

                        <div className="card-body p-4">
                            {activeTab === 'profile' && (
                                <form onSubmit={saveProfile}>
                                    <div className="row g-3">
                                        <div className="col-md-6">
                                            <label className="form-label text-muted small fw-bold">First Name</label>
                                            <input 
                                                type="text" 
                                                className="form-control" 
                                                name="firstName"
                                                value={profile.firstName} 
                                                onChange={handleProfileChange} 
                                                required 
                                            />
                                        </div>
                                        <div className="col-md-6">
                                            <label className="form-label text-muted small fw-bold">Last Name</label>
                                            <input 
                                                type="text" 
                                                className="form-control" 
                                                name="lastName"
                                                value={profile.lastName} 
                                                onChange={handleProfileChange} 
                                            />
                                        </div>
                                        <div className="col-md-12">
                                            <label className="form-label text-muted small fw-bold">Phone Number</label>
                                            <input 
                                                type="text" 
                                                className="form-control" 
                                                name="phone"
                                                value={profile.phone} 
                                                onChange={handleProfileChange} 
                                                required 
                                                placeholder="e.g. 9876543210"
                                            />
                                            <div className="form-text">Must be a valid 10-digit Indian mobile number.</div>
                                        </div>
                                    </div>
                                    <div className="mt-4 text-end">
                                        <button type="submit" className="btn btn-primary px-4" disabled={loading}>
                                            {loading ? 'Saving...' : 'Save Changes'}
                                        </button>
                                    </div>
                                </form>
                            )}

                            {activeTab === 'vendor' && (
                                <form onSubmit={saveVendorProfile}>
                                    <h5 className="mb-4 fw-bold">Vendor Registration Details</h5>
                                    <p className="text-muted small mb-4">You must complete this information before you can add products.</p>
                                    <div className="row g-3">
                                        <div className="col-md-6">
                                            <label className="form-label text-muted small fw-bold">Company Name</label>
                                            <input type="text" className="form-control" name="companyName" value={vendorProfile.companyName} onChange={handleVendorProfileChange} required />
                                        </div>
                                        <div className="col-md-6">
                                            <label className="form-label text-muted small fw-bold">Phone Number</label>
                                            <input type="text" className="form-control" name="phone" value={vendorProfile.phone} onChange={handleVendorProfileChange} required placeholder="10-digit mobile number" />
                                        </div>
                                        <div className="col-12">
                                            <label className="form-label text-muted small fw-bold">Street Address</label>
                                            <input type="text" className="form-control" name="streetAddress" value={vendorProfile.streetAddress} onChange={handleVendorProfileChange} required />
                                        </div>
                                        <div className="col-md-6">
                                            <label className="form-label text-muted small fw-bold">City</label>
                                            <input type="text" className="form-control" name="city" value={vendorProfile.city} onChange={handleVendorProfileChange} required />
                                        </div>
                                        <div className="col-md-6">
                                            <label className="form-label text-muted small fw-bold">State</label>
                                            <input type="text" className="form-control" name="state" value={vendorProfile.state} onChange={handleVendorProfileChange} required />
                                        </div>
                                        <div className="col-md-6">
                                            <label className="form-label text-muted small fw-bold">Country</label>
                                            <input type="text" className="form-control" name="country" value={vendorProfile.country} onChange={handleVendorProfileChange} required />
                                        </div>
                                        <div className="col-md-6">
                                            <label className="form-label text-muted small fw-bold">Postal Code</label>
                                            <input type="text" className="form-control" name="postalCode" value={vendorProfile.postalCode} onChange={handleVendorProfileChange} required />
                                        </div>
                                    </div>
                                    <div className="mt-4 text-end">
                                        <button type="submit" className="btn btn-primary px-4" disabled={loading}>
                                            {loading ? 'Saving...' : 'Save Vendor Profile'}
                                        </button>
                                    </div>
                                </form>
                            )}

                            {activeTab === 'address' && (
                                <div>
                                    {!showAddressForm ? (
                                        <div>
                                            <div className="d-flex justify-content-between align-items-center mb-3">
                                                <h5 className="mb-0 fw-bold">Saved Addresses</h5>
                                                <button 
                                                    className="btn btn-outline-primary btn-sm"
                                                    onClick={() => setShowAddressForm(true)}
                                                >
                                                    + Add New Address
                                                </button>
                                            </div>
                                            
                                            {addresses.length === 0 ? (
                                                <div className="text-center py-4 text-muted border rounded bg-light">
                                                    No addresses saved yet.
                                                </div>
                                            ) : (
                                                <div className="row g-3">
                                                    {addresses.map((addr, idx) => (
                                                        <div className="col-md-6" key={idx}>
                                                            <div className="card h-100 border-light shadow-sm">
                                                                <div className="card-body">
                                                                    <div className="d-flex justify-content-between align-items-start mb-2">
                                                                        <div>
                                                                            <h6 className="fw-bold mb-1">{addr.fullName}</h6>
                                                                            <span className="badge bg-secondary">{addr.addressType}</span>
                                                                        </div>
                                                                        <div className="d-flex gap-2">
                                                                            <button 
                                                                                className="btn btn-sm btn-outline-primary"
                                                                                onClick={() => handleEditAddress(addr)}
                                                                            >
                                                                                <i className="bi bi-pencil-fill"></i> Edit
                                                                            </button>
                                                                            <button 
                                                                                className="btn btn-sm btn-outline-danger"
                                                                                onClick={() => handleDeleteAddress(addr.id || addr.addressId)}
                                                                            >
                                                                                <i className="bi bi-trash-fill"></i> Delete
                                                                            </button>
                                                                        </div>
                                                                    </div>
                                                                    <p className="mb-1 text-muted small">{addr.streetAddress}</p>
                                                                    <p className="mb-1 text-muted small">{addr.city}, {addr.state} {addr.postalCode}</p>
                                                                    <p className="mb-1 text-muted small">{addr.country}</p>
                                                                    <p className="mb-0 text-muted small mt-2">
                                                                        <strong>Phone:</strong> {addr.phone}
                                                                    </p>
                                                                </div>
                                                            </div>
                                                        </div>
                                                    ))}
                                                </div>
                                            )}
                                        </div>
                                    ) : (
                                        <form onSubmit={saveAddress}>
                                            <h5 className="mb-4 fw-bold">Add New Address</h5>
                                            <div className="row g-3">
                                                <div className="col-md-6">
                                                    <label className="form-label text-muted small fw-bold">Full Name</label>
                                                    <input type="text" className="form-control" name="fullName" value={addressForm.fullName} onChange={handleAddressChange} required />
                                                </div>
                                                <div className="col-md-6">
                                                    <label className="form-label text-muted small fw-bold">Phone Number</label>
                                                    <input type="text" className="form-control" name="phone" value={addressForm.phone} onChange={handleAddressChange} required />
                                                </div>
                                                <div className="col-12">
                                                    <label className="form-label text-muted small fw-bold">Street Address</label>
                                                    <input type="text" className="form-control" name="streetAddress" value={addressForm.streetAddress} onChange={handleAddressChange} required />
                                                </div>
                                                <div className="col-md-6">
                                                    <label className="form-label text-muted small fw-bold">City</label>
                                                    <input type="text" className="form-control" name="city" value={addressForm.city} onChange={handleAddressChange} required />
                                                </div>
                                                <div className="col-md-6">
                                                    <label className="form-label text-muted small fw-bold">State</label>
                                                    <input type="text" className="form-control" name="state" value={addressForm.state} onChange={handleAddressChange} required />
                                                </div>
                                                <div className="col-md-4">
                                                    <label className="form-label text-muted small fw-bold">Country</label>
                                                    <input type="text" className="form-control" name="country" value={addressForm.country} onChange={handleAddressChange} required />
                                                </div>
                                                <div className="col-md-4">
                                                    <label className="form-label text-muted small fw-bold">Postal Code</label>
                                                    <input type="text" className="form-control" name="postalCode" value={addressForm.postalCode} onChange={handleAddressChange} required />
                                                </div>
                                                <div className="col-md-4">
                                                    <label className="form-label text-muted small fw-bold">Address Type</label>
                                                    <select className="form-select" name="addressType" value={addressForm.addressType} onChange={handleAddressChange}>
                                                        <option value="HOME">Home</option>
                                                        <option value="OFFICE">Office</option>
                                                    </select>
                                                </div>
                                            </div>
                                            <div className="mt-4 d-flex justify-content-end gap-2">
                                                <button type="button" className="btn btn-light" onClick={resetAddressForm} disabled={loading}>
                                                    Cancel
                                                </button>
                                                <button type="submit" className="btn btn-primary px-4" disabled={loading}>
                                                    {loading ? 'Saving...' : (editingAddressId ? 'Update Address' : 'Save Address')}
                                                </button>
                                            </div>
                                        </form>
                                    )}
                                </div>
                            )}

                            {activeTab === 'orders' && (
                                <div>
                                    <h5 className="mb-4 fw-bold">Order History</h5>
                                    {orders.length === 0 ? (
                                        <div className="text-center py-4 text-muted border rounded bg-light">
                                            No orders found.
                                        </div>
                                    ) : (
                                        <div className="table-responsive">
                                            <table className="table table-hover align-middle">
                                                <thead className="table-light">
                                                    <tr>
                                                        <th>Order ID</th>
                                                        <th>Date</th>
                                                        <th>Status</th>
                                                        <th>Total</th>
                                                        <th>Items</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    {orders.map((order, idx) => (
                                                        <tr key={idx}>
                                                            <td className="fw-bold text-primary">#{order.orderId || order.id}</td>
                                                            <td>{new Date(order.createdAt).toLocaleDateString()}</td>
                                                            <td>
                                                                <span className={`badge ${order.status === 'COMPLETED' || order.status === 'SUCCESS' ? 'bg-success' : order.status === 'FAILED' ? 'bg-danger' : 'bg-warning text-dark'}`}>
                                                                    {order.status || 'PENDING'}
                                                                </span>
                                                            </td>
                                                            <td className="fw-bold">₹{Number(order.totalAmount || 0).toFixed(2)}</td>
                                                            <td>{order.orderItems ? order.orderItems.length : 0} items</td>
                                                        </tr>
                                                    ))}
                                                </tbody>
                                            </table>
                                        </div>
                                    )}
                                </div>
                            )}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ProfilePage;
