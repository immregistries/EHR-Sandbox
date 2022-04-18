import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Facility, VaccinationEvent, Tenant, Vaccine } from 'src/app/_model/rest';
import { Code, CodeBaseMap, CodeMap, Form, FormCard, formType, Reference } from 'src/app/_model/structure';
import { Breakpoints, BreakpointObserver } from '@angular/cdk/layout';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CodeMapsService } from 'src/app/_services/code-maps.service';
import { VaccinationService } from 'src/app/_services/vaccination.service';
import { KeyValue, KeyValuePipe } from '@angular/common';

@Component({
  selector: 'app-vaccination-form',
  templateUrl: './vaccination-form.component.html',
  styleUrls: ['./vaccination-form.component.css']
})
export class VaccinationFormComponent implements OnInit {

  @Input()
  vaccination: VaccinationEvent = {id: -1, vaccine: {}, enteringClinician: {}, administeringClinician: {}, orderingClinician: {}};
  @Output()
  vaccinationChange = new EventEmitter<VaccinationEvent>();

  private codeBaseMap!:  CodeBaseMap;
  private codeBaseMapKeys!:  string[];
  private codeBaseMapValues!:  CodeMap[];
  public references: {[key:string]: Reference} = {};

  filteredOptions: {[key:string]: KeyValue<string, Code>[]} = {};

  referencesChange(value: Reference, codeMapKey?: string ) {
    if (codeMapKey ){
      if (value) {
        this.references[codeMapKey] = value
      } else {
        delete this.references[codeMapKey]
      }
    }
  }

  constructor(private breakpointObserver: BreakpointObserver,
    private _snackBar: MatSnackBar,
    public codeMapsService: CodeMapsService,
    private vaccinationService: VaccinationService,
    ) {
      this.refreshCodeMaps()
    }

  ngOnInit(): void {
    // this.refreshCodeMaps()
  }

  refreshCodeMaps() {
    this.codeMapsService.getObservableCodeBaseMap().subscribe((codeBaseMap) => {
      this.codeBaseMap = codeBaseMap
      this.codeBaseMapKeys = Object.keys(codeBaseMap)
      this.codeBaseMapValues = Object.values(codeBaseMap)
    })
  }

  // Allows Date type casting in HTML template
  asDate(val: any) : Date { return val; }

  getCodes() {
    this.codeMapsService.readCodeMaps().subscribe((res) => {
      // console.log(res)
    })
  }

  fillRandom(): void {
    this.vaccinationService.readRandom().subscribe((res) => {
      this.vaccination=res
    })
  }

  formCards: FormCard[] = [
    {title: "Vaccine",rows: 1, cols: 1, vaccineForms: [
      {type: formType.date, title: "Administered", attribute: "administeredDate"},
      {type: formType.short, title: "Admininistered amount", attribute: "administeredAmount" },
    ]},
    {title: "Codes",rows: 1, cols: 2, vaccineForms: [
      {type: formType.select, title: "Cvx", attribute: "vaccineCvxCode", codeMapLabel: "VACCINATION_CVX_CODE", codeLabel: "Cvx"},
      {type: formType.select, title: "Ndc", attribute: "vaccineNdcCode", codeMapLabel: "VACCINATION_NDC_CODE_UNIT_OF_USE", codeLabel: "NDC"},
    ]},
    {title: "Request",rows: 1, cols: 1, vaccineForms: [
      {type: formType.select, title: "Information source", attribute: "informationSource", codeMapLabel: "VACCINATION_INFORMATION_SOURCE", codeLabel: ""},
      {type: formType.select, title: "Action code", attribute: "actionCode", codeMapLabel: "VACCINATION_ACTION_CODE", codeLabel: ""},
    ]},
    {title: "Lot",rows: 1, cols: 2, vaccineForms: [
      {type: formType.text, title: "Lot number", attribute: "lotNumber"},
      {type: formType.select, title: "Manifacturer", attribute: "vaccineMvxCode", codeMapLabel: "VACCINATION_MANUFACTURER_CODE", codeLabel: "Mvx"},
      {type: formType.date, title: "Expiration date", attribute: "expirationDate"},
    ]},
    {title: "Funding",rows: 1, cols: 1, vaccineForms: [
      {type: formType.select, title: "Source", attribute: "fundingSource", codeMapLabel: "VACCINATION_FUNDING_SOURCE", codeLabel: ""},
      {type: formType.text, title: "Eligibility", attribute: "fundingEligibility"},
    ]},
    {title: "Injection route",rows: 1, cols: 1, vaccineForms: [
      {type: formType.select, title: "Route", attribute: "bodyRoute", codeMapLabel: "BODY_ROUTE", codeLabel: ""},
      {type: formType.select, title: "Site", attribute: "bodySite", codeMapLabel: "BODY_SITE", codeLabel: ""},
    ]},
    {title: "Injection status",rows: 1, cols: 1, vaccineForms: [
      {type: formType.select, title: "Completion status", attribute: "completionStatus", codeMapLabel: "VACCINATION_COMPLETION", codeLabel: "code"},
      {type: formType.select, title: "Refusal reason", attribute: "refusalReasonCode", codeMapLabel: "VACCINATION_REFUSAL", codeLabel: "code"},
    ]},

    {title: "Entering clinician",rows: 1, cols: 1, clinicianForms: [
      {type: formType.text, title: "First name", attribute: "nameFirst", role: "enteringClinician"},
      {type: formType.text, title: "Middle name", attribute: "nameMiddle", role: "enteringClinician"},
      {type: formType.text, title: "Last name", attribute: "nameLast", role: "enteringClinician"}
    ]},
    {title: "Ordering clinician",rows: 1, cols: 1, clinicianForms: [
      {type: formType.text, title: "First name", attribute: "nameFirst", role: "orderingClinician"},
      {type: formType.text, title: "Middle name", attribute: "nameMiddle", role: "orderingClinician"},
      {type: formType.text, title: "Last name", attribute: "nameLast", role: "orderingClinician"}
    ]},
    {title: "Administering clin.",rows: 1, cols: 1, clinicianForms: [
      {type: formType.text, title: "First name", attribute: "nameFirst", role: "administeringClinician"},
      {type: formType.text, title: "Middle name", attribute: "nameMiddle", role: "administeringClinician"},
      {type: formType.text, title: "Last name", attribute: "nameLast", role: "administeringClinician"}
    ]},

  ]

}
