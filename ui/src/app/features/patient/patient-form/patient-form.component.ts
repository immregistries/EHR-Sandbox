import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  inject,
  input,
  linkedSignal,
  output,
  signal,
} from '@angular/core';
import {FormGroup, ReactiveFormsModule} from '@angular/forms';
import {Store} from '@ngrx/store';
import {ButtonModule} from 'primeng/button';
import {MessageModule} from 'primeng/message';
import {EhrPatient, Feedback} from '../../../core/models';
import {PatientService} from '../../../core/services';
import {
  selectRouteFacilityId,
  selectRouteTenantId,
} from '../../../core/store/router.selectors';
import {TenantActions} from '../../../core/store/tenant/tenant.actions';
import {selectCurrentTenant, selectFlavors} from '../../../core/store/tenant/tenant.selectors';
import {EntityFormComponent} from '../../../shared/forms/entity-form.component';
import {FormConfig} from '../../../shared/forms/form-config';
import {activeFlavorKeys, applyFlavors} from '../../../shared/forms/form-flavors';
import {FormEngineService} from '../../../shared/forms/form-engine.service';
import {collectWarnings} from '../../../shared/forms/form-warnings';
import {PATIENT_FORM_CONFIG} from './patient-form.config';

/**
 * Patient create / edit form. Layout and validation come from
 * PATIENT_FORM_CONFIG; this component owns loading, saving and the random
 * fill, so the same shell can host any other entity form.
 */
@Component({
  selector: 'app-patient-form',
  standalone: true,
  imports: [ReactiveFormsModule, ButtonModule, MessageModule, EntityFormComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <form class="patient-form" [formGroup]="form()" (ngSubmit)="submit()">
      <app-entity-form
        [config]="config()"
        [form]="form()"
        [comparison]="comparison()"
        [warnings]="warnings()"
        [allowRandom]="isCreating()"
        (fillRandom)="fillRandom()"
        (jsonChange)="applyJson($event)"
      />

      @if (saveError(); as error) {
        <p-message severity="error" [text]="error" styleClass="full-width" />
      }

      <footer class="form-actions">
        <p-button
          label="Cancel"
          severity="secondary"
          [text]="true"
          (onClick)="cancelled.emit()"
        />
        <p-button
          type="submit"
          [label]="patient() ? 'Save changes' : 'Create patient'"
          icon="pi pi-check"
          [loading]="saving()"
        />
      </footer>
    </form>
  `,
  styleUrl: './patient-form.component.scss',
})
export class PatientFormComponent {
  private readonly store = inject(Store);
  private readonly engine = inject(FormEngineService);
  private readonly patientService = inject(PatientService);

  /** Existing patient when editing; absent when creating. */
  readonly patient = input<EhrPatient | undefined>(undefined);
  /** Registry feedback to surface against the fields it refers to. */
  readonly issues = input<Feedback[]>([]);
  /** A record to compare against, shown as "Previously:" hints. */
  readonly comparedPatient = input<EhrPatient | undefined>(undefined);

  readonly saved = output<EhrPatient | number>();
  readonly cancelled = output<void>();

  /**
   * The layout as the current tenant's flavors require it — a SINGLENAME
   * tenant, for instance, takes a single name with no name type.
   */
  readonly config = computed(() =>
    applyFlavors(PATIENT_FORM_CONFIG, activeFlavorKeys(this.tenantName(), this.flavorKeys())),
  );
  readonly saving = signal(false);
  readonly saveError = signal<string | null>(null);

  private readonly tenantId = this.store.selectSignal(selectRouteTenantId);
  private readonly facilityId = this.store.selectSignal(selectRouteFacilityId);
  // selectSignal must be called in an injection context, never inside a
  // computed — doing so creates a new subscription on every recomputation.
  private readonly tenant = this.store.selectSignal(selectCurrentTenant);
  private readonly flavors = this.store.selectSignal(selectFlavors);
  private readonly tenantName = computed(() => this.tenant()?.nameDisplay);
  private readonly flavorKeys = computed(() => this.flavors().map((flavor) => flavor.key));

  /**
   * Rebuilt whenever the flavored config changes — flavors load
   * asynchronously, so the shape of the form is not known at construction —
   * and repopulated whenever the bound patient changes. Current values are
   * carried across a rebuild so nothing typed is lost.
   */
  readonly form = linkedSignal<FormConfig<EhrPatient>, FormGroup>({
    source: () => this.config(),
    computation: (config, previous) =>
      this.engine.build(
        config,
        (previous?.value?.getRawValue() as Partial<EhrPatient>) ?? this.patient() ?? {},
      ),
  });

  readonly warnings = computed(() => collectWarnings(this.config(), this.issues()));

  /** Generating a record only makes sense when creating a new one. */
  readonly isCreating = computed(() => this.patient() === undefined);

  readonly comparison = computed(() => {
    const other = this.comparedPatient();
    return other ? (other as unknown as Record<string, unknown>) : undefined;
  });

  constructor() {
    // Flavors drive the form's shape, so make sure they are loaded.
    this.store.dispatch(TenantActions.loadFlavors());

    effect(() => {
      const patient = this.patient();
      this.engine.reset(this.form(), this.config(), patient ?? {});
    });
  }

  fillRandom(): void {
    this.patientService
      .readRandom(this.tenantId(), this.facilityId())
      .subscribe((patient) => this.engine.reset(this.form(), this.config(), patient));
  }

  applyJson(value: Record<string, unknown>): void {
    this.engine.reset(this.form(), this.config(), value as Partial<EhrPatient>);
  }

  submit(): void {
    if (this.form().invalid) {
      this.form().markAllAsTouched();
      return;
    }
    const value = this.engine.toEntity<EhrPatient>(this.form(), this.config());
    const existing = this.patient();
    this.saving.set(true);
    this.saveError.set(null);

    const onError = (error: { error?: { message?: string }; message?: string }) => {
      this.saving.set(false);
      this.saveError.set(error?.error?.message ?? error?.message ?? 'Could not save the patient');
    };

    if (existing?.id) {
      this.patientService
        .putPatient(this.tenantId(), this.facilityId(), {...value, id: existing.id} as EhrPatient)
        .subscribe({
          next: (saved) => {
            this.saving.set(false);
            this.saved.emit(saved);
          },
          error: onError,
        });
      return;
    }

    this.patientService
      .postPatient(this.tenantId(), this.facilityId(), value as EhrPatient)
      .subscribe({
        next: (response) => {
          this.saving.set(false);
          this.saved.emit(Number(response.body));
        },
        error: onError,
      });
  }
}
