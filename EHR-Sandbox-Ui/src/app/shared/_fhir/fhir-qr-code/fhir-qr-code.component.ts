import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FhirClientService } from 'src/app/core/_services/_fhir/fhir-client.service';
import { FeedbackService } from 'src/app/core/_services/feedback.service';
import { SnackBarService } from 'src/app/core/_services/snack-bar.service';
import { FhirResourceService } from 'src/app/core/_services/_fhir/fhir-resource.service';

@Component({
  selector: 'app-fhir-qr-code',
  templateUrl: './fhir-qr-code.component.html',
  styleUrls: ['./fhir-qr-code.component.css']
})
export class FhirQrCodeComponent {


  @Input()
  resourceLoading: Boolean = false
  requestLoading: Boolean = false
  answer: string = "";
  error: boolean = false;
  style: string = 'width: 100%'


  private _resource: string = "";
  public get resource(): string {
    return this._resource;
  }
  @Input()
  public set resource(value: string | null) {
    this._resource = value ?? "";
  }


  constructor(private fhirClient: FhirClientService,
    private fhirResourceService: FhirResourceService,
    public snackBarService: SnackBarService,
    private feedbackService: FeedbackService) { }

  send() {
    this.answer = ""
    this.requestLoading = true
    this.fhirResourceService.getQrCode(this.resource)
      .subscribe({
        next: (res) => {
          this.requestLoading = false
          this.error = false
          this.answer = res
          this.feedbackService.doRefresh()
        },
        error: (err) => {
          this.requestLoading = false
          this.error = true
          this.answer = err.error
          console.error(err)
          if (err.status == 400) {
            this.answer = err.error
            console.error(err)
            this.snackBarService.fatalFhirMessage(this.answer)
          } else {
            this.answer = err.error
          }
        }
      })
  }

  resultClass(): string {
    if (this.answer === "") {
      return "w3-left w3-padding"
    }
    return this.error ? 'w3-red w3-left w3-padding' : 'w3-green w3-left w3-padding'
  }
}
