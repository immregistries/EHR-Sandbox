import {FormCard, FormConfig, FormField} from './form-config';

/**
 * Tenant flavors change how a form behaves — the backend encodes them in the
 * tenant name, so a tenant called "SINGLENAME Hospital" has the SINGLENAME
 * flavor active.
 *
 * Fields declare their own adjustments under `flavors`, so adding a flavor is
 * a config change rather than a component change.
 */
export function activeFlavorKeys(
  tenantName: string | undefined,
  flavorKeys: string[],
): Set<string> {
  const name = (tenantName ?? '').toUpperCase();
  return new Set(flavorKeys.filter((key) => name.includes(key.toUpperCase())));
}

/** Returns the config as the given flavors require it. */
export function applyFlavors<T>(config: FormConfig<T>, active: Set<string>): FormConfig<T> {
  if (!active.size) {
    return config;
  }
  return config.map((card) => applyToCard(card, active));
}

function applyToCard<T>(card: FormCard<T>, active: Set<string>): FormCard<T> {
  const fields = card.fields
    .map((field) => applyToField(field, active))
    .filter((field): field is FormField<T> => field !== null);
  return {...card, fields};
}

function applyToField<T>(field: FormField<T>, active: Set<string>): FormField<T> | null {
  const overrides = Object.entries(field.flavors ?? {})
    .filter(([key]) => active.has(key))
    .map(([, override]) => override);

  if (!overrides.length) {
    return field;
  }

  let result: FormField<T> = {...field};
  for (const override of overrides) {
    if (override.omit) {
      return null;
    }
    if (override.singleEntry !== undefined) {
      result = {...result, singleEntry: override.singleEntry};
    }
    if (override.required !== undefined) {
      result = {...result, required: override.required};
    }
    if (override.omitItemFields?.length) {
      const omitted = new Set(override.omitItemFields);
      result = {
        ...result,
        itemFields: (result.itemFields ?? []).filter(
          (sub) => !omitted.has(sub.attributeName),
        ),
      };
    }
  }
  return result;
}
