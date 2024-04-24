import { Component, Inject, Input, OnInit, Optional } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Identifier } from 'fhir/r5';
import { map, Observable, of, switchMap, tap, throwError } from 'rxjs';
import { SnackBarService } from 'src/app/core/_services/snack-bar.service';
import { FhirClientService } from 'src/app/fhir/_services/fhir-client.service';

@Component({
  selector: 'app-fhir-get',
  templateUrl: './fhir-get.component.html',
  styleUrls: ['./fhir-get.component.css']
})
export class FhirGetComponent {

  @Input() resourceType: string = 'Patient'
  @Input() identifierString: string = ''
  result: string = ''
  loading = false
  error: boolean = false
  // identifier: Identifier = {};

  constructor(public fhir: FhirClientService, public snackBarService: SnackBarService,
    @Optional() public _dialogRef: MatDialogRef<FhirGetComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: { resourceType: string, identifierString: string }) {
    if (data) {
      if (data.resourceType) {
        this.resourceType = data.resourceType
      }
      if (data.identifierString) {
        this.identifierString = data.identifierString
      }
    }

  }

  // search() {
  //   if (this.identifier.value) {
  //     this.loading = true
  //     this.chooseRequest(this.identifierString).pipe(tap((res) => {
  //         this.loading = false
  //         this.result = res
  //       })).subscribe({
  //         next: (res) => {
  //           this.error = false
  //         },
  //         error: (err) => {
  //           this.error = true
  //         },
  //       })
  //   }
  // }

  get() {
    this.loading = true
    this.chooseRequest(this.identifierString).subscribe({
      next: (res) => {
        this.loading = false
        this.error = false
        this.result = JSON.parse(res)
      },
      error: (err) => {
        this.loading = false
        this.error = true
        if (err.error) {
          this.result = err.error
        } else {
          this.result = JSON.stringify(JSON.parse(err))
        }
      },
    })

  }

  private chooseRequest(identifierString: string): Observable<string> {
    if (identifierString) {
      const identifierSplit = identifierString.split("|")
      if (identifierSplit.length > 1) {
        return this.fhir.search(this.resourceType, {
          system: identifierSplit[0],
          value: identifierSplit[1],
        })
      } else {
        return this.fhir.getFromIIS(this.resourceType, identifierString)
      }
    } else {
      return this.fhir.getFromIIS(this.resourceType, identifierString)
    }
  }

  resultClass(): string {
    if (this.result === "") {
      return "w3-left w3-padding"
    }
    return this.error ? 'w3-red w3-left w3-padding' : 'w3-green w3-left w3-padding'
  }
}
