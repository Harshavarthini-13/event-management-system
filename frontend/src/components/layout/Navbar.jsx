import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

export default function Navbar() {
    const { user, role, logout } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    const studentLinks = [
        { to: '/student', label: 'Dashboard' },
        { to: '/student/events', label: 'Events' },
        { to: '/student/registrations', label: 'My Registrations' },
        { to: '/student/certificates', label: 'Certificates' },
    ];

    const organizerLinks = [
        { to: '/organizer', label: 'Dashboard' },
        { to: '/organizer/scan', label: 'Scan QR' },
        { to: '/organizer/attendance', label: 'Attendance' },
    ];

    const adminLinks = [
        { to: '/admin', label: 'Dashboard' },
        { to: '/admin/events', label: 'Events' },
        { to: '/admin/users', label: 'Users' },
        { to: '/admin/feedback', label: 'Feedback' },
    ];

    const links =
        role === 'STUDENT' ? studentLinks :
            role === 'ORGANIZER' ? organizerLinks :
                role === 'ADMIN' ? adminLinks : [];

    const roleColor =
        role === 'ADMIN' ? '#dc2626' :
            role === 'ORGANIZER' ? '#7c3aed' : '#2ecbc1';

    return (
        <nav
            className="w-full flex items-center justify-between px-8 py-4 shadow-sm"
            style={{ background: 'white', borderBottom: '2px solid #f3f4f6' }}
        >
            {/* Logo */}
            <div className="flex items-center gap-3">
                <div
                    className="w-9 h-9 rounded-xl flex items-center justify-center text-white font-bold text-lg"
                    style={{ background: roleColor }}
                >
                    E
                </div>
                <span className="font-bold text-gray-800 text-lg tracking-wide">EventHub</span>
            </div>

            {/* Nav Links */}
            <div className="flex items-center gap-6">
                {links.map((link) => (
                    <Link
                        key={link.to}
                        to={link.to}
                        className="text-sm font-semibold transition"
                        style={{
                            color: location.pathname === link.to ? roleColor : '#6b7280',
                            borderBottom: location.pathname === link.to
                                ? `2px solid ${roleColor}` : '2px solid transparent',
                            paddingBottom: '2px',
                        }}
                    >
                        {link.label}
                    </Link>
                ))}
            </div>

            {/* User + Logout */}
            <div className="flex items-center gap-4">
                <div className="text-right">
                    <p className="text-sm font-semibold text-gray-800">{user?.name}</p>
                    <p
                        className="text-xs font-bold uppercase tracking-wider"
                        style={{ color: roleColor }}
                    >
                        {role}
                    </p>
                </div>
                <button
                    onClick={handleLogout}
                    className="px-4 py-2 rounded-full text-sm font-semibold text-white transition"
                    style={{ background: roleColor }}
                >
                    Logout
                </button>
            </div>
        </nav>
    );
}