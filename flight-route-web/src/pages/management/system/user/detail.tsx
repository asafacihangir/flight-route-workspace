import { useParams } from "@/routes/hooks";
import { Card, CardContent } from "@/ui/card";
type LegacyUser = { id: string; username: string };
const USERS: LegacyUser[] = [];

export default function UserDetail() {
	const { id } = useParams();
	const user = USERS.find((user) => user.id === id);
	return (
		<Card>
			<CardContent>
				<p>This is the detail page of {user?.username}</p>
			</CardContent>
		</Card>
	);
}
