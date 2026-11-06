import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  inject,
  input,
  linkedSignal,
  output,
  signal
} from '@angular/core';
import {FormGroup, ReactiveFormsModule} from '@angular/forms';
import {Store} from '@ngrx/store';
import {ButtonModule} from 'primeng/button';
import {MessageModule} from 'primeng/message';
import {Feedback, VaccinationEvent} from '../../../core/models';
import {VaccinationService} from '../../../core/services';
import {ClinicianActions} from '../../../core/store/clinician/clinician.actions';
import {selectRouteFacilityId, selectRoutePatientId, selectRouteTenantId,} from '../../../core/store/router.selectors';
import {EntityFormComponent} from '../../../shared/forms/entity-form.component';
import {FormConfig} from '../../../shared/forms/form-config';
import {FormEngineService} from '../../../shared/forms/form-engine.service';
import {collectWarnings} from '../../../shared/forms/form-warnings';
import {VACCINATION_FORM_CONFIG} from './vaccination-form.config';

/**
 * Vaccination create / edit form, built on the shared form engine.
 *
 * A historical record (primarySource false) is allowed to be incomplete, so
 * required rules are lifted automatically unless it is a new administration.
 */
@Component({
  selector: 'app-vaccination-form',
  standalone: true,
  imports: [ReactiveFormsModule, ButtonModule, MessageModule, EntityFormComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <form class="vaccination-form" [formGroup]="form()" (ngSubmit)="submit()">
      <app-entity-form
        [config]="config"
        [form]="form()"
        [warnings]="warnings()"
        [allowRandom]="isCreating()"
        [liftRequired]="liftRequired()"
        (liftRequiredChange)="liftRequired.set($event)"
        (fillRandom)="fillRandom()"
        (jsonChange)="applyJson($event)"
      />

      @if (isHistorical()) {
        <p-message
          severity="info"
          text="Historical record — required-field rules do not apply."
          styleClass="full-width"
        />
      }

      @if (saveError(); as error) {
        <p-message severity="error" [text]="error" styleClass="full-width" />
      }

      <footer class="form-actions">
        <p-button label="Cancel" severity="secondary" [text]="true" (onClick)="cancelled.emit()" />
        <p-button
          type="submit"
          [label]="vaccination() ? 'Save changes' : 'Create immunization'"
          icon="pi pi-check"
          [loading]="saving()"
        />
      </footer>
    </form>
  `,
  styleUrl: './vaccination-form.component.scss',
})
export class VaccinationFormComponent {
  private readonly store = inject(Store);
  private readonly engine = inject(FormEngineService);
  private readonly vaccinationService = inject(VaccinationService);

  readonly vaccination = input<VaccinationEvent | undefined>(undefined);
  readonly issues = input<Feedback[]>([]);

  readonly saved = output<VaccinationEvent | number>();
  readonly cancelled = output<void>();

  readonly config: FormConfig<VaccinationEvent> = VACCINATION_FORM_CONFIG;
  readonly saving = signal(false);
  readonly saveError = signal<string | null>(null);

  private readonly tenantId = this.store.selectSignal(selectRouteTenantId);
  private readonly facilityId = this.store.selectSignal(selectRouteFacilityId);
  private readonly patientId = this.store.selectSignal(selectRoutePatientId);

  readonly isCreating = computed(() => this.vaccination() === undefined);

  readonly form = linkedSignal<FormConfig<VaccinationEvent>, FormGroup>({
    source: () => this.config,
    computation: (config, previous) =>
      this.engine.build(
        config,
        (previous?.value?.getRawValue() as Partial<VaccinationEvent>) ??
        this.vaccination() ??
        {},
      ),
  });

  readonly warnings = computed(() => collectWarnings(this.config, this.issues()));

  /**
   * Mirrors the old form: every field is required for a new administration,
   * none for a historical record.
   */
  readonly isHistorical = signal(false);
  readonly liftRequired = signal(false);

  constructor() {
    this.store.dispatch(ClinicianActions.refresh());

    effect(() => {
      const vaccination = this.vaccination();
      this.engine.reset(this.form(), this.config, vaccination ?? {});
    });

    // Follow the record nature as the user changes it.
    effect((onCleanup) => {
      const control = this.form().get('primarySource');
      const apply = (value: unknown) => {
        const historical = value === false || value === 'false';
        this.isHistorical.set(historical);
        this.liftRequired.set(historical);
      };
      apply(control?.value);
      const subscription = control?.valueChanges.subscribe(apply);
      onCleanup(() => subscription?.unsubscribe());
    });
  }

  fillRandom(): void {
    this.vaccinationService
      .readRandom(this.tenantId(), this.facilityId(), this.patientId())
      .subscribe((vaccination) => this.engine.reset(this.form(), this.config, vaccination));
  }

  applyJson(value: Record<string, unknown>): void {
    this.engine.reset(this.form(), this.config, value as Partial<VaccinationEvent>);
  }

  submit(): void {
    if (this.form().invalid) {
      this.form().markAllAsTouched();
      return;
    }
    const value = this.engine.toEntity<VaccinationEvent>(this.form(), this.config);
    const existing = this.vaccination();
    this.saving.set(true);
    this.saveError.set(null);

    const onError = (error: { error?: { message?: string }; message?: string }) => {
      this.saving.set(false);
      this.saveError.set(
        error?.error?.message ?? error?.message ?? 'Could not save the immunization',
      );
    };

    if (existing?.id) {
      this.vaccinationService
        .putVaccination(this.tenantId(), this.facilityId(), this.patientId(), {
          ...value,
          id: existing.id,
        } as VaccinationEvent)
        .subscribe({
          next: (saved) => {
            this.saving.set(false);
            this.saved.emit(saved);
          },
          error: onError,
        });
      return;
    }

    this.vaccinationService
      .postVaccination(this.tenantId(), this.facilityId(), this.patientId(), value as VaccinationEvent)
      .subscribe({
        next: (response) => {
          this.saving.set(false);
          this.saved.emit(Number(response.body));
        },
        error: onError,
      });
  }
}
