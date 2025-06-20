import { Component, Inject, Input } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { SnackBarService } from 'src/app/core/_services/snack-bar.service';
import { VaccinationService } from 'src/app/core/_services/vaccination.service';
import { Hl7Service } from '../../../core/_services/_fhir/hl7.service';
import { Hl7MessagingComponent } from '../hl7-messaging/hl7-messaging.component';
import { ImmunizationRegistryService } from 'src/app/core/_services/immunization-registry.service';
import { FeedbackService } from 'src/app/core/_services/feedback.service';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { AcknowledgementObject } from 'src/app/core/_model/form-structure';
import { Feedback } from 'src/app/core/_model/rest';
import { FhirV2Service } from 'src/app/core/_services/_fhir/fhir-v2.service';
import { IMessagingService } from 'src/app/core/_services/_fhir/i.messaging.service';

@Component({
  selector: 'app-hl7-post',
  templateUrl: './hl7-post.component.html',
  styleUrls: ['./hl7-post.component.css']
})
export class Hl7PostComponent {

  @Input() fhirV2Mode: boolean = false;
  @Input() isForcingVXU: boolean = false;
  @Input() vaccinationId: number = -1;
  @Input() patientId: number = -1;
  @Input() loading: boolean = false
  @Input() hl7Message: string = "";

  /**
   * TODO change this temp solution
   */
  @Input() displayStyle: string = "display: flex; width: 100%;";

  resultLoading: boolean = false

  public answer: string = "";
  public error: boolean = false;

  public resultObject?: AcknowledgementObject<Feedback>

  constructor(private vaccinationService: VaccinationService,
    private feedbackService: FeedbackService,
    private facilityService: FacilityService,
    private hl7Service: Hl7Service,
    private fhirV2Service: FhirV2Service,
    public snackBarService: SnackBarService,
    public immunizationRegistryService: ImmunizationRegistryService,
    public _dialogRef: MatDialogRef<Hl7MessagingComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { patientId: number, vaccinationId: number }) {
    this.patientId = data.patientId
    this.vaccinationId = data.vaccinationId
  }

  send() {
    let service: IMessagingService = this.hl7Service
    if (this.fhirV2Mode === true) {
      service = this.fhirV2Service
    }
    this.resultLoading = true
    if (this.vaccinationId > 0 || this.isForcingVXU === true) {
      service.quickPostVXU(this.patientId, this.vaccinationId, this.hl7Message).subscribe({
        next: this.successProcessing,
        error: this.errorProcessing
      })
    } else if (this.patientId > 0) {
      service.quickPostQBP(this.patientId, this.hl7Message).subscribe({
        next: this.successProcessing,
        error: this.errorProcessing
      })
    } else {
      this.answer = this.hl7Message
      this.feedbackService.convertAck(this.answer, this.immunizationRegistryService.getCurrentId(), this.patientId, this.vaccinationId)
        .subscribe(this.ackObjectProcessing);
      this.resultLoading = false
    }

  }


  private successProcessing = (ack: AcknowledgementObject<Feedback> | string) => {
    this.resultLoading = false
    this.error = false
    if (typeof ack === "string") {
      this.resultObject = { rawResult: ack, sortedResult: { errors: [], warnings: [], infos: [], notices: [] } }
    } else {
      this.resultObject = ack
    }
    // this.feedbackService.doRefresh()
  }

  private errorProcessing = (err: any) => {
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
    this.resultObject = { rawResult: this.answer, sortedResult: { errors: [], warnings: [], infos: [], notices: [] } }
    this.snackBarService.errorMessage(this.answer)
  }

  private ackObjectProcessing = (acknowledgementObject: AcknowledgementObject<Feedback>) => {
    acknowledgementObject.rawResult = this.answer
    this.resultObject = acknowledgementObject
    // this.feedbackService.doRefresh()
  }

  resultClass(): string {
    if (this.answer === "") {
      return "w3-left w3-padding"
    }
    return this.error ? 'w3-red w3-left w3-padding' : 'w3-green w3-left w3-padding'
  }
}
