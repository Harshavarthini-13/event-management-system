import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Navbar from '../../components/layout/Navbar';
import { useAuth } from '../../context/AuthContext';
import { getAllEvents } from '../../api/eventApi';
import { getAllUsers } from '../../api/userApi';

export default function AdminDashboard() {
    const { user } = useAuth();
    const [stats, setStats] = useState({ events: 0, users: 0, organizers: 0, upcoming: 0 });
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        Promise.all([getAllEvents(), getAllUsers()])
            .then(([evRes, usRes]) => {
                const events = evRes.data?.data || [];
                const users  = usRes.data?.data || [];
                setStats({
                    events: events.length,
                    users: users.length,
                    organizers: users.filter((u) => u.role === 'ORGANIZER').length,
                    upcoming: events.filter((e) => e.status === 'UPCOMING').length,
                });
            })
            .finally(() => setLoading(false));
    }, []);

    const cards = [
        { label: 'Total Events',    value: stats.events,     color: '#dc2626', icon: '📋', to: '/admin/events' },
        { label: 'Upcoming Events', value: stats.upcoming,   color: '#2ecbc1', icon: '📅', to: '/admin/events' },
        { label: 'Total Users',     value: stats.users,      color: '#3b82f6', icon: '👥', to: '/admin/users'  },
        { label: 'Organizers',      value: stats.organizers, color: '#7c3aed', icon: '🎯', to: '/admin/users'  },
    ];

    return (
        <div className="min-h-screen" style={{ background: '#f8fafc' }}>
            <Navbar />
            <div className="max-w-6xl mx-auto px-8 py-10">
                <div className="mb-8">
                    <h1 className="text-3xl font-bold text-gray-800">
                        Admin Dashboard 🛡️
                    </h1>
                    <p className="text-gray-500 mt-1">Welcome back, {user?.name}</p>
                </div>

                {loading ? (
                    <div className="flex justify-center py-20">
                        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-red-500" />
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
                                    <span className="text-3xl font-bold" style={{ color: c.color }}>
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
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                        {[
                            { to: '/admin/events',   icon: '➕', title: 'Create Event',  desc: 'Add a new event',      color: '#dc2626' },
                            { to: '/admin/users',    icon: '👥', title: 'Manage Users',  desc: 'View all users',       color: '#3b82f6' },
                            { to: '/admin/feedback', icon: '⭐', title: 'Feedback',      desc: 'View event ratings',   color: '#f59e0b' },
                            { to: '/admin/events',   icon: '📊', title: 'Event Stats',   desc: 'View statistics',      color: '#7c3aed' },
                        ].map((a) => (
                            <Link
                                key={a.title}
                                to={a.to}
                                className="flex flex-col gap-2 p-5 rounded-xl border-2 border-dashed
                           hover:opacity-80 transition text-center"
                                style={{ borderColor: a.color + '44', background: a.color + '0a' }}
                            >
                                <span className="text-2xl">{a.icon}</span>
                                <p className="font-semibold text-gray-800 text-sm">{a.title}</p>
                                <p className="text-xs text-gray-500">{a.desc}</p>
                            </Link>
                        ))}
                    </div>
                </div>
            </div>
        </div>
    );
}