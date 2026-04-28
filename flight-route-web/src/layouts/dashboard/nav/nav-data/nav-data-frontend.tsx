import { Icon } from "@/components/icon";
import type { NavProps } from "@/components/nav";

export const frontendNavData: NavProps["data"] = [
	{
		name: "sys.nav.management",
		items: [
			{
				title: "sys.nav.locations",
				path: "/management/locations",
				icon: <Icon icon="solar:map-point-bold-duotone" size="24" />,
				roles: ["ADMIN"],
			},
			{
				title: "sys.nav.transportations",
				path: "/management/transportations",
				icon: <Icon icon="solar:bus-bold-duotone" size="24" />,
				roles: ["ADMIN"],
			},
		],
	},
	{
		name: "sys.nav.operations",
		items: [
			{
				title: "sys.nav.routes",
				path: "/routes",
				icon: <Icon icon="solar:routing-bold-duotone" size="24" />,
				roles: ["ADMIN", "AGENCY"],
			},
		],
	},
];
