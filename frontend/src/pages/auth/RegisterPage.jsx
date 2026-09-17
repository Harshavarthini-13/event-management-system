import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import AuthTicketPanel from "./AuthTicketPanel";
// import { authApi } from "../api/authApi"; // wire this to your existing register call

export default function RegisterPage() {
    const navigate = useNavigate();
    const [form, setForm] = useState({ name: "", email: "", password: "", role: "STUDENT" });
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const handleChange = (e) => {
        setForm({ ...form, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError("");
        setLoading(true);
        try {
            // Replace with your real call, e.g.:
            // await authApi.register(form);
            // navigate("/login");
        } catch (err) {
            setError(err?.response?.data?.message || "Couldn't create your account. Try again.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen flex bg-[#FAFAF7]">
            <AuthTicketPanel
                eyebrow="EventHub"
                heading="Get your pass in under a minute."
                sub="Create an account to register for campus events and hold a digital ticket, ready to scan at the door."
                stat="24"
                statLabel="Events open for registration"
            />

            <div className="flex-1 flex items-center justify-center px-6 py-16">
                <div className="w-full max-w-[380px]">
                    <p className="font-mono text-[11px] tracking-[0.2em] uppercase text-[#C4626B]">
                        Get started
                    </p>
                    <h2
                        className="mt-3 text-[30px] text-[#171712]"
                        style={{ fontFamily: "'Fraunces', serif", fontWeight: 500 }}
                    >
                        Create your account
                    </h2>

                    <form onSubmit={handleSubmit} className="mt-10 space-y-5">
                        <div>
                            <label htmlFor="name" className="block text-[13px] text-[#6E6B60] mb-1.5">
                                Full name
                            </label>
                            <input
                                id="name"
                                name="name"
                                type="text"
                                required
                                placeholder="Your full name"
                                value={form.name}
                                onChange={handleChange}
                                className="w-full h-11 px-3.5 bg-transparent border border-[#E4E1D6] rounded-[6px] text-[15px] text-[#171712] placeholder:text-[#A6A38F] outline-none focus:border-[#C4626B] transition-colors"
                            />
                        </div>

                        <div>
                            <label htmlFor="email" className="block text-[13px] text-[#6E6B60] mb-1.5">
                                Email address
                            </label>
                            <input
                                id="email"
                                name="email"
                                type="email"
                                autoComplete="email"
                                required
                                placeholder="you@college.edu"
                                value={form.email}
                                onChange={handleChange}
                                className="w-full h-11 px-3.5 bg-transparent border border-[#E4E1D6] rounded-[6px] text-[15px] text-[#171712] placeholder:text-[#A6A38F] outline-none focus:border-[#C4626B] transition-colors"
                            />
                        </div>

                        <div>
                            <label htmlFor="password" className="block text-[13px] text-[#6E6B60] mb-1.5">
                                Password
                            </label>
                            <input
                                id="password"
                                name="password"
                                type="password"
                                autoComplete="new-password"
                                required
                                minLength={6}
                                placeholder="At least 6 characters"
                                value={form.password}
                                onChange={handleChange}
                                className="w-full h-11 px-3.5 bg-transparent border border-[#E4E1D6] rounded-[6px] text-[15px] text-[#171712] placeholder:text-[#A6A38F] outline-none focus:border-[#C4626B] transition-colors"
                            />
                        </div>

                        <div>
                            <label htmlFor="role" className="block text-[13px] text-[#6E6B60] mb-1.5">
                                I am a
                            </label>
                            <select
                                id="role"
                                name="role"
                                value={form.role}
                                onChange={handleChange}
                                className="w-full h-11 px-3.5 bg-transparent border border-[#E4E1D6] rounded-[6px] text-[15px] text-[#171712] outline-none focus:border-[#C4626B] transition-colors"
                            >
                                <option value="STUDENT">Student</option>
                                <option value="ORGANIZER">Organizer</option>
                            </select>
                        </div>

                        {error && (
                            <p className="text-[13px] text-[#C4626B] bg-[#FFDAD5] border border-[#F0B8B0] rounded-[6px] px-3 py-2">
                                {error}
                            </p>
                        )}

                        <button
                            type="submit"
                            disabled={loading}
                            className="w-full h-11 rounded-[6px] bg-[#171712] text-[#FAFAF7] text-[15px] font-medium hover:bg-[#2A2A22] transition-colors disabled:opacity-60"
                        >
                            {loading ? "Creating account..." : "Create account"}
                        </button>
                    </form>

                    <p className="mt-8 text-[14px] text-[#6E6B60] text-center">
                        Already have an account?{" "}
                        <Link to="/login" className="text-[#171712] font-medium hover:underline">
                            Sign in
                        </Link>
                    </p>
                </div>
            </div>
        </div>
    );
}