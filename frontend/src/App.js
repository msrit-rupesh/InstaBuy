import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, Outlet, useNavigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { CartProvider } from './context/CartContext';
import LoginPage from './pages/auth/LoginPage';
import RegisterPage from './pages/auth/RegisterPage';
import ForgotPasswordPage from './pages/auth/ForgotPasswordPage';
import FioriShell from './components/layout/FioriShell';
import './index.css';
import ProductStore from './pages/products/ProductStore';
import ProductManagement from './pages/products/ProductManagement';
import InventoryManagement from './pages/products/InventoryManagement';
import CartPage from './pages/checkout/CartPage';
import ProfilePage from './pages/user/ProfilePage';
import PaymentFailedPage from './pages/checkout/PaymentFailedPage';
import PaymentSuccessPage from './pages/checkout/PaymentSuccessPage';
import VendorDashboard from './pages/vendor/VendorDashboard';
import SalesReport from './pages/vendor/SalesReport';

const AdminVendorRoute = () => {
  const { user, loading } = useAuth();
  if (loading) return <div>Loading...</div>;
  if (!user || (user.role !== 'ADMIN' && user.role !== 'VENDOR')) return <Navigate to="/" replace />;
  return <Outlet />;
};

const ProtectedRoute = () => {
  const { user, loading } = useAuth();
  if (loading) return <div>Loading...</div>;
  if (!user) return <Navigate to="/login" replace />;
  return <Outlet />;
};


const AppLayout = () => {
  return (
    <FioriShell>
      <Outlet />
    </FioriShell>
  );
};

const HomePage = () => {
  const { user } = useAuth();

  // Vendor -> Dashboard
  if (user?.role === 'VENDOR') {
      return <VendorDashboard />;
  }

  // Admin -> Empty Dashboard
  if (user?.role === 'ADMIN') {
      return (
          <div className="container mt-4 text-center">
              <h2 className="text-muted mt-5 mb-4">
                  Welcome to the Management Dashboard
              </h2>
              <p className="text-secondary">Please use the navigation menu to manage products.</p>
          </div>
      );
  }

  // Customer or others -> Amazon-like Product Store
  return <ProductStore />;
}

function App() {
  return (
    <Router>
      <AuthProvider>
        <CartProvider>
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/forgot-password" element={<ForgotPasswordPage />} />

            <Route element={<ProtectedRoute />}>
              <Route element={<AppLayout />}>
                <Route path="/" element={<HomePage />} />

                {/* Product Service Routes */}
                <Route path="/products" element={<ProductStore />} />
                <Route path="/cart" element={<CartPage />} />
                
                {/* User & Checkout Routes */}
                <Route path="/profile" element={<ProfilePage />} />
                <Route path="/payment-success" element={<PaymentSuccessPage />} />
                <Route path="/payment-failure" element={<PaymentFailedPage />} />
                <Route path="/payment/failed/:id" element={<PaymentFailedPage />} />
                
                <Route element={<AdminVendorRoute />}>
                  <Route path="/products/manage" element={<ProductManagement />} />
                  <Route path="/inventory" element={<InventoryManagement />} />
                  <Route path="/sales-report" element={<SalesReport />} />
                </Route>

                <Route path="*" element={<Navigate to="/" replace />} />
              </Route>
            </Route>
          </Routes>
        </CartProvider>
      </AuthProvider>
    </Router>
  );
}

export default App;
