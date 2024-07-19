import { Component, EventEmitter, Input, OnInit, Output, Pipe, PipeTransform, TrackByFunction } from '@angular/core';
import { FhirClientService } from '../../../core/_services/_fhir/fhir-client.service';




@Component({
  selector: 'app-fhir-operation',
  templateUrl: './fhir-operation.component.html',
  styleUrls: ['./fhir-operation.component.css']
})
export class FhirOperationComponent implements OnInit {

  constructor(public fhirClient: FhirClientService) { }

  ngOnInit(): void {
  }

  loading: boolean = false
  error: boolean = false

  result: string = ""
  @Output()
  resultEmitter: EventEmitter<string> = new EventEmitter<string>()

  @Input()
  operation: string = "$everything" // TODO add type limitation
  @Input()
  resourceType: string = "Patient"
  @Input()
  resourceId: string = ""
  // @Input()
  parameters: { [name: string]: { value: string, type?: 'string' | 'date' | 'boolean' } } = {
    "_type": { value: "Immunization", type: 'string' },
    "_since": { value: "", type: 'date' },
    ":mdm": { value: "false", type: 'string' },
  }


  @Input()
  body: string = ""

  public requestUrl(): string {
    return "Request URI : /" + this.resourceType
      + this.as_path_variable(this.resourceId)
      + this.as_path_variable(this.operation)
      + this.parameter_string()
  }

  private as_path_variable(pathVariable: string): string {
    return pathVariable.length > 0 ? "/" + pathVariable : ""
  }

  parameter_string(): string {
    let paramString: string = ""
    for (const key in this.parameters) {
      if (Object.prototype.hasOwnProperty.call(this.parameters, key)) {
        const element = this.parameters[key];
        if (element.value.length > 0) {
          if (paramString.length > 0) {
            paramString += "&"
          }
          paramString += key + "=" + element.value
        }
      }
    }
    return "?" + paramString
  }



  send() {
    this.fhirClient.operation(this.operation, this.resourceType + this.as_path_variable(this.resourceId), this.parameter_string()).subscribe((res) => {
      this.result = res
    })
  }

  // Allows Date type casting in HTML template
  asDate(val: any): Date { return val; }
  formatDate(val: Date): string {
    return val.toISOString()
  }
}
