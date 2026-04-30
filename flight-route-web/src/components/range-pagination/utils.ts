export function computeRange(current: number, pageSize: number, total: number) {
	if (total <= 0) return { start: 0, end: 0 };
	const start = (current - 1) * pageSize + 1;
	const end = Math.min(current * pageSize, total);
	return { start, end };
}
