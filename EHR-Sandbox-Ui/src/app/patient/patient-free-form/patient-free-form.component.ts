import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Patient } from 'src/app/core/_model/rest';
import { FormCard } from 'src/app/core/_model/structure';

@Component({
  selector: 'app-patient-free-form',
  templateUrl: './patient-free-form.component.html',
  styleUrls: ['./patient-free-form.component.css']
})
export class PatientFreeFormComponent implements OnInit {

  @Input()
  patient: Patient = {id: -1};
  @Output()
  patientChange = new EventEmitter<Patient>();

  @Input()
  formCards!: FormCard[]

  constructor() { }

  ngOnInit(): void {
  }

}
