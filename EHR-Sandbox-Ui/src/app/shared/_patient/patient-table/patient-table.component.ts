import { animate, state, style, transition, trigger } from '@angular/animations';
import { AfterViewInit, Component, Input } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatTableDataSource } from '@angular/material/table';
import { Observable, merge, switchMap, tap } from 'rxjs';
import { Facility, EhrPatient } from 'src/app/core/_model/rest';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { PatientService } from 'src/app/core/_services/patient.service';
import { TenantService } from 'src/app/core/_services/tenant.service';
import { FeedbackTableComponent } from 'src/app/shared/_components/feedback-table/feedback-table.component';
import { PatientDashboardComponent } from '../patient-dashboard/patient-dashboard.component';
import { PatientFormComponent } from '../patient-form/patient-form.component';
import { AbstractDataTableComponent } from '../../_components/abstract-data-table/abstract-data-table.component';

@Component({
  selector: 'app-patient-table',
  templateUrl: './patient-table.component.html',
  styleUrls: ['./patient-table.component.css'],
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({ height: '0px', minHeight: '0' })),
      state('expanded', style({ height: '*' })),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ],
})
export class PatientTableComponent extends AbstractDataTableComponent<EhrPatient> implements AfterViewInit {
  @Input() facility!: Facility | null;
  columns: (keyof EhrPatient | 'alerts')[] = [
    "mrn",
    "nameLast",
    "nameFirst",
    // "nameMiddle",
    "birthDate",
    'alerts'
  ]

  constructor(public tenantService: TenantService,
    public override facilityService: FacilityService,
    public override patientService: PatientService,
    private dialog: MatDialog) {
    super(patientService, facilityService)
    // this.observableRefresh = merge(
    //   this.facilityService.getCurrentObservable()
    //     .pipe(tap(facility => { this.facility = facility })),
    //   this.patientService.getRefresh(),
    //   this.facilityService.getRefresh(),
    // );
    // this.observableSource = this.patientService.quickReadPatients();
  }

  openCreation() {
    const dialogRef = this.dialog.open(PatientFormComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '90%',
      panelClass: 'dialog-with-bar',
      data: {},
    });
    dialogRef.afterClosed().subscribe(result => {
      this.ngAfterViewInit()
    });
  }


  openPatient(patient: EhrPatient) {
    const dialogRef = this.dialog.open(PatientDashboardComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'dialog-with-bar',
      data: { patient: patient.id },
    });
    dialogRef.afterClosed().subscribe(result => {
      this.patientService.doRefresh()
    });
  }

}
