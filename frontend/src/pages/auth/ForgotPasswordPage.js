import React, { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { Link, useNavigate } from 'react-router-dom';
import './Auth.css';

const ForgotPasswordPage = () => {
    const [email, setEmail] = useState('');
    const [otp, setOtp] = useState('');
    const [otpId,setOtpId]=useState('');
    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [step, setStep] = useState(1); // 1 = Email, 2 = Reset
    const [isVendor, setIsVendor] = useState(false); // Vendor Toggle State

    const [showPassword, setShowPassword] = useState(false);
    const [showConfirmPassword, setShowConfirmPassword] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    const { sendForgotPasswordOtp, resetPassword } = useAuth();
    const navigate = useNavigate();

    const handleSendOtp = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');

        const roleId = isVendor ? 2 : 1;
        const { id,message,success} = await sendForgotPasswordOtp(email, roleId);
        if (success) {
            setSuccess("OTP sent successfully to your email.");
            setStep(2);
            setOtpId(id);
        } else {
            setError(message);
        }
    };
    

    const handleResetPassword = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');

        if (newPassword !== confirmPassword) {
            setError("Passwords do not match");
            return;
        }

        const roleId = isVendor ? 2 : 1;
        const result = await resetPassword(otpId,email, otp, newPassword, roleId);
        if (result.success) {
            setSuccess("Password reset successfully. Redirecting to login...");
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
                        {isVendor ? "Reset as User" : "Reset as Vendor"}
                    </button>
                </div>

                <div className="auth-header">
                    <div className="auth-icon" style={{ background: isVendor ? 'linear-gradient(135deg, #10b981, #059669)' : 'linear-gradient(135deg, #f59e0b, #d97706)' }}>
                        <span role="img" aria-label="Key">🔑</span>
                    </div>
                    <h2 className="auth-title">{isVendor ? "Vendor Password Reset" : "Forgot Password"}</h2>
                    <p className="auth-subtitle">{step === 1 ? "Enter email to receive OTP" : "Reset your password"}</p>
                </div>

                {error && <div className="auth-message error">{error}</div>}
                {success && <div className="auth-message success">{success}</div>}

                {step === 1 ? (
                    <form onSubmit={handleSendOtp} className="auth-form">
                        <div className="auth-input-group">
                            <label className="auth-label" htmlFor="email">Email</label>
                            <div className="auth-input-wrapper">
                                <input
                                    id="email"
                                    type="email"
                                    className="auth-input"
                                    value={email}
                                    onChange={(e) => setEmail(e.target.value)}
                                    placeholder="Enter your registered email"
                                    required
                                />
                            </div>
                        </div>
                        <button type="submit" className="auth-button">Send OTP</button>
                    </form>
                ) : (
                    <form onSubmit={handleResetPassword} className="auth-form">
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
                                    type={showPassword ? "text" : "password"}
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
                                    onClick={() => setShowPassword(!showPassword)}
                                    aria-label={showPassword ? "Hide password" : "Show password"}
                                >
                                    {showPassword ? "Hide" : "Show"}
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
                                    {showConfirmPassword ? "Hide" : "Show"}
                                </button>
                            </div>
                        </div>

                        <button type="submit" className="auth-button">Reset Password</button>
                    </form>
                )}

                <div className="auth-footer mt-4">
                    Remember your password?{' '}
                    <Link to="/login" className="auth-link">
                        Sign In here
                    </Link>
                </div>
            </div>
        </div>
    );
};

export default ForgotPasswordPage;
