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
    this.fhirService.postResource(this.resourceType,this.resource,this.operation,this.resourceInternId,this.parentId, this.overridingReferences).pipe(tap(() => {
      this.requestLoading = false
      this.feedbackService.doRefresh()
    })).subscribe({
      next: (res) => {
        this.error = false
        this.answer = res
      },
      error: (err) => {
        this.error = true
        if (err.error.error) {
          this.answer = err.error.error
        }else if (err.error.text) {
          this.answer = err.error.text
        } else {
          this.answer = "Error"
        }
        console.error(err)
        this.snackBarService.fatalFhirMessage("", this.resourceInternId)
      }
    })
  }


}
