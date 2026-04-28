import { useQuery } from "@tanstack/react-query";
import type { Route } from "#/entity";
import routeService, { type RouteSearchParams } from "@/api/services/routeService";

export const ROUTES_QUERY_KEY = "routes" as const;

export function useRoutesQuery(params: RouteSearchParams | null) {
	return useQuery<Route[]>({
		queryKey: [ROUTES_QUERY_KEY, params?.originId, params?.destinationId, params?.date],
		queryFn: () => routeService.list(params as RouteSearchParams),
		enabled: params !== null,
		staleTime: 5 * 60 * 1000,
	});
}
