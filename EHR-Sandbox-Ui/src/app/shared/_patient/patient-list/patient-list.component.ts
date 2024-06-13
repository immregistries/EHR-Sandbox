import { Component, Inject, Input, OnInit, Optional } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { Observable, map, switchMap } from 'rxjs';
import { Facility, EhrPatient } from 'src/app/core/_model/rest';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { PatientService } from 'src/app/core/_services/patient.service';
import { TenantService } from 'src/app/core/_services/tenant.service';
import { PatientDashboardComponent } from '../patient-dashboard/patient-dashboard.component';
import { PatientFormComponent } from '../patient-form/patient-form.component';


const DEFAULT_SETTINGS = {
  maxWidth: '95vw',
  maxHeight: '95vh',
  height: 'fit-content',
  width: '100%',
  panelClass: 'full-screen-modal',
}
@Component({
  selector: 'app-patient-list',
  templateUrl: './patient-list.component.html',
  styleUrls: ['./patient-list.component.css']
})
export class PatientListComponent implements OnInit {

  @Input() public list?: EhrPatient[];
  @Input() facility?: Facility;

  constructor(
    private dialog: MatDialog,
    private tenantService: TenantService,
    private facilityService: FacilityService,
    private patientService: PatientService,
    @Optional() public _dialogRef: MatDialogRef<PatientListComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: {}) {

  }

  ngOnInit(): void {
    this.facilityService.getCurrentObservable().pipe(switchMap(facility => {
      this.facility = facility
      return this.getList(facility)
    })).subscribe((res) => {
      this.list = res
    })
  }

  getList(facility: Facility | undefined): Observable<EhrPatient[]> {
    if (!facility?.id || facility.id <= 0) {
      this.tenantService.getCurrentObservable().subscribe(() => {
        return this.patientService.readAllPatients(this.tenantService.getCurrentId())
      })
      return this.patientService.readAllPatients(this.tenantService.getCurrentId())
    } else {
      return this.patientService.readPatients(this.tenantService.getCurrentId(), facility.id ?? -1)
    }

  }

  openPatient(patient: EhrPatient) {
    const dialogRef = this.dialog.open(PatientDashboardComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'full-screen-modal',
      data: { patient: patient }
    });
    dialogRef.afterClosed().subscribe(result => {
    });
  }


  openCreate() {
    const dialogRef = this.dialog.open(PatientFormComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'full-screen-modal',
      data: {}
    });
    dialogRef.afterClosed().subscribe(
      () => this.getList(this.facility).subscribe((res) => {
        this.list = res
      }))
  }

  onSelection(event: EhrPatient) {
    this._dialogRef?.close(event.id)
    // if (this.selectedOption && this.selectedOption.id == event.id){
    //   this.selectedOption = undefined
    //   // this.patientService.setCurrent({})
    // } else {
    //   this.selectedOption = event
    //   // this.patientService.setCurrent(event)
    // }
  }

}
