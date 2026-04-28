import apiClient from "../apiClient";

import type { UserRole } from "#/entity";

export interface LoginReq {
	username: string;
	password: string;
}

export interface LoginRes {
	accessToken: string;
	expiresIn?: number;
}

export interface MeRes {
	username: string;
	role: UserRole;
}

export enum AuthApi {
	Login = "/auth/login",
	Me = "/auth/me",
	Logout = "/auth/logout",
}

const login = (data: LoginReq) => apiClient.post<LoginRes>({ url: AuthApi.Login, data });
const me = () => apiClient.get<MeRes>({ url: AuthApi.Me });
const logout = () => apiClient.post<void>({ url: AuthApi.Logout });

export default {
	login,
	me,
	logout,
};
