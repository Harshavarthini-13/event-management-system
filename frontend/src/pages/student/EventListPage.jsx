import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import Navbar from '../../components/layout/Navbar';
import { getAllEvents } from '../../api/eventApi';
import { registerForEvent } from '../../api/registrationApi';

export default function EventListPage() {
    const [events, setEvents] = useState([]);
    const [loading, setLoading] = useState(true);
    const [registering, setRegistering] = useState(null);
    const navigate = useNavigate();

    useEffect(() => {
        getAllEvents()
            .then((res) => setEvents(res.data?.data || []))
            .catch(() => toast.error('Failed to load events'))
            .finally(() => setLoading(false));
    }, []);

    const handleRegister = async (eventId) => {
        setRegistering(eventId);
        try {
            await registerForEvent(eventId);
            toast.success('Registered successfully! Check your tickets for QR code.');
            navigate('/student/registrations');
        } catch (err) {
            toast.error(err.response?.data?.message || 'Registration failed');
        } finally {
            setRegistering(null);
        }
    };

    const statusColor = {
        UPCOMING: { bg: '#dcfce7', text: '#16a34a' },
        ONGOING:  { bg: '#dbeafe', text: '#2563eb' },
        COMPLETED:{ bg: '#f3f4f6', text: '#6b7280' },
        CANCELLED:{ bg: '#fee2e2', text: '#dc2626' },
    };

    return (
        <div className="min-h-screen" style={{ background: '#f8fafc' }}>
            <Navbar />
            <div className="max-w-6xl mx-auto px-8 py-10">
                <h1 className="text-3xl font-bold text-gray-800 mb-2">All Events</h1>
                <p className="text-gray-500 mb-8">Browse and register for upcoming events</p>

                {loading ? (
                    <div className="flex justify-center py-20">
                        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-teal-500" />
                    </div>
                ) : events.length === 0 ? (
                    <div className="text-center py-20 text-gray-400">
                        <p className="text-5xl mb-4">📅</p>
                        <p className="text-xl font-semibold">No events available</p>
                    </div>
                ) : (
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                        {events.map((event) => {
                            const sc = statusColor[event.status] || statusColor.UPCOMING;
                            return (
                                <div
                                    key={event.id}
                                    className="bg-white rounded-2xl shadow-sm hover:shadow-md transition overflow-hidden"
                                >
                                    {/* Top color bar */}
                                    <div className="h-2" style={{ background: '#2ecbc1' }} />
                                    <div className="p-6">
                                        {/* Status badge */}
                                        <span
                                            className="text-xs font-bold px-3 py-1 rounded-full"
                                            style={{ background: sc.bg, color: sc.text }}
                                        >
                      {event.status}
                    </span>

                                        <h3 className="text-lg font-bold text-gray-800 mt-3 mb-1">
                                            {event.title}
                                        </h3>
                                        <p className="text-sm text-gray-500 mb-4 line-clamp-2">
                                            {event.description}
                                        </p>

                                        <div className="space-y-1 text-sm text-gray-600 mb-5">
                                            <p>📅 {event.eventDate} at {event.eventTime}</p>
                                            <p>📍 {event.venue}</p>
                                            <p>👤 {event.organizerName}</p>
                                            <p>
                                                🎟️ {event.registrationCount}/{event.capacity} registered
                                            </p>
                                        </div>

                                        {event.status === 'UPCOMING' && (
                                            <button
                                                onClick={() => handleRegister(event.id)}
                                                disabled={
                                                    registering === event.id ||
                                                    event.registrationCount >= event.capacity
                                                }
                                                className="w-full py-2.5 rounded-full text-sm font-bold text-white
                                   transition flex items-center justify-center gap-2"
                                                style={{
                                                    background:
                                                        event.registrationCount >= event.capacity
                                                            ? '#d1d5db'
                                                            : registering === event.id
                                                                ? '#9ca3af'
                                                                : '#2ecbc1',
                                                }}
                                            >
                                                {registering === event.id ? (
                                                    <>
                            <span className="h-4 w-4 border-2 border-white
                                             border-t-transparent rounded-full animate-spin" />
                                                        Registering...
                                                    </>
                                                ) : event.registrationCount >= event.capacity ? (
                                                    'Full'
                                                ) : (
                                                    'Register Now'
                                                )}
                                            </button>
                                        )}
                                    </div>
                                </div>
                            );
                        })}
                    </div>
                )}
            </div>
        </div>
    );
}