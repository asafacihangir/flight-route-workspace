import type { AxiosError } from "axios";

export type TransportationApiError = "forbidden" | "notFound" | "validation" | "network" | "unknown";

interface BackendErrorBody {
	code?: string;
	field?: string;
	message?: string;
}

export function mapTransportationError(error: unknown): TransportationApiError {
	const axiosErr = error as AxiosError<BackendErrorBody> | undefined;
	const status = axiosErr?.response?.status;

	if (axiosErr && !axiosErr.response) return "network";

	if (status === 403) return "forbidden";
	if (status === 404) return "notFound";
	if (status === 400 || status === 422) return "validation";

	return "unknown";
}
