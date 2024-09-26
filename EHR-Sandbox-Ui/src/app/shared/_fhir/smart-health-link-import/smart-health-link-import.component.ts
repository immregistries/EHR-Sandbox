import { Component, Inject, Input, Optional } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FhirClientService } from 'src/app/core/_services/_fhir/fhir-client.service';
import { FhirResourceService } from 'src/app/core/_services/_fhir/fhir-resource.service';
import { FeedbackService } from 'src/app/core/_services/feedback.service';
import { SnackBarService } from 'src/app/core/_services/snack-bar.service';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';

@Component({
  selector: 'app-smart-health-link-import',
  templateUrl: './smart-health-link-import.component.html',
  styleUrls: ['./smart-health-link-import.component.css']
})
export class SmartHealthLinkImportComponent {

  readonly exampleUrl = [
    "https://shlink.ips.health/ips#shlink:/eyJ1cmwiOiJodHRwczovL2FwaS52YXh4LmxpbmsvYXBpL3NobC9xdlJYWW1LMTkwVmlVOXNmcXJ1c1dSby1zRVhOZmFxTE8yOUJUTDFPYTRnIiwiZXhwIjoxNzI3ODA2MTg1Ljg4MiwiZmxhZyI6IlAiLCJrZXkiOiJtZ2lQam1HUEg0TXUxbGFaZ0JYMVF2VmFLdWtIcm15aUhFbFF4blRKdWVFIiwibGFiZWwiOiJ0ZXN0IDEyMzQ1Njc4OSJ9",
    "https://viewer.tcpdev.org/shlink.html#shlink:/eyJ1cmwiOiJodHRwczovL3Jhdy5naXRodWJ1c2VyY29udGVudC5jb20vc2Vhbm5vL3NoYy1kZW1vLWRhdGEvbWFpbi9pcHMvSVBTX0lHLWJ1bmRsZS0wMS1lbmMudHh0IiwiZmxhZyI6IkxVIiwia2V5IjoicnhUZ1lsT2FLSlBGdGNFZDBxY2NlTjh3RVU0cDk0U3FBd0lXUWU2dVg3USIsImxhYmVsIjoiRGVtbyBTSEwgZm9yIElQU19JRy1idW5kbGUtMDEifQ",
  ]

  readonly exampleJwks = [
    `{"kty": "EC","kid": "3Kfdg-XwP-7gXyywtUfUADwBumDOPKMQx-iELL11W9s","use": "sig","alg": "ES256","crv": "P-256","x": "11XvRWy1I2S0EyJlyf_bWfw_TQ5CJJNLw78bHXNxcgw","y": "eZXwxvO1hvCY0KucrPfKo7yAyMT6Ajc3N7OkAB6VYy8","d": "FvOOk6hMixJ2o9zt4PCfan_UW7i4aOEnzj76ZaCI9Og"}`,
  ]


  @Input()
  url: string = ""
  @Input()
  password: string = ""
  @Input()
  jwk: string = ""
  requestLoading: Boolean = false
  answer?: string | undefined;
  answerArray?: string[];
  error: boolean = false;
  style: string = 'width: 100%'

  constructor(private fhirClient: FhirClientService,
    private fhirResourceService: FhirResourceService,
    public snackBarService: SnackBarService,
    private feedbackService: FeedbackService,
    @Optional() public _dialogRef: MatDialogRef<SmartHealthLinkImportComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: {
      patientId: number,
      url: string
    }) {
    if (data?.patientId) {

    }
    if (data?.url) {
      this.url = data.url
    }

  }

  readShl() {
    this.answer = undefined
    this.requestLoading = true
    this.fhirClient.shlink(this.url, this.password, this.jwk).subscribe({
      next: (res) => {
        this.requestLoading = false
        this.error = false
        this.answer = res
        // this.answerArray = res
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
          this.snackBarService.fatalFhirMessage(err.error ?? "")
        } else {
          this.answer = err.error
        }
      }
    })
  }

  resultClass(): string {
    if (this.answer === "" || !this.answer) {
      return "w3-left w3-padding"
    }
    return this.error ? 'w3-red w3-left w3-padding' : 'w3-green w3-left w3-padding'
  }
}
