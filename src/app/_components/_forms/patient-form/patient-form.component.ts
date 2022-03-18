import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Facility, Patient, Tenant } from 'src/app/_model/rest';
import { FormCard, formType } from 'src/app/_model/structure';
import { PatientService } from 'src/app/_services/patient.service';
import { Breakpoints, BreakpointObserver } from '@angular/cdk/layout';
import { map } from 'rxjs/operators';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialogRef } from '@angular/material/dialog';

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

  constructor(private patientService: PatientService,
    private breakpointObserver: BreakpointObserver,
    private _snackBar: MatSnackBar,
    ) { }

  // fillRandom(): void {
  //   this.patientService.readRandom().subscribe((res) => this.patient = res)
  // }

  // save(): void {
  //   this.patientService.quickPostPatient( this.patient).subscribe((res) => {
  //     this._snackBar.open(`${res}`, 'close')
  //   });
  // }

  // Allows Date type casting in HTML template
  asDate(val: any) : Date { return val; }

  ngOnInit(): void {
    this.patientService.readEmpty().subscribe((res) => this.patient = res)
  }


  formCards: FormCard[] =  [
    {title: 'Name',  cols: 3, rows: 1, forms: [
      {type: formType.text, title: 'First name', attribute: 'nameFirst'},
      {type: formType.text, title: 'Middle name', attribute: 'nameMiddle'},
      {type: formType.text, title: 'Last name', attribute: 'nameLast'},
      {type: formType.text, title: 'Mother maiden name', attribute: 'motherMaiden'},
    ]},
    {title: 'Birth',  cols: 1, rows: 1, forms: [
      {type: formType.date, title: 'Birth date', attribute: 'birthDate'},
      {type: formType.short, title: 'Order', attribute: 'birthOrder'},
      {type: formType.short, title: 'Flag', attribute: 'birthFlag'},
    ]},
    {title: 'identity',cols: 2, rows: 1, forms: [
      {type: formType.short, title: 'Sex', attribute: 'sex'},
      {type: formType.text, title: 'Ethnicity', attribute: 'ethnicity'},
      {type: formType.text, title: 'race', attribute: 'race'},
    ]},
    {title: 'Address', cols: 3, rows: 2, forms: [
      {type: formType.text, title: 'Line 1', attribute: 'addressLine1'},
      {type: formType.text, title: 'Line 2', attribute: 'addressLine2'},
      {type: formType.text, title: 'Zip code', attribute: 'addressCity'},
      {type: formType.text, title: 'City', attribute: 'addressCity'},
      {type: formType.text, title: 'County', attribute: 'addressCountyParish'},
      {type: formType.text, title: 'State', attribute: 'addressState'},
      {type: formType.text, title: 'Country', attribute: 'addressCountry'},
    ]},
    {title: 'Contact', cols: 1, rows: 1, forms: [
      {type: formType.text, title: 'Phone', attribute: 'phone'},
      {type: formType.text, title: 'Email', attribute: 'email'},
    ]},
    {title: 'Death', cols: 1, rows: 1, forms: [
      {type: formType.short, title: 'Death flag', attribute: 'deathFlag'},
      {type: formType.date, title: 'Death date', attribute: 'deathDate'},
    ]},
    {title: 'Protection', cols: 1, rows: 1, forms: [
      {type: formType.short, title: 'Indicator', attribute: 'deathFlag'},
      {type: formType.date, title: 'Date', attribute: 'deathDate'},
    ]},
    {title: 'Registry', cols: 1, rows: 1, forms: [
      {type: formType.short, title: 'Indicator', attribute: 'deathFlag'},
      {type: formType.date, title: 'Date', attribute: 'deathDate'},
    ]},
    {title: 'Publicity', cols: 1, rows: 1, forms: [
      {type: formType.short, title: 'Indicator', attribute: 'deathFlag'},
      {type: formType.date, title: 'Date', attribute: 'deathDate'},
    ]},
    {title: 'Guardian',cols: 3, rows: 1, forms: [
      {type: formType.text, title: 'First name', attribute: 'guardianFirst'},
      {type: formType.text, title: 'Middle name', attribute: 'guardianMiddle'},
      {type: formType.text, title: 'Last name', attribute: 'guardianLast'},
      {type: formType.text, title: 'Relationship', attribute: 'guardianRelationship'},
    ]},
  ]
}
