import { Injectable } from '@angular/core';
import { MatSnackBar, MatSnackBarConfig } from '@angular/material/snack-bar';
import { FeedbackService } from './feedback.service';
import { PatientService } from './patient.service';

@Injectable({
  providedIn: 'root'
})
export class SnackBarService {

  constructor(private _snackBar: MatSnackBar,
    private patientService: PatientService,
    private feedbackService: FeedbackService,
    ) { }

  open(message: string) {
    this.successMessage(message)
  }

  successMessage(message: string) {
    this._snackBar.open(message,`close`,{
      duration: 1000,
   })
  }

  notification() {
    this._snackBar.open("Data received, refresh required",`refresh`,{
      duration: 15000,
          }).onAction().subscribe(() => {this.patientService.doRefresh();this.feedbackService.doRefresh()})
  }

  errorMessage(message: string) {
    this._snackBar.open(message,`close`,{
      duration: 3000,
   })
  }
}
