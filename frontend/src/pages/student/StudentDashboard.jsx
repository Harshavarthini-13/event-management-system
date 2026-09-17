import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Navbar from '../../components/layout/Navbar';
import { useAuth } from '../../context/AuthContext';
import { getMyRegistrations } from '../../api/registrationApi';
import { getMyCertificates } from '../../api/certificateApi';
import { getUpcomingEvents } from '../../api/eventApi';

export default function StudentDashboard() {
    const { user } = useAuth();
    const [stats, setStats] = useState({
        registrations: 0,
        attended: 0,
        certificates: 0,
        upcoming: 0,
    });
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        async function fetchStats() {
            try {
                const [regs, certs, events] = await Promise.all([
                    getMyRegistrations(),
                    getMyCertificates(),
                    getUpcomingEvents(),
                ]);
                const regList = regs.data?.data || [];
                setStats({
                    registrations: regList.length,
                    attended: regList.filter((r) => r.status === 'ATTENDED').length,
                    certificates: certs.data?.data?.length || 0,
                    upcoming: events.data?.data?.length || 0,
                });
            } catch (_) {}
            finally { setLoading(false); }
        }
        fetchStats();
    }, []);

    const cards = [
        { label: 'My Registrations', value: stats.registrations, color: '#2ecbc1', icon: '🎟️', to: '/student/registrations' },
        { label: 'Events Attended', value: stats.attended, color: '#3b82f6', icon: '✅', to: '/student/registrations' },
        { label: 'Certificates', value: stats.certificates, color: '#f59e0b', icon: '🏆', to: '/student/certificates' },
        { label: 'Upcoming Events', value: stats.upcoming, color: '#8b5cf6', icon: '📅', to: '/student/events' },
    ];

    return (
        <div className="min-h-screen" style={{ background: '#f8fafc' }}>
            <Navbar />
            <div className="max-w-6xl mx-auto px-8 py-10">
                {/* Welcome */}
                <div className="mb-8">
                    <h1 className="text-3xl font-bold text-gray-800">
                        Welcome back, {user?.name}! 👋
                    </h1>
                    <p className="text-gray-500 mt-1">Here's your activity overview</p>
                </div>

                {/* Stat Cards */}
                {loading ? (
                    <div className="flex justify-center py-20">
                        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-teal-500" />
                    </div>
                ) : (
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-10">
                        {cards.map((c) => (
                            <Link
                                key={c.label}
                                to={c.to}
                                className="bg-white rounded-2xl p-6 shadow-sm hover:shadow-md transition block"
                            >
                                <div className="flex items-center justify-between mb-4">
                                    <span className="text-3xl">{c.icon}</span>
                                    <span
                                        className="text-3xl font-bold"
                                        style={{ color: c.color }}
                                    >
                    {c.value}
                  </span>
                                </div>
                                <p className="text-sm font-semibold text-gray-600">{c.label}</p>
                            </Link>
                        ))}
                    </div>
                )}

                {/* Quick Actions */}
                <div className="bg-white rounded-2xl p-8 shadow-sm">
                    <h2 className="text-xl font-bold text-gray-800 mb-6">Quick Actions</h2>
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                        <Link
                            to="/student/events"
                            className="flex items-center gap-4 p-5 rounded-xl border-2 border-dashed
                         border-teal-200 hover:border-teal-400 hover:bg-teal-50 transition"
                        >
                            <span className="text-2xl">🔍</span>
                            <div>
                                <p className="font-semibold text-gray-800">Browse Events</p>
                                <p className="text-xs text-gray-500">Find and register for events</p>
                            </div>
                        </Link>
                        <Link
                            to="/student/registrations"
                            className="flex items-center gap-4 p-5 rounded-xl border-2 border-dashed
                         border-blue-200 hover:border-blue-400 hover:bg-blue-50 transition"
                        >
                            <span className="text-2xl">🎟️</span>
                            <div>
                                <p className="font-semibold text-gray-800">My Tickets</p>
                                <p className="text-xs text-gray-500">View your QR tickets</p>
                            </div>
                        </Link>
                        <Link
                            to="/student/certificates"
                            className="flex items-center gap-4 p-5 rounded-xl border-2 border-dashed
                         border-yellow-200 hover:border-yellow-400 hover:bg-yellow-50 transition"
                        >
                            <span className="text-2xl">📜</span>
                            <div>
                                <p className="font-semibold text-gray-800">Certificates</p>
                                <p className="text-xs text-gray-500">Download your certificates</p>
                            </div>
                        </Link>
                    </div>
                </div>
            </div>
        </div>
    );
}