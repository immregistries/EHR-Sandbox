import { HttpParams } from '@angular/common/http';
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
    @Inject(MAT_DIALOG_DATA) public data: { patient?: EhrPatient | number, vaccination?: VaccinationEvent }) {
    if (data.patient) {
      if (typeof data.patient === "number" || typeof data.patient === "string") {
        this.patientService.quickReadPatient(+data.patient).subscribe((res) => {
          this.patient = res
        });
      } else {
        this.patient = JSON.parse(JSON.stringify(data.patient));
      }
    }
    if (data.vaccination) {
      this.vaccination = JSON.parse(JSON.stringify(data.vaccination));
    }
    this.facilityService.readAllFacilities().subscribe((list) => {
      this.facilityList = list.filter((facility) => { return facility.id != this.facilityService.getCurrentId() })
    })
  }

  ngOnInit(): void {
    // this.facilityService.readFacilities(this.tenantService.getCurrentId()).subscribe((list) => {
    //   this.facilityList = list
    //   //.filter((facility) => {facility.id != this.facilityService.getCurrentId()})
    // })
  }

  selectFacility(facility: Facility) {
    // this.facility = facility
  }

  copy(facility: Facility) {
    if (this.patient) {
      let params = new HttpParams()
        .append('copied_facility_id', this.facilityService.getCurrentId())
        .append('copied_entity_id', this.patient.id + '')
      let patientCopy: EhrPatient = JSON.parse(JSON.stringify(this.patient))
      patientCopy.id = undefined
      patientCopy.facility = undefined
      let tenantId: number;
      if (!facility.tenant) {
        tenantId = this.tenantService.getCurrentId()
      } else if (typeof facility.tenant === "object") {
        tenantId = facility.tenant.id
      } else {
        tenantId = +facility.tenant
      }
      this.patientService.postPatient(tenantId, facility.id, patientCopy, params).subscribe((res) => {
        if (this.vaccination && facility.tenant) {
          let vaccinationCopy: VaccinationEvent = JSON.parse(JSON.stringify(this.vaccination))
          if (!res.body) {
            this.snackBarService.errorMessage("Vaccination not copied problem happened in referencing patient")
          } else {
            params.set('copied_entity_id', this.vaccination.id + '')
            vaccinationCopy.id = undefined
            vaccinationCopy.vaccine.id = undefined
            vaccinationCopy.vaccine.vaccinationEvents = undefined
            /**
             * TODO copy clinicians ?
             */
            vaccinationCopy.administeringClinician = {}
            vaccinationCopy.enteringClinician = {}
            vaccinationCopy.orderingClinician = {}
            /**
             * vaccination set as historical
             *
             */
            if (this.setPrimarySourceToFalse) {
              vaccinationCopy.primarySource = false
            }
            this.vaccinationService.postVaccination(tenantId, facility.id, +res.body, vaccinationCopy, params).subscribe((res) => {
              this.snackBarService.successMessage("Vaccination copied to facility")
              this._dialogRef.close()
            })
          }
        } else {
          this.snackBarService.successMessage("Patient copied to facility")
          this._dialogRef.close()
        }
      })
    } else {
      this.snackBarService.errorMessage("unable to copy the data")
    }
  }

}
