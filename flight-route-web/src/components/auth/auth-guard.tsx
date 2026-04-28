import { getHomeRoute } from "@/routes/role-home";
import { useUserInfo, useUserToken } from "@/store/userStore";
import type { ReactNode } from "react";
import { Navigate, useLocation } from "react-router";
import type { UserRole } from "#/entity";
import { useAuthCheck } from "./use-auth";

interface AuthGuardProps {
	children: ReactNode;
	fallback?: ReactNode;
	/** Permission/role string-based check (legacy). */
	check?: string;
	checkAny?: string[];
	checkAll?: string[];
	baseOn?: "role" | "permission";
	/** UC-01: enforce that the authenticated user holds one of these roles. */
	allowedRoles?: UserRole[];
}

/**
 * Conditionally renders children based on authentication and role/permission.
 *
 * When `allowedRoles` is provided:
 *  - unauthenticated → redirect to `/login` preserving the original location
 *  - role mismatch → redirect to user's role-home
 *
 * When `check`/`checkAny`/`checkAll` is provided, falls back to legacy
 * permission/role string matching (used by sample permission pages).
 */
export const AuthGuard = ({ children, fallback = null, check, checkAny, checkAll, baseOn = "permission", allowedRoles }: AuthGuardProps) => {
	const { accessToken } = useUserToken();
	const { role } = useUserInfo();
	const location = useLocation();
	const checkFn = useAuthCheck(baseOn);

	if (allowedRoles) {
		if (!accessToken) {
			return <Navigate to="/login" replace state={{ from: location.pathname }} />;
		}
		if (role && !allowedRoles.includes(role)) {
			return <Navigate to={getHomeRoute(role)} replace />;
		}
		return <>{children}</>;
	}

	const hasAccess = check ? checkFn.check(check) : checkAny ? checkFn.checkAny(checkAny) : checkAll ? checkFn.checkAll(checkAll) : true;

	return hasAccess ? <>{children}</> : <>{fallback}</>;
};
