import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import AuthTicketPanel from "./AuthTicketPanel";
// import { authApi } from "../api/authApi"; // wire this to your existing login call

export default function LoginPage() {
    const navigate = useNavigate();
    const [form, setForm] = useState({ email: "", password: "" });
    const [showPassword, setShowPassword] = useState(false);
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
            // const res = await authApi.login(form.email, form.password);
            // localStorage.setItem("token", res.data.token);
            // navigate(`/${res.data.role.toLowerCase()}`);
        } catch (err) {
            setError(
                err?.response?.data?.message || "That email or password didn't match. Try again."
            );
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen flex bg-[#FAFAF7]">
            <AuthTicketPanel
                eyebrow="EventHub"
                heading="Every event, one clean pass."
                sub="Sign in to register for events, download your ticket, and check your attendance history."
                stat="128"
                statLabel="Students checked in this week"
            />

            <div className="flex-1 flex items-center justify-center px-6 py-16">
                <div className="w-full max-w-[380px]">
                    <p className="font-mono text-[11px] tracking-[0.2em] uppercase text-[#C4626B]">
                        Welcome back
                    </p>
                    <h2
                        className="mt-3 text-[30px] text-[#171712]"
                        style={{ fontFamily: "'Fraunces', serif", fontWeight: 500 }}
                    >
                        Sign in to your account
                    </h2>

                    <form onSubmit={handleSubmit} className="mt-10 space-y-5">
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
                            <div className="flex items-center justify-between mb-1.5">
                                <label htmlFor="password" className="block text-[13px] text-[#6E6B60]">
                                    Password
                                </label>
                                <Link to="/forgot-password" className="text-[13px] text-[#C4626B] hover:underline">
                                    Forgot password?
                                </Link>
                            </div>
                            <div className="relative">
                                <input
                                    id="password"
                                    name="password"
                                    type={showPassword ? "text" : "password"}
                                    autoComplete="current-password"
                                    required
                                    placeholder="Enter your password"
                                    value={form.password}
                                    onChange={handleChange}
                                    className="w-full h-11 px-3.5 pr-11 bg-transparent border border-[#E4E1D6] rounded-[6px] text-[15px] text-[#171712] placeholder:text-[#A6A38F] outline-none focus:border-[#C4626B] transition-colors"
                                />
                                <button
                                    type="button"
                                    onClick={() => setShowPassword((s) => !s)}
                                    className="absolute right-3 top-1/2 -translate-y-1/2 text-[13px] text-[#6E6B60] hover:text-[#171712]"
                                >
                                    {showPassword ? "Hide" : "Show"}
                                </button>
                            </div>
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
                            {loading ? "Signing in..." : "Sign in"}
                        </button>
                    </form>

                    <p className="mt-8 text-[14px] text-[#6E6B60] text-center">
                        New to EventHub?{" "}
                        <Link to="/register" className="text-[#171712] font-medium hover:underline">
                            Create an account
                        </Link>
                    </p>
                </div>
            </div>
        </div>
    );
}