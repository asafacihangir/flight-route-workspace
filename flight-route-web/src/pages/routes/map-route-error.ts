import type { AxiosError } from "axios";

export type RouteErrorCode = "invalidParams" | "locationNotFound" | "network" | "unknown";

interface BackendErrorBody {
	code?: string;
	message?: string;
}

export function mapRouteError(error: unknown): RouteErrorCode {
	const axiosErr = error as AxiosError<BackendErrorBody> | undefined;
	const status = axiosErr?.response?.status;

	if (axiosErr && !axiosErr.response) return "network";
	if (status === 400 || status === 422) return "invalidParams";
	if (status === 404) return "locationNotFound";
	if (status && status >= 500) return "network";

	return "unknown";
}
