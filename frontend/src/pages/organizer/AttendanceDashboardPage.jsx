import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import toast from 'react-hot-toast';
import Navbar from '../../components/layout/Navbar';
import { attendanceApi } from '../../api/attendanceApi';
import { getAllEvents } from '../../api/eventApi';

export default function AttendanceDashboardPage() {
    const [searchParams] = useSearchParams();
    const [events, setEvents] = useState([]);
    const [selectedEventId, setSelectedEventId] = useState(
        searchParams.get('eventId') || ''
    );
    const [dashboard, setDashboard] = useState(null);
    const [attendance, setAttendance] = useState([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        getAllEvents()
            .then((res) => setEvents(res.data?.data || []))
            .catch(() => {});
    }, []);

    useEffect(() => {
        if (!selectedEventId) return;
        setLoading(true);
        Promise.all([
            attendanceApi.getDashboard(selectedEventId),
            attendanceApi.getByEvent(selectedEventId),
        ])
            .then(([dashRes, attRes]) => {
                setDashboard(dashRes.data?.data || null);
                setAttendance(attRes.data?.data || []);
            })
            .catch(() => toast.error('Failed to load attendance data'))
            .finally(() => setLoading(false));
    }, [selectedEventId]);
    console.log('Dashboard data:', dashboard);
    const attendancePercent = dashboard?.attendancePercentage || 0;
    return (
        <div className="min-h-screen" style={{ background: '#f8fafc' }}>
            <Navbar />
            <div className="max-w-6xl mx-auto px-8 py-10">
                <h1 className="text-3xl font-bold text-gray-800 mb-2">Attendance Dashboard</h1>
                <p className="text-gray-500 mb-8">Monitor live attendance for your events</p>

                {/* Event Selector */}
                <div className="bg-white rounded-2xl shadow-sm p-6 mb-6">
                    <label className="block text-sm font-semibold text-gray-700 mb-2">
                        Select Event
                    </label>
                    <select
                        value={selectedEventId}
                        onChange={(e) => setSelectedEventId(e.target.value)}
                        className="w-full px-4 py-3 rounded-xl border-2 border-gray-200
                       focus:border-purple-400 focus:outline-none text-sm"
                    >
                        <option value="">-- Choose an event --</option>
                        {events.map((e) => (
                            <option key={e.id} value={e.id}>
                                {e.title} — {e.eventDate}
                            </option>
                        ))}
                    </select>
                </div>

                {loading && (
                    <div className="flex justify-center py-12">
                        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-500" />
                    </div>
                )}

                {dashboard && !loading && (
                    <>
                        {/* Stats */}
                        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
                            {[
                                { label: 'Total Registered', value: dashboard.totalRegistered, color: '#3b82f6' },
                                { label: 'Attended', value: dashboard.totalCheckedIn, color: '#16a34a' },
                                { label: 'Not Yet Arrived', value: dashboard.remaining, color: '#f59e0b' },
                            ].map((s) => (
                                <div key={s.label} className="bg-white rounded-2xl p-6 shadow-sm">
                                    <p className="text-3xl font-bold" style={{ color: s.color }}>
                                        {s.value}
                                    </p>
                                    <p className="text-sm text-gray-600 mt-1">{s.label}</p>
                                </div>
                            ))}
                        </div>

                        {/* Progress */}
                        <div className="bg-white rounded-2xl p-6 shadow-sm mb-6">
                            <div className="flex items-center justify-between mb-3">
                                <p className="font-semibold text-gray-700">Attendance Rate</p>
                                <p className="text-2xl font-bold" style={{ color: '#7c3aed' }}>
                                    {attendancePercent}%
                                </p>
                            </div>
                            <div className="w-full bg-gray-100 rounded-full h-4">
                                <div
                                    className="h-4 rounded-full transition-all duration-700"
                                    style={{
                                        width: `${attendancePercent}%`,
                                        background: 'linear-gradient(90deg, #7c3aed, #2ecbc1)',
                                    }}
                                />
                            </div>
                        </div>

                        {/* Attendance List */}
                        <div className="bg-white rounded-2xl p-6 shadow-sm">
                            <h2 className="text-lg font-bold text-gray-700 mb-4">
                                Attendance Records ({attendance.length})
                            </h2>
                            {attendance.length === 0 ? (
                                <p className="text-gray-400 text-center py-8">
                                    No attendance records yet
                                </p>
                            ) : (
                                <div className="overflow-x-auto">
                                    <table className="w-full text-sm">
                                        <thead>
                                        <tr className="border-b border-gray-100">
                                            <th className="text-left py-3 px-4 text-gray-500 font-semibold">#</th>
                                            <th className="text-left py-3 px-4 text-gray-500 font-semibold">Student</th>
                                            <th className="text-left py-3 px-4 text-gray-500 font-semibold">Email</th>
                                            <th className="text-left py-3 px-4 text-gray-500 font-semibold">Scanned At</th>
                                            <th className="text-left py-3 px-4 text-gray-500 font-semibold">Status</th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        {attendance.map((a, i) => (
                                            <tr
                                                key={a.id}
                                                className="border-b border-gray-50 hover:bg-gray-50 transition"
                                            >
                                                <td className="py-3 px-4 text-gray-400">{i + 1}</td>
                                                <td className="py-3 px-4 font-semibold text-gray-800">
                                                    {a.studentName}
                                                </td>
                                                <td className="py-3 px-4 text-gray-500">{a.studentEmail}</td>
                                                <td className="py-3 px-4 text-gray-500">
                                                    {new Date(a.scannedAt).toLocaleTimeString()}
                                                </td>
                                                <td className="py-3 px-4">
                            <span
                                className="text-xs font-bold px-2 py-1 rounded-full"
                                style={{
                                    background: a.isValid ? '#dcfce7' : '#fee2e2',
                                    color: a.isValid ? '#16a34a' : '#dc2626',
                                }}
                            >
                              {a.isValid ? 'Valid' : 'Invalid'}
                            </span>
                                                </td>
                                            </tr>
                                        ))}
                                        </tbody>
                                    </table>
                                </div>
                            )}
                        </div>
                    </>
                )}
            </div>
        </div>
    );
}