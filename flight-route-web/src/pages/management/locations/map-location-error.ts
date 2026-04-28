import type { AxiosError } from "axios";

export type LocationApiError = "duplicateCode" | "inUse" | "notFound" | "forbidden" | "generic";

interface BackendErrorBody {
	code?: string;
	field?: string;
	message?: string;
}

export function mapLocationError(error: unknown): LocationApiError {
	const axiosErr = error as AxiosError<BackendErrorBody> | undefined;
	const status = axiosErr?.response?.status;
	const body = axiosErr?.response?.data;

	if (status === 403) return "forbidden";
	if (status === 404) return "notFound";

	if (status === 409) {
		if (body?.code === "LOCATION_IN_USE") return "inUse";
		if (body?.code === "DUPLICATE_CODE") return "duplicateCode";
		return "generic";
	}

	if (status === 400 && body?.field === "code") return "duplicateCode";

	return "generic";
}
