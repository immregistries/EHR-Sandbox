import { Component, Input, OnInit } from '@angular/core';
import { map, merge, Observable, tap } from 'rxjs';
import { FacilityService } from 'src/app/core/_services/facility.service';

import { FeedbackService } from 'src/app/core/_services/feedback.service';
import { SnackBarService } from 'src/app/core/_services/snack-bar.service';
import { FhirClientService } from 'src/app/fhir/_services/fhir-client.service';

@Component({
  selector: 'app-fhir-post',
  templateUrl: './fhir-post.component.html',
  styleUrls: ['./fhir-post.component.css']
})
export class FhirPostComponent implements OnInit {
  resourceLoading: Boolean = false
  requestLoading: Boolean = false

  @Input()
  resourceType: string = "Group";
  @Input()
  resource: string =
`{
  "resourceType": "Group",
  "type": "person",
  "identifier": {
    "system": "ehr-sandbox/group",
    "value": "` + Math.trunc(Math.random() * Math.random() * 10000) + `"
  },
  "actual": true,
  "managingEntity": {
    "identifier": {
      "system": "ehr-sandbox/facility",
      "value": "`+ this.facilityService.getCurrentId() +`"
    }
  },
  "description": "Generated sample Group in EHR sandbox for testing"
}`;
  answer: string = "";
  error: boolean = false;

  style: string = 'width: 100%'

  @Input()
  operation:  "UpdateOrCreate" | "Create" | "Update" = "UpdateOrCreate";
  @Input()
  resourceLocalId: number = -1;
  @Input()
  parentId: number = -1;
  @Input()
  overridingReferences: {[reference: string]: string} = {};


  constructor(private fhirClient: FhirClientService,
    private facilityService: FacilityService,
    private snackBarService: SnackBarService,
    private feedbackService: FeedbackService) { }

  ngOnInit(): void {
  }

  send() {
    this.answer = ""
    this.requestLoading = true
    this.fhirClient.postResource(this.resourceType,this.resource,this.operation,this.resourceLocalId,this.parentId, this.overridingReferences)
    .subscribe({
      next: (res) => {
        this.requestLoading = false
        this.feedbackService.doRefresh()
        this.error = false
        this.answer = res
      },
      error: (err) => {
        this.requestLoading = false
        this.feedbackService.doRefresh()
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

      }
    })
  }

  match() {
    this.answer = ""
    this.requestLoading = true
    this.fhirClient.matchResource(this.resourceType,this.resource, this.resourceLocalId, this.parentId)
    .subscribe({
      next: (res) => {
        this.requestLoading = false
        this.feedbackService.doRefresh()
        this.error = false
        this.answer = JSON.stringify(res)
      },
      error: (err) => {
        this.requestLoading = false
        this.feedbackService.doRefresh()
        this.error = true
        this.answer = err.error
        console.error(err)
      }
    })
  }


}
