import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { SecurityProvider } from './context/SecurityContext';
import { AuthProvider, useAuth } from './context/AuthContext';
import { DashboardPage } from './pages/DashboardPage';
import { ApplicationsPage } from './pages/ApplicationsPage';
import { ApplicationDetailsPage } from './pages/ApplicationDetailsPage';
import { EndpointsPage } from './pages/EndpointsPage';
import { TrafficPage } from './pages/TrafficPage';
import { ThreatsPage } from './pages/ThreatsPage';
import { ThreatDetailsPage } from './pages/ThreatDetailsPage';
import { SettingsPage } from './pages/SettingsPage';
import { LoginPage } from './pages/LoginPage';

const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated } = useAuth();
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  return <>{children}</>;
};

export const App: React.FC = () => {
  return (
    <AuthProvider>
      <SecurityProvider>
        <BrowserRouter>
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/" element={<ProtectedRoute><DashboardPage /></ProtectedRoute>} />
            <Route path="/endpoints" element={<ProtectedRoute><EndpointsPage /></ProtectedRoute>} />
            <Route path="/applications" element={<ProtectedRoute><ApplicationsPage /></ProtectedRoute>} />
            <Route path="/applications/:id" element={<ProtectedRoute><ApplicationDetailsPage /></ProtectedRoute>} />
            <Route path="/traffic" element={<ProtectedRoute><TrafficPage /></ProtectedRoute>} />
            <Route path="/threats" element={<ProtectedRoute><ThreatsPage /></ProtectedRoute>} />
            <Route path="/threats/:id" element={<ProtectedRoute><ThreatDetailsPage /></ProtectedRoute>} />
            <Route path="/settings" element={<ProtectedRoute><SettingsPage /></ProtectedRoute>} />
          </Routes>
        </BrowserRouter>
      </SecurityProvider>
    </AuthProvider>
  );
};

export default App;

