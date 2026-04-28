import { ChevronRight, Plane } from "lucide-react";
import { useTranslation } from "react-i18next";
import type { Route } from "#/entity";
import { TransportationType } from "#/enum";
import { Badge } from "@/ui/badge";
import { cn } from "@/utils";

interface RouteListItemProps {
	route: Route;
	onClick: () => void;
}

const TYPE_VARIANT: Record<TransportationType, "default" | "secondary" | "outline"> = {
	[TransportationType.FLIGHT]: "default",
	[TransportationType.BUS]: "secondary",
	[TransportationType.SUBWAY]: "secondary",
	[TransportationType.UBER]: "outline",
};

export function RouteListItem({ route, onClick }: RouteListItemProps) {
	const { t } = useTranslation();
	const flightLegs = route.legs.filter((l) => l.type === TransportationType.FLIGHT);
	const flightLeg = flightLegs.length === 1 ? flightLegs[0] : undefined;
	const firstLeg = route.legs[0];
	const lastLeg = route.legs[route.legs.length - 1];

	const headline = flightLeg
		? t("sys.routes.list.via", { airport: `${flightLeg.origin.name} (${flightLeg.origin.code})` })
		: t("sys.routes.list.summary", {
				origin: firstLeg?.origin.code ?? "",
				destination: lastLeg?.destination.code ?? "",
			});

	return (
		<button
			type="button"
			onClick={onClick}
			className={cn(
				"flex w-full cursor-pointer items-center justify-between gap-3 rounded-lg border bg-card p-4 text-left transition-colors",
				"hover:border-primary/50 hover:bg-accent",
				"focus:outline-none focus-visible:ring-2 focus-visible:ring-ring",
			)}
		>
			<div className="flex flex-1 flex-col gap-2">
				<div className="flex items-center gap-2">
					<Plane className="h-4 w-4 text-primary" />
					<span className="font-semibold">{headline}</span>
				</div>
				<div className="flex flex-wrap items-center gap-1.5">
					{route.legs.map((leg, idx) => (
						<Badge
							key={`${leg.id}-${idx}`}
							variant={TYPE_VARIANT[leg.type]}
							className={cn(leg.type === TransportationType.FLIGHT && "font-semibold")}
						>
							{t(`sys.transportation.type.${leg.type}`)}
						</Badge>
					))}
				</div>
			</div>
			<ChevronRight className="h-4 w-4 shrink-0 text-muted-foreground" />
		</button>
	);
}
