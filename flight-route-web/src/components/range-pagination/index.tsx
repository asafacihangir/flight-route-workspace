import { ChevronLeft, ChevronRight } from "lucide-react";
import { useTranslation } from "react-i18next";
import { Button } from "@/ui/button";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/ui/select";
import { computeRange } from "./utils";

interface RangePaginationProps {
	current: number;
	pageSize: number;
	total: number;
	pageSizeOptions?: number[];
	onChange: (page: number, pageSize: number) => void;
}

const DEFAULT_PAGE_SIZES = [10, 20, 50, 100];

export function RangePagination({
	current,
	pageSize,
	total,
	pageSizeOptions = DEFAULT_PAGE_SIZES,
	onChange,
}: RangePaginationProps) {
	const { t } = useTranslation();
	const { start, end } = computeRange(current, pageSize, total);
	const isEmpty = total <= 0;
	const prevDisabled = isEmpty || current <= 1;
	const nextDisabled = isEmpty || current * pageSize >= total;

	const handleSizeChange = (value: string) => {
		const next = Number(value);
		if (!Number.isFinite(next) || next === pageSize) return;
		onChange(1, next);
	};

	return (
		<div className="flex items-center gap-3 text-sm">
			<span className="text-muted-foreground">{t("sys.pagination.rowsPerPage")}</span>
			<Select value={String(pageSize)} onValueChange={handleSizeChange} disabled={isEmpty}>
				<SelectTrigger className="h-8 w-[72px]">
					<SelectValue />
				</SelectTrigger>
				<SelectContent>
					{pageSizeOptions.map((opt) => (
						<SelectItem key={opt} value={String(opt)}>
							{opt}
						</SelectItem>
					))}
				</SelectContent>
			</Select>
			<span className="tabular-nums">
				{start} – {end} {t("sys.pagination.of")} {total}
			</span>
			<Button
				variant="ghost"
				size="icon"
				className="h-8 w-8"
				disabled={prevDisabled}
				onClick={() => onChange(current - 1, pageSize)}
				aria-label={t("sys.pagination.prev")}
			>
				<ChevronLeft className="h-4 w-4" />
			</Button>
			<Button
				variant="ghost"
				size="icon"
				className="h-8 w-8"
				disabled={nextDisabled}
				onClick={() => onChange(current + 1, pageSize)}
				aria-label={t("sys.pagination.next")}
			>
				<ChevronRight className="h-4 w-4" />
			</Button>
		</div>
	);
}
