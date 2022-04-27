import { Component, Inject, OnInit } from '@angular/core';
import { VaccinationEvent } from 'src/app/_model/rest';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { VaccinationService } from 'src/app/_services/vaccination.service';
import { HttpResponse } from '@angular/common/http';

@Component({
  selector: 'app-vaccination-creation',
  templateUrl: './vaccination-creation.component.html',
  styleUrls: ['./vaccination-creation.component.css']
})
export class VaccinationCreationComponent implements OnInit {
  public vaccination: VaccinationEvent = {
    enteringClinician: {},
    orderingClinician: {},
    administeringClinician: {},
    vaccine: {},
  }
  public patientId: number = -1

  public isEditionMode: boolean = false

  constructor(private _snackBar: MatSnackBar,
    public _dialogRef: MatDialogRef<VaccinationCreationComponent>,
    private vaccinationService: VaccinationService,
    @Inject(MAT_DIALOG_DATA) public data: {patientId: number, vaccination?: VaccinationEvent}) {
      this.patientId = data.patientId;
      if (data.vaccination){
        this.vaccination=data.vaccination
        this.isEditionMode = true
      }
  }

  ngOnInit(): void {
  }

  fillRandom(): void {
    this.vaccinationService.readRandom().subscribe((res) => {
      this.vaccination=res
    })
  }

  save(): void {
    if (this.isEditionMode == true){
      // TODO PUT implementation
      this.vaccinationService.quickPutVaccination( this.patientId, this.vaccination).subscribe({
        next: (res: VaccinationEvent) => {
          console.log(res)
          this._dialogRef.close(true)
        },
        error: (err) => {
          console.log(err.error)
          this._snackBar.open(`Error : ${err.error.error}`, 'close')
        }
      });
    } else {
      this.vaccinationService.quickPostVaccination( this.patientId, this.vaccination).subscribe({
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

  }

}
