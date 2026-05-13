import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { Link, useNavigate } from 'react-router-dom';
import './Auth.css'; // Import the new Auth styles

const LoginPage = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [showPassword, setShowPassword] = useState(false);
    const [rememberMe, setRememberMe] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [isVendor, setIsVendor] = useState(false); // Vendor Toggle State

    // Password Expiry State
    const [isPasswordExpired, setIsPasswordExpired] = useState(false);
    const [otp, setOtp] = useState('');
    const [otpId,setOtpId]=useState('');
    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [showNewPassword, setShowNewPassword] = useState(false);
    const [showConfirmPassword, setShowConfirmPassword] = useState(false);

    const { login, resetExpiredPassword } = useAuth();
    const navigate = useNavigate();

    useEffect(() => {
        const savedUsername = localStorage.getItem('rememberedUsername');
        if (savedUsername) {
            setUsername(savedUsername);
            setRememberMe(true);
        }
    }, []);

    const handleLoginSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');
        const roleId = isVendor ? 2 : 1;
        const result = await login(username, password, roleId);
        if (result.success) {
            navigate('/');
        } else if (result.isPasswordExpired) {
            setIsPasswordExpired(true);
            setSuccess("An OTP has been sent. Please reset your password.");
            setOtpId(result.id);
            setError('');
        } else {
            setError(result.message);
        }
    };

    const handleResetExpiredPasswordSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');

        if (newPassword !== confirmPassword) {
            setError("Passwords do not match");
            return;
        }

        const roleId = isVendor ? 2 : 1;
        const result = await resetExpiredPassword(otpId,username, otp, newPassword, roleId);
        if (result.success) {
            navigate('/');
        } else {
            setError(result.message);
        }
    };

    return (
        <div className="auth-page-container">
            <div className="auth-card">
                <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: '10px' }}>
                    <button
                        type="button"
                        onClick={() => { setIsVendor(!isVendor); setError(''); setSuccess(''); }}
                        className="auth-link small"
                        style={{ border: 'none', background: 'none', fontWeight: '600', padding: 0 }}
                    >
                        {isVendor ? "Switch to User Login" : "Login as Vendor"}
                    </button>
                </div>

                <div className="auth-header">
                    <div className="auth-icon" style={{ background: isVendor ? 'linear-gradient(135deg, #10b981, #059669)' : '' }}>
                        <span role="img" aria-label="Lock">🔒</span>
                    </div>
                    <h2 className="auth-title">
                        {isPasswordExpired ? "Password Expired" : (isVendor ? "Vendor Login" : "Welcome Back")}
                    </h2>
                    <p className="auth-subtitle">
                        {isPasswordExpired ? "Please reset your password to continue" : "Please sign in to your account"}
                    </p>
                </div>

                {error && <div className="auth-message error">{error}</div>}
                {success && <div className="auth-message success">{success}</div>}

                {!isPasswordExpired ? (
                    <form onSubmit={handleLoginSubmit} className="auth-form">
                        <div className="auth-input-group">
                            <label className="auth-label" htmlFor="username">Username</label>
                            <div className="auth-input-wrapper">
                                <input
                                    id="username"
                                    type="text"
                                    className="auth-input"
                                    value={username}
                                    onChange={(e) => setUsername(e.target.value)}
                                    placeholder="Enter your username"
                                    required
                                />
                            </div>
                        </div>

                        <div className="auth-input-group">
                            <label className="auth-label" htmlFor="password">Password</label>
                            <div className="auth-input-wrapper">
                                <input
                                    id="password"
                                    type={showPassword ? "text" : "password"}
                                    className="auth-input"
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    placeholder="Enter your password"
                                    required
                                    style={{ paddingRight: '2.5rem' }}
                                />
                                <button
                                    type="button"
                                    className="auth-icon-btn"
                                    onClick={() => setShowPassword(!showPassword)}
                                    aria-label={showPassword ? "Hide password" : "Show password"}
                                >
                                    {showPassword ? (
                                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16">
                                            <path d="M13.359 11.238C15.06 9.72 16 8 16 8s-3-5.5-8-5.5a7.028 7.028 0 0 0-2.79.588l.77.771A5.944 5.944 0 0 1 8 3.5C2.182 3.5 0 8 0 8s2.182 4.5 8 4.5c.702 0 1.373-.127 1.94-.36z" />
                                            <path d="M10.794 12.936 12.49 14.633a.5.5 0 0 0 .708-.708L2.451 .242a.5.5 0 0 0-.708.707l10.742 10.742a.5.5 0 0 0 .708.707z" />
                                        </svg>
                                    ) : (
                                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16">
                                            <path d="M16 8s-3-5.5-8-5.5S0 8 0 8s3 5.5 8 5.5S16 8 16 8zM1.173 8a13.133 13.133 0 0 1 1.66-2.043C4.12 4.668 5.88 3.5 8 3.5c2.12 0 3.879 1.168 5.168 2.457A13.133 13.133 0 0 1 14.828 8c-.058.087-.122.183-.195.288-.335.48-.83 1.12-1.465 1.755C11.879 11.332 10.119 12.5 8 12.5c-2.12 0-3.879-1.168-5.168-2.457A13.134 13.134 0 0 1 1.172 8z" />
                                            <path d="M8 5.5a2.5 2.5 0 1 0 0 5 2.5 2.5 0 0 0 0-5zM4.5 8a3.5 3.5 0 1 1 7 0 3.5 3.5 0 0 1-7 0z" />
                                        </svg>
                                    )}
                                </button>
                            </div>
                        </div>

                        <div className="auth-options" style={{ display: 'flex', justifyContent: 'flex-end', width: '100%' }}>
                            <Link to="/forgot-password" className="auth-link small">
                                Forgot Username/Password?
                            </Link>
                        </div>

                        <button type="submit" className="auth-button">Sign In</button>
                    </form>
                ) : (
                    <form onSubmit={handleResetExpiredPasswordSubmit} className="auth-form">
                        <div className="auth-input-group">
                            <label className="auth-label" htmlFor="otp">Enter OTP</label>
                            <div className="auth-input-wrapper">
                                <input
                                    id="otp"
                                    type="text"
                                    className="auth-input"
                                    value={otp}
                                    onChange={(e) => setOtp(e.target.value)}
                                    placeholder="Enter OTP sent to your email"
                                    required
                                />
                            </div>
                        </div>

                        <div className="auth-input-group">
                            <label className="auth-label" htmlFor="newPassword">New Password</label>
                            <div className="auth-input-wrapper">
                                <input
                                    id="newPassword"
                                    type={showNewPassword ? "text" : "password"}
                                    className="auth-input"
                                    value={newPassword}
                                    onChange={(e) => setNewPassword(e.target.value)}
                                    placeholder="Enter new password"
                                    required
                                    style={{ paddingRight: '2.5rem' }}
                                />
                                <button
                                    type="button"
                                    className="auth-icon-btn"
                                    onClick={() => setShowNewPassword(!showNewPassword)}
                                    aria-label={showNewPassword ? "Hide password" : "Show password"}
                                >
                                    {showNewPassword ? (
                                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16">
                                            <path d="M13.359 11.238C15.06 9.72 16 8 16 8s-3-5.5-8-5.5a7.028 7.028 0 0 0-2.79.588l.77.771A5.944 5.944 0 0 1 8 3.5C2.182 3.5 0 8 0 8s2.182 4.5 8 4.5c.702 0 1.373-.127 1.94-.36z" />
                                            <path d="M10.794 12.936 12.49 14.633a.5.5 0 0 0 .708-.708L2.451 .242a.5.5 0 0 0-.708.707l10.742 10.742a.5.5 0 0 0 .708.707z" />
                                        </svg>
                                    ) : (
                                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16">
                                            <path d="M16 8s-3-5.5-8-5.5S0 8 0 8s3 5.5 8 5.5S16 8 16 8zM1.173 8a13.133 13.133 0 0 1 1.66-2.043C4.12 4.668 5.88 3.5 8 3.5c2.12 0 3.879 1.168 5.168 2.457A13.133 13.133 0 0 1 14.828 8c-.058.087-.122.183-.195.288-.335.48-.83 1.12-1.465 1.755C11.879 11.332 10.119 12.5 8 12.5c-2.12 0-3.879-1.168-5.168-2.457A13.134 13.134 0 0 1 1.172 8z" />
                                            <path d="M8 5.5a2.5 2.5 0 1 0 0 5 2.5 2.5 0 0 0 0-5zM4.5 8a3.5 3.5 0 1 1 7 0 3.5 3.5 0 0 1-7 0z" />
                                        </svg>
                                    )}
                                </button>
                            </div>
                        </div>

                        <div className="auth-input-group">
                            <label className="auth-label" htmlFor="confirmPassword">Confirm Password</label>
                            <div className="auth-input-wrapper">
                                <input
                                    id="confirmPassword"
                                    type={showConfirmPassword ? "text" : "password"}
                                    className="auth-input"
                                    value={confirmPassword}
                                    onChange={(e) => setConfirmPassword(e.target.value)}
                                    placeholder="Re-enter new password"
                                    required
                                    style={{ paddingRight: '2.5rem' }}
                                />
                                <button
                                    type="button"
                                    className="auth-icon-btn"
                                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                                    aria-label={showConfirmPassword ? "Hide password" : "Show password"}
                                >
                                    {showConfirmPassword ? (
                                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16">
                                            <path d="M13.359 11.238C15.06 9.72 16 8 16 8s-3-5.5-8-5.5a7.028 7.028 0 0 0-2.79.588l.77.771A5.944 5.944 0 0 1 8 3.5C2.182 3.5 0 8 0 8s2.182 4.5 8 4.5c.702 0 1.373-.127 1.94-.36z" />
                                            <path d="M10.794 12.936 12.49 14.633a.5.5 0 0 0 .708-.708L2.451 .242a.5.5 0 0 0-.708.707l10.742 10.742a.5.5 0 0 0 .708.707z" />
                                        </svg>
                                    ) : (
                                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16">
                                            <path d="M16 8s-3-5.5-8-5.5S0 8 0 8s3 5.5 8 5.5S16 8 16 8zM1.173 8a13.133 13.133 0 0 1 1.66-2.043C4.12 4.668 5.88 3.5 8 3.5c2.12 0 3.879 1.168 5.168 2.457A13.133 13.133 0 0 1 14.828 8c-.058.087-.122.183-.195.288-.335.48-.83 1.12-1.465 1.755C11.879 11.332 10.119 12.5 8 12.5c-2.12 0-3.879-1.168-5.168-2.457A13.134 13.134 0 0 1 1.172 8z" />
                                            <path d="M8 5.5a2.5 2.5 0 1 0 0 5 2.5 2.5 0 0 0 0-5zM4.5 8a3.5 3.5 0 1 1 7 0 3.5 3.5 0 0 1-7 0z" />
                                        </svg>
                                    )}
                                </button>
                            </div>
                        </div>

                        <button type="submit" className="auth-button">Reset & Sign In</button>
                    </form>
                )}

                <div className="auth-footer mt-4">
                    Don't have an account?{' '}
                    <Link to="/register" className="auth-link">
                        Register here
                    </Link>
                </div>
            </div>
        </div>
    );
};

export default LoginPage;
