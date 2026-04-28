import { useState } from "react";
import { useTranslation } from "react-i18next";
import type { Route } from "#/entity";
import type { RouteSearchParams } from "@/api/services/routeService";
import { Card, CardContent, CardHeader, CardTitle } from "@/ui/card";
import { mapRouteError } from "./map-route-error";
import { RouteDetailSheet } from "./route-detail-sheet";
import { RouteList, type RouteListStatus } from "./route-list";
import { RouteSearchForm } from "./route-search-form";
import { useLocationsOptions } from "./use-locations-options";
import { useRoutesQuery } from "./use-routes";

export default function RoutesPage() {
	const { t } = useTranslation();
	const [searchParams, setSearchParams] = useState<RouteSearchParams | null>(null);
	const [selectedRoute, setSelectedRoute] = useState<Route | null>(null);

	const { options, isLoading: optionsLoading, refresh: refreshLocations } = useLocationsOptions();
	const query = useRoutesQuery(searchParams);

	const status: RouteListStatus = (() => {
		if (searchParams === null) return { kind: "idle" };
		if (query.isLoading || query.isFetching) return { kind: "loading" };
		if (query.isError) return { kind: "error", code: mapRouteError(query.error) };
		const routes = query.data ?? [];
		if (routes.length === 0) return { kind: "empty" };
		return { kind: "data", routes };
	})();

	const handleSearch = (params: RouteSearchParams) => {
		setSelectedRoute(null);
		setSearchParams(params);
	};

	return (
		<div className="flex flex-col gap-4 p-4 md:p-6">
			<Card>
				<CardHeader>
					<CardTitle>{t("sys.routes.title")}</CardTitle>
				</CardHeader>
				<CardContent>
					<RouteSearchForm
						options={options}
						loading={query.isFetching}
						optionsLoading={optionsLoading}
						onSearch={handleSearch}
					/>
				</CardContent>
			</Card>

			<Card>
				<CardHeader>
					<CardTitle>{t("sys.routes.list.title")}</CardTitle>
				</CardHeader>
				<CardContent>
					<RouteList
						status={status}
						onSelect={setSelectedRoute}
						onRetry={() => query.refetch()}
						onRefreshLocations={refreshLocations}
					/>
				</CardContent>
			</Card>

			<RouteDetailSheet route={selectedRoute} onClose={() => setSelectedRoute(null)} />
		</div>
	);
}
