import { Component, Inject, Input } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { SnackBarService } from 'src/app/core/_services/snack-bar.service';
import { VaccinationService } from 'src/app/core/_services/vaccination.service';
import { Hl7Service } from '../../../core/_services/_fhir/hl7.service';
import { Hl7MessagingComponent } from '../hl7-messaging/hl7-messaging.component';

@Component({
  selector: 'app-hl7-post',
  templateUrl: './hl7-post.component.html',
  styleUrls: ['./hl7-post.component.css']
})
export class Hl7PostComponent {
  @Input() vaccinationId: number = -1;
  @Input() patientId: number = -1;

  loading: boolean = false
  resultLoading: boolean = false

  @Input()
  public hl7Message: string = "";
  public answer: string = "";
  public error: boolean = false;

  constructor(private vaccinationService: VaccinationService,
    private hl7Service: Hl7Service,
    public snackBarService: SnackBarService,
    public _dialogRef: MatDialogRef<Hl7MessagingComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { patientId: number, vaccinationId: number }) {
    this.patientId = data.patientId
    this.vaccinationId = data.vaccinationId
  }

  ngOnInit(): void {
    this.loading = true
    if (this.vaccinationId > 0) {
      this.hl7Service.getVXU(this.patientId, this.vaccinationId).subscribe((res) => {
        this.hl7Message = res
        this.loading = false
      })
    } else if (this.patientId > 0) {
      this.hl7Service.getQBP(this.patientId).subscribe((res) => {
        this.hl7Message = res
        this.loading = false
      })
    } else {
      this.loading = false
    }

  }

  send() {
    this.resultLoading = true
    if (this.vaccinationId > 0) {
      this.hl7Service.quickPostVXU(this.patientId, this.vaccinationId, this.hl7Message).subscribe({
        next: (res) => {
          this.resultLoading = false
          this.error = false
          this.answer = res
        },
        error: (err) => {
          this.error = true
          this.resultLoading = false
          if (err.text) {
            this.answer = err.text
          } else if (err.error.text) {
            this.answer = err.error.text
          } else {
            this.answer = err.error
          }
          console.error(err)
        }
      })
    } else {
      this.hl7Service.quickPostQBP(this.patientId, this.hl7Message).subscribe({
        next: (res) => {
          this.resultLoading = false
          this.error = false
          this.answer = res
        },
        error: (err) => {
          this.error = true
          this.resultLoading = false
          if (err.text) {
            this.answer = err.text
          } else if (err.error.text) {
            this.answer = err.error.text
          } else {
            this.answer = err.error
          }
          console.error(err)
        }
      })
    }

  }

  resultClass(): string {
    if (this.answer === "") {
      return "w3-left w3-padding"
    }
    return this.error ? 'w3-red w3-left w3-padding' : 'w3-green w3-left w3-padding'
  }
}
