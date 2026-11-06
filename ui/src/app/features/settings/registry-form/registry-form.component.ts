import {ChangeDetectionStrategy, Component, effect, inject, input, linkedSignal, output, signal} from '@angular/core';
import {FormGroup, FormsModule, ReactiveFormsModule} from '@angular/forms';
import {Store} from '@ngrx/store';
import {ButtonModule} from 'primeng/button';
import {CheckboxModule} from 'primeng/checkbox';
import {MessageModule} from 'primeng/message';
import {TooltipModule} from 'primeng/tooltip';
import {ImmunizationRegistry} from '../../../core/models';
import {ImmunizationRegistryService} from '../../../core/services';
import {RegistryActions} from '../../../core/store/registry/registry.actions';
import {EntityFormComponent} from '../../../shared/forms/entity-form.component';
import {FormFooterComponent} from '../../../shared/forms/form-footer.component';
import {FormConfig} from '../../../shared/forms/form-config';
import {FormEngineService} from '../../../shared/forms/form-engine.service';
import {REGISTRY_FORM_CONFIG} from './registry-form.config';

/** Connection settings for one immunization registry. */
@Component({
  selector: 'app-registry-form',
  standalone: true,
  imports: [
    FormsModule,
    ReactiveFormsModule,
    ButtonModule,
    CheckboxModule,
    MessageModule,
    TooltipModule,
    EntityFormComponent,
    FormFooterComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <form class="registry-form" [formGroup]="form()" (ngSubmit)="submit()">
      <div class="form-dialog-body">
        <app-entity-form [config]="config" [form]="form()" />

      <p-message severity="info" styleClass="full-width">
        Basic authentication is the default. Leaving the username empty activates token
        bearer authentication, using the password as the token.
      </p-message>

      @if (saveError(); as error) {
        <p-message severity="error" [text]="error" styleClass="full-width" />
      }

      </div>

      <app-form-footer [allowLift]="false" [allowRandom]="false">
        <ng-container actions>
        <p-checkbox
          inputId="default-registry"
          [binary]="true"
          [ngModel]="isDefault()"
          (ngModelChange)="isDefault.set($event)"
          [ngModelOptions]="{standalone: true}"
        />
        <label for="default-registry" class="default-label">Selected by default</label>

        <span class="actions-spacer"></span>

        @if (registry()?.id) {
          <p-button
            label="Create copy"
            icon="pi pi-copy"
            severity="secondary"
            [outlined]="true"
            pTooltip="Duplicate these settings as a new registry"
            tooltipPosition="top"
            (onClick)="duplicate.emit()"
          />
          <p-button
            label="Save & check"
            icon="pi pi-link"
            severity="secondary"
            [outlined]="true"
            [loading]="saving()"
            pTooltip="Save, then test connectivity and authentication"
            tooltipPosition="top"
            (onClick)="submit(true)"
          />
          <p-button
            label="Delete"
            icon="pi pi-trash"
            severity="danger"
            [text]="true"
            (onClick)="remove()"
          />
        }
        <p-button type="submit" label="Save" icon="pi pi-check" [loading]="saving()" />
        </ng-container>
      </app-form-footer>
    </form>
  `,
  styleUrl: './registry-form.component.scss',
})
export class RegistryFormComponent {
  private readonly store = inject(Store);
  private readonly engine = inject(FormEngineService);
  private readonly registryService = inject(ImmunizationRegistryService);

  readonly registry = input<ImmunizationRegistry | undefined>(undefined);

  readonly saved = output<ImmunizationRegistry>();
  /** Emitted after saving when the user asked to check the connection. */
  readonly checkRequested = output<ImmunizationRegistry>();
  readonly duplicate = output<void>();
  readonly deleted = output<number>();

  readonly config: FormConfig<ImmunizationRegistry> = REGISTRY_FORM_CONFIG;
  readonly saving = signal(false);
  readonly saveError = signal<string | null>(null);
  readonly isDefault = signal(false);

  readonly form = linkedSignal<FormConfig<ImmunizationRegistry>, FormGroup>({
    source: () => this.config,
    computation: (config, previous) =>
      this.engine.build(
        config,
        (previous?.value?.getRawValue() as Partial<ImmunizationRegistry>) ??
        this.registry() ??
        {},
      ),
  });

  constructor() {
    effect(() => {
      const registry = this.registry();
      this.engine.reset(this.form(), this.config, registry ?? {});
      this.isDefault.set(registry?.default ?? false);
    });
  }

  submit(thenCheck = false): void {
    if (this.form().invalid) {
      this.form().markAllAsTouched();
      return;
    }
    const value = this.engine.toEntity<ImmunizationRegistry>(this.form(), this.config);
    const existing = this.registry();
    const payload = {
      ...value,
      ...(existing?.id ? {id: existing.id} : {}),
      default: this.isDefault(),
    } as ImmunizationRegistry;

    this.saving.set(true);
    this.saveError.set(null);

    const request = existing?.id
      ? this.registryService.putImmRegistry(payload)
      : this.registryService.postImmRegistry(payload);

    request.subscribe({
      next: (result) => {
        this.saving.set(false);
        this.store.dispatch(RegistryActions.loadRegistries());
        this.saved.emit(result);
        if (thenCheck) {
          this.checkRequested.emit(result);
        }
      },
      error: (error: { error?: { message?: string }; message?: string }) => {
        this.saving.set(false);
        this.saveError.set(
          error?.error?.message ?? error?.message ?? 'Could not save these settings',
        );
      },
    });
  }

  remove(): void {
    const id = this.registry()?.id;
    if (id) {
      this.deleted.emit(id);
    }
  }
}
