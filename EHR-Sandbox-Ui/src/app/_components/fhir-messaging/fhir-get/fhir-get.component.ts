import { Component, Input, OnInit } from '@angular/core';
import { map, switchMap } from 'rxjs';
import { FhirService } from 'src/app/_services/fhir.service';

@Component({
  selector: 'app-fhir-get',
  templateUrl: './fhir-get.component.html',
  styleUrls: ['./fhir-get.component.css']
})
export class FhirGetComponent {

  @Input() resourceType: 'Patient' | 'Immunization' = 'Patient'
  @Input() identifier: string = ''
  result: string = ''


  constructor(public fhir: FhirService) { }


  get() {
    if (this.identifier) {
      this.fhir.getFromIIS(this.resourceType, this.identifier).subscribe((res) => {
        this.result = res
      })

    }
  }

}