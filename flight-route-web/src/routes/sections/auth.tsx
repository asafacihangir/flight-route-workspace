import { Suspense, lazy } from "react";
import type { RouteObject } from "react-router";

const LoginPage = lazy(() => import("@/pages/sys/login"));

export const authRoutes: RouteObject[] = [
	{
		path: "login",
		element: (
			<Suspense>
				<LoginPage />
			</Suspense>
		),
	},
];
