import { Component, Input} from '@angular/core';
import { FeedbackService } from 'src/app/core/_services/feedback.service';
import { SnackBarService } from 'src/app/core/_services/snack-bar.service';
import { FhirClientService } from 'src/app/fhir/_services/fhir-client.service';

@Component({
  selector: 'app-fhir-post',
  templateUrl: './fhir-post.component.html',
  styleUrls: ['./fhir-post.component.css']
})
export class FhirPostComponent  {
  resourceLoading: Boolean = false
  requestLoading: Boolean = false
  answer: string = "";
  error: boolean = false;
  style: string = 'width: 100%'

  @Input()
  resourceType: string = "Patient";
  @Input()
  operation:  "UpdateOrCreate" | "Create" | "Update" | "$match" = "UpdateOrCreate";
  @Input()
  resourceLocalId: number = -1;
  @Input()
  parentId: number = -1;
  @Input()
  overridingReferences: {[reference: string]: string} = {};

  private _resource: string = "";
  public get resource(): string {
    return this._resource;
  }
  @Input()
  public set resource(value:string | null) {
    this._resource = value ?? "";
  }

  constructor(private fhirClient: FhirClientService,
    private snackBarService: SnackBarService,
    private feedbackService: FeedbackService) { }

  send() {
    this.answer = ""
    this.requestLoading = true
    this.fhirClient.postResource(this.resourceType,this.resource,this.operation,this.resourceLocalId,this.parentId, this.overridingReferences)
    .subscribe({
      next: (res) => {
        this.requestLoading = false
        this.error = false
        this.answer = res
        this.feedbackService.doRefresh()
      },
      error: (err) => {
        this.requestLoading = false
        this.error = true
        this.answer = err.error
        console.error(err)
        if(err.status == 400) {
          this.answer = err.error
          console.error(err)
          switch(this.resourceType) {
            case "Patient" : {
              this.snackBarService.fatalFhirMessage(this.answer, this.resourceLocalId)
              break;
            }
            case "Immunization" : {
              this.snackBarService.fatalFhirMessage(this.answer, this.parentId, this.resourceLocalId)
              break;
            }
          }
        } else {
          this.answer = JSON.stringify(err.error)
        }
        this.feedbackService.doRefresh()

      }
    })
  }
}
