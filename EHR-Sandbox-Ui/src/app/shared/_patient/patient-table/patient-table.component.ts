import { AfterViewInit, Component, EventEmitter, Input, Output } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Facility, EhrPatient } from 'src/app/core/_model/rest';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { PatientService } from 'src/app/core/_services/patient.service';
import { TenantService } from 'src/app/core/_services/tenant.service';
import { PatientDashboardComponent } from '../patient-dashboard/patient-dashboard.component';
import { PatientFormComponent } from '../patient-form/patient-form.component';
import { AbstractDataTableComponent } from '../../_components/abstract-data-table/abstract-data-table.component';

@Component({
  selector: 'app-patient-table',
  templateUrl: './patient-table.component.html',
  styleUrls: ['./patient-table.component.css'],
})
export class PatientTableComponent extends AbstractDataTableComponent<EhrPatient> implements AfterViewInit {


  @Input()
  title: string = 'Patients'
  @Input()
  facility!: Facility | null;
  @Input()
  columns: (keyof EhrPatient | "alerts" | "remove")[] = [
    "mrn",
    "nameLast",
    "nameFirst",
    "birthDate",
    "alerts"
  ]
  @Output()
  removeEmitter: EventEmitter<EhrPatient> = new EventEmitter<EhrPatient>()


  constructor(public tenantService: TenantService,
    public facilityService: FacilityService,
    public patientService: PatientService,
    private dialog: MatDialog) {
    super()
    // this.observableRefresh = merge(
    //   this.facilityService.getCurrentObservable()
    //     .pipe(tap(facility => { this.facility = facility })),
    //   this.patientService.getRefresh(),
    //   this.facilityService.getRefresh(),
    // );
    // this.observableSource = this.patientService.quickReadPatients();
  }

  remove(patient: EhrPatient) {
    this.removeEmitter.emit(patient)
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
      if (result) {
        this.ngAfterViewInit()
        if (result instanceof Object) {
          this.selectedElement = result
          this.selectEmitter.emit(this.selectedElement);
        } else {
          this.patientService.quickReadPatient(+result).subscribe((newPatient) => {
            this.selectedElement = newPatient
            this.selectEmitter.emit(this.selectedElement);
          })
        }
      }
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

  populate() {
    if (this.facility?.id) {
      this.facilityService.populate(this.tenantService.getCurrentId(), this.facility.id).subscribe((res) => {
        this.facilityService.doRefresh()
      })
    }
  }

}
