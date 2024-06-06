import { AfterViewInit, Component, EventEmitter, Inject, Input, OnDestroy, OnInit, Optional, Output, ViewChild } from '@angular/core';
import { VaccinationEvent } from 'src/app/core/_model/rest';
import FormType, { ComparisonResult, FormCard } from 'src/app/core/_model/structure';
import { Code, CodeReference } from "src/app/core/_model/code-base-map";
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
export class VaccinationFormComponent implements OnInit, AfterViewInit, OnDestroy {

  private _vaccination: VaccinationEvent = { id: -1, vaccine: { updatedDate: new Date() }, enteringClinician: {}, administeringClinician: {}, orderingClinician: {} };
  @Input()
  public set vaccination(v: VaccinationEvent) {
    this._vaccination = v;
    if (this._vaccination) {
      if (!this._vaccination.vaccine) {
        this._vaccination.vaccine = { updatedDate: new Date() };
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

  @Input()
  public patientId: number = -1
  public isEditionMode: boolean = false
  public compareTo: ComparisonResult | any | null = null
  @Output()
  savedEmitter = new EventEmitter<VaccinationEvent | number | string>();

  constructor(public codeMapsService: CodeMapsService,
    private snackBarService: SnackBarService,
    private vaccinationService: VaccinationService,
    private vaccineComparePipe: VaccinationComparePipe,
    @Optional() public _dialogRef: MatDialogRef<VaccinationFormComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: { patientId: number, vaccination?: VaccinationEvent, comparedVaccination?: VaccinationEvent, changePrimarySourceToFalse?: Boolean }) {
    if (data) {
      this.patientId = data.patientId;
      if (data.vaccination) {
        this.vaccination = data.vaccination
        this.isEditionMode = true
        if (data.comparedVaccination) {
          this.compareTo = vaccineComparePipe.transform(this.vaccination, data.comparedVaccination)
        }
        if (data.changePrimarySourceToFalse) {
          this.vaccination.primarySource = false;
        }
      }
    }
  }

  fillRandom(): void {
    this.vaccinationService.readRandom(this.patientId).subscribe((res) => {
      this.vaccination = res
    })
  }

  save(): void {
    let formerUpdatedDate = this.vaccination.vaccine.updatedDate
    if (this.isEditionMode == true) {
      // TODO PUT implementation
      this.vaccination.vaccine.updatedDate = new Date()
      this.vaccinationService.quickPutVaccination(this.patientId, this.vaccination).subscribe({
        next: (res: VaccinationEvent) => {
          this.closeDialog(res)
        },
        error: (err) => {
          this.vaccination.vaccine.updatedDate = formerUpdatedDate
          console.log(err.error)
          this.snackBarService.errorMessage(err.error.error)
        }
      });
    } else {
      this.vaccination.vaccine.updatedDate = new Date()
      this.vaccinationService.quickPostVaccination(this.patientId, this.vaccination).subscribe({
        next: (res: HttpResponse<string>) => {
          this.closeDialog(+(res.body ?? -1))
        },
        error: (err) => {
          this.vaccination.vaccine.updatedDate = formerUpdatedDate
          console.log(err.error)
          this.snackBarService.errorMessage(err.error.error)
        }
      });
    }
  }

  closeDialog(res: VaccinationEvent | number | string) {
    this.savedEmitter.emit(res)
    if (this._dialogRef?.close) {
      this._dialogRef.close(res);
    }
  }

  public references: BehaviorSubject<{ [key: string]: { reference: CodeReference, value: string } }> = new BehaviorSubject<{ [key: string]: { reference: CodeReference, value: string } }>({});
  @ViewChild('vaccinationForm', { static: true }) vaccinationForm!: NgForm;

  public filteredCodeMapsOptions: { [key: string]: KeyValue<string, Code>[] } = {};
  private formChangesSubscription!: Subscription;

  ngOnInit() {
    this.formChangesSubscription = this.vaccinationForm.form.valueChanges.subscribe(x => {
      this.references.next(this.references.getValue())
    })
  }

  ngAfterViewInit() {
    setTimeout(() => {
      if (this.vaccinationForm.form.controls['lotNumber']) {
        this.vaccinationForm.form.controls['lotNumber'].valueChanges.subscribe((lotNumber) => {
          this.lotNumberValid = true
          if (lotNumber) {
            for (const ref in this.references.getValue()) {
              // stops checking as soon as validity is assessed
              if (!this.checkLotNumberValidity(this.references.getValue()[ref].reference)) {
                break;
              }
            }
          }
        })
      }
    }, 0);
  }

  ngOnDestroy() {
    this.formChangesSubscription.unsubscribe();
  }

  referencesChange(emitted: { reference: CodeReference, value: string }, codeMapKey?: string): void {
    if (codeMapKey) {
      let newRefList: { [key: string]: { reference: CodeReference, value: string } } = JSON.parse(JSON.stringify(this.references.value))
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
      if (ref.codeset == "VACCINATION_LOT_NUMBER_PATTERN") {
        scanned = true
        if (!this.vaccination.vaccine.lotNumber || this.vaccination.vaccine.lotNumber.length == 0) {
          this.vaccination.vaccine.lotNumber = randexp(ref.value)
          regexFit = true
          break;
        } else if (new RegExp(ref.value).test(this.vaccination.vaccine.lotNumber)) {
          regexFit = true
          break;
        }
      }
    }
    if (scanned && !regexFit) {
      this.lotNumberValid = false
    }
    return this.lotNumberValid
  }

  allFieldsRequired(): boolean {
    // if Not historical, all fields are required
    /**
     * TODO investigate behaviour with string
     */
    //@ts-ignore
    if (this.vaccination.primarySource === true || this.vaccination.primarySource === 'true') {
      return true
    } else {
      return false
    }
  }

  readonly VACCINATION_FORM_CARDS: FormCard[] = [
    {
      title: "Vaccine", rows: 1, cols: 1, vaccineForms: [
        { type: FormType.date, title: "Administered", attribute: "administeredDate", required: true },
        { type: FormType.text, title: "Amount Admininistered (mL)", attribute: "administeredAmount" },
      ], vaccinationForms: [
        // { type: FormType.boolean, title: "Primary Source", attribute: "primarySource" },
        {
          type: FormType.select, title: "Record Nature", attribute: "primarySource", options: [{ value: true, label: 'New Administration' }, { value: false, label: 'Historical' },]
          // , tooltip: 'Wether the '
        },
      ]
    },
    {
      title: "Codes", rows: 1, cols: 2, vaccineForms: [
        { type: FormType.code, title: "Vaccine type (Cvx)", attribute: "vaccineCvxCode", codeMapLabel: "VACCINATION_CVX_CODE", required: true },
        { type: FormType.code, title: "Ndc", attribute: "vaccineNdcCode", codeMapLabel: "VACCINATION_NDC_CODE_UNIT_OF_USE" },
      ]
    },
    {
      title: "Request", rows: 1, cols: 2, vaccineForms: [
        { type: FormType.code, title: "Information source", attribute: "informationSource", codeMapLabel: "VACCINATION_INFORMATION_SOURCE", required: true },
        { type: FormType.code, title: "Action code", attribute: "actionCode", codeMapLabel: "VACCINATION_ACTION_CODE" },
      ]
    },
    {
      title: "Lot", rows: 1, cols: 2, vaccineForms: [
        { type: FormType.code, title: "Manifacturer (Mvx)", attribute: "vaccineMvxCode", codeMapLabel: "VACCINATION_MANUFACTURER_CODE" },
        { type: FormType.text, title: "Lot number", attribute: "lotNumber", codeMapLabel: "VACCINATION_LOT_NUMBER_PATTERN" },
        { type: FormType.date, title: "Expiration date", attribute: "expirationDate" },
      ]
    },
    {
      title: "Funding", rows: 1, cols: 1, vaccineForms: [
        { type: FormType.code, title: "Source", attribute: "fundingSource", codeMapLabel: "VACCINATION_FUNDING_SOURCE" },
        { type: FormType.select, title: "Eligibility", attribute: "fundingEligibility", options: [{ value: 'Y', label: 'Y' }, { value: 'N', label: 'N' }] },
      ]
    },
    {
      title: "Injection route", rows: 1, cols: 1, vaccineForms: [
        { type: FormType.code, title: "Route", attribute: "bodyRoute", codeMapLabel: "BODY_ROUTE" },
        { type: FormType.code, title: "Site", attribute: "bodySite", codeMapLabel: "BODY_SITE" },
      ]
    },
    {
      title: "Injection status", rows: 1, cols: 1, vaccineForms: [
        { type: FormType.code, title: "Completion status", attribute: "completionStatus", codeMapLabel: "VACCINATION_COMPLETION" },
        { type: FormType.code, title: "Refusal reason", attribute: "refusalReasonCode", codeMapLabel: "VACCINATION_REFUSAL" },
      ]
    },
    {
      title: "Clinicians", rows: 1, cols: 1, vaccinationForms: [
        { type: FormType.clinician, title: "Entering", attribute: "enteringClinician" },
        { type: FormType.clinician, title: "Ordering", attribute: "orderingClinician" },
        { type: FormType.clinician, title: "Administering", attribute: "administeringClinician" }
      ]
    },
    {
      title: "Update dates", rows: 1, cols: 1, vaccineForms: [
        { type: FormType.date, title: "Creation date", attribute: "createdDate", disabled: true },
        { type: FormType.date, title: "Updated date", attribute: "updatedDate", disabled: true },
      ]
    },
  ]

}
