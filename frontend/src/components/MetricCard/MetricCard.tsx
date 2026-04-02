import { StatCard, type StatCardVariant } from "../StatCard/StatCard";

type Props = {
  label: string;
  value: number | string;
  hint?: string;
  variant?: "default" | "success" | "warn" | "danger";
};

const MAP: Record<NonNullable<Props["variant"]>, StatCardVariant> = {
  default: "default",
  success: "success",
  warn: "warn",
  danger: "danger"
};

/** @deprecated Prefer StatCard; mantido para compatibilidade */
export function MetricCard({ label, value, hint, variant = "default" }: Props) {
  return <StatCard label={label} value={value} hint={hint} variant={MAP[variant]} />;
}
