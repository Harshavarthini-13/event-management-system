import { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import Navbar from '../../components/layout/Navbar';
import { getMyCertificates, generateCertificate } from '../../api/certificateApi';
import { getMyRegistrations } from '../../api/registrationApi';

export default function CertificatesPage() {
    const [certificates, setCertificates] = useState([]);
    const [attendedRegs, setAttendedRegs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [generating, setGenerating] = useState(null);

    const fetchAll = async () => {
        try {
            const [certsRes, regsRes] = await Promise.all([
                getMyCertificates(),
                getMyRegistrations(),
            ]);
            setCertificates(certsRes.data?.data || []);
            const regs = regsRes.data?.data || [];
            setAttendedRegs(regs.filter((r) => r.status === 'ATTENDED'));
        } catch (_) {
            toast.error('Failed to load certificates');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
         fetchAll().catch(console.error);
        }, []);

    const handleGenerate = async (registrationId) => {
        setGenerating(registrationId);
        try {
            await generateCertificate(registrationId);
            toast.success('Certificate generated!');
            fetchAll();
        } catch (err) {
            toast.error(err.response?.data?.message || 'Failed to generate certificate');
        } finally {
            setGenerating(null);
        }
    };

    const certRegIds = new Set(certificates.map((c) => c.registrationId));

    return (
        <div className="min-h-screen" style={{ background: '#f8fafc' }}>
            <Navbar />
            <div className="max-w-5xl mx-auto px-8 py-10">
                <h1 className="text-3xl font-bold text-gray-800 mb-2">Certificates</h1>
                <p className="text-gray-500 mb-8">Download your event completion certificates</p>

                {loading ? (
                    <div className="flex justify-center py-20">
                        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-yellow-500" />
                    </div>
                ) : (
                    <>
                        {/* Available to generate */}
                        {attendedRegs.filter((r) => !certRegIds.has(r.id)).length > 0 && (
                            <div className="mb-8">
                                <h2 className="text-lg font-bold text-gray-700 mb-4">
                                    Ready to Generate
                                </h2>
                                <div className="space-y-3">
                                    {attendedRegs
                                        .filter((r) => !certRegIds.has(r.id))
                                        .map((reg) => (
                                            <div
                                                key={reg.id}
                                                className="bg-white rounded-2xl shadow-sm p-5 flex
                                   items-center justify-between"
                                            >
                                                <div>
                                                    <p className="font-semibold text-gray-800">{reg.eventTitle}</p>
                                                    <p className="text-sm text-gray-500">{reg.eventDate}</p>
                                                </div>
                                                <button
                                                    onClick={() => handleGenerate(reg.id)}
                                                    disabled={generating === reg.id}
                                                    className="px-5 py-2 rounded-full text-sm font-bold
                                     text-white transition"
                                                    style={{ background: generating === reg.id ? '#9ca3af' : '#f59e0b' }}
                                                >
                                                    {generating === reg.id ? 'Generating...' : 'Generate'}
                                                </button>
                                            </div>
                                        ))}
                                </div>
                            </div>
                        )}

                        {/* Existing certificates */}
                        {certificates.length === 0 && attendedRegs.length === 0 ? (
                            <div className="text-center py-20 text-gray-400">
                                <p className="text-5xl mb-4">🏆</p>
                                <p className="text-xl font-semibold">No certificates yet</p>
                                <p className="text-sm mt-2">Attend events to earn certificates</p>
                            </div>
                        ) : (
                            <div>
                                <h2 className="text-lg font-bold text-gray-700 mb-4">My Certificates</h2>
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                                    {certificates.map((cert) => (
                                        <div
                                            key={cert.id}
                                            className="bg-white rounded-2xl shadow-sm overflow-hidden"
                                        >
                                            <div
                                                className="h-3"
                                                style={{ background: 'linear-gradient(90deg, #f59e0b, #ef4444)' }}
                                            />
                                            <div className="p-6">
                                                <div className="flex items-start justify-between mb-3">
                                                    <span className="text-3xl">🏆</span>
                                                    <span
                                                        className="text-xs font-bold px-2 py-1 rounded-full"
                                                        style={{ background: '#fef3c7', color: '#92400e' }}
                                                    >
                            Certified
                          </span>
                                                </div>
                                                <h3 className="font-bold text-gray-800 text-lg">
                                                    {cert.eventTitle}
                                                </h3>
                                                <p className="text-xs text-gray-400 mt-1 font-mono">
                                                    #{cert.certificateNumber}
                                                </p>
                                                <p className="text-sm text-gray-500 mt-1">
                                                    Issued: {new Date(cert.issuedAt).toLocaleDateString()}
                                                </p>
                                                {cert.pdfUrl && (
                                                    <button
                                                        onClick={async () => {
                                                            try {
                                                                const token = JSON.parse(localStorage.getItem('user'))?.token;
                                                                const response = await fetch(cert.pdfUrl, {
                                                                    headers: { Authorization: `Bearer ${token}` }
                                                                });
                                                                const blob = await response.blob();
                                                                const url = window.URL.createObjectURL(blob);
                                                                const link = document.createElement('a');
                                                                link.href = url;
                                                                link.download = `certificate-${cert.certificateNumber}.pdf`;
                                                                document.body.appendChild(link);
                                                                link.click();
                                                                document.body.removeChild(link);
                                                                window.URL.revokeObjectURL(url);
                                                                toast.success('Certificate downloaded!');
                                                            } catch (err) {
                                                                toast.error('Failed to download certificate');
                                                            }
                                                        }}
                                                        className="mt-4 w-full py-2 rounded-full text-sm font-bold text-white transition"
                                                        style={{ background: '#f59e0b' }}
                                                    >
                                                        Download PDF
                                                    </button>
                                                )}
                                            </div>
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