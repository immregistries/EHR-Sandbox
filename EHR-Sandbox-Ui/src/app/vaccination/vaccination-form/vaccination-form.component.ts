import { AfterViewInit, Component, EventEmitter, Input, OnDestroy, OnInit, Output, ViewChild } from '@angular/core';
import { VaccinationEvent } from 'src/app/core/_model/rest';
import { Code, CodeBaseMap, CodeMap, Form, FormCard, formType, Reference} from 'src/app/core/_model/structure';
import { Breakpoints, BreakpointObserver } from '@angular/cdk/layout';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CodeMapsService } from 'src/app/core/_services/code-maps.service';
import { VaccinationService } from 'src/app/core/_services/vaccination.service';
import { KeyValue } from '@angular/common';
import { BehaviorSubject, Subscription } from 'rxjs';
import { NgForm, Validators } from '@angular/forms';
import { randexp } from 'randexp';

@Component({
  selector: 'app-vaccination-form',
  templateUrl: './vaccination-form.component.html',
  styleUrls: ['./vaccination-form.component.css']
})
export class VaccinationFormComponent implements OnInit, AfterViewInit, OnDestroy{

  @Input() vaccination: VaccinationEvent = {id: -1, vaccine: {}, enteringClinician: {}, administeringClinician: {}, orderingClinician: {}};
  @Output() vaccinationChange = new EventEmitter<VaccinationEvent>();

  private codeBaseMap!:  CodeBaseMap;
  private codeBaseMapKeys!:  string[];
  private codeBaseMapValues!:  CodeMap[];

  public references: BehaviorSubject<{[key:string]: {reference: Reference, value: string}}> = new BehaviorSubject<{[key:string]: {reference: Reference, value: string}}>({});
  @ViewChild('vaccinationForm', { static: true }) vaccinationForm!: NgForm;

  public filteredOptions: {[key:string]: KeyValue<string, Code>[]} = {};

  constructor(public codeMapsService: CodeMapsService,
    private vaccinationService: VaccinationService) {}

  private formChangesSubscription!: Subscription;
  ngOnInit() {
    if (this.vaccination) {
      this.vaccination.vaccine ? null : this.vaccination.vaccine = {};
      this.vaccination.enteringClinician ? null : this.vaccination.enteringClinician = {};
      this.vaccination.administeringClinician ? null : this.vaccination.administeringClinician = {};
      this.vaccination.orderingClinician ? null : this.vaccination.orderingClinician = {};
    }
    this.codeMapsService.getObservableCodeBaseMap().subscribe((codeBaseMap) => {
      this.codeBaseMap = codeBaseMap
      this.codeBaseMapKeys = Object.keys(codeBaseMap)
      this.codeBaseMapValues = Object.values(codeBaseMap)
    })
    this.formChangesSubscription = this.vaccinationForm.form.valueChanges.subscribe(x => {
      // this.references.next({})
      this.references.next(this.references.getValue())
    })
  }
  ngAfterViewInit() {
    setTimeout(() => {
      this.vaccinationForm.form.controls['lotNumber'].valueChanges.subscribe((lotNumber) => {
        this.lotNumberValid = true
        if (lotNumber) {
          for (const ref  in this.references.getValue()) {
            // stops checking as soon as validity is assessed
            if (!this.checkLotNumberValidity(this.references.getValue()[ref].reference)) {
              break;
            }
          }
        }
      })
    }, 0);
  }


  ngOnDestroy() {
    this.formChangesSubscription.unsubscribe();
  }

  referencesChange(emitted: {reference: Reference, value: string}, codeMapKey?: string): void {
    if (codeMapKey ){
      let newRefList: {[key:string]: {reference: Reference, value: string}} = JSON.parse(JSON.stringify(this.references.value))
      if (emitted) {
        newRefList[codeMapKey] = emitted
        this.checkLotNumberValidity(emitted.reference)
      } else {
        delete newRefList[codeMapKey]
      }
      this.references.next(newRefList)
    }
  }

  public lotNumberValid: boolean = true
  checkLotNumberValidity(reference: Reference): boolean {
    let scanned = false
    let regexFit = false
    for (const ref of reference.linkTo) {
      if (ref.codeset == "VACCINATION_LOT_NUMBER_PATTERN"){
        scanned = true
        if (!this.vaccination.vaccine.lotNumber || this.vaccination.vaccine.lotNumber.length == 0 ) {
          this.vaccination.vaccine.lotNumber = randexp(ref.value)
          regexFit = true
          break;
        } else if (new RegExp(ref.value).test(this.vaccination.vaccine.lotNumber)) {
          regexFit = true
          break;
        }
      }
    }
    if (scanned && !regexFit){
      this.lotNumberValid = false
    }
    return this.lotNumberValid
  }

  /**
   * Allows Date type casting in HTML template
   * @param val
   * @returns date type value
   */
  asDate(val: any) : Date { return val; }
  /**
   * Allows String type casting in HTML template
   * @param val
   * @returns String type value
   */
  asString(val: any) : string { return val; }

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
      {type: formType.code, title: "Vaccine type (Cvx)", attribute: "vaccineCvxCode", codeMapLabel: "VACCINATION_CVX_CODE"},
      {type: formType.code, title: "Ndc", attribute: "vaccineNdcCode", codeMapLabel: "VACCINATION_NDC_CODE_UNIT_OF_USE"},
    ]},
    {title: "Request",rows: 1, cols: 2, vaccineForms: [
      {type: formType.code, title: "Information source", attribute: "informationSource", codeMapLabel: "VACCINATION_INFORMATION_SOURCE"},
      {type: formType.code, title: "Action code", attribute: "actionCode", codeMapLabel: "VACCINATION_ACTION_CODE"},
    ]},
    {title: "Lot",rows: 1, cols: 2, vaccineForms: [
      {type: formType.code, title: "Manifacturer (Mvx)", attribute: "vaccineMvxCode", codeMapLabel: "VACCINATION_MANUFACTURER_CODE"},
      {type: formType.text, title: "Lot number", attribute: "lotNumber", codeMapLabel: "VACCINATION_LOT_NUMBER_PATTERN"},
      {type: formType.date, title: "Expiration date", attribute: "expirationDate"},
    ]},
    {title: "Funding",rows: 1, cols: 1, vaccineForms: [
      {type: formType.code, title: "Source", attribute: "fundingSource", codeMapLabel: "VACCINATION_FUNDING_SOURCE"},
      {type: formType.select, title: "Eligibility", attribute: "fundingEligibility", options: [{value: 'Y', label: 'Y'},{value: 'N', label: 'N'}]},
    ]},
    {title: "Injection route",rows: 1, cols: 1, vaccineForms: [
      {type: formType.code, title: "Route", attribute: "bodyRoute", codeMapLabel: "BODY_ROUTE"},
      {type: formType.code, title: "Site", attribute: "bodySite", codeMapLabel: "BODY_SITE"},
    ]},
    {title: "Injection status",rows: 1, cols: 1, vaccineForms: [
      {type: formType.code, title: "Completion status", attribute: "completionStatus", codeMapLabel: "VACCINATION_COMPLETION"},
      {type: formType.code, title: "Refusal reason", attribute: "refusalReasonCode", codeMapLabel: "VACCINATION_REFUSAL"},
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
