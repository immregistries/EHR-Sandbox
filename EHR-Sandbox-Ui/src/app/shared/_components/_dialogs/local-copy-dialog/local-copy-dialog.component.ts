import { Component, Inject, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Facility, Patient, VaccinationEvent } from 'src/app/core/_model/rest';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { PatientService } from 'src/app/core/_services/patient.service';
import { TenantService } from 'src/app/core/_services/tenant.service';
import { VaccinationService } from 'src/app/core/_services/vaccination.service';

@Component({
  selector: 'app-local-copy-dialog',
  templateUrl: './local-copy-dialog.component.html',
  styleUrls: ['./local-copy-dialog.component.css']
})
export class LocalCopyDialogComponent implements OnInit {


  // facility!: Facility;
  facilityList!: Facility[];
  patient?: Patient;
  vaccination?: VaccinationEvent;

  // function loadPatient (patient: Patient | number): patient is Patient {


  // }

  constructor(private tenantService: TenantService,
    public facilityService: FacilityService,
    private vaccinationService: VaccinationService,
    private patientService: PatientService,
    private _snackBar: MatSnackBar,
    public _dialogRef: MatDialogRef<LocalCopyDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: {patient?: Patient | number, vaccination?: VaccinationEvent}) {
      if(data.patient) {
        console.log(data.patient)
        console.log(typeof data.patient)
        if (typeof data.patient === "number" ||  "string") {
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
      this.facilityService.readFacilities(this.tenantService.getTenantId()).subscribe((list) => {
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
    if (this.patient) {
      this.patient.id = undefined
      this.patientService.postPatient(this.tenantService.getTenantId(),facility.id,this.patient).subscribe((res) => {
        // this._snackBar.open("Patient copied to facility")
        console.log(res)
        if(this.vaccination) {
          if (res.body) {
            this.vaccination.id = undefined
            this.vaccination.vaccine.id = undefined
            this.vaccination.vaccine.vaccinationEvents = undefined
            this.vaccinationService.postVaccination(this.tenantService.getTenantId(),facility.id,+res.body,this.vaccination).subscribe((res) => {
              this._snackBar.open("Vaccination copied to facility")
              this._dialogRef.close()
              // if (vaccinationId)
            })
          } else {
            this._snackBar.open("Vaccination not copied problem happened in referencing patient")
          }
        }
        this._dialogRef.close()

      })

    } else {

    }
  }

}
