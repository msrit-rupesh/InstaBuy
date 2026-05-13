import React, { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { Link, useNavigate } from 'react-router-dom';
import './Auth.css'; // Import the new Auth styles

const RegisterPage = () => {
    const [name, setName] = useState('');
    const [username, setUsername] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [otp, setOtp] = useState('');
    const [otpId,setOtpId]=useState('');
    const [isOtpSent, setIsOtpSent] = useState(false);
    const [isVendor, setIsVendor] = useState(false); // Vendor Toggle State

    const [showPassword, setShowPassword] = useState(false);
    const [showConfirmPassword, setShowConfirmPassword] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const { register, verifyRegisterOtp } = useAuth();
    const navigate = useNavigate();

    const handleRegisterSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');

        if (password !== confirmPassword) {
            setError("Passwords do not match");
            return;
        }

        const roleId = isVendor ? 2 : 1;
        const response = await register(name, username, email, password, roleId);
        const {id ,success, isOtpSent,message }=response;
        if (success) {
            setSuccess("OTP sent successfully to your email.");
            setIsOtpSent(success);
            setOtpId(id);
        } else {
            setError(message);
        }
    };

    const handleOtpSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');

        const roleId = isVendor ? 2 : 1;
        const result = await verifyRegisterOtp(otpId, otp,name, username, email, password, roleId);
        if (result.success) {
            setSuccess("User registered successfully. Redirecting to login...");
            setTimeout(() => {
                navigate('/login');
            }, 2000);
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
                        {isVendor ? "Register as User" : "Register as Vendor"}
                    </button>
                </div>

                <div className="auth-header">
                    <div className="auth-icon" style={{ background: isVendor ? 'linear-gradient(135deg, #f59e0b, #d97706)' : 'linear-gradient(135deg, #10b981, #059669)' }}>
                        <span role="img" aria-label="User Add">👤</span>
                    </div>
                    <h2 className="auth-title">{isVendor ? "Vendor Registration" : "Create Account"}</h2>
                    <p className="auth-subtitle">{isOtpSent ? "Verify your OTP" : "Join us to get started"}</p>
                </div>

                {error && <div className="auth-message error">{error}</div>}
                {success && <div className="auth-message success">{success}</div>}

                {!isOtpSent ? (
                    <form onSubmit={handleRegisterSubmit} className="auth-form">
                        <div className="auth-input-group">
                            <label className="auth-label" htmlFor="name">Full Name</label>
                            <div className="auth-input-wrapper">
                                <input
                                    id="name"
                                    type="text"
                                    className="auth-input"
                                    value={name}
                                    onChange={(e) => setName(e.target.value)}
                                    placeholder="John Doe"
                                    required
                                />
                            </div>
                        </div>

                        <div className="auth-input-group">
                            <label className="auth-label" htmlFor="username">Username</label>
                            <div className="auth-input-wrapper">
                                <input
                                    id="username"
                                    type="text"
                                    className="auth-input"
                                    value={username}
                                    onChange={(e) => setUsername(e.target.value)}
                                    placeholder="Choose a username"
                                    required
                                />
                            </div>
                        </div>

                        <div className="auth-input-group">
                            <label className="auth-label" htmlFor="email">Email</label>
                            <div className="auth-input-wrapper">
                                <input
                                    id="email"
                                    type="email"
                                    className="auth-input"
                                    value={email}
                                    onChange={(e) => setEmail(e.target.value)}
                                    placeholder="Enter your email"
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
                                    placeholder="Create a strong password"
                                    required
                                    minLength="8"
                                    pattern="^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@!#$%^&+=]).{8,}$"
                                    title="Password must be at least 8 characters, including uppercase, lowercase, number and special character (@#$%^&+=)"
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

                        <div className="auth-input-group">
                            <label className="auth-label" htmlFor="confirmPassword">Confirm Password</label>
                            <div className="auth-input-wrapper">
                                <input
                                    id="confirmPassword"
                                    type={showConfirmPassword ? "text" : "password"}
                                    className="auth-input"
                                    value={confirmPassword}
                                    onChange={(e) => setConfirmPassword(e.target.value)}
                                    placeholder="Re-enter your password"
                                    required
                                    minLength="8"
                                    pattern="^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).{8,}$"
                                    title="Password must be at least 8 characters, including uppercase, lowercase, number and special character (@#$%^&+=)"
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

                        <button type="submit" className="auth-button">Register</button>
                    </form>
                ) : (
                    <form onSubmit={handleOtpSubmit} className="auth-form">
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
                        <button type="submit" className="auth-button">Verify OTP</button>
                    </form>
                )}

                <div className="auth-footer">
                    Already have an account?{' '}
                    <Link to="/login" className="auth-link">
                        Sign In here
                    </Link>
                </div>
            </div>
        </div>
    );
};

export default RegisterPage;
