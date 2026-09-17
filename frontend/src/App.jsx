import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/layout/ProtectedRoute';

// Auth
import LoginPage    from './pages/auth/LoginPage';
import RegisterPage from './pages/auth/RegisterPage';

// Student
import StudentDashboard    from './pages/student/StudentDashboard';
import EventListPage       from './pages/student/EventListPage';
import MyRegistrationsPage from './pages/student/MyRegistrationsPage';
import QrTicketPage        from './pages/student/QrTicketPage';
import FeedbackPage        from './pages/student/FeedbackPage';
import CertificatesPage    from './pages/student/CertificatesPage';

// Organizer
import OrganizerDashboard      from './pages/organizer/OrganizerDashboard';
import QrScannerPage           from './pages/organizer/QrScannerPage';
import AttendanceDashboardPage from './pages/organizer/AttendanceDashboardPage';

// Admin
import AdminDashboard       from './pages/admin/AdminDashboard';
import EventManagementPage  from './pages/admin/EventManagementPage';
import UserManagementPage   from './pages/admin/UserManagementPage';
import FeedbackOverviewPage from './pages/admin/FeedbackOverviewPage';

export default function App() {
    return (
        <AuthProvider>
            <BrowserRouter>
                <Toaster position="top-right" toastOptions={{ duration: 3000 }} />
                <Routes>
                    {/* Public */}
                    <Route path="/login"    element={<LoginPage />} />
                    <Route path="/register" element={<RegisterPage />} />
                    <Route path="/"         element={<Navigate to="/login" replace />} />

                    {/* Student routes */}
                    <Route path="/student" element={
                        <ProtectedRoute allowedRoles={['STUDENT']}>
                            <StudentDashboard />
                        </ProtectedRoute>
                    } />
                    <Route path="/student/events" element={
                        <ProtectedRoute allowedRoles={['STUDENT']}>
                            <EventListPage />
                        </ProtectedRoute>
                    } />
                    <Route path="/student/registrations" element={
                        <ProtectedRoute allowedRoles={['STUDENT']}>
                            <MyRegistrationsPage />
                        </ProtectedRoute>
                    } />
                    <Route path="/student/ticket/:id" element={
                        <ProtectedRoute allowedRoles={['STUDENT']}>
                            <QrTicketPage />
                        </ProtectedRoute>
                    } />
                    <Route path="/student/feedback/:eventId" element={
                        <ProtectedRoute allowedRoles={['STUDENT']}>
                            <FeedbackPage />
                        </ProtectedRoute>
                    } />
                    <Route path="/student/certificates" element={
                        <ProtectedRoute allowedRoles={['STUDENT']}>
                            <CertificatesPage />
                        </ProtectedRoute>
                    } />

                    {/* Organizer routes */}
                    <Route path="/organizer" element={
                        <ProtectedRoute allowedRoles={['ORGANIZER']}>
                            <OrganizerDashboard />
                        </ProtectedRoute>
                    } />
                    <Route path="/organizer/scan" element={
                        <ProtectedRoute allowedRoles={['ORGANIZER']}>
                            <QrScannerPage />
                        </ProtectedRoute>
                    } />
                    <Route path="/organizer/attendance" element={
                        <ProtectedRoute allowedRoles={['ORGANIZER']}>
                            <AttendanceDashboardPage />
                        </ProtectedRoute>
                    } />

                    {/* Admin routes */}
                    <Route path="/admin" element={
                        <ProtectedRoute allowedRoles={['ADMIN']}>
                            <AdminDashboard />
                        </ProtectedRoute>
                    } />
                    <Route path="/admin/events" element={
                        <ProtectedRoute allowedRoles={['ADMIN']}>
                            <EventManagementPage />
                        </ProtectedRoute>
                    } />
                    <Route path="/admin/users" element={
                        <ProtectedRoute allowedRoles={['ADMIN']}>
                            <UserManagementPage />
                        </ProtectedRoute>
                    } />
                    <Route path="/admin/feedback" element={
                        <ProtectedRoute allowedRoles={['ADMIN']}>
                            <FeedbackOverviewPage />
                        </ProtectedRoute>
                    } />

                    {/* Fallback */}
                    <Route path="*" element={<Navigate to="/login" replace />} />
                </Routes>
            </BrowserRouter>
        </AuthProvider>
    );
}