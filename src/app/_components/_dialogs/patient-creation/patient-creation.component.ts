import { HttpResponse } from '@angular/common/http';
import { Component, Inject, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Patient } from 'src/app/_model/rest';
import { PatientService } from 'src/app/_services/patient.service';
@Component({
  selector: 'app-patient-creation',
  templateUrl: './patient-creation.component.html',
  styleUrls: ['./patient-creation.component.css']
})
export class PatientCreationComponent implements OnInit {

  patient: Patient = {id: -1};

  constructor(private patientService: PatientService,
    private _snackBar: MatSnackBar,
    public _dialogRef: MatDialogRef<PatientCreationComponent>,) {
     }

  fillRandom(): void {
    this.patientService.readRandom().subscribe((res) => this.patient = res)
  }

  save(): void {
    this.patientService.quickPostPatient( this.patient).subscribe({
      next: (res: HttpResponse<string>) => {
        console.log(res)
        if (res.body) {
          this._snackBar.open(res.body, 'close')
        }
        this._dialogRef.close(true)
      },
      error: (err) => {
        console.log(err.error)
        this._snackBar.open(`Error : ${err.error.error}`, 'close')
      }
    });
  }

  ngOnInit(): void {
    if (this.patient.id == -1) {
      this.patientService.readEmpty().subscribe((res) => this.patient = res)
    }
  }

}
