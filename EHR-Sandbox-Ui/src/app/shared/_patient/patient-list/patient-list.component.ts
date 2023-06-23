import { Component, Inject, Input, OnInit, Optional } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { switchMap } from 'rxjs';
import { Facility, EhrPatient } from 'src/app/core/_model/rest';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { PatientService } from 'src/app/core/_services/patient.service';
import { TenantService } from 'src/app/core/_services/tenant.service';
import { PatientFormComponent } from '../patient-form/patient-form.component';
import { GroupService } from 'src/app/core/_services/group.service';

@Component({
  selector: 'app-patient-list',
  templateUrl: './patient-list.component.html',
  styleUrls: ['./patient-list.component.css']
})
export class PatientListComponent implements OnInit {

  @Input() public list?: EhrPatient[];
  @Input() facility?: Facility;

  selectedOption?: EhrPatient;


  constructor(private tenantService: TenantService,
    private facilityService: FacilityService,
    private patientService: PatientService,
    private groupService: GroupService,
    private dialog: MatDialog,
    @Optional() public _dialogRef: MatDialogRef<PatientListComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: {}) {

    }

  ngOnInit(): void {
    this.facilityService.getCurrentObservable().pipe(switchMap(facility =>{
      this.facility = facility
      if (!facility || facility.id <= 0){
        this.tenantService.getCurrentObservable().subscribe(() => {
          return this.patientService.readAllPatients(this.tenantService.getCurrentId())
        })
        return this.patientService.readAllPatients(this.tenantService.getCurrentId())
      } else {
        return this.patientService.readPatients(this.tenantService.getCurrentId(), facility.id)
      }
    })).subscribe((res) => {
      this.list = res
    })
  }

  // openDialog() {
  //   const dialogRef = this.dialog.open(PatientFormComponent, {
  //     maxWidth: '95vw',
  //     maxHeight: '95vh',
  //     height: 'fit-content',
  //     width: '100%',
  //     panelClass: 'full-screen-modal',
  //   });
  //   dialogRef.afterClosed().subscribe(result => {
  //     if (this.facility){
  //       this.patientService.readPatients(this.tenantService.getCurrentId(), this.facility.id).subscribe((res) => {
  //         this.list = res
  //       })
  //     }
  //   });
  // }

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
