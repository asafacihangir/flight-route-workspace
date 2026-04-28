import type { NavItemDataProps } from "@/components/nav/types";
import { GLOBAL_CONFIG } from "@/global-config";
import { useUserRole } from "@/store/userStore";
import { useMemo } from "react";
import type { UserRole } from "#/entity";
import { backendNavData } from "./nav-data-backend";
import { frontendNavData } from "./nav-data-frontend";

const navData = GLOBAL_CONFIG.routerMode === "backend" ? backendNavData : frontendNavData;

const filterItems = (items: NavItemDataProps[], role: UserRole | undefined): NavItemDataProps[] => {
	return items.filter((item) => {
		const allowedByRole = !item.roles || (role && item.roles.includes(role));
		if (!allowedByRole) return false;

		if (item.children?.length) {
			const filteredChildren = filterItems(item.children, role);
			if (filteredChildren.length === 0) {
				return false;
			}
			item.children = filteredChildren;
		}

		return true;
	});
};

const filterNavData = (role: UserRole | undefined) => {
	return navData
		.map((group) => {
			const filteredItems = filterItems(group.items, role);
			if (filteredItems.length === 0) {
				return null;
			}
			return { ...group, items: filteredItems };
		})
		.filter((group): group is NonNullable<typeof group> => group !== null);
};

export const useFilteredNavData = () => {
	const role = useUserRole();
	const filteredNavData = useMemo(() => filterNavData(role), [role]);
	return filteredNavData;
};
