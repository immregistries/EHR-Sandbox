import { Component, Input } from '@angular/core';
import { FhirService } from 'src/app/fhir/_services/fhir.service';

@Component({
  selector: 'app-fhir-bulk-ndjson-manual',
  templateUrl: './fhir-bulk-ndjson-manual.component.html',
  styleUrls: ['./fhir-bulk-ndjson-manual.component.css']
})
export class FhirBulkNdjsonManualComponent {

  constructor(public fhir: FhirService) { }


  @Input()
  ndJsonInput: string = ''

  result: string = ''
  loading = false
  error = false

  load() {
    if (this.ndJsonInput) {
      this.loading = true
      this.fhir.loadNdJson(this.ndJsonInput).subscribe((res) => {
        // this.ndResult = res.trim()
        this.result = res
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
