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
import {Clinician} from '../../../core/models';
import {ClinicianService} from '../../../core/services';
import {selectRouteTenantId} from '../../../core/store/router.selectors';
import {EntityFormComponent} from '../../../shared/forms/entity-form.component';
import {FormConfig} from '../../../shared/forms/form-config';
import {FormEngineService} from '../../../shared/forms/form-engine.service';
import {CLINICIAN_FORM_CONFIG} from './clinician-form.config';

/** Clinician create / edit form, built on the shared form engine. */
@Component({
  selector: 'app-clinician-form',
  standalone: true,
  imports: [ReactiveFormsModule, ButtonModule, MessageModule, EntityFormComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <form class="clinician-form" [formGroup]="form()" (ngSubmit)="submit()">
      <app-entity-form
        [config]="config"
        [form]="form()"
        [allowRandom]="isCreating()"
        (fillRandom)="fillRandom()"
        (jsonChange)="applyJson($event)"
      />

      @if (saveError(); as error) {
        <p-message severity="error" [text]="error" styleClass="full-width" />
      }

      <footer class="form-actions">
        <p-button label="Cancel" severity="secondary" [text]="true" (onClick)="cancelled.emit()" />
        <p-button
          type="submit"
          [label]="clinician() ? 'Save changes' : 'Create clinician'"
          icon="pi pi-check"
          [loading]="saving()"
        />
      </footer>
    </form>
  `,
  styleUrl: './clinician-form.component.scss',
})
export class ClinicianFormComponent {
  private readonly store = inject(Store);
  private readonly engine = inject(FormEngineService);
  private readonly clinicianService = inject(ClinicianService);

  readonly clinician = input<Clinician | undefined>(undefined);

  readonly saved = output<Clinician>();
  readonly cancelled = output<void>();

  readonly config: FormConfig<Clinician> = CLINICIAN_FORM_CONFIG;
  readonly saving = signal(false);
  readonly saveError = signal<string | null>(null);

  private readonly tenantId = this.store.selectSignal(selectRouteTenantId);

  readonly isCreating = computed(() => this.clinician() === undefined);

  readonly form = linkedSignal<FormConfig<Clinician>, FormGroup>({
    source: () => this.config,
    computation: (config, previous) =>
      this.engine.build(
        config,
        (previous?.value?.getRawValue() as Partial<Clinician>) ?? this.clinician() ?? {},
      ),
  });

  constructor() {
    effect(() => {
      const clinician = this.clinician();
      this.engine.reset(this.form(), this.config, clinician ?? {});
    });
  }

  fillRandom(): void {
    this.clinicianService
      .random(this.tenantId())
      .subscribe((clinician) => this.engine.reset(this.form(), this.config, clinician));
  }

  applyJson(value: Record<string, unknown>): void {
    this.engine.reset(this.form(), this.config, value as Partial<Clinician>);
  }

  submit(): void {
    if (this.form().invalid) {
      this.form().markAllAsTouched();
      return;
    }
    const value = this.engine.toEntity<Clinician>(this.form(), this.config);
    const existing = this.clinician();
    this.saving.set(true);
    this.saveError.set(null);

    const request = existing?.id
      ? this.clinicianService.putClinician(this.tenantId(), {...value, id: existing.id} as Clinician)
      : this.clinicianService.postClinician(this.tenantId(), value as Clinician);

    request.subscribe({
      next: (saved) => {
        this.saving.set(false);
        this.saved.emit(saved);
      },
      error: (error: { error?: { message?: string }; message?: string }) => {
        this.saving.set(false);
        this.saveError.set(
          error?.error?.message ?? error?.message ?? 'Could not save the clinician',
        );
      },
    });
  }
}
