import { Injectable } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarConfig } from '@angular/material/snack-bar';
import { filter } from 'rxjs';
import { PatientDashboardDialogComponent } from 'src/app/patient/patient-dashboard/patient-dashboard-dialog/patient-dashboard-dialog.component';
import { EhrPatient } from '../_model/rest';
import { FeedbackService } from './feedback.service';
import { PatientService } from './patient.service';

@Injectable({
  providedIn: 'root'
})
export class SnackBarService {

  constructor(private _snackBar: MatSnackBar,
    private patientService: PatientService,
    private feedbackService: FeedbackService,
    private dialog: MatDialog,
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
    this._snackBar.open("Data received, refresh required",`refresh`,
    {duration: 15000}).onAction().subscribe(() => {this.patientService.doRefresh();this.feedbackService.doRefresh()})
  }

  errorMessage(message: string) {
    this._snackBar.open(message,`close`,{
      duration: 3000,
   })
  }

  fatalFhirMessage(message: string,destination : string) {
    this._snackBar.open(message,`open`,{duration: 15000})
      .onAction().pipe().subscribe(() => {
        this.patientService.doRefresh();
        this.feedbackService.doRefresh();
      })
  }


  private openPatient(patient: EhrPatient){
    const dialogRef = this.dialog.open(PatientDashboardDialogComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'dialog-with-bar',
      data: {patient: patient.id},
    });
    dialogRef.afterClosed().subscribe(result => {
      this.patientService.doRefresh()
    });
  }
}
