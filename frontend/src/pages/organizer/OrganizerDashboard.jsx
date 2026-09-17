import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Navbar from '../../components/layout/Navbar';
import { useAuth } from '../../context/AuthContext';
import { getAllEvents } from '../../api/eventApi';

export default function OrganizerDashboard() {
    const { user } = useAuth();
    const [events, setEvents] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        getAllEvents()
            .then((res) => setEvents(res.data?.data || []))
            .finally(() => setLoading(false));
    }, []);

    const myEvents = events.filter((e) => e.organizerName === user?.name);
    const upcomingCount = myEvents.filter((e) => e.status === 'UPCOMING').length;
    const ongoingCount  = myEvents.filter((e) => e.status === 'ONGOING').length;

    return (
        <div className="min-h-screen" style={{ background: '#f8fafc' }}>
            <Navbar />
            <div className="max-w-6xl mx-auto px-8 py-10">
                <div className="mb-8">
                    <h1 className="text-3xl font-bold text-gray-800">
                        Welcome, {user?.name}! 👋
                    </h1>
                    <p className="text-gray-500 mt-1">Organizer Dashboard</p>
                </div>

                {/* Stats */}
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-10">
                    {[
                        { label: 'My Events', value: myEvents.length, color: '#7c3aed', icon: '📋' },
                        { label: 'Upcoming', value: upcomingCount, color: '#2ecbc1', icon: '📅' },
                        { label: 'Ongoing', value: ongoingCount, color: '#f59e0b', icon: '🔴' },
                    ].map((c) => (
                        <div key={c.label} className="bg-white rounded-2xl p-6 shadow-sm">
                            <div className="flex items-center justify-between mb-4">
                                <span className="text-3xl">{c.icon}</span>
                                <span className="text-3xl font-bold" style={{ color: c.color }}>
                  {c.value}
                </span>
                            </div>
                            <p className="text-sm font-semibold text-gray-600">{c.label}</p>
                        </div>
                    ))}
                </div>

                {/* Quick Actions */}
                <div className="bg-white rounded-2xl p-8 shadow-sm mb-8">
                    <h2 className="text-xl font-bold text-gray-800 mb-6">Quick Actions</h2>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <Link
                            to="/organizer/scan"
                            className="flex items-center gap-4 p-5 rounded-xl border-2 border-dashed
                         border-purple-200 hover:border-purple-400 hover:bg-purple-50 transition"
                        >
                            <span className="text-2xl">📷</span>
                            <div>
                                <p className="font-semibold text-gray-800">Scan QR Code</p>
                                <p className="text-xs text-gray-500">Mark student attendance</p>
                            </div>
                        </Link>
                        <Link
                            to="/organizer/attendance"
                            className="flex items-center gap-4 p-5 rounded-xl border-2 border-dashed
                         border-teal-200 hover:border-teal-400 hover:bg-teal-50 transition"
                        >
                            <span className="text-2xl">📊</span>
                            <div>
                                <p className="font-semibold text-gray-800">Attendance Dashboard</p>
                                <p className="text-xs text-gray-500">View live attendance stats</p>
                            </div>
                        </Link>
                    </div>
                </div>

                {/* My Events List */}
                <div className="bg-white rounded-2xl p-8 shadow-sm">
                    <h2 className="text-xl font-bold text-gray-800 mb-6">My Events</h2>
                    {loading ? (
                        <div className="flex justify-center py-8">
                            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-purple-500" />
                        </div>
                    ) : myEvents.length === 0 ? (
                        <p className="text-gray-400 text-center py-8">No events assigned to you yet.</p>
                    ) : (
                        <div className="space-y-3">
                            {myEvents.map((event) => (
                                <div
                                    key={event.id}
                                    className="flex items-center justify-between p-4 rounded-xl
                             border border-gray-100 hover:bg-gray-50 transition"
                                >
                                    <div>
                                        <p className="font-semibold text-gray-800">{event.title}</p>
                                        <p className="text-sm text-gray-500">
                                            {event.eventDate} | {event.venue}
                                        </p>
                                    </div>
                                    <div className="flex items-center gap-3">
                    <span
                        className="text-xs font-bold px-3 py-1 rounded-full"
                        style={{
                            background: event.status === 'UPCOMING' ? '#dcfce7' :
                                event.status === 'ONGOING' ? '#dbeafe' : '#f3f4f6',
                            color: event.status === 'UPCOMING' ? '#16a34a' :
                                event.status === 'ONGOING' ? '#2563eb' : '#6b7280',
                        }}
                    >
                      {event.status}
                    </span>
                                        <Link
                                            to={`/organizer/attendance?eventId=${event.id}`}
                                            className="text-xs font-semibold px-3 py-1 rounded-full text-white"
                                            style={{ background: '#7c3aed' }}
                                        >
                                            View Attendance
                                        </Link>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}