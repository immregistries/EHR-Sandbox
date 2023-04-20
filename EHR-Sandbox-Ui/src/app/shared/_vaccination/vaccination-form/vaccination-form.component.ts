import { AfterViewInit, Component, EventEmitter, Inject, Input, OnDestroy, OnInit, Optional, Output, ViewChild } from '@angular/core';
import { VaccinationEvent } from 'src/app/core/_model/rest';
import { Code, CodeBaseMap, CodeMap, ComparisonResult, BaseForm, FormCard, formType, CodeReference} from 'src/app/core/_model/structure';
import { CodeMapsService } from 'src/app/core/_services/code-maps.service';
import { VaccinationService } from 'src/app/core/_services/vaccination.service';
import { KeyValue } from '@angular/common';
import { BehaviorSubject, Subscription } from 'rxjs';
import { NgForm } from '@angular/forms';
import { randexp } from 'randexp';
import { SnackBarService } from 'src/app/core/_services/snack-bar.service';
import { HttpResponse } from '@angular/common/http';
import { VaccinationComparePipe } from '../../_pipes/vaccination-compare.pipe';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-vaccination-form',
  templateUrl: './vaccination-form.component.html',
  styleUrls: ['./vaccination-form.component.css']
})
export class VaccinationFormComponent implements OnInit, AfterViewInit, OnDestroy{

  private _vaccination: VaccinationEvent = {id: -1, vaccine: {}, enteringClinician: {}, administeringClinician: {}, orderingClinician: {}};
  @Input()
  public set vaccination(v: VaccinationEvent) {
    this._vaccination = v;
    if (this._vaccination) {
      if (!this._vaccination.vaccine) {
        this._vaccination.vaccine = {};
      }
      if (!this._vaccination.enteringClinician) {
        this._vaccination.enteringClinician = {};
      }
      if (!this._vaccination.administeringClinician) {
        this._vaccination.administeringClinician = {};
      }
      if (!this._vaccination.orderingClinician) {
        this._vaccination.orderingClinician = {};
      }
    }
  }
  public get vaccination(): VaccinationEvent {
    return this._vaccination
  }

  @Output() vaccinationChange = new EventEmitter<VaccinationEvent>();

  public patientId: number = -1
  public isEditionMode: boolean = false
  public compareTo: ComparisonResult | any | null = null

  constructor(public codeMapsService: CodeMapsService,
    private snackBarService: SnackBarService,
    private vaccinationService: VaccinationService,
    private vaccineComparePipe: VaccinationComparePipe,
    @Optional() public _dialogRef: MatDialogRef<VaccinationFormComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: {patientId: number, vaccination?: VaccinationEvent, comparedVaccination?: VaccinationEvent}) {
      if (data) {
        this.patientId = data.patientId;
        if (data.vaccination){
          this.vaccination = data.vaccination
          this.isEditionMode = true
          if (data.comparedVaccination) {
            this.compareTo = vaccineComparePipe.transform(this.vaccination, data.comparedVaccination)
          }
        }
      }
  }

  fillRandom(): void {
    this.vaccinationService.readRandom().subscribe((res) => {
      this.vaccination=res
    })
  }

  save(): void {
    if (this.isEditionMode == true){
      // TODO PUT implementation
      this.vaccinationService.quickPutVaccination( this.patientId, this.vaccination).subscribe({
        next: (res: VaccinationEvent) => {
          this._dialogRef?.close(true)
        },
        error: (err) => {
          console.log(err.error)
          this.snackBarService.errorMessage(err.error.error)
        }
      });
    } else {
      this.vaccinationService.quickPostVaccination( this.patientId, this.vaccination).subscribe({
        next: (res: HttpResponse<string>) => {
          this._dialogRef?.close(true)
        },
        error: (err) => {
          console.log(err.error)
          this.snackBarService.errorMessage(err.error.error)
        }
      });
    }

  }

  public references: BehaviorSubject<{[key:string]: {reference: CodeReference, value: string}}> = new BehaviorSubject<{[key:string]: {reference: CodeReference, value: string}}>({});
  @ViewChild('vaccinationForm', { static: true }) vaccinationForm!: NgForm;

  public filteredOptions: {[key:string]: KeyValue<string, Code>[]} = {};
  private formChangesSubscription!: Subscription;

  ngOnInit() {
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

  referencesChange(emitted: {reference: CodeReference, value: string}, codeMapKey?: string): void {
    if (codeMapKey ){
      let newRefList: {[key:string]: {reference: CodeReference, value: string}} = JSON.parse(JSON.stringify(this.references.value))
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
  checkLotNumberValidity(reference: CodeReference): boolean {
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


  formCards: FormCard[] = [
    {title: "Vaccine",rows: 1, cols: 1, vaccineForms: [
      {type: formType.date, title: "Administered", attribute: "administeredDate"},
      {type: formType.short, title: "Admininistered amount", attribute: "administeredAmount" },
    ], vaccinationForms: [
      {type: formType.boolean, title: "Primary Source", attribute: "primarySource"},
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
