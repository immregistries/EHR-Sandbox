import { HttpResponse } from '@angular/common/http';
import { Component, Inject, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { EhrPatient } from 'src/app/core/_model/rest';
import { PatientService } from 'src/app/core/_services/patient.service';
import { SnackBarService } from 'src/app/core/_services/snack-bar.service';
@Component({
  selector: 'app-patient-form-dialog',
  templateUrl: './patient-form-dialog.component.html',
  styleUrls: ['./patient-form-dialog.component.css']
})
export class PatientFormDialogComponent implements OnInit {

  patient: EhrPatient = {id: -1};
  isEditionMode: boolean = false;

  constructor(private patientService: PatientService,
    private snackBarService: SnackBarService,
    public _dialogRef: MatDialogRef<PatientFormDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: {patient: EhrPatient}) {
      if (data.patient){
        this.patient = data.patient;
        this.isEditionMode = true
      }
    }

  fillRandom(): void {
    this.patientService.readRandom().subscribe((res) => this.patient = res)
  }

  save(): void {
    if (this.isEditionMode) {
      this.patientService.quickPutPatient(this.patient).subscribe({
        next: (res: EhrPatient) => {
          this.patientService.doRefresh()
          this._dialogRef.close(true)
        },
        error: (err) => {
          console.log(err.error)
          this.snackBarService.errorMessage(err.error.error);
        }
      });
    } else {
      this.patientService.quickPostPatient( this.patient).subscribe({
        next: (res: HttpResponse<string>) => {
          if (res.body) {
            // this.snackBarService.successMessage(res.body)
          }
          this.patientService.doRefresh()
          this._dialogRef.close(true)
        },
        error: (err) => {
          console.log(err.error)
          this.snackBarService.errorMessage(err.error.error);
        }
      });
    }

  }

  ngOnInit(): void {
  }

}

