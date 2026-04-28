import type { Location, LocationCreateInput, LocationUpdateInput } from "#/entity";
import apiClient from "../apiClient";

export enum LocationApi {
	Locations = "/locations",
}

const list = () => apiClient.get<Location[]>({ url: LocationApi.Locations });

const findById = (id: string) => apiClient.get<Location>({ url: `${LocationApi.Locations}/${id}` });

const create = (data: LocationCreateInput) => apiClient.post<Location>({ url: LocationApi.Locations, data });

const update = (id: string, data: LocationUpdateInput) =>
	apiClient.put<Location>({ url: `${LocationApi.Locations}/${id}`, data });

const remove = (id: string) => apiClient.delete<void>({ url: `${LocationApi.Locations}/${id}` });

export default {
	list,
	findById,
	create,
	update,
	remove,
};
