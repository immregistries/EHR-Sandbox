import { Component, Inject, Input, Optional } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FhirClientService } from 'src/app/core/_services/_fhir/fhir-client.service';
import { FhirResourceService } from 'src/app/core/_services/_fhir/fhir-resource.service';
import { FeedbackService } from 'src/app/core/_services/feedback.service';
import { SnackBarService } from 'src/app/core/_services/snack-bar.service';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';

@Component({
  selector: 'app-smart-health-link-import',
  templateUrl: './smart-health-link-import.component.html',
  styleUrls: ['./smart-health-link-import.component.css']
})
export class SmartHealthLinkImportComponent {

  readonly exampleUrl = [
    "https://shlink.ips.health/ips#shlink:/eyJ1cmwiOiJodHRwczovL2FwaS52YXh4LmxpbmsvYXBpL3NobC9pZU9VdU9vZkF2aUU2eEtjV0hwQkx1azA4LUVLZjRYdTROT1BOb2Fmb2RZIiwiZXhwIjoxOTQ4MjI3NjI5LjcwOCwiZmxhZyI6IiIsImtleSI6IjJ1UkR0TzZIbTBwa3VudFNPeEgtSmlZZEhOWWg2N0VFbzh4NGNpM2xWR00iLCJsYWJlbCI6IlNITCBmcm9tIDIwMjQtMDktMjcifQ",
    "https://viewer.tcpdev.org/shlink.html#shlink:/eyJ1cmwiOiJodHRwczovL3Jhdy5naXRodWJ1c2VyY29udGVudC5jb20vc2Vhbm5vL3NoYy1kZW1vLWRhdGEvbWFpbi9pcHMvSVBTX0lHLWJ1bmRsZS0wMS1lbmMudHh0IiwiZmxhZyI6IkxVIiwia2V5IjoicnhUZ1lsT2FLSlBGdGNFZDBxY2NlTjh3RVU0cDk0U3FBd0lXUWU2dVg3USIsImxhYmVsIjoiRGVtbyBTSEwgZm9yIElQU19JRy1idW5kbGUtMDEifQ",
  ]

  readonly exampleJwks = [
    `{"kty": "EC","kid": "3Kfdg-XwP-7gXyywtUfUADwBumDOPKMQx-iELL11W9s","use": "sig","alg": "ES256","crv": "P-256","x": "11XvRWy1I2S0EyJlyf_bWfw_TQ5CJJNLw78bHXNxcgw","y": "eZXwxvO1hvCY0KucrPfKo7yAyMT6Ajc3N7OkAB6VYy8","d": "FvOOk6hMixJ2o9zt4PCfan_UW7i4aOEnzj76ZaCI9Og"}`,
  ]

  @Input()
  patientId: number = -1;
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
    private feedbackService: FeedbackService, public sanitizer: DomSanitizer,
    @Optional() public _dialogRef: MatDialogRef<SmartHealthLinkImportComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: {
      patientId: number,
      url: string
    }) {
    if (data?.patientId) {
      this.patientId = data.patientId
    }
    if (data?.url) {
      this.url = data.url
    }

  }

  sanitizedHtml?: SafeHtml;

  readShl() {
    this.answer = undefined
    this.requestLoading = true
    this.sanitizedHtml = undefined
    this.fhirClient.shlinkRead(this.url, this.password, this.jwk).subscribe({
      next: (res) => {
        this.requestLoading = false
        this.error = false
        this.answer = res

        let jsonRes = JSON.parse(res);
        let composition;
        if (jsonRes["credentialSubject"]['fhirBundle']['entry'][0]['resource']) {
          composition = jsonRes["credentialSubject"]['fhirBundle']['entry'][0]['resource']
        } else if (jsonRes['entry'][0]['resource']) {
          composition = jsonRes['entry'][0]['resource']
        }
        if (composition['text']['div']) {
          this.sanitizedHtml = this.sanitizer.bypassSecurityTrustHtml(composition['text']['div']);
        }
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


  importVaccinations() {
    this.answer = undefined
    this.requestLoading = true
    this.fhirClient.importShlinkForPatient(this.patientId, this.url, this.password, this.jwk).subscribe({
      next: (res) => {
        this.requestLoading = false
        this._dialogRef.close(res)
        // this.answerArray = res
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
