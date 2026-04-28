import { Inbox, RefreshCcw, Search } from "lucide-react";
import { useTranslation } from "react-i18next";
import type { Route } from "#/entity";
import { Button } from "@/ui/button";
import { Skeleton } from "@/ui/skeleton";
import type { RouteErrorCode } from "./map-route-error";
import { RouteListItem } from "./route-list-item";

export type RouteListStatus =
	| { kind: "idle" }
	| { kind: "loading" }
	| { kind: "empty" }
	| { kind: "data"; routes: Route[] }
	| { kind: "error"; code: RouteErrorCode };

interface RouteListProps {
	status: RouteListStatus;
	onSelect: (route: Route) => void;
	onRetry: () => void;
	onRefreshLocations: () => void;
}

export function RouteList({ status, onSelect, onRetry, onRefreshLocations }: RouteListProps) {
	const { t } = useTranslation();

	if (status.kind === "idle") {
		return (
			<EmptyState icon={<Search className="h-10 w-10 text-muted-foreground" />} message={t("sys.routes.list.idle")} />
		);
	}

	if (status.kind === "loading") {
		return (
			<div className="flex flex-col gap-3">
				{["s1", "s2", "s3"].map((k) => (
					<Skeleton key={k} className="h-20 w-full rounded-lg" />
				))}
			</div>
		);
	}

	if (status.kind === "empty") {
		return (
			<EmptyState icon={<Inbox className="h-10 w-10 text-muted-foreground" />} message={t("sys.routes.list.empty")} />
		);
	}

	if (status.kind === "error") {
		const message = t(`sys.routes.error.${status.code}`);
		const isLocationError = status.code === "locationNotFound";
		return (
			<div className="flex flex-col items-center gap-3 rounded-lg border border-dashed p-8 text-center">
				<p className="text-sm text-muted-foreground">{message}</p>
				<Button type="button" variant="outline" onClick={isLocationError ? onRefreshLocations : onRetry}>
					<RefreshCcw className="mr-2 h-4 w-4" />
					{t(isLocationError ? "sys.routes.error.refresh" : "sys.routes.error.retry")}
				</Button>
			</div>
		);
	}

	return (
		<div className="flex flex-col gap-3">
			{status.routes.map((route) => (
				<RouteListItem key={route.legs.map((l) => l.id).join("-")} route={route} onClick={() => onSelect(route)} />
			))}
		</div>
	);
}

function EmptyState({ icon, message }: { icon: React.ReactNode; message: string }) {
	return (
		<div className="flex flex-col items-center justify-center gap-3 rounded-lg border border-dashed p-12 text-center">
			{icon}
			<p className="text-sm text-muted-foreground">{message}</p>
		</div>
	);
}
