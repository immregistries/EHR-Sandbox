import { Component, EventEmitter, Input, Output, ViewChild } from '@angular/core';
import { JsonFormComponent } from '../abstract-json-form.component';
import { EhrPatient } from 'src/app/core/_model/rest';
import { PatientService } from 'src/app/core/_services/patient.service';
import { MatInput } from '@angular/material/input';

@Component({
  selector: 'app-patient-json-form',
  templateUrl: '../abstract-json-form.component.html',
  styleUrls: ['../abstract-json-form.component.css']
})
export class PatientJsonFormComponent extends JsonFormComponent<EhrPatient> {

  @ViewChild('txtarea', { static: false })
  public txtArea!: HTMLTextAreaElement;
  @ViewChild('txtarea', { static: false })
  public matInput!: MatInput;

  @Input()
  override set model(value: string) {
    super.model = value
  }

  @Output()
  modelChange: EventEmitter<EhrPatient> = new EventEmitter()

  constructor(private patientService: PatientService) {
    super()
  }

  checkType(value: EhrPatient): void {

  }

  // override send(): void {
  //   this.patientService.quickPostPatient(JSON.parse(this.model)).subscribe(
  //     {
  //       next: (res) => {
  //         this.resultLoading = false
  //         this.error = false
  //         this.answer = res.body ?? '**empty result**'
  //       },
  //       error: (err) => {
  //         this.error = true
  //         this.resultLoading = false
  //         if (err.text) {
  //           this.answer = err.text
  //         } else if (err.error.text) {
  //           this.answer = err.error.text
  //         } else {
  //           this.answer = err.error
  //         }
  //         console.error(err)
  //       }
  //     }
  //   )
  // }

}
