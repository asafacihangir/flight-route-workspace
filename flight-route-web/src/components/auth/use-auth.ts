import { useUserToken } from "@/store/userStore";

/**
 * Legacy permission/role check hook.
 *
 * UC-01 simplified `UserInfo` to `{ username, role }` — granular `permissions[]`
 * and `roles[]` collections no longer exist on the user info, so these checks
 * always return `false`. Sample pages that still call into this API keep
 * compiling; UC-01 enforcement happens via `AuthGuard.allowedRoles`.
 */
export const useAuthCheck = (_baseOn: "role" | "permission" = "permission") => {
	const { accessToken } = useUserToken();

	const check = (_item: string): boolean => {
		if (!accessToken) return false;
		return false;
	};

	const checkAny = (items: string[]) => (items.length === 0 ? true : items.some((item) => check(item)));
	const checkAll = (items: string[]) => (items.length === 0 ? true : items.every((item) => check(item)));

	return { check, checkAny, checkAll };
};
