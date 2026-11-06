import {Feedback} from '../../core/models';
import {FormConfig, FormType, hl7LocationMatches} from './form-config';

/**
 * Warnings for a form, keyed by attribute name. Repeating fields nest one
 * level further: `{names: {0: {nameLast: ['...']}}}`.
 */
export type FormWarnings = Record<string, string[] | Record<number, Record<string, string[]>>>;

/**
 * Matches registry feedback to the fields it refers to, using the HL7
 * locations declared in the form config. Feedback that matches no field is
 * returned separately so it can still be shown, rather than silently lost.
 */
export function collectWarnings<T>(config: FormConfig<T>, issues: Feedback[]): FormWarnings {
  const warnings: FormWarnings = {};
  if (!issues?.length) {
    return warnings;
  }

  for (const card of config) {
    for (const field of card.fields) {
      if (field.type === FormType.list) {
        const perEntry: Record<number, Record<string, string[]>> = {};
        for (const sub of field.itemFields ?? []) {
          for (const issue of issues) {
            if (!matches(issue, sub.hl7Location ?? field.hl7Location)) {
              continue;
            }
            // HL7 repetitions are 1-based; absent means the first entry.
            const index = Math.max((issue.hl7Locations?.[0]?.fieldRepetition ?? 1) - 1, 0);
            perEntry[index] ??= {};
            perEntry[index][sub.attributeName] ??= [];
            perEntry[index][sub.attributeName].push(describe(issue));
          }
        }
        if (Object.keys(perEntry).length) {
          warnings[field.attributeName] = perEntry;
        }
        continue;
      }

      const matched = issues.filter((issue) => matches(issue, field.hl7Location));
      if (matched.length) {
        warnings[field.attributeName] = matched.map(describe);
      }
    }
  }

  return warnings;
}

/** Feedback that could not be attached to any field in the config. */
export function unmatchedWarnings<T>(config: FormConfig<T>, issues: Feedback[]): Feedback[] {
  const locations = config.flatMap((card) =>
    card.fields.flatMap((field) => [
      field.hl7Location,
      ...(field.itemFields ?? []).map((sub) => sub.hl7Location),
    ]),
  );
  return (issues ?? []).filter(
    (issue) => !locations.some((location) => matches(issue, location)),
  );
}

function matches(issue: Feedback, location: Parameters<typeof hl7LocationMatches>[1]): boolean {
  if (!location || !issue.hl7Locations?.length) {
    return false;
  }
  return issue.hl7Locations.some((candidate) => hl7LocationMatches(candidate, location));
}

function describe(issue: Feedback): string {
  const severity = issue.severity ? `${issue.severity}: ` : '';
  return `${severity}${issue.content ?? issue.code ?? 'Reported by the registry'}`;
}
