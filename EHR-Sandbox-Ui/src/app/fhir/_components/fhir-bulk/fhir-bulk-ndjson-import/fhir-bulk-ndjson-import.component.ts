import { Component, Input, OnInit } from '@angular/core';
import { FhirService } from 'src/app/fhir/_services/fhir.service';

@Component({
  selector: 'app-fhir-bulk-ndjson-import',
  templateUrl: './fhir-bulk-ndjson-import.component.html',
  styleUrls: ['./fhir-bulk-ndjson-import.component.css']
})
export class FhirBulkNdjsonImportComponent implements OnInit {

  constructor(public fhir: FhirService) { }

  ngOnInit(): void {
  }

  @Input()
  ndUrl: string = ''

  @Input()
  resultList?: [key:{type: string,url:string}];


  result: string = ''
  loading = false
  error = false

  loadInFacility: boolean = false
  ndJson() {
    if (this.ndUrl && this.ndUrl.length > 1) {
      this.loading = true
      this.fhir.groupNdJson(this.ndUrl, this.loadInFacility).subscribe((res) => {
        this.result = res.trim()
        this.loading = false
        this.error = false
      }, (err) => {
        this.result = err.message
        console.error(err)
        this.loading = false
        this.error = true
      })
    }
  }

}
