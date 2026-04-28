import { cn } from "@/utils";
import type { HTMLAttributes } from "react";

type AlertVariant = "destructive" | "default";

interface AlertProps extends HTMLAttributes<HTMLDivElement> {
	variant?: AlertVariant;
}

const variantClass: Record<AlertVariant, string> = {
	default: "border-border bg-muted text-foreground",
	destructive: "border-destructive/50 bg-destructive/10 text-destructive",
};

export function Alert({ className, variant = "default", ...props }: AlertProps) {
	return <div role="alert" className={cn("rounded-md border px-3 py-2 text-sm", variantClass[variant], className)} {...props} />;
}
