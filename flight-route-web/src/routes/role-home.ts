import type { UserRole } from "#/entity";

export const ROLE_HOME: Record<UserRole, string> = {
	ADMIN: "/management/locations",
	AGENCY: "/routes",
};

export function getHomeRoute(role?: UserRole): string {
	if (!role) return "/login";
	return ROLE_HOME[role];
}
