import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { EhrPatient } from 'src/app/core/_model/rest';
import { FormCard } from 'src/app/core/_model/structure';

@Component({
  selector: 'app-patient-free-form',
  templateUrl: './patient-free-form.component.html',
  styleUrls: ['./patient-free-form.component.css']
})
export class PatientFreeFormComponent implements OnInit {

  @Input()
  patient: EhrPatient = {id: -1};
  @Output()
  patientChange = new EventEmitter<EhrPatient>();

  @Input()
  formCards!: FormCard[]

  constructor() { }

  ngOnInit(): void {
  }

}