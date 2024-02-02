import { Component, Input } from '@angular/core';
import { EhrPatient } from 'src/app/core/_model/rest';

@Component({
  selector: 'app-patient-group-list',
  templateUrl: './patient-group-list.component.html',
  styleUrls: ['./patient-group-list.component.css']
})
export class PatientGroupListComponent {
  @Input()
  patient!: EhrPatient;
}
