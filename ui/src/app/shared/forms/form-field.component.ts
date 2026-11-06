import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  effect,
  inject,
  input,
  signal,
} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {Store} from '@ngrx/store';
import {AutoCompleteModule} from 'primeng/autocomplete';
import {DatePickerModule} from 'primeng/datepicker';
import {InputTextModule} from 'primeng/inputtext';
import {MessageModule} from 'primeng/message';
import {SelectModule} from 'primeng/select';
import {TextareaModule} from 'primeng/textarea';
import {TooltipModule} from 'primeng/tooltip';
import {selectClinicianOptions} from '../../core/store/clinician/clinician.selectors';
import {
  selectCodeSetOptions,
  selectIdentifierTypeOptions,
  selectQualificationOptions,
} from '../../core/store/code-maps/code-maps.selectors';
import {FormField, FormOption, FormType, hl7LocationLabel} from './form-config';

/** hasValidator compares by identity, so the reference must be shared. */
const REQUIRED = Validators.required;

const YES_NO: FormOption[] = [
  {code: 'Y', display: 'Yes'},
  {code: 'N', display: 'No'},
];

const TRUE_FALSE: FormOption[] = [
  {code: 'true', display: 'True'},
  {code: 'false', display: 'False'},
];

/**
 * Renders one field of a FormConfig. Everything field-specific lives here, so
 * entity forms never deal with control types directly.
 */
@Component({
  selector: 'app-form-field',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    AutoCompleteModule,
    DatePickerModule,
    InputTextModule,
    MessageModule,
    SelectModule,
    TextareaModule,
    TooltipModule,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="field" [class.field--invalid]="showError()" [class.field--warned]="warnings().length">
      <label [for]="inputId()">
        {{ field().title }}
        @if (isRequired()) {
          <span class="required" aria-hidden="true">*</span>
        }
        @if (hl7Label()) {
          <span class="hl7" [pTooltip]="'HL7 location ' + hl7Label()" tooltipPosition="top">
            {{ hl7Label() }}
          </span>
        }
      </label>

      @switch (field().type) {
        @case ('date') {
          <p-datepicker
            [inputId]="inputId()"
            [formControl]="control()"
            dateFormat="yy-mm-dd"
            [showIcon]="true"
            [showButtonBar]="true"
            appendTo="body"
          />
        }
        @case ('textarea') {
          <textarea pTextarea [id]="inputId()" [formControl]="control()" rows="3"></textarea>
        }
        @case ('yesNo') {
          <p-select
            [inputId]="inputId()"
            [formControl]="control()"
            [options]="yesNo"
            optionLabel="display"
            optionValue="code"
            placeholder="—"
            [showClear]="true"
            appendTo="body"
          />
        }
        @case ('boolean') {
          <p-select
            [inputId]="inputId()"
            [formControl]="control()"
            [options]="trueFalse"
            optionLabel="display"
            optionValue="code"
            placeholder="—"
            [showClear]="true"
            appendTo="body"
          />
        }
        @case ('select') {
          <p-select
            [inputId]="inputId()"
            [formControl]="control()"
            [options]="field().options ?? []"
            optionLabel="display"
            optionValue="code"
            placeholder="—"
            [showClear]="true"
            appendTo="body"
          />
        }
        @case ('code') {
          <p-select
            [inputId]="inputId()"
            [formControl]="control()"
            [options]="codeOptions()"
            optionLabel="label"
            optionValue="value"
            [filter]="true"
            filterBy="label,value"
            placeholder="—"
            [showClear]="true"
            appendTo="body"
            [virtualScroll]="codeOptions().length > 50"
            [virtualScrollItemSize]="38"
          >
            <ng-template #item let-option>
              <span class="code-option">
                <span class="code-option__label">{{ option.label }}</span>
                <span class="code-option__value">{{ option.value }}</span>
              </span>
            </ng-template>
          </p-select>
        }
        @case ('clinician') {
          <p-select
            [inputId]="inputId()"
            [formControl]="control()"
            [options]="clinicians()"
            optionLabel="label"
            optionValue="id"
            [filter]="true"
            filterBy="label"
            placeholder="—"
            [showClear]="true"
            appendTo="body"
          />
        }
        @default {
          <input
            pInputText
            [id]="inputId()"
            [formControl]="control()"
            [class.short]="field().type === 'short'"
          />
        }
      }

      @if (showError()) {
        <small class="error">{{ errorText() }}</small>
      } @else if (hintText()) {
        <small class="hint">{{ hintText() }}</small>
      }

      @if (comparison() !== undefined && comparison() !== null) {
        <small class="hint hint--compare">
          <i class="pi pi-history"></i> Previously: {{ comparison() }}
        </small>
      }

      @for (warning of warnings(); track warning) {
        <small class="warning"><i class="pi pi-exclamation-triangle"></i> {{ warning }}</small>
      }
    </div>
  `,
  styleUrl: './form-field.component.scss',
})
export class FormFieldComponent {
  private readonly store = inject(Store);
  private readonly destroyRef = inject(DestroyRef);

  readonly field = input.required<FormField<never>>();
  readonly group = input.required<FormGroup>();
  /** Previous value of this field, when comparing against another record. */
  readonly comparison = input<unknown>(undefined);
  /** Registry feedback matched to this field's HL7 location. */
  readonly warnings = input<string[]>([]);
  /** Disambiguates ids when the same field appears in several list entries. */
  readonly idPrefix = input('');

  readonly yesNo = YES_NO;
  readonly trueFalse = TRUE_FALSE;

  readonly control = computed(
    () => this.group().get(this.field().attributeName) as FormControl,
  );

  /**
   * A FormControl is not a signal, so `computed()` over its `touched` or
   * validator state would never recompute. Every control event (value,
   * status, touched, pristine) bumps this counter, which the computeds below
   * read to declare the dependency.
   */
  private readonly controlVersion = signal(0);

  constructor() {
    // Re-subscribe whenever the bound control changes.
    effect((onCleanup) => {
      const subscription = this.control()
        .events.pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe(() => this.controlVersion.update((version) => version + 1));
      onCleanup(() => subscription.unsubscribe());
    });
  }

  readonly inputId = computed(() => `${this.idPrefix()}${this.field().attributeName}`);

  readonly hl7Label = computed(() => hl7LocationLabel(this.field().hl7Location));

  readonly isRequired = computed(() => {
    this.controlVersion();
    return this.control()?.hasValidator?.(REQUIRED) ?? false;
  });

  private readonly codeSetOptions = computed(() =>
    this.store.selectSignal(selectCodeSetOptions(this.field().codeMapLabel)),
  );

  private readonly identifierTypes = this.store.selectSignal(selectIdentifierTypeOptions);
  private readonly qualificationTypes = this.store.selectSignal(selectQualificationOptions);

  /**
   * Options for a code field. Most come from the codemaps, but identifier
   * and qualification types are published as bundled FHIR CodeSystems.
   */
  readonly codeOptions = computed(() => {
    switch (this.field().codeSystem) {
      case 'identifierType':
        return this.identifierTypes();
      case 'qualification':
        return this.qualificationTypes();
      default:
        return this.codeSetOptions()();
    }
  });

  readonly clinicians = this.store.selectSignal(selectClinicianOptions);

  readonly showError = computed(() => {
    this.controlVersion();
    const control = this.control();
    return Boolean(control?.invalid && (control.dirty || control.touched));
  });

  readonly errorText = computed(() => {
    this.controlVersion();
    const errors = this.control()?.errors;
    if (!errors) {
      return '';
    }
    if (errors['required']) {
      return `${this.field().title} is required`;
    }
    const custom = errors['customIssue'];
    return typeof custom === 'string' ? custom : 'Invalid value';
  });

  readonly hintText = computed(() => {
    this.controlVersion();
    return this.field().hint?.(this.control()?.value);
  });
}
