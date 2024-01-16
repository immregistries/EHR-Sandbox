import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FhirBulkService } from 'src/app/fhir/_services/fhir-bulk.service';

@Component({
  selector: 'app-fhir-bulk-status-check',
  templateUrl: './fhir-bulk-status-check.component.html',
  styleUrls: ['./fhir-bulk-status-check.component.css']
})
export class FhirBulkStatusCheckComponent implements OnInit {

  constructor(public fhirBulkService: FhirBulkService) { }

  ngOnInit(): void {
  }

  @Input()
  contentUrl: string = ''

  result: string = ''
  loading = false
  error = false

  autofillNdUrl: boolean = true

  resultList?: [key:{type: string,url:string}];

  @Output()
  resultListEmmitter: EventEmitter<[key:{type: string,url:string}]> = new EventEmitter<[key:{type: string,url:string}]>();

  @Output()
  ndUrl: EventEmitter<string> = new EventEmitter<string>();

  status() {
    if (this.contentUrl) {
      this.loading = true
      this.fhirBulkService.groupExportStatus(this.contentUrl).subscribe((res) => {
        this.result = res.trim()
        this.loading = false
        this.error = false
        if (res.startsWith('{')) {
          this.resultList = JSON.parse(res).output ?? []
          if (JSON.parse(res).error && JSON.parse(res).error[0]){
            this.resultList?.push(JSON.parse(res).error[0])
          }
          this.resultListEmmitter.emit(this.resultList)
          if (this.autofillNdUrl) {
            this.ndUrl.emit(this.resultList?.find((value: {type: string,url:string})  => value.type == "Patient" )?.url ?? '')
          }
        }
      }, (err) => {
        this.result = err.message
        console.error(err)
        this.loading = false
        this.error = true
      })
    }
  }


  cancelLoading: boolean = false
  cancel() {
    if (this.contentUrl) {
      this.loading = true
      this.fhirBulkService.groupExportDelete(this.contentUrl).subscribe((res) => {
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
