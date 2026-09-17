import { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import Navbar from '../../components/layout/Navbar';
import { getEventFeedbackSummary } from '../../api/feedbackApi';
import { getAllEvents } from '../../api/eventApi';

export default function FeedbackOverviewPage() {
    const [events, setEvents] = useState([]);
    const [selectedEventId, setSelectedEventId] = useState('');
    const [summary, setSummary] = useState(null);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        getAllEvents()
            .then((res) => setEvents(res.data?.data || []))
            .catch(() => {});
    }, []);

    useEffect(() => {
        if (!selectedEventId) return;
        setLoading(true);
        getEventFeedbackSummary(selectedEventId)
            .then((res) => setSummary(res.data?.data || null))
            .catch(() => { toast.error('No feedback found for this event'); setSummary(null); })
            .finally(() => setLoading(false));
    }, [selectedEventId]);

    const ratingLabels = ['', 'Poor', 'Fair', 'Good', 'Very Good', 'Excellent'];
    const ratingColors = ['', '#ef4444', '#f97316', '#f59e0b', '#3b82f6', '#16a34a'];

    return (
        <div className="min-h-screen" style={{ background: '#f8fafc' }}>
            <Navbar />
            <div className="max-w-5xl mx-auto px-8 py-10">
                <h1 className="text-3xl font-bold text-gray-800 mb-2">Feedback Overview</h1>
                <p className="text-gray-500 mb-8">View ratings and comments for each event</p>

                {/* Event Selector */}
                <div className="bg-white rounded-2xl shadow-sm p-6 mb-6">
                    <label className="block text-sm font-semibold text-gray-700 mb-2">
                        Select Event
                    </label>
                    <select
                        value={selectedEventId}
                        onChange={(e) => setSelectedEventId(e.target.value)}
                        className="w-full px-4 py-3 rounded-xl border-2 border-gray-200
                       focus:border-red-400 focus:outline-none text-sm"
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
                        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-red-500" />
                    </div>
                )}

                {summary && !loading && (
                    <>
                        {/* Average Rating */}
                        <div className="bg-white rounded-2xl shadow-sm p-8 mb-6 text-center">
                            <p className="text-7xl font-bold" style={{ color: '#f59e0b' }}>
                                {summary.averageRating?.toFixed(1) ?? '—'}
                            </p>
                            <div className="flex justify-center gap-1 mt-2">
                                {[1,2,3,4,5].map((s) => (
                                    <span
                                        key={s}
                                        style={{
                                            fontSize: '1.5rem',
                                            color: s <= Math.round(summary.averageRating) ? '#f59e0b' : '#d1d5db'
                                        }}
                                    >★</span>
                                ))}
                            </div>
                            <p className="text-gray-500 mt-2">
                                Based on {summary.totalFeedbacks} responses
                            </p>
                        </div>

                        {/* Rating Distribution */}
                        <div className="bg-white rounded-2xl shadow-sm p-8 mb-6">
                            <h2 className="text-lg font-bold text-gray-700 mb-6">Rating Breakdown</h2>
                            <div className="space-y-3">
                                {[5,4,3,2,1].map((star) => {
                                    const count = summary.ratingDistribution?.[star] || 0;
                                    const pct = summary.totalFeedbacks
                                        ? Math.round((count / summary.totalFeedbacks) * 100) : 0;
                                    return (
                                        <div key={star} className="flex items-center gap-4">
                      <span className="text-sm font-semibold w-20 text-gray-600">
                        {ratingLabels[star]}
                      </span>
                                            <div className="flex-1 bg-gray-100 rounded-full h-4">
                                                <div
                                                    className="h-4 rounded-full transition-all"
                                                    style={{ width: `${pct}%`, background: ratingColors[star] }}
                                                />
                                            </div>
                                            <span className="text-sm font-bold w-12 text-right" style={{ color: ratingColors[star] }}>
                        {count}
                      </span>
                                        </div>
                                    );
                                })}
                            </div>
                        </div>

                        {/* Comments */}
                        {summary.feedbacks?.length > 0 && (
                            <div className="bg-white rounded-2xl shadow-sm p-8">
                                <h2 className="text-lg font-bold text-gray-700 mb-4">Recent Comments</h2>
                                <div className="space-y-4">
                                    {summary.feedbacks.map((fb) => (
                                        <div
                                            key={fb.id}
                                            className="p-4 rounded-xl"
                                            style={{ background: '#fafafa' }}
                                        >
                                            <div className="flex items-center justify-between mb-2">
                                                <p className="font-semibold text-gray-800 text-sm">{fb.userName}</p>
                                                <div className="flex gap-0.5">
                                                    {[1,2,3,4,5].map((s) => (
                                                        <span
                                                            key={s}
                                                            style={{ color: s <= fb.rating ? '#f59e0b' : '#d1d5db', fontSize: '0.9rem' }}
                                                        >★</span>
                                                    ))}
                                                </div>
                                            </div>
                                            {fb.comment && (
                                                <p className="text-sm text-gray-600">{fb.comment}</p>
                                            )}
                                        </div>
                                    ))}
                                </div>
                            </div>
                        )}
                    </>
                )}
            </div>
        </div>
    );
}