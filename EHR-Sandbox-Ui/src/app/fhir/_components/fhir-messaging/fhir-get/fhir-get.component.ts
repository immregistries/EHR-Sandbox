import { Component, Input, OnInit } from '@angular/core';
import { Identifier } from 'fhir/r5';
import { map, Observable, of, switchMap, tap, throwError } from 'rxjs';
import { FhirService } from 'src/app/fhir/_services/fhir.service';

@Component({
  selector: 'app-fhir-get',
  templateUrl: './fhir-get.component.html',
  styleUrls: ['./fhir-get.component.css']
})
export class FhirGetComponent {

  @Input() resourceType: 'Patient' | 'Immunization' = 'Patient'
  @Input() identifierString: string = ''
  result: string = ''
  loading = false
  error: boolean = false
  // identifier: Identifier = {};

  constructor(public fhir: FhirService) {}

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
    if (this.identifierString) {
      this.loading = true
      this.chooseRequest(this.identifierString).pipe(tap((res) => {
          this.loading = false
          this.result = res
        })).subscribe({
          next: (res) => {
            this.error = false
          },
          error: (err) => {
            this.error = true
          },
        })
    }
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
    }
    return throwError('No identifier specified')
  }
}
