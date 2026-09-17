import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import toast from 'react-hot-toast';
import Navbar from '../../components/layout/Navbar';
import { getMyRegistrations, cancelRegistration } from '../../api/registrationApi';

export default function MyRegistrationsPage() {
    const [registrations, setRegistrations] = useState([]);
    const [loading, setLoading] = useState(true);
    const [cancelling, setCancelling] = useState(null);

    const fetchRegistrations = () => {
        getMyRegistrations()
            .then((res) => setRegistrations(res.data?.data || []))
            .catch(() => toast.error('Failed to load registrations'))
            .finally(() => setLoading(false));
    };

    useEffect(() => { fetchRegistrations(); }, []);

    const handleCancel = async (id) => {
        if (!window.confirm('Cancel this registration?')) return;
        setCancelling(id);
        try {
            await cancelRegistration(id);
            toast.success('Registration cancelled');
            fetchRegistrations();
        } catch (err) {
            toast.error(err.response?.data?.message || 'Failed to cancel');
        } finally {
            setCancelling(null);
        }
    };

    const statusColor = {
        REGISTERED: { bg: '#dcfce7', text: '#16a34a' },
        ATTENDED:   { bg: '#dbeafe', text: '#2563eb' },
        CANCELLED:  { bg: '#fee2e2', text: '#dc2626' },
    };

    return (
        <div className="min-h-screen" style={{ background: '#f8fafc' }}>
            <Navbar />
            <div className="max-w-5xl mx-auto px-8 py-10">
                <h1 className="text-3xl font-bold text-gray-800 mb-2">My Registrations</h1>
                <p className="text-gray-500 mb-8">View your event tickets and QR codes</p>

                {loading ? (
                    <div className="flex justify-center py-20">
                        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-teal-500" />
                    </div>
                ) : registrations.length === 0 ? (
                    <div className="text-center py-20 text-gray-400">
                        <p className="text-5xl mb-4">🎟️</p>
                        <p className="text-xl font-semibold">No registrations yet</p>
                        <Link
                            to="/student/events"
                            className="mt-4 inline-block px-6 py-2 rounded-full text-white font-semibold text-sm"
                            style={{ background: '#2ecbc1' }}
                        >
                            Browse Events
                        </Link>
                    </div>
                ) : (
                    <div className="space-y-4">
                        {registrations.map((reg) => {
                            const sc = statusColor[reg.status] || statusColor.REGISTERED;
                            return (
                                <div
                                    key={reg.id}
                                    className="bg-white rounded-2xl shadow-sm p-6 flex items-center
                             justify-between gap-6"
                                >
                                    <div className="flex-1">
                                        <div className="flex items-center gap-3 mb-2">
                                            <h3 className="text-lg font-bold text-gray-800">
                                                {reg.eventTitle}
                                            </h3>
                                            <span
                                                className="text-xs font-bold px-3 py-1 rounded-full"
                                                style={{ background: sc.bg, color: sc.text }}
                                            >
                        {reg.status}
                      </span>
                                        </div>
                                        <p className="text-sm text-gray-500">
                                            📅 {reg.eventDate} &nbsp;|&nbsp; 📍 {reg.eventVenue}
                                        </p>
                                        <p className="text-xs text-gray-400 mt-1">
                                            Ticket ID: {reg.ticketId}
                                        </p>
                                    </div>

                                    <div className="flex items-center gap-3">
                                        {reg.status === 'REGISTERED' && (
                                            <>
                                                <Link
                                                    to={`/student/ticket/${reg.id}`}
                                                    className="px-4 py-2 rounded-full text-sm font-semibold
                                     text-white transition"
                                                    style={{ background: '#2ecbc1' }}
                                                >
                                                    View QR
                                                </Link>
                                                <button
                                                    onClick={() => handleCancel(reg.id)}
                                                    disabled={cancelling === reg.id}
                                                    className="px-4 py-2 rounded-full text-sm font-semibold
                                     border-2 border-red-300 text-red-500
                                     hover:bg-red-50 transition"
                                                >
                                                    {cancelling === reg.id ? 'Cancelling...' : 'Cancel'}
                                                </button>
                                            </>
                                        )}
                                        {reg.status === 'ATTENDED' && (
                                            <Link
                                                to="/student/certificates"
                                                className="px-4 py-2 rounded-full text-sm font-semibold
                                   text-white transition"
                                                style={{ background: '#f59e0b' }}
                                            >
                                                Get Certificate
                                            </Link>
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