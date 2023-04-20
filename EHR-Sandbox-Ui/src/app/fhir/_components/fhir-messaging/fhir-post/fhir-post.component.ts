import { Component, Input, OnInit } from '@angular/core';
import { map, merge, Observable, tap } from 'rxjs';

import { FeedbackService } from 'src/app/core/_services/feedback.service';
import { ImmunizationRegistryService } from 'src/app/core/_services/immunization-registry.service';
import { PatientService } from 'src/app/core/_services/patient.service';
import { SnackBarService } from 'src/app/core/_services/snack-bar.service';
import { VaccinationService } from 'src/app/core/_services/vaccination.service';
import { FhirService } from 'src/app/fhir/_services/fhir.service';

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
  resource: string = `{
    "resourceType": "Group",
    "type": "person",
    "actual": true,
    "member": [
      {
        "entity": {
          "reference": "Patient/"
        },
        "period": {
          "start": "2014-10-08"
        }
      }
    ]
  }`;
  answer: string = "";
  error: boolean = false;

  style: string = 'width: 100%'

  @Input()
  operation:  "UpdateOrCreate" | "Create" | "Update" = "UpdateOrCreate";
  @Input()
  resourceInternId: number = -1;
  @Input()
  parentId: number = -1;
  @Input()
  overridingReferences: {[reference: string]: string} = {};


  constructor(private fhirService: FhirService,
    private snackBarService: SnackBarService,
    private feedbackService: FeedbackService) { }

  ngOnInit(): void {
  }

  send() {
    this.answer = ""
    this.requestLoading = true
    this.fhirService.postResource(this.resourceType,this.resource,this.operation,this.resourceInternId,this.parentId, this.overridingReferences)
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
              this.snackBarService.fatalFhirMessage(this.answer, this.resourceInternId)
              break;
            }
            case "Immunization" : {
              this.snackBarService.fatalFhirMessage(this.answer, this.parentId, this.resourceInternId)
              break;
            }
          }
        } else {
          this.answer = JSON.stringify(err.error)
        }

      }
    })
  }


}
