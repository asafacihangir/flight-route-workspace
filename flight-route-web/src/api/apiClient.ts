import { GLOBAL_CONFIG } from "@/global-config";
import { t } from "@/locales/i18n";
import userStore from "@/store/userStore";
import axios, { type AxiosError, type AxiosRequestConfig, type AxiosResponse } from "axios";
import { toast } from "sonner";

const axiosInstance = axios.create({
	baseURL: GLOBAL_CONFIG.apiBaseUrl,
	timeout: 50000,
	headers: { "Content-Type": "application/json;charset=utf-8" },
});

axiosInstance.interceptors.request.use(
	(config) => {
		const accessToken = userStore.getState().userToken.accessToken;
		if (accessToken) {
			config.headers.Authorization = `Bearer ${accessToken}`;
		}
		return config;
	},
	(error) => Promise.reject(error),
);

axiosInstance.interceptors.response.use(
	(res: AxiosResponse) => res.data,
	(error: AxiosError<{ message?: string }>) => {
		const { response, config, message } = error || {};
		const status = response?.status;
		const requestUrl = config?.url ?? "";
		const isLoginRequest = requestUrl.startsWith("/auth/login");

		if (!isLoginRequest && status === 401) {
			userStore.getState().actions.clearUserInfoAndToken();
			toast.error(t("sys.api.sessionExpired"), { position: "top-center" });
			window.location.assign("/login");
			return Promise.reject(error);
		}

		if (!isLoginRequest) {
			const errMsg = response?.data?.message || message || t("sys.api.errorMessage");
			toast.error(errMsg, { position: "top-center" });
		}

		return Promise.reject(error);
	},
);

class APIClient {
	get<T = unknown>(config: AxiosRequestConfig): Promise<T> {
		return this.request<T>({ ...config, method: "GET" });
	}
	post<T = unknown>(config: AxiosRequestConfig): Promise<T> {
		return this.request<T>({ ...config, method: "POST" });
	}
	put<T = unknown>(config: AxiosRequestConfig): Promise<T> {
		return this.request<T>({ ...config, method: "PUT" });
	}
	delete<T = unknown>(config: AxiosRequestConfig): Promise<T> {
		return this.request<T>({ ...config, method: "DELETE" });
	}
	request<T = unknown>(config: AxiosRequestConfig): Promise<T> {
		return axiosInstance.request<unknown, T>(config);
	}
}

export default new APIClient();
