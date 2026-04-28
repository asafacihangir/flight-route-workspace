import type { NavItemDataProps } from "@/components/nav/types";
import type { BasicStatus, OperatingDay, PermissionType, TransportationType } from "./enum";

export type UserRole = "ADMIN" | "AGENCY";

export interface UserToken {
	accessToken?: string;
	expiresIn?: number;
}

export interface UserInfo {
	username: string;
	role: UserRole;
}

export interface Permission_Old {
	id: string;
	parentId: string;
	name: string;
	label: string;
	type: PermissionType;
	route: string;
	status?: BasicStatus;
	order?: number;
	icon?: string;
	component?: string;
	hide?: boolean;
	hideTab?: boolean;
	frameSrc?: URL;
	newFeature?: boolean;
	children?: Permission_Old[];
}

export interface Role_Old {
	id: string;
	name: string;
	code: string;
	status: BasicStatus;
	order?: number;
	desc?: string;
	permission?: Permission_Old[];
}

export interface CommonOptions {
	status?: BasicStatus;
	desc?: string;
	createdAt?: string;
	updatedAt?: string;
}
export interface User extends CommonOptions {
	id: string; // uuid
	username: string;
	password: string;
	email: string;
	phone?: string;
	avatar?: string;
}

export interface Role extends CommonOptions {
	id: string; // uuid
	name: string;
	code: string;
}

export interface Permission extends CommonOptions {
	id: string; // uuid
	name: string;
	code: string; // resource:action  example: "user-management:read"
}

export interface Menu extends CommonOptions, MenuMetaInfo {
	id: string; // uuid
	parentId: string;
	name: string;
	code: string;
	order?: number;
	type: PermissionType;
}

export type MenuMetaInfo = Partial<Pick<NavItemDataProps, "path" | "icon" | "caption" | "info" | "disabled" | "auth" | "hidden">> & {
	externalLink?: URL;
	component?: string;
};

export type MenuTree = Menu & {
	children?: MenuTree[];
};

export interface Location {
	id: string;
	name: string;
	country: string;
	city: string;
	code: string;
}

export type LocationCreateInput = Omit<Location, "id">;
export type LocationUpdateInput = LocationCreateInput;

export interface Transportation {
	id: string;
	origin: Location;
	destination: Location;
	type: TransportationType;
	operatingDays: OperatingDay[];
}

export interface TransportationCreateInput {
	originId: string;
	destinationId: string;
	type: TransportationType;
	operatingDays: OperatingDay[];
}

export type TransportationUpdateInput = TransportationCreateInput;

export interface Route {
	legs: Transportation[];
}
