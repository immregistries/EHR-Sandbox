import {inject, Injectable} from '@angular/core';
import {FormArray, FormBuilder, FormControl, FormGroup, ValidatorFn, Validators} from '@angular/forms';
import {FormConfig, FormField, FormType} from './form-config';

/** Reads a dotted path, e.g. 'nextOfKin.nameFirst'. */
function readPath(source: Record<string, unknown>, path: string): unknown {
  return path
    .split('.')
    .reduce<unknown>(
      (value, key) => (value && typeof value === 'object'
        ? (value as Record<string, unknown>)[key]
        : undefined),
      source,
    );
}

/** Writes a dotted path, creating intermediate objects. */
function writePath(target: Record<string, unknown>, path: string, value: unknown): void {
  const keys = path.split('.');
  const last = keys.pop() as string;
  let cursor = target;
  for (const key of keys) {
    cursor[key] = (cursor[key] as Record<string, unknown>) ?? {};
    cursor = cursor[key] as Record<string, unknown>;
  }
  cursor[last] = value;
}

/**
 * Builds a reactive form from a declarative FormConfig, and keeps it in sync
 * when required-field rules are lifted.
 *
 * Required rules are tracked separately from other validators so lifting them
 * does not discard a field's custom validation.
 */
@Injectable({providedIn: 'root'})
export class FormEngineService {
  private readonly fb = inject(FormBuilder);

  /** Builds a FormGroup for one entity from its config. */
  build<T extends object>(config: FormConfig<T>, value: Partial<T> = {}): FormGroup {
    const group: Record<string, FormControl | FormArray> = {};
    for (const card of config) {
      for (const field of card.fields) {
        const current = (value as Record<string, unknown>)[field.attributeName];
        group[field.attributeName] =
          field.type === FormType.list
            ? this.buildList(field, Array.isArray(current) ? current : [])
            : this.buildControl(field, current);
      }
    }
    return this.fb.group(group);
  }

  /**
   * Repopulates an existing form from a value. List fields are rebuilt so the
   * number of entry groups matches the incoming data.
   */
  reset<T extends object>(form: FormGroup, config: FormConfig<T>, value: Partial<T>): void {
    const source = (value ?? {}) as Record<string, unknown>;
    for (const card of config) {
      for (const field of card.fields) {
        const control = form.get(field.attributeName);
        if (!control) {
          continue;
        }
        if (field.type === FormType.list) {
          const array = control as FormArray;
          array.clear({emitEvent: false});
          const raw = source[field.attributeName];
          const items = Array.isArray(raw) ? raw : [];
          const entries = field.singleEntry
            ? [items[0] ?? {...(field.itemDefault ?? {})}]
            : items;
          for (const item of entries) {
            array.push(this.buildListItem(field, item), {emitEvent: false});
          }
          // Each push was silent to avoid a storm of events; emit once now so
          // views bound to this array re-render with the new controls.
          array.updateValueAndValidity();
          continue;
        }
        (control as FormControl).setValue(source[field.attributeName] ?? null, {
          emitEvent: false,
        });
      }
    }
    form.markAsPristine();
    form.markAsUntouched();
    form.updateValueAndValidity();
  }

  /**
   * A FormArray of FormGroups, one per entry of a repeating field.
   * A single-entry field always holds exactly one group, so the template
   * never has to create one mid-render.
   */
  buildList(field: FormField<never>, items: unknown[]): FormArray {
    const source = field.singleEntry ? [items[0] ?? {...(field.itemDefault ?? {})}] : items;
    return this.fb.array(source.map((item) => this.buildListItem(field, item)));
  }

  /** One entry of a repeating field. */
  buildListItem(field: FormField<never>, item: unknown = {}): FormGroup {
    const source = (item ?? {}) as Record<string, unknown>;
    const group: Record<string, FormControl> = {};
    for (const sub of field.itemFields ?? []) {
      group[sub.attributeName] = this.buildControl(sub, readPath(source, sub.path ?? sub.attributeName));
    }
    return this.fb.group(group);
  }

  /** Appends a new, empty entry to a repeating field. */
  addListItem(array: FormArray, field: FormField<never>): void {
    array.push(this.buildListItem(field, {...(field.itemDefault ?? {})}));
  }

  private buildControl(field: FormField<never>, value: unknown): FormControl {
    const control = this.fb.control({value: value ?? null, disabled: field.disabled === true});
    control.setValidators(this.validatorsFor(field, false));
    return control;
  }

  private validatorsFor(field: FormField<never>, liftRequired: boolean): ValidatorFn[] {
    const validators: ValidatorFn[] = [];
    if (field.required && !liftRequired) {
      validators.push(Validators.required);
    }
    if (field.validator) {
      validators.push(field.validator);
    }
    return validators;
  }

  /**
   * Re-applies validators across a built form. Called when the user toggles
   * "lift required rules", so the same form instance (and its current values)
   * is reused rather than rebuilt.
   */
  applyRequiredRules<T extends object>(
    form: FormGroup,
    config: FormConfig<T>,
    liftRequired: boolean,
  ): void {
    for (const card of config) {
      for (const field of card.fields) {
        const control = form.get(field.attributeName);
        if (!control) {
          continue;
        }
        if (field.type === FormType.list) {
          for (const entry of (control as FormArray).controls) {
            this.applyItemRules(entry as FormGroup, field, liftRequired);
          }
          continue;
        }
        control.setValidators(this.validatorsFor(field, liftRequired));
        // Must emit: views derive "is required" and error visibility from
        // control events, so suppressing them leaves a stale display.
        control.updateValueAndValidity();
      }
    }
  }

  private applyItemRules(group: FormGroup, field: FormField<never>, liftRequired: boolean): void {
    for (const sub of field.itemFields ?? []) {
      const control = group.get(sub.attributeName);
      if (control) {
        control.setValidators(this.validatorsFor(sub, liftRequired));
        control.updateValueAndValidity();
      }
    }
  }

  /**
   * Reads the form back as a partial entity, dropping empty strings so the
   * API receives absent values rather than blanks, and restoring nested
   * shapes for fields declared with a `path`.
   */
  toEntity<T extends object>(form: FormGroup, config: FormConfig<T>): Partial<T> {
    const raw = form.getRawValue() as Record<string, unknown>;
    for (const card of config) {
      for (const field of card.fields) {
        if (field.type !== FormType.list || !this.hasPaths(field)) {
          continue;
        }
        const entries = (raw[field.attributeName] as Record<string, unknown>[]) ?? [];
        raw[field.attributeName] = entries.map((entry) => {
          const nested: Record<string, unknown> = {};
          for (const sub of field.itemFields ?? []) {
            writePath(nested, sub.path ?? sub.attributeName, entry[sub.attributeName]);
          }
          return nested;
        });
      }
    }
    return (this.prune(raw) ?? {}) as Partial<T>;
  }

  private hasPaths(field: FormField<never>): boolean {
    return (field.itemFields ?? []).some((sub) => sub.path !== undefined);
  }

  private prune(value: unknown): unknown {
    if (Array.isArray(value)) {
      const items = value.map((item) => this.prune(item)).filter((item) => item !== undefined);
      return items.length ? items : undefined;
    }
    if (value instanceof Date) {
      return value;
    }
    if (value && typeof value === 'object') {
      const result: Record<string, unknown> = {};
      for (const [key, raw] of Object.entries(value as Record<string, unknown>)) {
        const pruned = this.prune(raw);
        if (pruned !== undefined) {
          result[key] = pruned;
        }
      }
      return Object.keys(result).length ? result : undefined;
    }
    return value === '' || value === null ? undefined : value;
  }
}
