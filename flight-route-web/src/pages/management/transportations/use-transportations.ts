import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import type { Transportation, TransportationCreateInput, TransportationUpdateInput } from "#/entity";
import transportationService from "@/api/services/transportationService";

export const TRANSPORTATIONS_QUERY_KEY = ["transportations"] as const;

export function useTransportationsQuery() {
	return useQuery<Transportation[]>({
		queryKey: TRANSPORTATIONS_QUERY_KEY,
		queryFn: () => transportationService.list(),
		staleTime: 0,
	});
}

export function useCreateTransportation() {
	const queryClient = useQueryClient();
	return useMutation({
		mutationFn: (data: TransportationCreateInput) => transportationService.create(data),
		onSuccess: () => {
			queryClient.invalidateQueries({ queryKey: TRANSPORTATIONS_QUERY_KEY });
		},
	});
}

export function useUpdateTransportation() {
	const queryClient = useQueryClient();
	return useMutation({
		mutationFn: ({ id, data }: { id: string; data: TransportationUpdateInput }) =>
			transportationService.update(id, data),
		onSuccess: () => {
			queryClient.invalidateQueries({ queryKey: TRANSPORTATIONS_QUERY_KEY });
		},
	});
}

export function useDeleteTransportation() {
	const queryClient = useQueryClient();
	return useMutation({
		mutationFn: (id: string) => transportationService.remove(id),
		onSuccess: () => {
			queryClient.invalidateQueries({ queryKey: TRANSPORTATIONS_QUERY_KEY });
		},
	});
}
