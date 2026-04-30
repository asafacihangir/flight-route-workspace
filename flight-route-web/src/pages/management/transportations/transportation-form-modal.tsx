import { zodResolver } from "@hookform/resolvers/zod";
import { Check, ChevronsUpDown, Loader2 } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import { z } from "zod";
import type { Location, TransportationCreateInput } from "#/entity";
import { type OperatingDay, TransportationType } from "#/enum";
import { Button } from "@/ui/button";
import {
	Command,
	CommandEmpty,
	CommandGroup,
	CommandInput,
	CommandItem,
	CommandList,
} from "@/ui/command";
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle } from "@/ui/dialog";
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/ui/form";
import { Popover, PopoverContent, PopoverTrigger } from "@/ui/popover";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/ui/select";
import { cn } from "@/utils";

export type TransportationFormMode = "create" | "edit";

interface TransportationFormModalProps {
	open: boolean;
	mode: TransportationFormMode;
	locations: Location[];
	defaultValues?: Partial<TransportationCreateInput> | null;
	onSubmit: (data: TransportationCreateInput) => Promise<void>;
	onClose: () => void;
}

const ALL_DAYS: OperatingDay[] = [1, 2, 3, 4, 5, 6, 7];
const WEEKDAYS: OperatingDay[] = [1, 2, 3, 4, 5];
const WEEKENDS: OperatingDay[] = [6, 7];

const TYPE_DOT: Record<TransportationType, string> = {
	[TransportationType.FLIGHT]: "bg-blue-500",
	[TransportationType.BUS]: "bg-green-500",
	[TransportationType.SUBWAY]: "bg-purple-500",
	[TransportationType.UBER]: "bg-zinc-900 dark:bg-zinc-100",
};

type TransportationFormValues = {
	originId?: number;
	destinationId?: number;
	type: TransportationType;
	operatingDays: OperatingDay[];
};

const EMPTY_DEFAULTS: TransportationFormValues = {
	originId: undefined,
	destinationId: undefined,
	type: TransportationType.FLIGHT,
	operatingDays: [],
};

export function TransportationFormModal({
	open,
	mode,
	locations,
	defaultValues,
	onSubmit,
	onClose,
}: TransportationFormModalProps) {
	const { t } = useTranslation();

	const schema = useMemo(
		() =>
			z
				.object({
					originId: z.number({ required_error: t("sys.transportation.error.required") }).int().positive(t("sys.transportation.error.required")),
					destinationId: z.number({ required_error: t("sys.transportation.error.required") }).int().positive(t("sys.transportation.error.required")),
					type: z.nativeEnum(TransportationType),
					operatingDays: z
						.array(z.union([z.literal(1), z.literal(2), z.literal(3), z.literal(4), z.literal(5), z.literal(6), z.literal(7)]))
						.min(1, t("sys.transportation.error.operatingDaysRequired")),
				})
				.refine((data) => data.originId !== data.destinationId, {
					message: t("sys.transportation.error.sameOriginDestination"),
					path: ["destinationId"],
				}),
		[t],
	);

	const form = useForm<TransportationFormValues>({
		resolver: zodResolver(schema) as never,
		defaultValues: defaultValues ?? EMPTY_DEFAULTS,
		mode: "onSubmit",
	});

	useEffect(() => {
		if (!open) return;
		form.reset(mode === "edit" && defaultValues ? { ...EMPTY_DEFAULTS, ...defaultValues } : EMPTY_DEFAULTS);
	}, [open, mode, defaultValues, form]);

	const isSubmitting = form.formState.isSubmitting;

	const handleSubmit = form.handleSubmit(async (data) => {
		await onSubmit(data as TransportationCreateInput);
	});

	const originId = form.watch("originId");
	const destinationId = form.watch("destinationId");
	const operatingDays = form.watch("operatingDays") ?? [];

	useEffect(() => {
		if (originId && destinationId && originId === destinationId) {
			form.setValue("destinationId", undefined, { shouldValidate: false });
		}
	}, [originId, destinationId, form]);

	const setDays = (next: OperatingDay[]) => {
		form.setValue("operatingDays", next, { shouldValidate: form.formState.isSubmitted });
	};

	const toggleDay = (day: OperatingDay) => {
		const set = new Set(operatingDays);
		if (set.has(day)) set.delete(day);
		else set.add(day);
		const sorted = Array.from(set).sort((a, b) => a - b) as OperatingDay[];
		setDays(sorted);
	};

	return (
		<Dialog
			open={open}
			onOpenChange={(next) => {
				if (!next && !isSubmitting) onClose();
			}}
		>
			<DialogContent className="sm:max-w-[560px]">
				<DialogHeader>
					<DialogTitle>{mode === "create" ? t("sys.transportation.new") : t("sys.transportation.edit")}</DialogTitle>
				</DialogHeader>
				<Form {...form}>
					<form onSubmit={handleSubmit} className="space-y-4">
						<FormField
							control={form.control}
							name="originId"
							render={({ field }) => (
								<FormItem className="grid grid-cols-4 items-start gap-3">
									<FormLabel className="col-span-1 pt-2">{t("sys.transportation.field.origin")}</FormLabel>
									<div className="col-span-3 space-y-1">
										<FormControl>
											<LocationCombobox
												value={field.value}
												onChange={field.onChange}
												locations={locations}
												excludeId={destinationId}
												placeholder={t("sys.transportation.field.originPlaceholder")}
												searchPlaceholder={t("sys.transportation.field.searchLocation")}
											/>
										</FormControl>
										<FormMessage />
									</div>
								</FormItem>
							)}
						/>

						<FormField
							control={form.control}
							name="destinationId"
							render={({ field }) => (
								<FormItem className="grid grid-cols-4 items-start gap-3">
									<FormLabel className="col-span-1 pt-2">{t("sys.transportation.field.destination")}</FormLabel>
									<div className="col-span-3 space-y-1">
										<FormControl>
											<LocationCombobox
												value={field.value}
												onChange={field.onChange}
												locations={locations}
												excludeId={originId}
												placeholder={t("sys.transportation.field.destinationPlaceholder")}
												searchPlaceholder={t("sys.transportation.field.searchLocation")}
											/>
										</FormControl>
										<FormMessage />
									</div>
								</FormItem>
							)}
						/>

						<FormField
							control={form.control}
							name="type"
							render={({ field }) => (
								<FormItem className="grid grid-cols-4 items-start gap-3">
									<FormLabel className="col-span-1 pt-2">{t("sys.transportation.field.type")}</FormLabel>
									<div className="col-span-3 space-y-1">
										<FormControl>
											<Select value={field.value} onValueChange={field.onChange}>
												<SelectTrigger className="w-full">
													<SelectValue />
												</SelectTrigger>
												<SelectContent>
													{Object.values(TransportationType).map((tp) => (
														<SelectItem key={tp} value={tp}>
															<span className="flex items-center gap-2">
																<span className={cn("inline-block size-2 rounded-full", TYPE_DOT[tp])} />
																{t(`sys.transportation.type.${tp}`)}
															</span>
														</SelectItem>
													))}
												</SelectContent>
											</Select>
										</FormControl>
										<FormMessage />
									</div>
								</FormItem>
							)}
						/>

						<FormField
							control={form.control}
							name="operatingDays"
							render={() => (
								<FormItem className="grid grid-cols-4 items-start gap-3">
									<FormLabel className="col-span-1 pt-2">{t("sys.transportation.field.operatingDays")}</FormLabel>
									<div className="col-span-3 space-y-2">
										<div className="flex gap-1">
											{ALL_DAYS.map((day) => {
												const active = operatingDays.includes(day);
												return (
													<button
														key={day}
														type="button"
														onClick={() => toggleDay(day)}
														className={cn(
															"flex h-8 w-9 items-center justify-center rounded-md border text-xs font-medium transition-colors",
															active
																? "border-primary bg-primary text-primary-foreground"
																: "border-border bg-background text-muted-foreground hover:bg-accent",
														)}
														aria-pressed={active}
														aria-label={t(`sys.transportation.day.long.${day}`)}
													>
														{t(`sys.transportation.day.short.${day}`)}
													</button>
												);
											})}
										</div>
										<div className="flex flex-wrap gap-2">
											<Button type="button" size="sm" variant="outline" onClick={() => setDays([...ALL_DAYS])}>
												{t("sys.transportation.preset.everyDay")}
											</Button>
											<Button type="button" size="sm" variant="outline" onClick={() => setDays([...WEEKDAYS])}>
												{t("sys.transportation.preset.weekdays")}
											</Button>
											<Button type="button" size="sm" variant="outline" onClick={() => setDays([...WEEKENDS])}>
												{t("sys.transportation.preset.weekends")}
											</Button>
										</div>
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

interface LocationComboboxProps {
	value: number | undefined;
	onChange: (value: number) => void;
	locations: Location[];
	excludeId?: number;
	placeholder: string;
	searchPlaceholder: string;
}

function LocationCombobox({ value, onChange, locations, excludeId, placeholder, searchPlaceholder }: LocationComboboxProps) {
	const [open, setOpen] = useState(false);
	const options = useMemo(() => locations.filter((l) => l.id !== excludeId), [locations, excludeId]);
	const selected = locations.find((l) => l.id === value);

	return (
		<Popover open={open} onOpenChange={setOpen}>
			<PopoverTrigger asChild>
				<Button type="button" variant="outline" role="combobox" className="w-full justify-between font-normal">
					{selected ? (
						<span className="truncate">
							<span className="font-mono text-xs mr-2">{selected.code}</span>
							{selected.name}
							<span className="text-muted-foreground"> · {selected.city}</span>
						</span>
					) : (
						<span className="text-muted-foreground">{placeholder}</span>
					)}
					<ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
				</Button>
			</PopoverTrigger>
			<PopoverContent className="w-[--radix-popover-trigger-width] p-0" align="start">
				<Command
					filter={(itemValue, search) => {
						const haystack = itemValue.toLowerCase();
						return haystack.includes(search.toLowerCase()) ? 1 : 0;
					}}
				>
					<CommandInput placeholder={searchPlaceholder} />
					<CommandList>
						<CommandEmpty>—</CommandEmpty>
						<CommandGroup>
							{options.map((loc) => {
								const itemValue = `${loc.code} ${loc.name} ${loc.city} ${loc.country}`;
								return (
									<CommandItem
										key={loc.id}
										value={itemValue}
										onSelect={() => {
											onChange(loc.id);
											setOpen(false);
										}}
									>
										<Check className={cn("mr-2 h-4 w-4", value === loc.id ? "opacity-100" : "opacity-0")} />
										<span className="font-mono text-xs mr-2 w-12">{loc.code}</span>
										<span className="flex-1 truncate">{loc.name}</span>
										<span className="text-muted-foreground text-xs">{loc.city}</span>
									</CommandItem>
								);
							})}
						</CommandGroup>
					</CommandList>
				</Command>
			</PopoverContent>
		</Popover>
	);
}
