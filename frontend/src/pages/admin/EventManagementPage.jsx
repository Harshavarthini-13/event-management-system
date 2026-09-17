import { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import Navbar from '../../components/layout/Navbar';
import { getAllEvents, createEvent, updateEvent, deleteEvent } from '../../api/eventApi';
import { getOrganizers } from '../../api/userApi';

const EMPTY_FORM = {
    title: '',
    description: '',
    eventDate: '',
    endDate: '',
    eventTime: '',
    venue: '',
    capacity: 100,
    organizerId: '',
};

export default function EventManagementPage() {
    const [events, setEvents] = useState([]);
    const [organizers, setOrganizers] = useState([]);
    const [organizersLoading, setOrganizersLoading] = useState(true); // NEW
    const [loading, setLoading] = useState(true);
    const [showModal, setShowModal] = useState(false);
    const [editing, setEditing] = useState(null);
    const [formData, setFormData] = useState(EMPTY_FORM);
    const [saving, setSaving] = useState(false);

    const fetchEvents = () => {
        getAllEvents()
            .then((res) => setEvents(res.data?.data || []))
            .finally(() => setLoading(false));
    };

    // NEW: pulled out so we can re-fetch organizers from a button if needed
    const fetchOrganizers = () => {
        setOrganizersLoading(true);
        getOrganizers()
            .then((res) => {
                console.log('Organizers API raw response:', res.data); // NEW - debug log, remove later
                // NEW: handle multiple possible response shapes
                const raw = res.data;
                const list =
                    raw?.data?.organizers ||  // { data: { organizers: [...] } }
                    raw?.data ||               // { data: [...] }
                    raw?.organizers ||         // { organizers: [...] }
                    raw ||                     // [...] directly
                    [];
                setOrganizers(Array.isArray(list) ? list : []);
            })
            .catch((err) => {
                console.error('Organizers error:', err.response?.data || err.message); // IMPROVED logging
                toast.error('Failed to load organizers'); // NEW - now visible to you, not just console
            })
            .finally(() => setOrganizersLoading(false)); // NEW
    };

    useEffect(() => {
        fetchEvents();
        fetchOrganizers(); // CHANGED - now uses the named function above
    }, []);

    const openCreate = () => {
        setEditing(null);
        setFormData(EMPTY_FORM);
        setShowModal(true);
    };

    const openEdit = (event) => {
        setEditing(event.id);
        setFormData({
            title: event.title,
            description: event.description || '',
            eventDate: event.eventDate,
            endDate: event.endDate || '',
            eventTime: event.eventTime,
            venue: event.venue,
            capacity: event.capacity,
            organizerId: event.organizerId,
        });
        setShowModal(true);
    };

    const handleSave = async (e) => {
        e.preventDefault();

        // NEW: guard so you can't submit without picking an organizer
        if (!formData.organizerId) {
            toast.error('Please select an organizer');
            return;
        }

        // Guard: end date (if provided) cannot be before start date
        if (formData.endDate && formData.endDate < formData.eventDate) {
            toast.error('End date cannot be before the start date');
            return;
        }

        // Send endDate as null instead of empty string when not set
        const payload = {
            ...formData,
            endDate: formData.endDate || null,
        };

        setSaving(true);
        try {
            if (editing) {
                await updateEvent(editing, payload);
                toast.success('Event updated!');
            } else {
                await createEvent(payload);
                toast.success('Event created!');
            }
            setShowModal(false);
            fetchEvents();
        } catch (err) {
            toast.error(err.response?.data?.message || 'Failed to save event');
        } finally {
            setSaving(false);
        }
    };

    const handleDelete = async (id) => {
        if (!window.confirm('Delete this event?')) return;
        try {
            await deleteEvent(id);
            toast.success('Event deleted');
            fetchEvents();
        } catch (err) {
            toast.error(err.response?.data?.message || 'Failed to delete');
        }
    };

    const statusColor = {
        UPCOMING:  { bg: '#dcfce7', text: '#16a34a' },
        ONGOING:   { bg: '#dbeafe', text: '#2563eb' },
        COMPLETED: { bg: '#f3f4f6', text: '#6b7280' },
        CANCELLED: { bg: '#fee2e2', text: '#dc2626' },
    };

    return (
        <div className="min-h-screen" style={{ background: '#f8fafc' }}>
            <Navbar />
            <div className="max-w-7xl mx-auto px-8 py-10">

                <div className="flex items-center justify-between mb-8">
                    <div>
                        <h1 className="text-3xl font-bold text-gray-800">Event Management</h1>
                        <p className="text-gray-500 mt-1">Create and manage all events</p>
                    </div>
                    <button
                        onClick={openCreate}
                        className="px-6 py-3 rounded-full text-white font-bold transition"
                        style={{ background: '#dc2626' }}
                    >
                        + Create Event
                    </button>
                </div>

                {loading ? (
                    <div className="flex justify-center py-20">
                        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-red-500" />
                    </div>
                ) : events.length === 0 ? (
                    <div className="text-center py-20 text-gray-400">
                        <p className="text-5xl mb-4">📋</p>
                        <p className="text-xl font-semibold">No events yet</p>
                    </div>
                ) : (
                    <div className="bg-white rounded-2xl shadow-sm overflow-hidden">
                        <table className="w-full text-sm">
                            <thead>
                            <tr style={{ background: '#fef2f2' }}>
                                {['Title', 'Organizer', 'Date', 'Venue', 'Capacity', 'Status', 'Actions'].map((h) => (
                                    <th key={h} className="text-left py-4 px-5 font-semibold text-gray-600">
                                        {h}
                                    </th>
                                ))}
                            </tr>
                            </thead>
                            <tbody>
                            {events.map((event) => {
                                const sc = statusColor[event.status] || statusColor.UPCOMING;
                                return (
                                    <tr
                                        key={event.id}
                                        className="border-b border-gray-50 hover:bg-gray-50 transition"
                                    >
                                        <td className="py-4 px-5 font-semibold text-gray-800">
                                            {event.title}
                                        </td>
                                        <td className="py-4 px-5 text-gray-600">
                                            {event.organizerName}
                                        </td>
                                        <td className="py-4 px-5 text-gray-600">
                                            {event.endDate && event.endDate !== event.eventDate
                                                ? `${event.eventDate} – ${event.endDate}`
                                                : event.eventDate}
                                        </td>
                                        <td className="py-4 px-5 text-gray-600">
                                            {event.venue}
                                        </td>
                                        <td className="py-4 px-5 text-gray-600">
                                            {event.registrationCount}/{event.capacity}
                                        </td>
                                        <td className="py-4 px-5">
                        <span
                            className="text-xs font-bold px-2 py-1 rounded-full"
                            style={{ background: sc.bg, color: sc.text }}
                        >
                          {event.status}
                        </span>
                                        </td>
                                        <td className="py-4 px-5">
                                            <div className="flex gap-2">
                                                <button
                                                    onClick={() => openEdit(event)}
                                                    className="px-3 py-1.5 rounded-lg text-xs font-bold text-white"
                                                    style={{ background: '#3b82f6' }}
                                                >
                                                    Edit
                                                </button>
                                                <button
                                                    onClick={() => handleDelete(event.id)}
                                                    className="px-3 py-1.5 rounded-lg text-xs font-bold text-white"
                                                    style={{ background: '#ef4444' }}
                                                >
                                                    Delete
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                );
                            })}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>

            {/* ── Modal ── */}
            {showModal && (
                <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
                    <div
                        className="bg-white rounded-2xl w-full max-w-lg shadow-2xl flex flex-col"
                        style={{ maxHeight: '90vh' }}
                    >
                        {/* Modal Header */}
                        <div
                            className="px-8 py-5 text-white rounded-t-2xl flex-shrink-0"
                            style={{ background: '#dc2626' }}
                        >
                            <h2 className="text-xl font-bold">
                                {editing ? 'Edit Event' : 'Create New Event'}
                            </h2>
                        </div>

                        {/* Modal Body - scrollable */}
                        <div className="overflow-y-auto flex-1 px-8 py-6">
                            <form onSubmit={handleSave} className="space-y-4" id="event-form">

                                <input
                                    type="text"
                                    placeholder="Event Title"
                                    required
                                    value={formData.title}
                                    onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                                    className="w-full px-4 py-3 rounded-xl border-2 border-gray-200
                             focus:border-red-400 focus:outline-none text-sm"
                                />

                                <textarea
                                    placeholder="Description"
                                    value={formData.description}
                                    onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                                    rows={2}
                                    className="w-full px-4 py-3 rounded-xl border-2 border-gray-200
                             focus:border-red-400 focus:outline-none text-sm resize-none"
                                />

                                <div className="grid grid-cols-2 gap-3">
                                    <div>
                                        <label className="text-xs font-semibold text-gray-500 mb-1 block">
                                            Start Date
                                        </label>
                                        <input
                                            type="date"
                                            required
                                            value={formData.eventDate}
                                            onChange={(e) => setFormData({ ...formData, eventDate: e.target.value })}
                                            className="w-full px-4 py-3 rounded-xl border-2 border-gray-200
                                   focus:border-red-400 focus:outline-none text-sm"
                                        />
                                    </div>
                                    <div>
                                        <label className="text-xs font-semibold text-gray-500 mb-1 block">
                                            End Date (optional)
                                        </label>
                                        <input
                                            type="date"
                                            value={formData.endDate}
                                            min={formData.eventDate || undefined}
                                            onChange={(e) => setFormData({ ...formData, endDate: e.target.value })}
                                            className="w-full px-4 py-3 rounded-xl border-2 border-gray-200
                                   focus:border-red-400 focus:outline-none text-sm"
                                        />
                                    </div>
                                </div>

                                <input
                                    type="time"
                                    required
                                    value={formData.eventTime}
                                    onChange={(e) => setFormData({ ...formData, eventTime: e.target.value })}
                                    className="w-full px-4 py-3 rounded-xl border-2 border-gray-200
                             focus:border-red-400 focus:outline-none text-sm"
                                />

                                <input
                                    type="text"
                                    placeholder="Venue"
                                    required
                                    value={formData.venue}
                                    onChange={(e) => setFormData({ ...formData, venue: e.target.value })}
                                    className="w-full px-4 py-3 rounded-xl border-2 border-gray-200
                             focus:border-red-400 focus:outline-none text-sm"
                                />

                                <div className="grid grid-cols-2 gap-3">
                                    <input
                                        type="number"
                                        placeholder="Capacity"
                                        min={1}
                                        required
                                        value={formData.capacity}
                                        onChange={(e) => setFormData({ ...formData, capacity: Number(e.target.value) })}
                                        className="w-full px-4 py-3 rounded-xl border-2 border-gray-200
                               focus:border-red-400 focus:outline-none text-sm"
                                    />

                                    {/* NEW: select now shows loading / empty states instead of just being blank */}
                                    <select
                                        required
                                        value={formData.organizerId}
                                        onChange={(e) => setFormData({ ...formData, organizerId: Number(e.target.value) })}
                                        disabled={organizersLoading} // NEW
                                        className="w-full px-4 py-3 rounded-xl border-2 border-gray-200
                               focus:border-red-400 focus:outline-none text-sm disabled:bg-gray-100"
                                    >
                                        <option value="">
                                            {organizersLoading
                                                ? 'Loading organizers...'
                                                : organizers.length === 0
                                                    ? 'No organizers found'
                                                    : 'Select Organizer'}
                                        </option>
                                        {organizers.map((o) => (
                                            // NEW: fallback to o.id in case backend uses "id" instead of "userId"
                                            <option key={o.userId ?? o.id} value={o.userId ?? o.id}>
                                                {o.name}
                                            </option>
                                        ))}
                                    </select>
                                </div>

                            </form>
                        </div>

                        {/* Modal Footer - always visible */}
                        <div
                            className="px-8 py-5 flex gap-3 flex-shrink-0 border-t border-gray-100"
                            style={{ background: 'white', borderRadius: '0 0 1rem 1rem' }}
                        >
                            <button
                                type="button"
                                onClick={() => setShowModal(false)}
                                className="flex-1 py-3 rounded-full border-2 border-gray-300
                           text-gray-600 font-bold text-sm hover:bg-gray-50 transition"
                            >
                                Cancel
                            </button>
                            <button
                                type="submit"
                                form="event-form"
                                disabled={saving}
                                className="flex-1 py-3 rounded-full text-white font-bold text-sm transition"
                                style={{ background: saving ? '#9ca3af' : '#dc2626' }}
                            >
                                {saving ? 'Saving...' : editing ? 'Update' : 'Create'}
                            </button>
                        </div>

                    </div>
                </div>
            )}
        </div>
    );
}