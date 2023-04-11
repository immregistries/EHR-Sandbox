import { Component, Input, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { switchMap } from 'rxjs';
import { Facility, EhrPatient } from 'src/app/core/_model/rest';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { PatientService } from 'src/app/core/_services/patient.service';
import { TenantService } from 'src/app/core/_services/tenant.service';
import { PatientFormComponent } from '../patient-form/patient-form.component';

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
    private dialog: MatDialog) { }

  ngOnInit(): void {
    this.facilityService.getObservableFacility().pipe(switchMap(facility =>{
      this.facility = facility
      if (!facility || facility.id <= 0){
        this.tenantService.getObservableTenant().subscribe(() => {
          return this.patientService.readAllPatients(this.tenantService.getTenantId())
        })
        return this.patientService.readAllPatients(this.tenantService.getTenantId())
      } else {
        return this.patientService.readPatients(this.tenantService.getTenantId(), facility.id)
      }
    })).subscribe((res) => {
      this.list = res
    })
  }

  openDialog() {
    const dialogRef = this.dialog.open(PatientFormComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'full-screen-modal',
    });
    dialogRef.afterClosed().subscribe(result => {
      if (this.facility){
        this.patientService.readPatients(this.tenantService.getTenantId(), this.facility.id).subscribe((res) => {
          this.list = res
        })
      }
    });
  }

  onSelection(event: EhrPatient) {
    if (this.selectedOption && this.selectedOption.id == event.id){
      this.selectedOption = undefined
      this.patientService.setPatient({})
    } else {
      this.selectedOption = event
      this.patientService.setPatient(event)
    }
  }

}
