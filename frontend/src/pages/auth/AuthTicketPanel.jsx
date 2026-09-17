export default function AuthTicketPanel({ eyebrow, heading, sub, stat, statLabel }) {
    return (
        <div className="relative hidden lg:flex lg:w-[42%] flex-col justify-between bg-[#FFE7CF] px-12 py-14 overflow-hidden">
            <div className="relative">
                <p className="font-mono text-[11px] tracking-[0.2em] uppercase text-[#C4626B]">
                    {eyebrow}
                </p>
                <h1
                    className="mt-4 text-[42px] leading-[1.08] text-[#171712]"
                    style={{ fontFamily: "'Fraunces', serif", fontWeight: 500 }}
                >
                    {heading}
                </h1>
                <p className="mt-4 text-[15px] leading-relaxed text-[#7A6E5E] max-w-[320px]">
                    {sub}
                </p>
            </div>

            <div className="relative">
                <svg viewBox="0 0 320 140" className="w-full max-w-[320px]" aria-hidden="true">
                    <rect x="1" y="1" width="318" height="138" rx="10" fill="#FFDAD5" stroke="#F0B8B0" strokeWidth="1" />
                    <line x1="220" y1="1" x2="220" y2="139" stroke="#F0B8B0" strokeWidth="1" strokeDasharray="4 4" />
                    <circle cx="220" cy="1" r="6" fill="#FFE7CF" stroke="#F0B8B0" />
                    <circle cx="220" cy="139" r="6" fill="#FFE7CF" stroke="#F0B8B0" />

                    <text x="24" y="34" fontFamily="'Fraunces', serif" fontSize="15" fill="#171712">
                        {statLabel}
                    </text>
                    <text x="24" y="70" fontFamily="'Fraunces', serif" fontSize="34" fontWeight="500" fill="#171712">
                        {stat}
                    </text>
                    <text x="24" y="116" fontFamily="ui-monospace, monospace" fontSize="10" letterSpacing="1" fill="#A17F6F">
                        REG NO. EH-2026-0417
                    </text>

                    <g transform="translate(270,70) rotate(-18)">
                        <circle r="34" fill="none" stroke="#C4626B" strokeWidth="2" />
                        <circle r="28" fill="none" stroke="#C4626B" strokeWidth="1" />
                        <text
                            x="0"
                            y="-4"
                            textAnchor="middle"
                            fontFamily="ui-monospace, monospace"
                            fontSize="9"
                            letterSpacing="1.5"
                            fill="#C4626B"
                        >
                            ADMIT
                        </text>
                        <text
                            x="0"
                            y="10"
                            textAnchor="middle"
                            fontFamily="ui-monospace, monospace"
                            fontSize="9"
                            letterSpacing="1.5"
                            fill="#C4626B"
                        >
                            ONE
                        </text>
                    </g>
                </svg>
            </div>
        </div>
    );
}