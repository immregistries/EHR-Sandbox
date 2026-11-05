/**
 * The API sends date-only values as `YYYY-MM-DD`. `new Date('1985-12-09')`
 * parses those as UTC midnight, which DatePipe then renders in local time —
 * west of UTC that shows the *previous* day. Parse them as a local date so a
 * birth date is the date that was entered, in every timezone.
 */
const DATE_ONLY = /^\d{4}-\d{2}-\d{2}$/;

export function parseApiDate(value: Date | string | null | undefined): Date | undefined {
  if (!value) {
    return undefined;
  }
  if (value instanceof Date) {
    return Number.isNaN(value.getTime()) ? undefined : value;
  }
  if (DATE_ONLY.test(value)) {
    const [year, month, day] = value.split('-').map(Number);
    return new Date(year, month - 1, day);
  }
  const parsed = new Date(value);
  return Number.isNaN(parsed.getTime()) ? undefined : parsed;
}
