import { Component, Input, OnInit } from '@angular/core';
import { map, switchMap } from 'rxjs';
import { FhirService } from 'src/app/fhir/_services/fhir.service';

@Component({
  selector: 'app-fhir-get',
  templateUrl: './fhir-get.component.html',
  styleUrls: ['./fhir-get.component.css']
})
export class FhirGetComponent {

  @Input() resourceType: 'Patient' | 'Immunization' = 'Patient'
  @Input() identifier: string = ''
  result: string = ''
  loading = false


  constructor(public fhir: FhirService) { }


  get() {
    if (this.identifier) {
      this.loading = true
      if (this.identifier.includes("/")) {
        this.fhir.get(this.identifier).subscribe((res) => {
          this.result = res
          this.loading = false
        })
      } else {
        this.fhir.getFromIIS(this.resourceType, this.identifier).subscribe((res) => {
          this.result = res
          this.loading = false
        })
      }
    }
    // this.fhir.readOperationOutcome(this.identifier).subscribe((res) => {
    //   console.log(res);

    // })
  }

}
