import { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import Navbar from '../../components/layout/Navbar';
import { getAllUsers, deactivateUser } from '../../api/userApi';

export default function UserManagementPage() {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [filter, setFilter] = useState('ALL');
    const [deactivating, setDeactivating] = useState(null);

    const fetchUsers = () => {
        getAllUsers()
            .then((res) => setUsers(res.data?.data || []))
            .finally(() => setLoading(false));
    };

    useEffect(() => { fetchUsers(); }, []);

    const handleDeactivate = async (id) => {
        if (!window.confirm('Deactivate this user?')) return;
        setDeactivating(id);
        try {
            await deactivateUser(id);
            toast.success('User deactivated');
            fetchUsers();
        } catch (err) {
            toast.error(err.response?.data?.message || 'Failed to deactivate');
        } finally {
            setDeactivating(null);
        }
    };

    const filtered = filter === 'ALL'
        ? users
        : users.filter((u) => u.role === filter);

    const roleColor = {
        ADMIN:     { bg: '#fee2e2', text: '#dc2626' },
        ORGANIZER: { bg: '#f5f3ff', text: '#7c3aed' },
        STUDENT:   { bg: '#dcfce7', text: '#16a34a' },
    };

    return (
        <div className="min-h-screen" style={{ background: '#f8fafc' }}>
            <Navbar />
            <div className="max-w-7xl mx-auto px-8 py-10">
                <h1 className="text-3xl font-bold text-gray-800 mb-2">User Management</h1>
                <p className="text-gray-500 mb-8">View and manage all registered users</p>

                <div className="flex gap-3 mb-6">
                    {['ALL', 'ADMIN', 'ORGANIZER', 'STUDENT'].map((f) => (
                        <button
                            key={f}
                            onClick={() => setFilter(f)}
                            className="px-5 py-2 rounded-full text-sm font-bold border-2 transition"
                            style={{
                                background: filter === f ? '#dc2626' : 'white',
                                color: filter === f ? 'white' : '#6b7280',
                                borderColor: filter === f ? '#dc2626' : '#e5e7eb',
                            }}
                        >
                            {f} ({f === 'ALL' ? users.length : users.filter((u) => u.role === f).length})
                        </button>
                    ))}
                </div>

                {loading ? (
                    <div className="flex justify-center py-20">
                        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-red-500" />
                    </div>
                ) : (
                    <div className="bg-white rounded-2xl shadow-sm overflow-hidden">
                        <table className="w-full text-sm">
                            <thead>
                            <tr style={{ background: '#fef2f2' }}>
                                {['#', 'Name', 'Email', 'Role', 'Status', 'Joined', 'Actions'].map((h) => (
                                    <th key={h} className="text-left py-4 px-5 font-semibold text-gray-600">
                                        {h}
                                    </th>
                                ))}
                            </tr>
                            </thead>
                            <tbody>
                            {filtered.map((u, i) => {
                                const rc = roleColor[u.role] || roleColor.STUDENT;
                                return (
                                    <tr key={u.id} className="border-b border-gray-50 hover:bg-gray-50 transition">
                                        <td className="py-4 px-5 text-gray-400">{i + 1}</td>
                                        <td className="py-4 px-5 font-semibold text-gray-800">{u.name}</td>
                                        <td className="py-4 px-5 text-gray-600">{u.email}</td>
                                        <td className="py-4 px-5">
                                            <span className="text-xs font-bold px-2 py-1 rounded-full" style={{ background: rc.bg, color: rc.text }}>
                                                {u.role}
                                            </span>
                                        </td>
                                        <td className="py-4 px-5">
                                            <span
                                                className="text-xs font-bold px-2 py-1 rounded-full"
                                                style={{
                                                    background: u.isActive ? '#dcfce7' : '#fee2e2',
                                                    color: u.isActive ? '#16a34a' : '#dc2626',
                                                }}
                                            >
                                                {u.isActive ? 'Active' : 'Inactive'}
                                            </span>
                                        </td>
                                        <td className="py-4 px-5 text-gray-500">
                                            {u.createdAt ? new Date(u.createdAt).toLocaleDateString() : '—'}
                                        </td>
                                        <td className="py-4 px-5">
                                            {u.isActive && u.role !== 'ADMIN' && (
                                                <button
                                                    onClick={() => handleDeactivate(u.id)}
                                                    disabled={deactivating === u.id}
                                                    className="px-3 py-1.5 rounded-lg text-xs font-bold text-white transition"
                                                    style={{ background: deactivating === u.id ? '#9ca3af' : '#ef4444' }}
                                                >
                                                    {deactivating === u.id ? '...' : 'Deactivate'}
                                                </button>
                                            )}
                                        </td>
                                    </tr>
                                );
                            })}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>
        </div>
    );
}