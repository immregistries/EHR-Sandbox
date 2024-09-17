import { Injectable } from '@angular/core';
import { PatientDashboardComponent } from 'src/app/shared/_patient/patient-dashboard/patient-dashboard.component';
import { VaccinationDashboardComponent } from 'src/app/shared/_vaccination/vaccination-dashboard/vaccination-dashboard.component';
import { EhrPatient, VaccinationEvent } from '../_model/rest';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class SnackBarService {

  constructor(private _snackBar: MatSnackBar,
    private dialog: MatDialog,
  ) { }

  open(message: string) {
    return this.successMessage(message)
  }

  open404() {
    let snack = this._snackBar.open("Lost connection to backend", `Refresh all`)
    snack.onAction().subscribe(() => {
      window.location.reload()
    })
    return snack
  }

  successMessage(message: string) {
    return this._snackBar.open(message, `close`, {
      duration: 1000,
    })
  }

  notification(onAction: () => void) {
    return this._snackBar.open("Data received, refresh required", `refresh`,
      { duration: 15000 }).onAction().subscribe(onAction)
  }

  errorMessage(message: string) {
    this._snackBar.open(message, `close`, {
      duration: 3000,
    })
  }

  /**
   * TODO change color with custom snack component
   * @param message
   * @param patient
   * @param vaccination
   */
  fatalFhirMessage(message: string, patient?: EhrPatient | number, vaccination?: VaccinationEvent | number) {
    return this._snackBar.open("Critical fhir issue : " + message, `open`, { duration: 15000 })
      .onAction().subscribe(() => {
        if (patient && vaccination) {
          this.openVaccination(patient, vaccination)
        } else if (patient) {
          this.openPatient(patient)
        }
      })
  }


  private openPatient(patient: EhrPatient | number) {
    const dialogRef = this.dialog.open(PatientDashboardComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'dialog-with-bar',
      data: { patient: patient },
    });
    dialogRef.afterClosed().subscribe(result => {
      // this.patientService.doRefresh()
    });
  }

  private openVaccination(patient: EhrPatient | number, vaccination: VaccinationEvent | number) {
    const dialogRef = this.dialog.open(VaccinationDashboardComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'dialog-with-bar',
      data: { patient: patient, vaccination: vaccination },
    });
    dialogRef.afterClosed().subscribe(result => {
      // this.patientService.doRefresh()
    });
  }
}
