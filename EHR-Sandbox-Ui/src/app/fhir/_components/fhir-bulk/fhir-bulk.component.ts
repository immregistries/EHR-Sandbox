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

  @Input() asynchronous: boolean = true;

  groupId: string = ''
  exportArguments: string = '_type=Patient,Immunization'
  autofillContentUrl: boolean = true;
  exportResult: string = ''
  exportLoading = false
  exportError = false
  export() {
    if (this.asynchronous) {
      this.exportAsynch()
    } else {
      this.exportSynch()
    }
  }

  exportAsynch() {
    if (this.groupId) {
      this.exportLoading = true
      this.fhir.groupExportAsynch(this.groupId, this.exportArguments).subscribe((res) => {
        this.exportResult = res.trim()
        this.exportLoading = false
        this.exportError = false
        if (this.autofillContentUrl) {
          this.contentUrl = res.trim()
        }
      }, (err) => {
        this.exportResult = err.message
        console.error(err)
        this.exportLoading = false
        this.exportError = true
      })
    }
  }

  exportSynch() {
    if (this.groupId) {
      this.exportLoading = true
      this.fhir.groupExportSynch(this.groupId, this.exportArguments).subscribe((res) => {
        this.exportResult = res.trim()
        this.exportLoading = false
        this.exportError = false
        if (res.startsWith('{')) {
          this.resultList = JSON.parse(res).output ?? []
          if (JSON.parse(res).error && JSON.parse(res).error[0]){
            this.resultList?.push(JSON.parse(res).error[0])
          }
          if (this.autofillContentUrl) {
            this.ndUrl = this.resultList?.find((value: {type: string,url:string})  => value.type == "Patient" )?.url ?? ''
          }
        }
      }, (err) => {
        this.exportResult = err.message
        console.error(err)
        this.exportLoading = false
        this.exportError = true
      })
    }
  }

  resultList?: [key:{type: string,url:string}];

  contentUrl: string = ''
  statusResult: string = ''
  statusLoading = false
  statusError = false
  autofillNdUrl: boolean = true
  status() {
    if (this.contentUrl) {
      this.statusLoading = true
      this.fhir.groupExportStatus(this.contentUrl).subscribe((res) => {
        this.statusResult = res.trim()
        this.statusLoading = false
        this.statusError = false
        if (res.startsWith('{')) {
          this.resultList = JSON.parse(res).output ?? []
          if (JSON.parse(res).error && JSON.parse(res).error[0]){
            this.resultList?.push(JSON.parse(res).error[0])
          }
          if (this.autofillNdUrl) {
            this.ndUrl = this.resultList?.find((value: {type: string,url:string})  => value.type == "Patient" )?.url ?? ''
          }
        }
      }, (err) => {
        this.statusResult = err.message
        console.error(err)
        this.statusLoading = false
        this.statusError = true
      })
    }
  }
  cancelLoading: boolean = false
  cancel() {
    if (this.contentUrl) {
      this.statusLoading = true
      this.fhir.groupExportDelete(this.contentUrl).subscribe((res) => {
        this.statusResult = res
        this.statusLoading = false
        this.statusError = false
      }, (err) => {
        this.statusResult = err.message
        console.error(err)
        this.statusLoading = false
        this.statusError = true
      })
    }
  }

  ndUrl: string = ''
  ndResult: string = ''
  ndLoading = false
  ndError = false
  loadInFacility: boolean = false
  ndJson() {
    if (this.ndUrl && this.ndUrl.length > 1) {
      this.ndLoading = true
      this.fhir.groupNdJson(this.ndUrl, this.loadInFacility).subscribe((res) => {
        this.ndResult = res.trim()
        this.ndLoading = false
        this.ndError = false

        // if (this.loadInFacility) {

        // }
      }, (err) => {
        this.ndResult = err.message
        console.error(err)
        this.ndLoading = false
        this.ndError = true
      })
    } else if (this.ndResult) {
      this.ndLoading = true
      this.fhir.loadNdJson(this.ndResult).subscribe((res) => {
        // this.ndResult = res.trim()
        this.ndLoading = false
        this.ndError = false
      }, (err) => {
        this.ndResult = err.message
        console.error(err)
        this.ndLoading = false
        this.ndError = true
      })
    }
  }

  rowHeight(): string {
    return (window.innerHeight - 130) + 'px'
  }

}
