import { Bus, Car, Plane, Train } from "lucide-react";
import { useTranslation } from "react-i18next";
import type { Route, Transportation } from "#/entity";
import { type OperatingDay, TransportationType } from "#/enum";
import { Sheet, SheetContent, SheetDescription, SheetHeader, SheetTitle } from "@/ui/sheet";
import { cn } from "@/utils";

interface RouteDetailSheetProps {
	route: Route | null;
	onClose: () => void;
}

const TYPE_ICON: Record<TransportationType, React.ComponentType<{ className?: string }>> = {
	[TransportationType.FLIGHT]: Plane,
	[TransportationType.BUS]: Bus,
	[TransportationType.SUBWAY]: Train,
	[TransportationType.UBER]: Car,
};

export function RouteDetailSheet({ route, onClose }: RouteDetailSheetProps) {
	const { t } = useTranslation();
	const open = route !== null;

	const flightLeg = route?.legs.find((l) => l.type === TransportationType.FLIGHT);
	const firstLeg = route?.legs[0];
	const lastLeg = route?.legs[route.legs.length - 1];

	return (
		<Sheet open={open} onOpenChange={(next) => !next && onClose()}>
			<SheetContent side="right" className="w-full overflow-y-auto sm:max-w-md">
				{route && (
					<>
						<SheetHeader>
							<SheetTitle>
								{firstLeg?.origin.city} → {lastLeg?.destination.city}
							</SheetTitle>
							{flightLeg && (
								<SheetDescription>
									{t("sys.routes.list.via", {
										airport: `${flightLeg.origin.name} (${flightLeg.origin.code})`,
									})}
								</SheetDescription>
							)}
						</SheetHeader>
						<div className="flex flex-col gap-3 px-4 pb-6">
							{route.legs.map((leg, idx) => (
								<LegCard key={`${leg.id}-${idx}`} leg={leg} />
							))}
						</div>
					</>
				)}
			</SheetContent>
		</Sheet>
	);
}

function LegCard({ leg }: { leg: Transportation }) {
	const { t } = useTranslation();
	const Icon = TYPE_ICON[leg.type];
	const isFlight = leg.type === TransportationType.FLIGHT;

	const days = (leg.operatingDays as OperatingDay[])
		.slice()
		.sort((a, b) => a - b)
		.map((d) => t(`sys.routes.detail.day.${d}`))
		.join(", ");

	return (
		<div
			className={cn(
				"flex flex-col gap-2 rounded-lg border p-4",
				isFlight ? "border-primary border-2 bg-primary/5" : "bg-card",
			)}
		>
			<div className="flex items-center gap-2">
				<Icon className={cn("h-4 w-4", isFlight ? "text-primary" : "text-muted-foreground")} />
				<span className={cn("text-sm font-semibold", isFlight && "text-primary")}>
					{isFlight ? t("sys.routes.detail.flightLeg") : t("sys.routes.detail.transferLeg")}
				</span>
				<span className="text-xs text-muted-foreground">· {t(`sys.transportation.type.${leg.type}`)}</span>
			</div>
			<div className="text-sm">
				<span className="font-medium">
					{leg.origin.name} ({leg.origin.code})
				</span>
				<span className="mx-2 text-muted-foreground">→</span>
				<span className="font-medium">
					{leg.destination.name} ({leg.destination.code})
				</span>
			</div>
			<div className="text-xs text-muted-foreground">
				<span className="font-medium">{t("sys.routes.detail.operatingDays")}:</span> {days}
			</div>
		</div>
	);
}
