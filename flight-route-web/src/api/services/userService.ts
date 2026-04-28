import apiClient from "../apiClient";

import type { UserInfo } from "#/entity";

export enum UserApi {
	User = "/user",
}

const findById = (id: string) => apiClient.get<UserInfo>({ url: `${UserApi.User}/${id}` });

export default {
	findById,
};
