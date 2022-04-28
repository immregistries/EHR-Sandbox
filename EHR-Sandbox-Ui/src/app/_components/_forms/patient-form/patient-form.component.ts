import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Facility, Patient, Tenant } from 'src/app/_model/rest';
import { FormCard, formType, Reference } from 'src/app/_model/structure';
import { PatientService } from 'src/app/_services/patient.service';
import { Breakpoints, BreakpointObserver } from '@angular/cdk/layout';
import { MatSnackBar } from '@angular/material/snack-bar';
import { BehaviorSubject } from 'rxjs';

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

  /**
   * Currently unusused, just initialised
   */
  public references: BehaviorSubject<{[key:string]: {reference: Reference, value: string}}> = new BehaviorSubject<{[key:string]: {reference: Reference, value: string}}>({});


  constructor(private patientService: PatientService,
    private breakpointObserver: BreakpointObserver,
    private _snackBar: MatSnackBar,
    ) { }

  // Allows Date type casting in HTML template
  asDate(val: any) : Date { return val; }
  asString(val: any) : string { return val; }

  ngOnInit(): void {
  }


  formCards: FormCard[] =  [
    {title: 'Name',  cols: 3, rows: 1, patientForms: [
      {type: formType.text, title: 'First name', attribute: 'nameFirst'},
      {type: formType.text, title: 'Middle name', attribute: 'nameMiddle'},
      {type: formType.text, title: 'Last name', attribute: 'nameLast'},
      {type: formType.text, title: 'Mother maiden name', attribute: 'motherMaiden'},
    ]},
    {title: 'Birth',  cols: 1, rows: 1, patientForms: [
      {type: formType.date, title: 'Birth date', attribute: 'birthDate'},
      {type: formType.yesNo, title: 'Multiple birth', attribute: 'birthFlag'},
      {type: formType.short, title: 'Order', attribute: 'birthOrder'},

    ]},
    {title: 'Identity',cols: 1, rows: 1, patientForms: [
      {type: formType.select, title: 'Sex', attribute: 'sex', options: [{value: 'M'},{value: 'F'}]},
      {type: formType.text, title: 'Ethnicity', attribute: 'ethnicity'},
      {type: formType.text, title: 'Race', attribute: 'race'},
    ]},
    {title: 'Address', cols: 1, rows: 2, patientForms: [
      {type: formType.text, title: 'Line 1', attribute: 'addressLine1'},
      {type: formType.text, title: 'Line 2', attribute: 'addressLine2'},
      {type: formType.text, title: 'Zip code', attribute: 'addressZip'},
      {type: formType.text, title: 'City', attribute: 'addressCity'},
      {type: formType.text, title: 'County', attribute: 'addressCountyParish'},
      {type: formType.text, title: 'State', attribute: 'addressState'},
      {type: formType.text, title: 'Country', attribute: 'addressCountry'},
    ]},
    {title: 'Contact', cols: 1, rows: 1, patientForms: [
      {type: formType.text, title: 'Phone', attribute: 'phone'},
      {type: formType.text, title: 'Email', attribute: 'email'},
    ]},
    {title: 'Death', cols: 1, rows: 1, patientForms: [
      {type: formType.yesNo, title: 'Death flag', attribute: 'deathFlag'},
      {type: formType.date, title: 'Death date', attribute: 'deathDate'},
    ]},
    {title: 'Protection', cols: 1, rows: 1, patientForms: [
      {type: formType.short, title: 'Indicator', attribute: 'protectionIndicator'},
      {type: formType.date, title: 'Date', attribute: 'protectionIndicatorDate'},
    ]},
    {title: 'Registry', cols: 1, rows: 1, patientForms: [
      {type: formType.short, title: 'Indicator', attribute: 'registryStatusIndicator'},
      {type: formType.date, title: 'Date', attribute: 'registryStatusIndicatorDate'},
    ]},
    {title: 'Publicity', cols: 1, rows: 1, patientForms: [
      {type: formType.short, title: 'Indicator', attribute: 'publicityIndicator'},
      {type: formType.date, title: 'Date', attribute: 'publicityIndicatorDate'},
    ]},
    {title: 'Guardian',cols: 3, rows: 1, patientForms: [
      {type: formType.text, title: 'First name', attribute: 'guardianFirst'},
      {type: formType.text, title: 'Middle name', attribute: 'guardianMiddle'},
      {type: formType.text, title: 'Last name', attribute: 'guardianLast'},
      {type: formType.code, title: 'Relationship', attribute: 'guardianRelationship', codeMapLabel: "PERSON_RELATIONSHIP"},
    ]},
  ]
}
