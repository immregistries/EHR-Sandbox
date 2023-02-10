import { Component, Input, OnInit } from '@angular/core';

import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Feedback } from 'src/app/core/_model/rest';
import { FeedbackService } from 'src/app/core/_services/feedback.service';
import { ImmunizationRegistryService } from 'src/app/core/_services/immunization-registry.service';
import { PatientService } from 'src/app/core/_services/patient.service';
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
  type: string = "Group";
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
  resourceInternId:  number = -1;
  @Input()
  parentId: number = -1;
  @Input()
  referenceId: string = "";


  constructor(private fhirService: FhirService,
    private patientService: PatientService,
    private vaccinationService: VaccinationService,
    private _snackBar: MatSnackBar,
    private feedbackService: FeedbackService,
    private immRegistriesService: ImmunizationRegistryService) { }

  ngOnInit(): void {
  }

  send() {
    this.answer = ""
    this.requestLoading = true
    console.log(this.type)
    this.fhirService.postResource(this.type,this.resource,this.operation,this.resourceInternId,this.parentId,this.referenceId).subscribe({
      next: (res) => {
        this.requestLoading = false
        this.answer = res
        this.error = false
      },
      error: (err) => {
        this.requestLoading = false
        this.error = true
        if (err.error.error) {
          this.answer = err.error.error
        }else if (err.error.text) {
          this.answer = err.error.text
        } else {
          this.answer = "Error"
        }
        console.error(err)
      }
    })
  }


}
