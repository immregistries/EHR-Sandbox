import { HttpResponse } from '@angular/common/http';
import { Component, Inject, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Patient } from 'src/app/core/_model/rest';
import { PatientService } from 'src/app/core/_services/patient.service';
@Component({
  selector: 'app-patient-creation',
  templateUrl: './patient-creation.component.html',
  styleUrls: ['./patient-creation.component.css']
})
export class PatientCreationComponent implements OnInit {

  patient: Patient = {id: -1};
  isEditionMode: boolean = false;

  constructor(private patientService: PatientService,
    private _snackBar: MatSnackBar,
    public _dialogRef: MatDialogRef<PatientCreationComponent>,
    @Inject(MAT_DIALOG_DATA) public data: {patient: Patient}) {
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
      this.patientService.quickPutPatient( this.patient).subscribe({
        next: (res: Patient) => {
          this.patientService.doRefresh()
          this._dialogRef.close(true)
        },
        error: (err) => {
          console.log(err.error)
          this._snackBar.open(`Error : ${err.error.error}`, 'close')
        }
      });
    } else {
      this.patientService.quickPostPatient( this.patient).subscribe({
        next: (res: HttpResponse<string>) => {
          if (res.body) {
            this._snackBar.open(res.body, 'close')
          }
          this.patientService.doRefresh()
          this._dialogRef.close(true)
        },
        error: (err) => {
          console.log(err.error)
          this._snackBar.open(`Error : ${err.error.error}`, 'close')
        }
      });
    }

  }

  ngOnInit(): void {
  }

}
