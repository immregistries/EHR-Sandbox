import {ChangeDetectionStrategy, Component, computed, inject, signal} from '@angular/core';
import {Store} from '@ngrx/store';
import {ConfirmationService, MessageService} from 'primeng/api';
import {ButtonModule} from 'primeng/button';
import {ConfirmDialogModule} from 'primeng/confirmdialog';
import {DialogModule} from 'primeng/dialog';
import {TagModule} from 'primeng/tag';
import {TooltipModule} from 'primeng/tooltip';
import {ImmunizationRegistry} from '../../../core/models';
import {RegistryActions} from '../../../core/store/registry/registry.actions';
import {selectAllRegistries, selectRegistriesLoading,} from '../../../core/store/registry/registry.selectors';
import {RegistryCheckComponent} from '../registry-check/registry-check.component';
import {RegistryFormComponent} from '../registry-form/registry-form.component';

/**
 * IIS settings: the registries this user can send to, and the connection
 * details of the selected one. Registries belong to the user, not to a
 * tenant, so this page is reachable without one.
 */
@Component({
  selector: 'app-settings-page',
  standalone: true,
  imports: [
    ButtonModule,
    ConfirmDialogModule,
    DialogModule,
    TagModule,
    TooltipModule,
    RegistryCheckComponent,
    RegistryFormComponent,
  ],
  providers: [ConfirmationService],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="page">
      <header class="page-header">
        <div class="page-title">
          <h1>IIS settings</h1>
          <span class="subtitle">Registries this sandbox can send to</span>
        </div>
        <p-button label="New registry" icon="pi pi-plus" (onClick)="startNew()" />
      </header>

      <div class="layout">
        <nav class="registry-list" aria-label="Registries">
          @for (registry of registries(); track registry.id) {
            <button
              type="button"
              class="registry"
              [class.registry--active]="registry.id === selectedId()"
              (click)="select(registry)"
            >
              <span class="registry-name">{{ registry.name || 'Unnamed registry' }}</span>
              @if (registry.default) {
                <p-tag value="default" severity="success" [rounded]="true" />
              }
              <span class="registry-url">{{ registry.iisFhirUrl || registry.iisHl7Url || '—' }}</span>
            </button>
          } @empty {
            <p class="empty">
              @if (loading()) {
                Loading registries…
              } @else {
                No registries yet. Create one to connect to an IIS.
              }
            </p>
          }
        </nav>

        <section class="registry-detail">
          @if (editing() || creating()) {
            <app-registry-form
              [registry]="editing()"
              (saved)="onSaved($event)"
              (checkRequested)="openCheck($event)"
              (duplicate)="duplicateSelected()"
              (deleted)="confirmDelete($event)"
            />
          } @else {
            <p class="empty">Select a registry, or create one.</p>
          }
        </section>
      </div>

      <p-dialog
        header="Connection check"
        [visible]="checking() !== undefined"
        (visibleChange)="!$event && checking.set(undefined)"
        [modal]="true"
        [draggable]="false"
        [style]="{width: 'min(560px, 95vw)'}"
      >
        @if (checking(); as registry) {
          <app-registry-check [registry]="registry" />
        }
      </p-dialog>

      <p-confirmdialog />
    </div>
  `,
  styleUrl: './settings-page.component.scss',
})
export class SettingsPageComponent {
  private readonly store = inject(Store);
  private readonly messageService = inject(MessageService);
  private readonly confirmationService = inject(ConfirmationService);

  readonly registries = this.store.selectSignal(selectAllRegistries);
  readonly loading = this.store.selectSignal(selectRegistriesLoading);

  readonly selectedId = signal<number | undefined>(undefined);
  /** Unsaved registry being created, optionally seeded from a duplicate. */
  readonly draft = signal<ImmunizationRegistry | undefined>(undefined);
  readonly checking = signal<ImmunizationRegistry | undefined>(undefined);

  readonly creating = computed(() => this.draft() !== undefined);

  /** What the form edits: a saved registry, or the current draft. */
  readonly editing = computed(
    () => this.draft() ?? this.registries().find((r) => r.id === this.selectedId()),
  );

  readonly selected = computed(() =>
    this.draft() ? undefined : this.registries().find((r) => r.id === this.selectedId()),
  );

  constructor() {
    this.store.dispatch(RegistryActions.loadRegistries());
  }

  select(registry: ImmunizationRegistry): void {
    this.draft.set(undefined);
    this.selectedId.set(registry.id);
  }

  startNew(): void {
    this.selectedId.set(undefined);
    this.draft.set({});
  }

  onSaved(registry: ImmunizationRegistry): void {
    this.draft.set(undefined);
    this.selectedId.set(registry.id);
    this.messageService.add({
      severity: 'success',
      summary: 'Settings saved',
      life: 2500,
    });
  }

  openCheck(registry: ImmunizationRegistry): void {
    this.checking.set(registry);
  }

  /** Copies the selected registry's settings into a new, unsaved one. */
  duplicateSelected(): void {
    const current = this.selected();
    if (!current) {
      return;
    }
    this.selectedId.set(undefined);
    this.draft.set({
      ...current,
      id: undefined,
      default: false,
      name: `${current.name ?? 'Registry'} copy`,
    });
  }

  confirmDelete(id: number): void {
    this.confirmationService.confirm({
      header: 'Delete registry',
      message: 'These connection settings will be removed. This cannot be undone.',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Delete',
      rejectLabel: 'Cancel',
      acceptButtonStyleClass: 'p-button-danger',
      accept: () => {
        this.store.dispatch(RegistryActions.deleteRegistry({registryId: id}));
        this.selectedId.set(undefined);
      },
    });
  }
}
