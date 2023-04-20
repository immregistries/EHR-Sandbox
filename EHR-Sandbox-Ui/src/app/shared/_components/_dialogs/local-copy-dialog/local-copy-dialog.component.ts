import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Facility, EhrPatient, VaccinationEvent } from 'src/app/core/_model/rest';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { PatientService } from 'src/app/core/_services/patient.service';
import { SnackBarService } from 'src/app/core/_services/snack-bar.service';
import { TenantService } from 'src/app/core/_services/tenant.service';
import { VaccinationService } from 'src/app/core/_services/vaccination.service';

@Component({
  selector: 'app-local-copy-dialog',
  templateUrl: './local-copy-dialog.component.html',
  styleUrls: ['./local-copy-dialog.component.css']
})
export class LocalCopyDialogComponent implements OnInit {

  facilityList!: Facility[];
  patient?: EhrPatient;
  vaccination?: VaccinationEvent;

  setPrimarySourceToFalse: boolean = true;

  constructor(private tenantService: TenantService,
    public facilityService: FacilityService,
    private vaccinationService: VaccinationService,
    private patientService: PatientService,
    private snackBarService: SnackBarService,
    public _dialogRef: MatDialogRef<LocalCopyDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: {patient?: EhrPatient | number, vaccination?: VaccinationEvent}) {
      if(data.patient) {
        if (typeof data.patient === "number" || typeof data.patient ===  "string") {
          this.patientService.quickReadPatient(+data.patient).subscribe((res) => {
            this.patient = res
          });
        } else {
          this.patient = JSON.parse(JSON.stringify(data.patient));
        }
      }
      if(data.vaccination){
        this.vaccination =  JSON.parse(JSON.stringify(data.vaccination));
      }
      this.facilityService.readAllFacilities().subscribe((list) => {
        this.facilityList = list.filter((facility) => {return facility.id != this.facilityService.getFacilityId()})
      })
     }

  ngOnInit(): void {
    // this.facilityService.readFacilities(this.tenantService.getTenantId()).subscribe((list) => {
    //   this.facilityList = list
    //   //.filter((facility) => {facility.id != this.facilityService.getFacilityId()})
    // })
  }

  selectFacility(facility:Facility) {
    // this.facility = facility
  }

  copy(facility: Facility) {
    if (this.patient && facility.tenant) {
      this.patient.id = undefined
      this.patient.facility = undefined
      let tenantId: number = (typeof facility.tenant === "object" )? facility.tenant.id : +facility.tenant
      this.patientService.postPatient(tenantId, facility.id,this.patient).subscribe((res) => {
        if(this.vaccination && facility.tenant) {
          if (res.body) {
            this.vaccination.id = undefined
            this.vaccination.vaccine.id = undefined
            this.vaccination.vaccine.vaccinationEvents = undefined
            /**
             * vaccination set as historical
             *
             */
            if (this.setPrimarySourceToFalse){
              this.vaccination.primarySource = false
            }
            this.vaccinationService.postVaccination(tenantId,facility.id,+res.body,this.vaccination).subscribe((res) => {
              this.snackBarService.successMessage("Vaccination copied to facility")
              this._dialogRef.close()
              // if (vaccinationId)
            })
          } else {
            this.snackBarService.errorMessage("Vaccination not copied problem happened in referencing patient")
          }
        } else {
          this.snackBarService.successMessage("Vaccination copied to facility")
          this._dialogRef.close()
        }
      })
    } else {

    }
  }

}
