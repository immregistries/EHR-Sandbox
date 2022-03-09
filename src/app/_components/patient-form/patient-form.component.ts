import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Facility, Patient, Tenant } from 'src/app/_model/rest';
import { FormCard, formType } from 'src/app/_model/structure';
import { PatientService } from 'src/app/_services/patient.service';
import { Breakpoints, BreakpointObserver } from '@angular/cdk/layout';
import { map } from 'rxjs/operators';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-patient-form',
  templateUrl: './patient-form.component.html',
  styleUrls: ['./patient-form.component.css']
})
export class PatientFormComponent implements OnInit {

  @Input()
  patient: Patient = {id: -1};
  @Output()
  patientChange = new EventEmitter<Patient>();

  tenant?: Tenant;
  facility?: Facility;



  constructor(private patientService: PatientService, private breakpointObserver: BreakpointObserver, private _snackBar: MatSnackBar) {

   }

  fillRandom(): void {
    this.patientService.getRandom().subscribe((res) => this.patient = res)
  }

  save(): void {
    if (!this.tenant) {
      this._snackBar.open("Tenant Missing", "close")
    } else if (!this.facility) {
      this._snackBar.open("Facility Missing", "close")

    } else {
      this.patientService.postPatient(this.tenant,this.facility, this.patient);
    }

  }

  // Allows Date type casting in HTML template
  asDate(val: any) : Date { return val; }

  ngOnInit(): void {
    this.patientService.getEmpty().subscribe((res) => this.patient = res)
  }


  formCards: FormCard[] =  [
    {title: 'Name',  cols: 2, rows: 2, forms: [
      {type: formType.text, title: 'First name', attribute: 'nameFirst'},
      {type: formType.text, title: 'Middle name', attribute: 'nameMiddle'},
      {type: formType.text, title: 'Last name', attribute: 'nameLast'},
      {type: formType.date, title: 'Birth date', attribute: 'birthDate'},
      {type: formType.text, title: 'Birth order', attribute: 'birthOrder'},
      {type: formType.text, title: 'Birth flag', attribute: 'birthFlag'},

    ]},
    {title: 'Address', cols: 4, rows: 2, forms: [
      {type: formType.text, title: 'Line 1', attribute: 'addressLine1'},
      {type: formType.text, title: 'Line 2', attribute: 'addressLine2'},
      {type: formType.text, title: 'Zip code', attribute: 'addressCity'},
      {type: formType.text, title: 'City', attribute: 'addressCity'},
      {type: formType.text, title: 'County', attribute: 'addressCountyParish'},
      {type: formType.text, title: 'State', attribute: 'addressState'},
      {type: formType.text, title: 'Country', attribute: 'addressCountry'},
    ]},
    {title: 'Contact', cols: 2, rows: 1, forms: [
      {type: formType.text, title: 'Phone', attribute: 'phone'},
      {type: formType.text, title: 'Email', attribute: 'email'},
    ]},
    {cols: 2, rows: 1, forms: [
      {type: formType.text, title: 'Sex', attribute: 'sex'},
      {type: formType.text, title: 'Ethnicity', attribute: 'ethnicity'},
      {type: formType.text, title: 'race', attribute: 'race'},
    ]},


  ]


}
