import { Component, Input, OnInit, ViewEncapsulation } from '@angular/core';
import { FhirService } from '../../_services/fhir.service';

@Component({
  selector: 'app-fhir-bulk',
  templateUrl: './fhir-bulk.component.html',
  styleUrls: ['./fhir-bulk.component.css'],
  // encapsulation: ViewEncapsulation.None
})
export class FhirBulkComponent implements OnInit {


  ngOnInit(): void {
  }

  constructor(public fhir: FhirService) { }

  groupId: string = ''
  exportArguments: string = '_type=Patient,Immunization'
  autofillContentUrl: boolean = true;
  exportResult: string = ''
  exportLoading = false
  asynchronous: boolean = true;
  export() {
    if (this.groupId) {
      if (this.asynchronous) {
        this.exportLoading = true
        this.fhir.groupExportAsynch(this.groupId, this.exportArguments).subscribe((res) => {
          this.exportResult = res.trim()
          this.exportLoading = false
          if (this.autofillContentUrl) {
            this.contentUrl = res.trim()
          }
        })
      } else {
        this.exportLoading = true
        this.fhir.groupExportSynch(this.groupId, this.exportArguments).subscribe((res) => {
          this.exportResult = res.trim()
          this.exportLoading = false
          if (this.autofillContentUrl) {
            // this.contentUrl = res.trim()
          }
        })
      }

    }
  }

  contentUrl: string = ''
  statusResult: string = ''
  statusLoading = false
  autofillNdUrl: boolean = true
  status() {
    if (this.contentUrl) {
      this.statusLoading = true
      this.fhir.groupExportStatus(this.contentUrl).subscribe((res) => {
        this.statusResult = res.trim()
        this.statusLoading = false
        if (this.autofillNdUrl && res.startsWith('{')) {
          this.ndUrl = JSON.parse(res).output[0].url

        }
      })
    }
  }
  cancelLoading: boolean = false
  cancel() {
    if (this.contentUrl) {
      this.cancelLoading = true
      this.fhir.groupExportDelete(this.contentUrl).subscribe((res) => {
        this.cancelLoading = false
      })
    }
  }

  ndUrl: string = ''
  ndResult: string = ''
  ndLoading = false
  loadInFacility: boolean = false
  ndJson() {
    if (this.ndUrl) {
      this.ndLoading = true
      this.fhir.groupNdJson(this.ndUrl, this.loadInFacility).subscribe((res) => {
        this.ndResult = res.trim()
        this.ndLoading = false
        if (this.loadInFacility) {

        }
      })
    }
  }

  rowHeight(): string {
    return (window.innerHeight - 35) + 'px'
  }

}
