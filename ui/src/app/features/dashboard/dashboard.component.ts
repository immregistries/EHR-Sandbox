import {ChangeDetectionStrategy, Component, computed, effect, inject, signal} from '@angular/core';
import {Router} from '@angular/router';
import {Store} from '@ngrx/store';
import {ButtonModule} from 'primeng/button';
import {CardModule} from 'primeng/card';
import {DialogModule} from 'primeng/dialog';
import {EhrPatient} from '../../core/models';
import {PatientActions} from '../../core/store/patient/patient.actions';
import {
  selectAllPatients,
  selectCurrentPatient,
  selectCurrentPatientId,
  selectPatientsLoading,
} from '../../core/store/patient/patient.selectors';
import {selectRouteFacilityId, selectRouteTenantId,} from '../../core/store/router.selectors';
import {VaccinationActions} from '../../core/store/vaccination/vaccination.actions';
import {selectAllVaccinations, selectVaccinationsLoading,} from '../../core/store/vaccination/vaccination.selectors';
import {MessageService} from 'primeng/api';
import {RecordToolAction} from '../../shared/components/record-tools/record-tools.component';
import {PatientFormComponent} from '../patient/patient-form/patient-form.component';
import {DashboardPanelComponent} from './dashboard-panel/dashboard-panel.component';
import {
  PatientSummaryTableComponent,
  PatientToolRequest,
} from './patient-summary-table/patient-summary-table.component';
import {
  VaccinationSummaryTableComponent,
  VaccinationToolRequest,
} from './vaccination-summary-table/vaccination-summary-table.component';

/**
 * Facility dashboard: patients on the left, the selected patient's
 * vaccinations below, with quality and forecast panels alongside.
 *
 * Selecting a patient navigates (…/patients/:patientId) rather than setting
 * a service field, so the dashboard state is shareable as a url.
 */
@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    ButtonModule,
    CardModule,
    DialogModule,
    PatientFormComponent,
    DashboardPanelComponent,
    PatientSummaryTableComponent,
    VaccinationSummaryTableComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="dashboard">
      <app-dashboard-panel
        class="panel panel--patients"
        title="Patients"
        icon="pi pi-users"
        [count]="patients().length"
        [loading]="patientsLoading()"
      >
        <ng-container actions>
          <p-button
            icon="pi pi-refresh"
            [text]="true"
            size="small"
            severity="secondary"
            ariaLabel="Refresh patients"
            (onClick)="refreshPatients()"
          />
        </ng-container>

        <app-patient-summary-table
          [patients]="patients()"
          [loading]="patientsLoading()"
          [selectedId]="selectedPatientId()"
          [canEdit]="hasFacility()"
          (patientSelect)="selectPatient($event)"
          (create)="openCreate()"
          (populate)="populateFacility()"
          (toolSelect)="onPatientTool($event)"
        />
      </app-dashboard-panel>

      <app-dashboard-panel
        class="panel panel--quality"
        title="Data quality"
        icon="pi pi-exclamation-triangle"
      >
        <p class="placeholder">
          Acknowledgements and feedback reported by the registry will appear here.
        </p>
      </app-dashboard-panel>

      <app-dashboard-panel
        class="panel panel--vaccinations"
        [title]="vaccinationsTitle()"
        icon="pi pi-syringe"
        [count]="vaccinations().length"
        [loading]="vaccinationsLoading()"
      >
        <ng-container actions>
          <p-button
            icon="pi pi-refresh"
            [text]="true"
            size="small"
            severity="secondary"
            ariaLabel="Refresh vaccinations"
            (onClick)="refreshVaccinations()"
          />
        </ng-container>

        @if (selectedPatientId() > 0) {
          <app-vaccination-summary-table
            [vaccinations]="vaccinations()"
            [loading]="vaccinationsLoading()"
            [canEdit]="selectedPatientId() > 0"
            (create)="notImplemented('Immunization entry')"
            (populate)="populatePatient()"
            (toolSelect)="onVaccinationTool($event)"
          />
        } @else {
          <p class="placeholder">Select a patient to see their immunizations.</p>
        }
      </app-dashboard-panel>

      <p-dialog
        [header]="editingPatient() ? 'Edit patient' : 'New patient'"
        [visible]="formOpen()"
        (visibleChange)="formOpen.set($event)"
        [modal]="true"
        [draggable]="false"
        [dismissableMask]="false"
        styleClass="patient-form-dialog"
        [style]="{width: 'min(1100px, 95vw)'}"
      >
        @if (formOpen()) {
          <app-patient-form
            [patient]="editingPatient()"
            (saved)="onSaved()"
            (cancelled)="formOpen.set(false)"
          />
        }
      </p-dialog>

      <app-dashboard-panel
        class="panel panel--forecast"
        title="Recommendations"
        icon="pi pi-calendar"
      >
        <p class="placeholder">
          @if (selectedPatientId() > 0) {
            Forecast returned by the registry will appear here.
          } @else {
            Select a patient to request a forecast.
          }
        </p>
      </app-dashboard-panel>
    </div>
  `,
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent {
  private readonly store = inject(Store);
  private readonly router = inject(Router);
  private readonly messageService = inject(MessageService);

  readonly tenantId = this.store.selectSignal(selectRouteTenantId);
  readonly facilityId = this.store.selectSignal(selectRouteFacilityId);

  readonly patients = this.store.selectSignal(selectAllPatients);
  readonly patientsLoading = this.store.selectSignal(selectPatientsLoading);
  readonly selectedPatientId = this.store.selectSignal(selectCurrentPatientId);
  readonly selectedPatient = this.store.selectSignal(selectCurrentPatient);

  readonly formOpen = signal(false);
  readonly editingPatient = signal<EhrPatient | undefined>(undefined);

  readonly vaccinations = this.store.selectSignal(selectAllVaccinations);
  readonly vaccinationsLoading = this.store.selectSignal(selectVaccinationsLoading);

  readonly hasFacility = computed(() => this.tenantId() > 0 && this.facilityId() > 0);

  readonly vaccinationsTitle = computed(() => {
    const patient = this.selectedPatient();
    const name = patient?.names?.[0];
    return name ? `Immunizations — ${name.nameFirst} ${name.nameLast}` : 'Immunizations';
  });

  constructor() {
    // Load the facility's patients whenever the route context resolves.
    effect(() => {
      const tenantId = this.tenantId();
      const facilityId = this.facilityId();
      if (tenantId > 0 && facilityId > 0) {
        this.store.dispatch(PatientActions.loadPatients({tenantId, facilityId}));
      }
    });

    // And the selected patient's vaccinations.
    effect(() => {
      const tenantId = this.tenantId();
      const facilityId = this.facilityId();
      const patientId = this.selectedPatientId();
      if (tenantId > 0 && facilityId > 0 && patientId > 0) {
        this.store.dispatch(
          VaccinationActions.loadVaccinations({tenantId, facilityId, patientId}),
        );
      }
    });
  }

  selectPatient(patient: EhrPatient): void {
    this.router.navigate([
      '/t',
      this.tenantId(),
      'f',
      this.facilityId(),
      'dashboard',
      'patients',
      patient.id,
    ]);
  }

  refreshPatients(): void {
    this.store.dispatch(PatientActions.refresh());
  }

  refreshVaccinations(): void {
    this.store.dispatch(VaccinationActions.refresh());
  }

  populateFacility(): void {
    this.store.dispatch(
      PatientActions.populateFacility({
        tenantId: this.tenantId(),
        facilityId: this.facilityId(),
      }),
    );
  }

  populatePatient(): void {
    this.store.dispatch(
      PatientActions.populatePatient({
        tenantId: this.tenantId(),
        facilityId: this.facilityId(),
        patientId: this.selectedPatientId(),
      }),
    );
  }

  onPatientTool({action, patient}: PatientToolRequest): void {
    if (action === 'open') {
      this.selectPatient(patient);
      return;
    }
    if (action === 'edit') {
      this.openEdit(patient);
      return;
    }
    this.notImplemented(this.toolName(action));
  }

  openCreate(): void {
    this.editingPatient.set(undefined);
    this.formOpen.set(true);
  }

  openEdit(patient: EhrPatient): void {
    this.editingPatient.set(patient);
    this.formOpen.set(true);
  }

  onSaved(): void {
    this.formOpen.set(false);
    this.store.dispatch(PatientActions.refresh());
  }

  onVaccinationTool({action}: VaccinationToolRequest): void {
    this.notImplemented(this.toolName(action));
  }

  /** Dialogs are not built yet; say so rather than failing silently. */
  notImplemented(feature: string): void {
    this.messageService.add({
      severity: 'info',
      summary: `${feature} is not available yet`,
      detail: 'This dialog has not been migrated to the new UI yet.',
      life: 3500,
    });
  }

  private toolName(action: RecordToolAction): string {
    const names: Record<RecordToolAction, string> = {
      open: 'Patient dashboard',
      edit: 'Editing',
      hl7: 'HL7v2 messaging',
      fhir: 'FHIR messaging',
      ips: 'IPS bundle',
      localCopy: 'Local facility copy',
      remoteHistory: 'Remote history fetch',
      json: 'Raw record inspection',
    };
    return names[action];
  }
}
