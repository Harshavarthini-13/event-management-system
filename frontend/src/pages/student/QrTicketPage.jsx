import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import toast from 'react-hot-toast';
import Navbar from '../../components/layout/Navbar';
import { getMyRegistrations } from '../../api/registrationApi';

export default function QrTicketPage() {
    const { id } = useParams();
    const [registration, setRegistration] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        getMyRegistrations()
            .then((res) => {
                const regs = res.data?.data || [];
                const found = regs.find((r) => String(r.id) === String(id));
                setRegistration(found || null);
            })
            .catch(() => toast.error('Failed to load ticket'))
            .finally(() => setLoading(false));
    }, [id]);

    const handleDownload = async () => {
        if (!registration?.qrCodeUrl) return;
        try {
            const response = await fetch(registration.qrCodeUrl);
            const blob = await response.blob();
            const url = window.URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.href = url;
            link.download = `ticket-${registration.ticketId}.png`;
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            window.URL.revokeObjectURL(url);
            toast.success('QR code downloaded!');
        } catch (err) {
            window.open(registration.qrCodeUrl, '_blank');
            toast.success('QR code opened — right click and save!');
        }
    };

    if (loading) {
        return (
            <div className="min-h-screen" style={{ background: '#f8fafc' }}>
                <Navbar />
                <div className="flex justify-center py-20">
                    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-teal-500" />
                </div>
            </div>
        );
    }

    if (!registration) {
        return (
            <div className="min-h-screen" style={{ background: '#f8fafc' }}>
                <Navbar />
                <div className="text-center py-20">
                    <p className="text-5xl mb-4">❌</p>
                    <p className="text-xl font-semibold text-gray-600">Ticket not found</p>
                    <Link to="/student/registrations"
                          className="mt-4 inline-block text-teal-500 hover:underline text-sm">
                        ← Back to Registrations
                    </Link>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen" style={{ background: '#f8fafc' }}>
            <Navbar />
            <div className="max-w-lg mx-auto px-8 py-10">
                <Link to="/student/registrations"
                      className="text-sm text-teal-500 hover:underline mb-6 inline-block">
                    ← Back to Registrations
                </Link>

                {/* Ticket Card */}
                <div className="bg-white rounded-2xl shadow-lg overflow-hidden">
                    {/* Header */}
                    <div
                        className="px-8 py-6 text-white"
                        style={{ background: 'linear-gradient(135deg, #2ecbc1, #3b82f6)' }}
                    >
                        <p className="text-xs font-bold uppercase tracking-widest opacity-80 mb-1">
                            Event Ticket
                        </p>
                        <h2 className="text-2xl font-bold">{registration.eventTitle}</h2>
                        <p className="text-sm opacity-90 mt-1">
                            {registration.eventDate} &nbsp;|&nbsp; {registration.eventVenue}
                        </p>
                    </div>

                    {/* QR Code */}
                    <div className="px-8 py-8 flex flex-col items-center">
                        {registration.qrCodeUrl ? (
                            <img
                                src={registration.qrCodeUrl}
                                alt="QR Code"
                                className="w-56 h-56 rounded-xl border-4 border-gray-100 shadow"
                                onError={(e) => { e.target.style.display = 'none'; }}
                            />
                        ) : (
                            <div
                                className="w-56 h-56 rounded-xl flex items-center justify-center"
                                style={{ background: '#f3f4f6' }}
                            >
                                <p className="text-gray-400 text-sm text-center px-4">
                                    QR code not available
                                </p>
                            </div>
                        )}
                        <p className="text-xs text-gray-400 mt-4 text-center">
                            Show this QR code at the event entrance
                        </p>

                        {/* Download Button */}
                        {registration.qrCodeUrl && (
                            <button
                                onClick={handleDownload}
                                className="mt-4 px-6 py-2.5 rounded-xl text-white font-bold text-sm transition"
                                style={{ background: 'linear-gradient(135deg, #2ecbc1, #3b82f6)' }}
                            >
                                ⬇ Download QR Code
                            </button>
                        )}
                    </div>

                    {/* Ticket Details */}
                    <div className="px-8 pb-8 space-y-3">
                        <div className="flex justify-between text-sm">
                            <span className="text-gray-500">Attendee</span>
                            <span className="font-semibold text-gray-800">{registration.userName}</span>
                        </div>
                        <div className="flex justify-between text-sm">
                            <span className="text-gray-500">Ticket ID</span>
                            <span className="font-mono text-xs text-gray-600">{registration.ticketId}</span>
                        </div>
                        <div className="flex justify-between text-sm">
                            <span className="text-gray-500">Status</span>
                            <span
                                className="font-bold"
                                style={{
                                    color: registration.status === 'ATTENDED' ? '#16a34a' :
                                        registration.status === 'CANCELLED' ? '#dc2626' : '#2ecbc1'
                                }}
                            >
                                {registration.status}
                            </span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}