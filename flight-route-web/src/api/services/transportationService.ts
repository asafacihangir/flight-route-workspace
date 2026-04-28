import type { Transportation, TransportationCreateInput, TransportationUpdateInput } from "#/entity";
import apiClient from "../apiClient";

export enum TransportationApi {
	Transportations = "/transportations",
}

const list = () => apiClient.get<Transportation[]>({ url: TransportationApi.Transportations });

const findById = (id: string) =>
	apiClient.get<Transportation>({ url: `${TransportationApi.Transportations}/${id}` });

const create = (data: TransportationCreateInput) =>
	apiClient.post<Transportation>({ url: TransportationApi.Transportations, data });

const update = (id: string, data: TransportationUpdateInput) =>
	apiClient.put<Transportation>({ url: `${TransportationApi.Transportations}/${id}`, data });

const remove = (id: string) =>
	apiClient.delete<void>({ url: `${TransportationApi.Transportations}/${id}` });

export default {
	list,
	findById,
	create,
	update,
	remove,
};
