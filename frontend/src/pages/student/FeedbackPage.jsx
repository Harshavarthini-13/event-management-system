import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import Navbar from '../../components/layout/Navbar';
import { submitFeedback } from '../../api/feedbackApi';

export default function FeedbackPage() {
    const { eventId } = useParams();
    const navigate = useNavigate();
    const [formData, setFormData] = useState({ rating: 0, comment: '' });
    const [hoveredRating, setHoveredRating] = useState(0);
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (formData.rating === 0) {
            toast.error('Please select a rating');
            return;
        }
        setLoading(true);
        try {
            await submitFeedback({ eventId: Number(eventId), ...formData });
            toast.success('Feedback submitted! Thank you.');
            navigate('/student/registrations');
        } catch (err) {
            toast.error(err.response?.data?.message || 'Failed to submit feedback');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen" style={{ background: '#f8fafc' }}>
            <Navbar />
            <div className="max-w-lg mx-auto px-8 py-10">
                <h1 className="text-3xl font-bold text-gray-800 mb-2">Submit Feedback</h1>
                <p className="text-gray-500 mb-8">Share your experience about this event</p>

                <div className="bg-white rounded-2xl shadow-sm p-8">
                    <form onSubmit={handleSubmit} className="space-y-6">
                        {/* Star Rating */}
                        <div>
                            <label className="block text-sm font-semibold text-gray-700 mb-3">
                                Your Rating
                            </label>
                            <div className="flex gap-2">
                                {[1, 2, 3, 4, 5].map((star) => (
                                    <button
                                        key={star}
                                        type="button"
                                        onClick={() => setFormData({ ...formData, rating: star })}
                                        onMouseEnter={() => setHoveredRating(star)}
                                        onMouseLeave={() => setHoveredRating(0)}
                                        className="text-4xl transition"
                                        style={{
                                            color: star <= (hoveredRating || formData.rating)
                                                ? '#f59e0b' : '#d1d5db',
                                        }}
                                    >
                                        ★
                                    </button>
                                ))}
                            </div>
                            {formData.rating > 0 && (
                                <p className="text-sm text-gray-500 mt-1">
                                    {['', 'Poor', 'Fair', 'Good', 'Very Good', 'Excellent'][formData.rating]}
                                </p>
                            )}
                        </div>

                        {/* Comment */}
                        <div>
                            <label className="block text-sm font-semibold text-gray-700 mb-2">
                                Comments (optional)
                            </label>
                            <textarea
                                value={formData.comment}
                                onChange={(e) => setFormData({ ...formData, comment: e.target.value })}
                                placeholder="Share your thoughts about the event..."
                                rows={4}
                                className="w-full px-4 py-3 rounded-xl border-2 border-gray-200
                           focus:border-teal-400 focus:outline-none text-sm resize-none"
                            />
                        </div>

                        <button
                            type="submit"
                            disabled={loading}
                            className="w-full py-3 rounded-full font-bold text-white transition
                         flex items-center justify-center gap-2"
                            style={{ background: loading ? '#9ca3af' : '#2ecbc1' }}
                        >
                            {loading ? (
                                <>
                  <span className="h-4 w-4 border-2 border-white
                                   border-t-transparent rounded-full animate-spin" />
                                    Submitting...
                                </>
                            ) : (
                                'Submit Feedback'
                            )}
                        </button>
                    </form>
                </div>
            </div>
        </div>
    );
}