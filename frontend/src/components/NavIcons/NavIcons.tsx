type IconProps = { size?: number };

export function IconDashboard({ size = 20 }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden>
      <path
        d="M4 13h7V4H4v9zm0 7h7v-5H4v5zm9 0h7V11h-7v9zm0-18v5h7V4h-7z"
        fill="currentColor"
        opacity="0.88"
      />
    </svg>
  );
}

export function IconTarget({ size = 20 }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden>
      <circle cx="12" cy="12" r="9" stroke="currentColor" strokeWidth="1.75" opacity="0.75" />
      <circle cx="12" cy="12" r="4" stroke="currentColor" strokeWidth="1.75" opacity="0.95" />
      <circle cx="12" cy="12" r="1.2" fill="currentColor" />
    </svg>
  );
}

export function IconHero({ size = 20 }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden>
      <path
        d="M12 4l2.2 4.5 5 .7-3.6 3.5.9 5L12 16.2 7.5 17.8l.9-5L4.8 9.2l5-.7L12 4z"
        stroke="currentColor"
        strokeWidth="1.4"
        strokeLinejoin="round"
        fill="rgba(255,255,255,0.06)"
      />
    </svg>
  );
}

export function IconTeam({ size = 20 }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden>
      <circle cx="9" cy="9" r="3" stroke="currentColor" strokeWidth="1.6" />
      <circle cx="16" cy="10" r="2.5" stroke="currentColor" strokeWidth="1.6" />
      <path
        d="M4 19c.8-2.8 3.2-4 6-4s5.2 1.2 6 4M13 19c.6-1.8 2-2.8 3.8-3"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeLinecap="round"
      />
    </svg>
  );
}

export function IconPlus({ size = 20 }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden>
      <path
        d="M12 5v14M5 12h14"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
      />
    </svg>
  );
}

export function IconUserCircle({ size = 20 }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden>
      <circle cx="12" cy="12" r="9" stroke="currentColor" strokeWidth="1.6" opacity="0.72" />
      <circle cx="12" cy="9" r="2.5" fill="currentColor" opacity="0.5" />
      <path
        d="M6.5 18c1-2.5 3.5-3.5 5.5-3.5s4.5 1 5.5 3.5"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeLinecap="round"
      />
    </svg>
  );
}
