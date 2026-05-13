import React, { createContext, useContext, useState, useEffect } from 'react';
import api from '../services/api';

const AuthContext = createContext(null);

// Helper to decode JWT and extract the 'id' claim
const getUserIdFromToken = (token) => {
    try {
        if (!token) return null;
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(window.atob(base64).split('').map(function(c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join(''));
        const decoded = JSON.parse(jsonPayload);
        return decoded.id || null;
    } catch (e) {
        console.error("Failed to decode token", e);
        return null;
    }
};

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const storedUserStr = localStorage.getItem('user');
        const token = localStorage.getItem('accessToken');
        if (storedUserStr && token) {
            try {
                const storedUser = JSON.parse(storedUserStr);
                // Ensure the 'id' property is populated if missing but token exists
                if (!storedUser.id) {
                    storedUser.id = getUserIdFromToken(token);
                    localStorage.setItem('user', JSON.stringify(storedUser));
                }
                setUser(storedUser);
            } catch (e) {
                console.error("Failed to parse user", e);
                localStorage.removeItem('user');
            }
        }
        setLoading(false);
    }, []);

    const login = async (username, password, roleId) => {
        try {
            const response = await api.post('/auth/login',
                {
                    "username": username,
                    "password": password,
                    "roleId": roleId
                });
            const { accessToken, name } = response.data;

            // Use the name from backend, fallback to username
            // Also store RoleId

            const extractedId = getUserIdFromToken(accessToken) || response.data.id || response.data.userId;

            const userObj = {
                username,
                id: extractedId,
                name: response.data.Name || name || response.data.name || username,
                roleId: response.data.roleId || response.data.RoleId || roleId,
                role: response.data.role || response.data.Role
            };

            localStorage.setItem('accessToken', accessToken);
            localStorage.setItem('user', JSON.stringify(userObj));
            setUser(userObj);
            return { success: true };
        } catch (error) {
            console.error("Login Error", error);
            const errMessage = error.response?.data?.message || (typeof error.response?.data === 'string' ? error.response.data : "Invalid credentials");
            if (error.response?.status === 403) {
                return {
                    success: false,
                    isPasswordExpired: true,
                    message: errMessage || "Password expired. Please reset your password.",
                    id:error.response.data.id
                };
            }
            return {
                success: false,
                message: errMessage
            };
        }
    };

    const register = async (name, username, email, password, roleId) => {
        try {
            const response = await api.post('auth/register/init', {
                "name": name,
                "username": username,
                "email": email,
                "password": password,
                "roleId": roleId
            });
            return { 
                id:response.data.id,
                success: true, 
                isOtpSent: true,
                message:response.data.message 
            };
        } catch (error) {
            let errMessage = "Registration failed";
            if (error.response?.data) {
                if (error.response.data.message) {
                    errMessage = error.response.data.message;
                } else if (typeof error.response.data === 'string') {
                    errMessage = error.response.data;
                } else if (typeof error.response.data === 'object') {
                    errMessage = Object.values(error.response.data)[0];
                }
            }
            
            return {
                success: false,
                message: errMessage
            };
        }
    };

    const verifyRegisterOtp = async (otpId,otp,name, username, email, password, roleId) => {
        try {
            await api.post('auth/otp/verify', {
                "id":otpId,
                "email":email,
                "otp": otp,
            });
            await api.post('auth/register/complete',{
                "id":otpId,
                "name": name,
                "username": username,
                "email": email,
                "password": password,
                "roleId": roleId
            });
            return { success: true };
        } catch (error) {
            const errMessage = error.response?.data?.message || (typeof error.response?.data === 'string' ? error.response.data : "OTP Verification failed");
            return {
                success: false,
                message: errMessage
            };
        }
    };

    const sendForgotPasswordOtp = async (email, roleId) => {
        try {
            
            const response = await api.post('/auth/forget_username_password', { email, roleId });
            const { id, message } = response.data;
            return { id:id , message:message , success: true };
        } catch (error) {
            const errMessage = error.response?.data?.message || (typeof error.response?.data === 'string' ? error.response.data : "Failed to send OTP to email");
            return {
                success: false,
                message: errMessage
            };
        }
    };

    const resetPassword = async (otpId,email, otp, newPassword, roleId) => {
        try {
            await api.post('/auth/otp/verify',{ 
                "id":otpId,
                "email":email,
                "otp":otp
            });

            await api.post('/auth/reset-password', {
                "id":otpId,
                "email":email, 
                "password":newPassword, 
                "roleId":roleId 
            });
            return { success: true };
        } catch (error) {
            const errMessage = error.response?.data?.message || (typeof error.response?.data === 'string' ? error.response.data : "Failed to reset password");
            return {
                success: false,
                message: errMessage
            };
        }
    };

    const resetExpiredPassword = async (otpId,username, otp, newPassword,roleId) => {
        try {
            const response = await api.post('/auth/reset-expired-password', { 
                "id":otpId,
                "username":username, 
                "otp":otp,
                "password":newPassword, 
                "roleId":roleId
            });

            // Assuming the response from this endpoint acts like a fresh login and returns tokens
            const { accessToken, name, role } = response.data;
            const extractedId = getUserIdFromToken(accessToken) || response.data.id || response.data.userId;

            const userObj = {
                username,
                id: extractedId,
                name: response.data.Name || name || response.data.name || username,
                roleId: response.data.roleId || response.data.RoleId || roleId,
                role: response.data.role || role
            };

            localStorage.setItem('accessToken', accessToken);
            localStorage.setItem('user', JSON.stringify(userObj));
            setUser(userObj);

            return { success: true };
        } catch (error) {
            const errMessage = error.response?.data?.message || (typeof error.response?.data === 'string' ? error.response.data : "Failed to reset expired password");
            return {
                success: false,
                message: errMessage
            };
        }
    };

    const logout = async () => {
        try {
            // Optional: call backend logout without refresh token if supported
            // await api.post('/logout');
        } catch (e) {
            console.error("Logout error", e);
        } finally {
            localStorage.removeItem('accessToken');
            localStorage.removeItem('user');
            setUser(null);
        }
    };

    return (
        <AuthContext.Provider value={{ user, login, register, verifyRegisterOtp, sendForgotPasswordOtp, resetPassword, resetExpiredPassword, logout, loading }}>
            {!loading && children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => useContext(AuthContext);
