import { useEffect, useRef, useState } from 'react';
import toast from 'react-hot-toast';
import Navbar from '../../components/layout/Navbar';
import { attendanceApi } from '../../api/attendanceApi';
import axiosClient from '../../api/axiosClient';

export default function QrScannerPage() {
    const [scanning, setScanning] = useState(false);
    const [result, setResult] = useState(null);
    const [manualTicket, setManualTicket] = useState('');
    const [submitting, setSubmitting] = useState(false);
    const [events, setEvents] = useState([]);
    const [selectedEventId, setSelectedEventId] = useState('');
    const html5QrCodeRef = useRef(null);

    useEffect(() => {
        axiosClient.get('/events/my-events')
            .then((res) => {
                const list = res.data?.data || [];
                setEvents(list);
                if (list.length > 0) setSelectedEventId(String(list[0].id));
            })
            .catch(() => {
                axiosClient.get('/events')
                    .then((res) => {
                        const list = res.data?.data || [];
                        setEvents(list);
                        if (list.length > 0) setSelectedEventId(String(list[0].id));
                    })
                    .catch(() => toast.error('Failed to load events'));
            });
    }, []);

    const startScanner = async () => {
        if (!selectedEventId) {
            toast.error('Please select an event first');
            return;
        }
        const { Html5QrcodeScanner } = await import('html5-qrcode');
        if (html5QrCodeRef.current) return;

        html5QrCodeRef.current = new Html5QrcodeScanner(
            'qr-reader',
            { fps: 10, qrbox: { width: 250, height: 250 } },
            false
        );

        html5QrCodeRef.current.render(
            async (decodedText) => {
                await stopScanner();
                await handleScan(decodedText);
            },
            () => {}
        );
        setScanning(true);
    };

    const stopScanner = async () => {
        if (html5QrCodeRef.current) {
            try {
                await html5QrCodeRef.current.clear();
            } catch (_) {}
            html5QrCodeRef.current = null;
        }
        setScanning(false);
    };

    useEffect(() => {
        return () => { stopScanner(); };
    }, []);

    const handleScan = async (ticketId) => {
        if (!selectedEventId) {
            toast.error('Please select an event first');
            return;
        }
        setSubmitting(true);
        try {
            const res = await attendanceApi.scanQr(ticketId, Number(selectedEventId));
            setResult({ success: true, data: res.data?.data });
            toast.success('Attendance marked successfully!');
        } catch (err) {
            setResult({ success: false, message: err.response?.data?.message || 'Invalid QR code' });
            toast.error(err.response?.data?.message || 'Scan failed');
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <div className="min-h-screen" style={{ background: '#f8fafc' }}>
            <Navbar />
            <div className="max-w-2xl mx-auto px-8 py-10">
                <h1 className="text-3xl font-bold text-gray-800 mb-2">QR Scanner</h1>
                <p className="text-gray-500 mb-8">Scan student QR codes to mark attendance</p>

                {/* Event Selector */}
                <div className="bg-white rounded-2xl shadow-sm p-6 mb-6">
                    <h2 className="text-lg font-bold text-gray-700 mb-3">Select Event</h2>
                    <select
                        value={selectedEventId}
                        onChange={(e) => setSelectedEventId(e.target.value)}
                        className="w-full px-4 py-2.5 rounded-xl border-2 border-gray-200
                                   focus:border-purple-400 focus:outline-none text-sm"
                    >
                        <option value="">-- Select an event --</option>
                        {events.map((ev) => (
                            <option key={ev.id} value={ev.id}>
                                {ev.title} — {ev.eventDate}
                            </option>
                        ))}
                    </select>
                </div>

                {/* Scanner Box */}
                <div className="bg-white rounded-2xl shadow-sm p-8 mb-6">
                    {!scanning && !result && (
                        <div className="text-center py-8">
                            <div
                                className="w-24 h-24 rounded-2xl flex items-center justify-center
                                           text-5xl mx-auto mb-4"
                                style={{ background: '#f5f3ff' }}
                            >
                                📷
                            </div>
                            <p className="text-gray-600 mb-6">
                                Click to start camera and scan a student's QR code
                            </p>
                            <button
                                onClick={startScanner}
                                className="px-8 py-3 rounded-full text-white font-bold transition"
                                style={{ background: '#7c3aed' }}
                            >
                                Start Camera
                            </button>
                        </div>
                    )}

                    <div id="qr-reader" className={scanning ? 'block' : 'hidden'} />

                    {scanning && (
                        <div className="text-center mt-4">
                            <button
                                onClick={stopScanner}
                                className="px-6 py-2 rounded-full border-2 border-gray-300
                                           text-gray-600 font-semibold text-sm hover:bg-gray-50 transition"
                            >
                                Stop Camera
                            </button>
                        </div>
                    )}

                    {result && (
                        <div className="text-center py-6">
                            <div className="text-6xl mb-4">
                                {result.success ? '✅' : '❌'}
                            </div>
                            {result.success ? (
                                <div>
                                    <p className="text-xl font-bold text-green-600 mb-2">
                                        Attendance Marked!
                                    </p>
                                    <p className="text-gray-600 font-semibold">{result.data?.studentName}</p>
                                    <p className="text-sm text-gray-500">{result.data?.eventTitle}</p>
                                    <p className="text-xs text-gray-400 mt-1">
                                        Scanned at: {result.data?.scannedAt}
                                    </p>
                                </div>
                            ) : (
                                <div>
                                    <p className="text-xl font-bold text-red-500 mb-2">Scan Failed</p>
                                    <p className="text-gray-500 text-sm">{result.message}</p>
                                </div>
                            )}
                            <button
                                onClick={() => { setResult(null); startScanner(); }}
                                className="mt-4 px-6 py-2 rounded-full text-white font-bold transition"
                                style={{ background: '#7c3aed' }}
                            >
                                Scan Next
                            </button>
                        </div>
                    )}
                </div>

                {/* Upload QR Image */}
                <div className="bg-white rounded-2xl shadow-sm p-6 mb-6">
                    <h2 className="text-lg font-bold text-gray-700 mb-4">Upload QR Image</h2>
                    <p className="text-sm text-gray-500 mb-4">
                        Upload a QR code image from your gallery
                    </p>
                    <input
                        type="file"
                        accept="image/*"
                        onChange={async (e) => {
                            const file = e.target.files[0];
                            if (!file) return;
                            const { Html5Qrcode } = await import('html5-qrcode');
                            const reader = new Html5Qrcode('qr-reader-file');
                            try {
                                const result = await reader.scanFile(file, true);
                                await handleScan(result);
                            } catch (err) {
                                toast.error('Could not read QR code from image');
                            }
                        }}
                        className="w-full px-4 py-2.5 rounded-xl border-2 border-dashed
                   border-gray-300 text-sm text-gray-500 cursor-pointer"
                    />
                    <div id="qr-reader-file" className="hidden" />
                </div>

                {/* Manual Entry */}
                <div className="bg-white rounded-2xl shadow-sm p-6">
                    <h2 className="text-lg font-bold text-gray-700 mb-4">Manual Ticket Entry</h2>
                    <p className="text-sm text-gray-500 mb-4">
                        Enter ticket ID manually if camera is not available
                    </p>
                    <div className="flex gap-3">
                        <input
                            type="text"
                            value={manualTicket}
                            onChange={(e) => setManualTicket(e.target.value)}
                            onKeyDown={(e) => {
                                if (e.key === 'Enter' && manualTicket.trim() && !submitting) {
                                    handleScan(manualTicket.trim());
                                    setManualTicket('');
                                }
                            }}
                            placeholder="Enter ticket UUID..."
                            className="flex-1 px-4 py-2.5 rounded-xl border-2 border-gray-200
                                       focus:border-purple-400 focus:outline-none text-sm"
                        />
                        <button
                            onClick={() => {
                                if (!manualTicket.trim() || submitting) return;
                                handleScan(manualTicket.trim());
                                setManualTicket('');
                            }}
                            disabled={submitting || !manualTicket.trim()}
                            className="px-6 py-2.5 rounded-xl text-white font-bold text-sm transition"
                            style={{ background: submitting ? '#9ca3af' : '#7c3aed' }}
                        >
                            {submitting ? 'Scanning...' : 'Submit'}
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}