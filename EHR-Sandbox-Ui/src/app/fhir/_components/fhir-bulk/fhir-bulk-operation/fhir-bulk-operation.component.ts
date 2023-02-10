import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FhirService } from 'src/app/fhir/_services/fhir.service';

@Component({
  selector: 'app-fhir-bulk-operation',
  templateUrl: './fhir-bulk-operation.component.html',
  styleUrls: ['./fhir-bulk-operation.component.css']
})
export class FhirBulkOperationComponent implements OnInit {

  @Input() asynchronous: boolean = true;
  @Input() type: "Patient" | "Group" = "Group";

  constructor(public fhir: FhirService) { }

  ngOnInit(): void {
  }

  @Output()
  contentUrl: EventEmitter<string> = new EventEmitter<string>();

  @Output()
  ndUrl: EventEmitter<string> = new EventEmitter<string>();

  resultList?: [key:{type: string,url:string}];

  @Output()
  resultListEmmitter: EventEmitter<[key:{type: string,url:string}]> = new EventEmitter<[key:{type: string,url:string}]>();

  resourceId: string = ''
  exportArguments: string = '_type=Patient,Immunization'
  autofillContentUrl: boolean = true;
  result: string = ''
  loading = false
  error = false
  export() {
    if (this.asynchronous) {
      this.exportAsynch()
    } else {
      this.exportSynch()
    }
  }

  exportAsynch() {
    if (this.resourceId) {
      this.loading = true
      this.fhir.groupExportAsynch(this.resourceId, this.exportArguments).subscribe((res) => {
        this.result = res.trim()
        this.loading = false
        this.error = false
        if (this.autofillContentUrl) {
          this.contentUrl.emit(res.trim());
        }
      }, (err) => {
        this.result = err.message
        console.error(err)
        this.loading = false
        this.error = true
      })
    }
  }

  exportSynch() {
    if (this.resourceId) {
      this.loading = true
      this.fhir.groupExportSynch(this.resourceId, this.exportArguments).subscribe((res) => {
        this.result = res.trim()
        this.loading = false
        this.error = false
        if (res.startsWith('{')) {
          this.resultList = JSON.parse(res).output ?? []
          if (JSON.parse(res).error && JSON.parse(res).error[0]){
            this.resultList?.push(JSON.parse(res).error[0])
          }
          this.resultListEmmitter.emit(this.resultList)
          if (this.autofillContentUrl) {
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


}
