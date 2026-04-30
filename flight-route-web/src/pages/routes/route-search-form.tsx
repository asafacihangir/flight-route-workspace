import { format, startOfToday } from "date-fns";
import { CalendarIcon, Check, ChevronsUpDown, Loader2, Search } from "lucide-react";
import { useId, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import type { RouteSearchParams } from "@/api/services/routeService";
import { Button } from "@/ui/button";
import { Calendar } from "@/ui/calendar";
import { Command, CommandEmpty, CommandGroup, CommandInput, CommandItem, CommandList } from "@/ui/command";
import { Popover, PopoverContent, PopoverTrigger } from "@/ui/popover";
import { cn } from "@/utils";
import type { LocationOption } from "./use-locations-options";

interface RouteSearchFormProps {
	options: LocationOption[];
	loading: boolean;
	optionsLoading: boolean;
	onSearch: (params: RouteSearchParams) => void;
	onFiltersChange: () => void;
}

interface FormErrors {
	origin?: string;
	destination?: string;
	date?: string;
	sameLocation?: string;
}

export function RouteSearchForm({ options, loading, optionsLoading, onSearch, onFiltersChange }: RouteSearchFormProps) {
	const { t } = useTranslation();
	const [originId, setOriginId] = useState<number | undefined>();
	const [destinationId, setDestinationId] = useState<number | undefined>();
	const [date, setDate] = useState<Date | undefined>();
	const [errors, setErrors] = useState<FormErrors>({});

	const handleSubmit = (e: React.FormEvent) => {
		e.preventDefault();
		const next: FormErrors = {};
		if (!originId) next.origin = t("sys.routes.error.required");
		if (!destinationId) next.destination = t("sys.routes.error.required");
		if (!date) next.date = t("sys.routes.error.required");
		if (originId && destinationId && originId === destinationId) {
			next.sameLocation = t("sys.routes.error.sameLocation");
		}
		setErrors(next);
		if (Object.keys(next).length > 0) return;

		onSearch({
			originId: originId as number,
			destinationId: destinationId as number,
			date: format(date as Date, "yyyy-MM-dd"),
		});
	};

	return (
		<form onSubmit={handleSubmit} className="grid grid-cols-1 gap-3 md:grid-cols-12">
			<div className="md:col-span-4">
				<LocationField
					label={t("sys.routes.search.origin")}
					placeholder={t("sys.routes.search.placeholder.origin")}
					searchPlaceholder={t("sys.routes.search.placeholder.searchLocation")}
					value={originId}
					onChange={(v) => {
						setOriginId(v);
						setErrors((e) => ({ ...e, origin: undefined, sameLocation: undefined }));
						onFiltersChange();
					}}
					options={options}
					disabled={optionsLoading}
					error={errors.origin}
				/>
			</div>
			<div className="md:col-span-4">
				<LocationField
					label={t("sys.routes.search.destination")}
					placeholder={t("sys.routes.search.placeholder.destination")}
					searchPlaceholder={t("sys.routes.search.placeholder.searchLocation")}
					value={destinationId}
					onChange={(v) => {
						setDestinationId(v);
						setErrors((e) => ({ ...e, destination: undefined, sameLocation: undefined }));
						onFiltersChange();
					}}
					options={options}
					disabled={optionsLoading}
					error={errors.destination ?? errors.sameLocation}
				/>
			</div>
			<div className="md:col-span-2">
				<DateField
					label={t("sys.routes.search.date")}
					placeholder={t("sys.routes.search.placeholder.date")}
					value={date}
					onChange={(d) => {
						setDate(d);
						setErrors((e) => ({ ...e, date: undefined }));
						onFiltersChange();
					}}
					error={errors.date}
				/>
			</div>
			<div className="flex items-end md:col-span-2">
				<Button type="submit" className="w-full" disabled={loading}>
					{loading ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : <Search className="mr-2 h-4 w-4" />}
					{t("sys.routes.search.button")}
				</Button>
			</div>
		</form>
	);
}

interface LocationFieldProps {
	label: string;
	placeholder: string;
	searchPlaceholder: string;
	value?: number;
	onChange: (v: number) => void;
	options: LocationOption[];
	disabled?: boolean;
	error?: string;
}

function LocationField({
	label,
	placeholder,
	searchPlaceholder,
	value,
	onChange,
	options,
	disabled,
	error,
}: LocationFieldProps) {
	const [open, setOpen] = useState(false);
	const triggerId = useId();
	const selected = useMemo(() => options.find((o) => o.id === value), [options, value]);

	return (
		<div className="space-y-1">
			<label htmlFor={triggerId} className="text-sm font-medium">
				{label}
			</label>
			<Popover open={open} onOpenChange={setOpen}>
				<PopoverTrigger asChild>
					<Button
						id={triggerId}
						type="button"
						variant="outline"
						aria-haspopup="listbox"
						disabled={disabled}
						className={cn("w-full justify-between font-normal", error && "border-destructive")}
					>
						{selected ? (
							<span className="truncate">
								<span className="mr-2 font-mono text-xs">{selected.code}</span>
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
					<Command filter={(itemValue, search) => (itemValue.toLowerCase().includes(search.toLowerCase()) ? 1 : 0)}>
						<CommandInput placeholder={searchPlaceholder} />
						<CommandList>
							<CommandEmpty>—</CommandEmpty>
							<CommandGroup>
								{options.map((opt) => {
									const itemValue = `${opt.code} ${opt.name} ${opt.city} ${opt.country}`;
									return (
										<CommandItem
											key={opt.id}
											value={itemValue}
											onSelect={() => {
												onChange(opt.id);
												setOpen(false);
											}}
										>
											<Check className={cn("mr-2 h-4 w-4", value === opt.id ? "opacity-100" : "opacity-0")} />
											<span className="mr-2 w-12 font-mono text-xs">{opt.code}</span>
											<span className="flex-1 truncate">{opt.name}</span>
											<span className="text-xs text-muted-foreground">{opt.city}</span>
										</CommandItem>
									);
								})}
							</CommandGroup>
						</CommandList>
					</Command>
				</PopoverContent>
			</Popover>
			{error && <p className="text-xs text-destructive">{error}</p>}
		</div>
	);
}

interface DateFieldProps {
	label: string;
	placeholder: string;
	value?: Date;
	onChange: (d: Date | undefined) => void;
	error?: string;
}

function DateField({ label, placeholder, value, onChange, error }: DateFieldProps) {
	const [open, setOpen] = useState(false);
	const triggerId = useId();
	const today = startOfToday();

	return (
		<div className="space-y-1">
			<label htmlFor={triggerId} className="text-sm font-medium">
				{label}
			</label>
			<Popover open={open} onOpenChange={setOpen}>
				<PopoverTrigger asChild>
					<Button
						id={triggerId}
						type="button"
						variant="outline"
						className={cn("w-full justify-start font-normal", error && "border-destructive")}
					>
						<CalendarIcon className="mr-2 h-4 w-4" />
						{value ? format(value, "yyyy-MM-dd") : <span className="text-muted-foreground">{placeholder}</span>}
					</Button>
				</PopoverTrigger>
				<PopoverContent className="w-auto p-0" align="start">
					<Calendar
						mode="single"
						selected={value}
						onSelect={(d) => {
							onChange(d);
							setOpen(false);
						}}
						disabled={(d) => d < today}
						initialFocus
					/>
				</PopoverContent>
			</Popover>
			{error && <p className="text-xs text-destructive">{error}</p>}
		</div>
	);
}
