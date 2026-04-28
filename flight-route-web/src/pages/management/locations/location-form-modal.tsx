import { zodResolver } from "@hookform/resolvers/zod";
import { Loader2 } from "lucide-react";
import { useEffect } from "react";
import { useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import { z } from "zod";
import type { LocationCreateInput } from "#/entity";
import { Button } from "@/ui/button";
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle } from "@/ui/dialog";
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/ui/form";
import { Input } from "@/ui/input";

export type LocationFormMode = "create" | "edit";

interface LocationFormModalProps {
	open: boolean;
	mode: LocationFormMode;
	defaultValues?: LocationCreateInput | null;
	onSubmit: (data: LocationCreateInput) => Promise<void>;
	onClose: () => void;
}

const EMPTY_DEFAULTS: LocationCreateInput = {
	name: "",
	country: "",
	city: "",
	code: "",
};

export function LocationFormModal({ open, mode, defaultValues, onSubmit, onClose }: LocationFormModalProps) {
	const { t } = useTranslation();

	const schema = z.object({
		name: z.string().trim().min(1, t("sys.location.error.required")).max(120, t("sys.location.error.required")),
		country: z.string().trim().min(1, t("sys.location.error.required")).max(80, t("sys.location.error.required")),
		city: z.string().trim().min(1, t("sys.location.error.required")).max(80, t("sys.location.error.required")),
		code: z
			.string()
			.trim()
			.min(2, t("sys.location.error.codeFormat"))
			.max(10, t("sys.location.error.codeFormat"))
			.regex(/^[A-Z0-9]+$/, t("sys.location.error.codeFormat")),
	});

	const form = useForm<LocationCreateInput>({
		resolver: zodResolver(schema),
		defaultValues: defaultValues ?? EMPTY_DEFAULTS,
		mode: "onSubmit",
	});

	useEffect(() => {
		if (!open) return;
		if (mode === "edit" && defaultValues) {
			form.reset(defaultValues);
		} else {
			form.reset(EMPTY_DEFAULTS);
		}
	}, [open, mode, defaultValues, form]);

	const isSubmitting = form.formState.isSubmitting;

	const handleSubmit = form.handleSubmit(async (data) => {
		try {
			await onSubmit(data);
		} catch (err) {
			const code = (err as { __locationError?: string } | null)?.__locationError;
			if (code === "duplicateCode") {
				form.setError("code", { type: "manual", message: t("sys.location.error.duplicateCode") });
				return;
			}
			throw err;
		}
	});

	return (
		<Dialog
			open={open}
			onOpenChange={(next) => {
				if (!next && !isSubmitting) onClose();
			}}
		>
			<DialogContent>
				<DialogHeader>
					<DialogTitle>{mode === "create" ? t("sys.location.new") : t("sys.location.edit")}</DialogTitle>
				</DialogHeader>
				<Form {...form}>
					<form onSubmit={handleSubmit} className="space-y-4">
						<FormField
							control={form.control}
							name="name"
							render={({ field }) => (
								<FormItem className="grid grid-cols-4 items-start gap-3">
									<FormLabel className="col-span-1 pt-2">{t("sys.location.field.name")}</FormLabel>
									<div className="col-span-3 space-y-1">
										<FormControl>
											<Input {...field} autoFocus />
										</FormControl>
										<FormMessage />
									</div>
								</FormItem>
							)}
						/>
						<FormField
							control={form.control}
							name="country"
							render={({ field }) => (
								<FormItem className="grid grid-cols-4 items-start gap-3">
									<FormLabel className="col-span-1 pt-2">{t("sys.location.field.country")}</FormLabel>
									<div className="col-span-3 space-y-1">
										<FormControl>
											<Input {...field} />
										</FormControl>
										<FormMessage />
									</div>
								</FormItem>
							)}
						/>
						<FormField
							control={form.control}
							name="city"
							render={({ field }) => (
								<FormItem className="grid grid-cols-4 items-start gap-3">
									<FormLabel className="col-span-1 pt-2">{t("sys.location.field.city")}</FormLabel>
									<div className="col-span-3 space-y-1">
										<FormControl>
											<Input {...field} />
										</FormControl>
										<FormMessage />
									</div>
								</FormItem>
							)}
						/>
						<FormField
							control={form.control}
							name="code"
							render={({ field }) => (
								<FormItem className="grid grid-cols-4 items-start gap-3">
									<FormLabel className="col-span-1 pt-2">{t("sys.location.field.code")}</FormLabel>
									<div className="col-span-3 space-y-1">
										<FormControl>
											<Input
												{...field}
												className="font-mono uppercase"
												onChange={(e) => field.onChange(e.target.value.toUpperCase())}
											/>
										</FormControl>
										<FormMessage />
									</div>
								</FormItem>
							)}
						/>
						<DialogFooter>
							<Button type="button" variant="outline" onClick={onClose} disabled={isSubmitting}>
								{t("common.cancelText")}
							</Button>
							<Button type="submit" disabled={isSubmitting}>
								{isSubmitting && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
								{t("common.saveText")}
							</Button>
						</DialogFooter>
					</form>
				</Form>
			</DialogContent>
		</Dialog>
	);
}
