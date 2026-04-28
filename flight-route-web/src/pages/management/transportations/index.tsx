import { Table } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { Link } from "react-router";
import { toast } from "sonner";
import type { Transportation, TransportationCreateInput } from "#/entity";
import { type OperatingDay, TransportationType } from "#/enum";
import { Icon } from "@/components/icon";
import { useLocationsQuery } from "@/pages/management/locations/use-locations";
import { Button } from "@/ui/button";
import { Card, CardContent, CardHeader } from "@/ui/card";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/ui/dialog";
import { Input } from "@/ui/input";
import { cn } from "@/utils";
import { mapTransportationError } from "./map-transportation-error";
import { TransportationFormModal } from "./transportation-form-modal";
import {
	useCreateTransportation,
	useDeleteTransportation,
	useTransportationsQuery,
	useUpdateTransportation,
} from "./use-transportations";

const ALL_DAYS: OperatingDay[] = [1, 2, 3, 4, 5, 6, 7];

const TYPE_DOT: Record<TransportationType, string> = {
	[TransportationType.FLIGHT]: "bg-blue-500",
	[TransportationType.BUS]: "bg-green-500",
	[TransportationType.SUBWAY]: "bg-purple-500",
	[TransportationType.UBER]: "bg-zinc-900 dark:bg-zinc-100",
};

export default function TransportationsPage() {
	const { t } = useTranslation();
	const query = useTransportationsQuery();
	const locationsQuery = useLocationsQuery();
	const createMut = useCreateTransportation();
	const updateMut = useUpdateTransportation();
	const deleteMut = useDeleteTransportation();

	const [search, setSearch] = useState("");
	const [creating, setCreating] = useState(false);
	const [editing, setEditing] = useState<Transportation | null>(null);
	const [deleting, setDeleting] = useState<Transportation | null>(null);

	const locations = locationsQuery.data ?? [];
	const noLocations = !locationsQuery.isPending && locations.length === 0;

	const filtered = useMemo(() => {
		const list = query.data ?? [];
		const term = search.trim().toLowerCase();
		if (!term) return list;
		return list.filter((row) => {
			const haystack = [
				row.origin?.name,
				row.origin?.code,
				row.origin?.city,
				row.destination?.name,
				row.destination?.code,
				row.destination?.city,
				row.type,
				t(`sys.transportation.type.${row.type}`),
			]
				.filter(Boolean)
				.join(" ")
				.toLowerCase();
			return haystack.includes(term);
		});
	}, [query.data, search, t]);

	const handleSharedError = (code: ReturnType<typeof mapTransportationError>) => {
		switch (code) {
			case "forbidden":
				toast.error(t("sys.transportation.error.forbidden"));
				break;
			case "notFound":
				toast.error(t("sys.transportation.error.notFound"));
				query.refetch();
				break;
			case "validation":
				toast.error(t("sys.transportation.error.validation"));
				break;
			case "network":
				toast.error(t("sys.transportation.error.network"));
				break;
			default:
				toast.error(t("sys.api.errorMessage"));
				break;
		}
	};

	const handleCreate = async (data: TransportationCreateInput) => {
		try {
			await createMut.mutateAsync(data);
			toast.success(t("sys.api.operationSuccess"));
			setCreating(false);
		} catch (err) {
			handleSharedError(mapTransportationError(err));
			setCreating(false);
		}
	};

	const handleUpdate = async (data: TransportationCreateInput) => {
		if (!editing) return;
		try {
			await updateMut.mutateAsync({ id: editing.id, data });
			toast.success(t("sys.api.operationSuccess"));
			setEditing(null);
		} catch (err) {
			const code = mapTransportationError(err);
			if (code === "notFound") setEditing(null);
			handleSharedError(code);
		}
	};

	const handleDelete = async () => {
		if (!deleting) return;
		try {
			await deleteMut.mutateAsync(deleting.id);
			toast.success(t("sys.api.operationSuccess"));
			setDeleting(null);
		} catch (err) {
			handleSharedError(mapTransportationError(err));
			setDeleting(null);
		}
	};

	const columns: ColumnsType<Transportation> = [
		{
			title: t("sys.transportation.field.origin"),
			key: "origin",
			render: (_, row) => <LocationCell location={row.origin} />,
		},
		{
			title: t("sys.transportation.field.destination"),
			key: "destination",
			render: (_, row) => <LocationCell location={row.destination} />,
		},
		{
			title: t("sys.transportation.field.type"),
			key: "type",
			render: (_, row) => <TypeBadge type={row.type} />,
		},
		{
			title: t("sys.transportation.field.operatingDays"),
			key: "operatingDays",
			render: (_, row) => <OperatingDaysStrip days={row.operatingDays ?? []} />,
		},
		{
			title: "",
			key: "operation",
			align: "center",
			width: 120,
			render: (_, record) => (
				<div className="flex w-full justify-center text-gray-500">
					<Button variant="ghost" size="icon" onClick={() => setEditing(record)}>
						<Icon icon="solar:pen-bold-duotone" size={18} />
					</Button>
					<Button variant="ghost" size="icon" onClick={() => setDeleting(record)}>
						<Icon icon="mingcute:delete-2-fill" size={18} className="text-error!" />
					</Button>
				</div>
			),
		},
	];

	const editDefaults = editing
		? {
				originId: editing.origin?.id ?? "",
				destinationId: editing.destination?.id ?? "",
				type: editing.type,
				operatingDays: editing.operatingDays ?? [],
			}
		: null;

	return (
		<>
			<Card>
				<CardHeader>
					<div className="flex items-center justify-between gap-3">
						<div className="text-base font-semibold">{t("sys.transportation.title")}</div>
						<div className="flex items-center gap-2">
							<Input
								placeholder={t("common.searchText")}
								value={search}
								onChange={(e) => setSearch(e.target.value)}
								className="w-64"
								disabled={noLocations}
							/>
							<Button onClick={() => setCreating(true)} disabled={noLocations}>
								{t("sys.transportation.new")}
							</Button>
						</div>
					</div>
				</CardHeader>
				<CardContent>
					{noLocations ? (
						<div className="flex flex-col items-center justify-center gap-3 py-12 text-center">
							<Icon icon="solar:map-point-bold-duotone" size={48} className="text-muted-foreground" />
							<div className="text-muted-foreground">{t("sys.transportation.empty.noLocations")}</div>
							<Link to="/management/locations">
								<Button variant="outline">{t("sys.transportation.empty.goToLocations")}</Button>
							</Link>
						</div>
					) : (
						<Table
							rowKey="id"
							size="small"
							loading={query.isPending || locationsQuery.isPending}
							columns={columns}
							dataSource={filtered}
							pagination={{ pageSize: 10, showSizeChanger: true }}
							scroll={{ x: "max-content" }}
						/>
					)}
				</CardContent>
			</Card>

			<TransportationFormModal
				open={creating}
				mode="create"
				locations={locations}
				onSubmit={handleCreate}
				onClose={() => setCreating(false)}
			/>

			<TransportationFormModal
				open={editing !== null}
				mode="edit"
				locations={locations}
				defaultValues={editDefaults}
				onSubmit={handleUpdate}
				onClose={() => setEditing(null)}
			/>

			<Dialog open={deleting !== null} onOpenChange={(next) => !next && setDeleting(null)}>
				<DialogContent>
					<DialogHeader>
						<DialogTitle>{t("sys.transportation.delete.title")}</DialogTitle>
						<DialogDescription>
							{deleting
								? t("sys.transportation.delete.confirm", {
										type: t(`sys.transportation.type.${deleting.type}`),
										originCode: deleting.origin?.code ?? "",
										destinationCode: deleting.destination?.code ?? "",
									})
								: ""}
						</DialogDescription>
					</DialogHeader>
					<DialogFooter>
						<Button variant="outline" onClick={() => setDeleting(null)} disabled={deleteMut.isPending}>
							{t("common.cancelText")}
						</Button>
						<Button variant="destructive" onClick={handleDelete} disabled={deleteMut.isPending}>
							{t("common.delText")}
						</Button>
					</DialogFooter>
				</DialogContent>
			</Dialog>
		</>
	);
}

function LocationCell({ location }: { location?: { name: string; code: string; city: string } }) {
	if (!location) return <span className="text-muted-foreground">—</span>;
	return (
		<div className="flex flex-col">
			<span className="flex items-center gap-2">
				<span className="font-mono text-xs">{location.code}</span>
				<span>{location.name}</span>
			</span>
			<span className="text-muted-foreground text-xs">{location.city}</span>
		</div>
	);
}

function TypeBadge({ type }: { type: TransportationType }) {
	const { t } = useTranslation();
	return (
		<span className="inline-flex items-center gap-2 rounded-full border px-2 py-0.5 text-xs">
			<span className={cn("inline-block size-2 rounded-full", TYPE_DOT[type])} />
			{t(`sys.transportation.type.${type}`)}
		</span>
	);
}

function OperatingDaysStrip({ days }: { days: OperatingDay[] }) {
	const { t } = useTranslation();
	const set = new Set(days);
	return (
		<div className="flex gap-1">
			{ALL_DAYS.map((day) => {
				const active = set.has(day);
				return (
					<span
						key={day}
						title={t(`sys.transportation.day.long.${day}`)}
						className={cn(
							"flex h-6 w-7 items-center justify-center rounded text-[10px] font-medium",
							active ? "bg-primary text-primary-foreground" : "bg-muted text-muted-foreground",
						)}
					>
						{t(`sys.transportation.day.short.${day}`)}
					</span>
				);
			})}
		</div>
	);
}
