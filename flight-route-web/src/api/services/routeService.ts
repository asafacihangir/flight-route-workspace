import type { Route } from "#/entity";
import apiClient from "../apiClient";

export enum RouteApi {
	Routes = "/routes",
}

export interface RouteSearchParams {
	originId: string;
	destinationId: string;
	date: string; // YYYY-MM-DD
}

const list = async (params: RouteSearchParams): Promise<Route[]> => {
	const result = await apiClient.get<Route[] | undefined>({
		url: RouteApi.Routes,
		params,
	});
	return result ?? [];
};

export default {
	list,
};
