import { Card, CardContent, CardHeader, CardTitle } from "@/ui/card";
import { Text } from "@/ui/typography";

/**
 * Legacy permission demo placeholder.
 *
 * UC-01 simplified the auth model to two roles (ADMIN/AGENCY) without
 * granular permissions. The original demo content depended on the removed
 * `permissions[]` / `roles[]` shape; this stub keeps the route reachable.
 */
export default function PermissionPage() {
	return (
		<div className="flex flex-col gap-4">
			<Card>
				<CardHeader>
					<CardTitle>Permission demo (deprecated)</CardTitle>
				</CardHeader>
				<CardContent>
					<Text variant="body1">This template page is no longer wired up after the UC-01 auth refactor.</Text>
				</CardContent>
			</Card>
		</div>
	);
}
