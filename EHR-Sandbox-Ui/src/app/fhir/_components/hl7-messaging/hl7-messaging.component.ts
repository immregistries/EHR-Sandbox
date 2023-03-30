import { Component, Inject, Input, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Hl7Service } from 'src/app/fhir/_services/hl7.service';
import { VaccinationService } from 'src/app/core/_services/vaccination.service';
import { SnackBarService } from 'src/app/core/_services/snack-bar.service';
import { tap } from 'rxjs';

@Component({
  selector: 'app-hl7-messaging',
  templateUrl: './hl7-messaging.component.html',
  styleUrls: ['./hl7-messaging.component.css']
})
export class Hl7MessagingComponent implements OnInit {

  @Input() vaccinationId: number = -1;
  @Input() patientId: number = -1;

  loading: boolean = false

  public hl7Message: string = "";
  public answer: string = "";
  public error: string = "";

  constructor(private vaccinationService: VaccinationService,
    private hl7Service: Hl7Service,
    private snackBackService: SnackBarService,
    public _dialogRef: MatDialogRef<Hl7MessagingComponent>,
    @Inject(MAT_DIALOG_DATA) public data: {patientId: number, vaccinationId: number}) {
      this.patientId = data.patientId
      this.vaccinationId = data.vaccinationId
     }

  ngOnInit(): void {
    this.loading = true
    if (this.vaccinationId > 0){
      this.hl7Service.getVXU(this.patientId,this.vaccinationId).subscribe((res) => {
        this.hl7Message = res
        this.loading = false
      })
    } else {
      this.hl7Service.getQBP(this.patientId).subscribe((res) => {
        this.hl7Message = res
        this.loading = false
      })
    }

  }

  send() {
    this.loading = true
    if (this.vaccinationId > 0){
      this.hl7Service.quickPostVXU(this.patientId,this.vaccinationId, this.hl7Message).pipe(tap(() => {this.loading = false})).subscribe({
        next: (res) => {
          this.answer = res
          this.error = ""
        },
        error : (err) => {
          this.answer = ""
          if (err.error.text) {
            this.error = err.error.text
          } else {
            this.error = err.error
          }
          console.error(err)
        }
      })
    } else {
      this.hl7Service.quickPostQBP(this.patientId, this.hl7Message).pipe(tap(() => {this.loading = false})).subscribe({
        next: (res) => {
          this.answer = res
          this.error = ""
        },
        error : (err) => {
          this.answer = ""
          if (err.error.text) {
            this.error = err.error.text
          } else {
            this.error = err.error
          }
          console.error(err)
        }
      })
    }

  }
}
