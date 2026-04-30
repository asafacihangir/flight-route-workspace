import { useQueryClient } from "@tanstack/react-query";
import { useMemo } from "react";
import type { Location } from "#/entity";
import { LOCATIONS_QUERY_KEY, useLocationsQuery } from "@/pages/management/locations/use-locations";

export interface LocationOption {
	id: number;
	label: string;
	code: string;
	name: string;
	city: string;
	country: string;
}

export function useLocationsOptions() {
	const query = useLocationsQuery();
	const queryClient = useQueryClient();

	const options = useMemo<LocationOption[]>(() => {
		const list: Location[] = query.data ?? [];
		return list.map((l) => ({
			id: l.id,
			label: `${l.name} · ${l.code}`,
			code: l.code,
			name: l.name,
			city: l.city,
			country: l.country,
		}));
	}, [query.data]);

	const refresh = () => queryClient.invalidateQueries({ queryKey: LOCATIONS_QUERY_KEY });

	return {
		options,
		isLoading: query.isLoading,
		isError: query.isError,
		refresh,
	};
}
