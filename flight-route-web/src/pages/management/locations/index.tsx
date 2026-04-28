import { Table } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { toast } from "sonner";
import type { Location, LocationCreateInput } from "#/entity";
import { Icon } from "@/components/icon";
import { Alert } from "@/ui/alert";
import { Button } from "@/ui/button";
import { Card, CardContent, CardHeader } from "@/ui/card";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/ui/dialog";
import { Input } from "@/ui/input";
import { LocationFormModal } from "./location-form-modal";
import { mapLocationError } from "./map-location-error";
import { useCreateLocation, useDeleteLocation, useLocationsQuery, useUpdateLocation } from "./use-locations";

export default function LocationsPage() {
	const { t } = useTranslation();
	const query = useLocationsQuery();
	const createMut = useCreateLocation();
	const updateMut = useUpdateLocation();
	const deleteMut = useDeleteLocation();

	const [search, setSearch] = useState("");
	const [creating, setCreating] = useState(false);
	const [editing, setEditing] = useState<Location | null>(null);
	const [deleting, setDeleting] = useState<Location | null>(null);
	const [deleteInUse, setDeleteInUse] = useState(false);

	const filtered = useMemo(() => {
		const list = query.data ?? [];
		const term = search.trim().toLowerCase();
		if (!term) return list;
		return list.filter((row) =>
			[row.name, row.city, row.country, row.code].some((v) => v?.toLowerCase().includes(term)),
		);
	}, [query.data, search]);

	const handleCreate = async (data: LocationCreateInput) => {
		try {
			await createMut.mutateAsync(data);
			toast.success(t("sys.api.operationSuccess"));
			setCreating(false);
		} catch (err) {
			const code = mapLocationError(err);
			if (code === "duplicateCode") {
				throw Object.assign(new Error("duplicateCode"), { __locationError: "duplicateCode" });
			}
			handleSharedError(code);
			setCreating(false);
		}
	};

	const handleUpdate = async (data: LocationCreateInput) => {
		if (!editing) return;
		try {
			await updateMut.mutateAsync({ id: editing.id, data });
			toast.success(t("sys.api.operationSuccess"));
			setEditing(null);
		} catch (err) {
			const code = mapLocationError(err);
			if (code === "duplicateCode") {
				throw Object.assign(new Error("duplicateCode"), { __locationError: "duplicateCode" });
			}
			handleSharedError(code);
			setEditing(null);
		}
	};

	const handleDelete = async () => {
		if (!deleting) return;
		try {
			await deleteMut.mutateAsync(deleting.id);
			toast.success(t("sys.api.operationSuccess"));
			setDeleting(null);
			setDeleteInUse(false);
		} catch (err) {
			const code = mapLocationError(err);
			if (code === "inUse") {
				setDeleteInUse(true);
				return;
			}
			handleSharedError(code);
			setDeleting(null);
			setDeleteInUse(false);
		}
	};

	const handleSharedError = (code: ReturnType<typeof mapLocationError>) => {
		switch (code) {
			case "forbidden":
				toast.error(t("sys.location.error.forbidden"));
				break;
			case "notFound":
				toast.error(t("sys.location.error.notFound"));
				query.refetch();
				break;
			default:
				toast.error(t("sys.api.errorMessage"));
				break;
		}
	};

	const columns: ColumnsType<Location> = [
		{ title: t("sys.location.field.name"), dataIndex: "name", key: "name" },
		{ title: t("sys.location.field.city"), dataIndex: "city", key: "city" },
		{ title: t("sys.location.field.country"), dataIndex: "country", key: "country" },
		{
			title: t("sys.location.field.code"),
			dataIndex: "code",
			key: "code",
			render: (value: string) => <span className="font-mono">{value}</span>,
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
					<Button
						variant="ghost"
						size="icon"
						onClick={() => {
							setDeleting(record);
							setDeleteInUse(false);
						}}
					>
						<Icon icon="mingcute:delete-2-fill" size={18} className="text-error!" />
					</Button>
				</div>
			),
		},
	];

	return (
		<>
			<Card>
				<CardHeader>
					<div className="flex items-center justify-between gap-3">
						<div className="text-base font-semibold">{t("sys.location.title")}</div>
						<div className="flex items-center gap-2">
							<Input
								placeholder={t("common.searchText")}
								value={search}
								onChange={(e) => setSearch(e.target.value)}
								className="w-64"
							/>
							<Button onClick={() => setCreating(true)}>{t("sys.location.new")}</Button>
						</div>
					</div>
				</CardHeader>
				<CardContent>
					<Table
						rowKey="id"
						size="small"
						loading={query.isPending}
						columns={columns}
						dataSource={filtered}
						pagination={{ pageSize: 10, showSizeChanger: true }}
						scroll={{ x: "max-content" }}
					/>
				</CardContent>
			</Card>

			<LocationFormModal open={creating} mode="create" onSubmit={handleCreate} onClose={() => setCreating(false)} />

			<LocationFormModal
				open={editing !== null}
				mode="edit"
				defaultValues={
					editing ? { name: editing.name, country: editing.country, city: editing.city, code: editing.code } : null
				}
				onSubmit={handleUpdate}
				onClose={() => setEditing(null)}
			/>

			<Dialog
				open={deleting !== null}
				onOpenChange={(next) => {
					if (!next) {
						setDeleting(null);
						setDeleteInUse(false);
					}
				}}
			>
				<DialogContent>
					<DialogHeader>
						<DialogTitle>{t("sys.location.delete.title")}</DialogTitle>
						<DialogDescription>
							{deleting ? t("sys.location.delete.confirm", { name: deleting.name, code: deleting.code }) : ""}
						</DialogDescription>
					</DialogHeader>
					{deleteInUse && (
						<Alert variant="destructive" className="mt-2">
							{t("sys.location.error.inUse")}
						</Alert>
					)}
					<DialogFooter>
						<Button
							variant="outline"
							onClick={() => {
								setDeleting(null);
								setDeleteInUse(false);
							}}
							disabled={deleteMut.isPending}
						>
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
