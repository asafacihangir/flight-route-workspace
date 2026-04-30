import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import type { Location, LocationCreateInput, LocationUpdateInput } from "#/entity";
import locationService from "@/api/services/locationService";

export const LOCATIONS_QUERY_KEY = ["locations"] as const;

export function useLocationsQuery() {
	return useQuery<Location[]>({
		queryKey: LOCATIONS_QUERY_KEY,
		queryFn: () => locationService.list(),
		staleTime: 0,
	});
}

export function useCreateLocation() {
	const queryClient = useQueryClient();
	return useMutation({
		mutationFn: (data: LocationCreateInput) => locationService.create(data),
		onSuccess: () => {
			queryClient.invalidateQueries({ queryKey: LOCATIONS_QUERY_KEY });
		},
	});
}

export function useUpdateLocation() {
	const queryClient = useQueryClient();
	return useMutation({
		mutationFn: ({ id, data }: { id: number; data: LocationUpdateInput }) => locationService.update(id, data),
		onSuccess: () => {
			queryClient.invalidateQueries({ queryKey: LOCATIONS_QUERY_KEY });
		},
	});
}

export function useDeleteLocation() {
	const queryClient = useQueryClient();
	return useMutation({
		mutationFn: (id: number) => locationService.remove(id),
		onSuccess: () => {
			queryClient.invalidateQueries({ queryKey: LOCATIONS_QUERY_KEY });
		},
	});
}
