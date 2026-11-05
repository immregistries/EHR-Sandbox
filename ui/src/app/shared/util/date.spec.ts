import {parseApiDate} from './date';

describe('parseApiDate', () => {
  it('keeps a date-only string on the day it names, regardless of timezone', () => {
    const parsed = parseApiDate('1985-12-09');

    // new Date('1985-12-09') would be UTC midnight, which renders as the
    // previous day anywhere west of UTC.
    expect(parsed?.getFullYear()).toBe(1985);
    expect(parsed?.getMonth()).toBe(11);
    expect(parsed?.getDate()).toBe(9);
  });

  it('passes a Date through unchanged', () => {
    const date = new Date(2020, 0, 15);

    expect(parseApiDate(date)).toBe(date);
  });

  it('parses a full timestamp normally', () => {
    const parsed = parseApiDate('2024-06-01T10:30:00Z');

    expect(parsed?.toISOString()).toBe('2024-06-01T10:30:00.000Z');
  });

  it('returns undefined for empty or invalid input', () => {
    expect(parseApiDate(undefined)).toBeUndefined();
    expect(parseApiDate(null)).toBeUndefined();
    expect(parseApiDate('')).toBeUndefined();
    expect(parseApiDate('not-a-date')).toBeUndefined();
    expect(parseApiDate(new Date('nope'))).toBeUndefined();
  });
});
