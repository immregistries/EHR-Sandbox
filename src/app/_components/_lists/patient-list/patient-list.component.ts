import { Component, Input, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Facility, Patient } from 'src/app/_model/rest';
import { FacilityService } from 'src/app/_services/facility.service';
import { PatientService } from 'src/app/_services/patient.service';
import { TenantService } from 'src/app/_services/tenant.service';
import { PatientFormComponent } from '../../patient-form/patient-form.component';

@Component({
  selector: 'app-patient-list',
  templateUrl: './patient-list.component.html',
  styleUrls: ['./patient-list.component.css']
})
export class PatientListComponent implements OnInit {

  @Input() list?: Patient[];
  @Input() facility: Facility = {id: -1};

  selectedOption?: Patient;


  constructor(private tenantService: TenantService,
    private facilityService: FacilityService,
    private patientService: PatientService,
    private dialog: MatDialog) { }

  ngOnInit(): void {
    this.tenantService.getObservableTenant().subscribe(tenant => {
      this.patientService.readAllPatients(this.tenantService.getTenantId()).subscribe((res) => {
        this.list = res
      })
    })
    this.facilityService.getObservableTenant().subscribe(facility =>{
      this.facility = facility
      if (!facility){
        this.tenantService.getObservableTenant().subscribe(tenant => {
          this.patientService.readAllPatients(this.tenantService.getTenantId()).subscribe((res) => {
            this.list = res
          })
        })
      } else {
        this.patientService.readPatients(this.tenantService.getTenantId(), facility.id).subscribe((res) => {
          this.list = res
        })
      }
    })
    // this.tenantService.readTenants().subscribe((res) => {this.list = res})
  }

  openDialog() {
    const dialogRef = this.dialog.open(PatientFormComponent, {
      maxWidth: '100vw',
      maxHeight: '100vh',
      height: '90%',
      width: '90%',
      panelClass: 'full-screen-modal'
    });
    dialogRef.afterClosed().subscribe(result => {
      console.log(`Dialog result: ${result}`);
      this.ngOnInit();
    });
  }

  onSelection(event: Patient) {
    this.selectedOption = event
  }

}
