import { Injectable } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { PatientDashboardComponent } from 'src/app/patient/patient-dashboard/patient-dashboard.component';
import { VaccinationDashboardComponent } from 'src/app/vaccination/vaccination-dashboard/vaccination-dashboard.component';
import { EhrPatient, VaccinationEvent } from '../_model/rest';
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

  /**
   * TODO change color with custom snack component
   * @param message
   * @param patient
   * @param vaccination
   */
  fatalFhirMessage(message: string, patient? : EhrPatient| number, vaccination?: VaccinationEvent | number) {
    this._snackBar.open("Critical fhir issue : " + message,`open`,{duration: 15000})
      .onAction().subscribe(() => {
        if (patient && vaccination) {
          this.openVaccination(patient,vaccination)
        } else if (patient) {
          this.openPatient(patient)
        }
      })
  }


  private openPatient(patient: EhrPatient | number){
    const dialogRef = this.dialog.open(PatientDashboardComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'dialog-with-bar',
      data: {patient: patient},
    });
    dialogRef.afterClosed().subscribe(result => {
      this.patientService.doRefresh()
    });
  }

  private openVaccination(patient: EhrPatient | number, vaccination: VaccinationEvent | number){
    const dialogRef = this.dialog.open(VaccinationDashboardComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'dialog-with-bar',
      data: {patient: patient, vaccination: vaccination},
    });
    dialogRef.afterClosed().subscribe(result => {
      this.patientService.doRefresh()
    });
  }
}
