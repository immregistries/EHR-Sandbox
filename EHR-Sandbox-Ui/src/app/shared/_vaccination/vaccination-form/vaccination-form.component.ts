import { AfterViewInit, Component, EventEmitter, Inject, Input, OnDestroy, OnInit, Optional, Output, ViewChild } from '@angular/core';
import { VaccinationEvent } from 'src/app/core/_model/rest';
import { Code, CodeReference, CodeReferenceTable, CodeReferenceTableMember } from "src/app/core/_model/code-base-map";
import { CodeMapsService } from 'src/app/core/_services/code-maps.service';
import { VaccinationService } from 'src/app/core/_services/vaccination.service';
import { KeyValue } from '@angular/common';
import { BehaviorSubject, Subscription } from 'rxjs';
import { AbstractControl, FormGroup, NgForm, ValidationErrors, ValidatorFn } from '@angular/forms';
import { randexp } from 'randexp';
import { SnackBarService } from 'src/app/core/_services/snack-bar.service';
import { HttpResponse } from '@angular/common/http';
import { VaccinationComparePipe } from '../../_pipes/vaccination-compare.pipe';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import FormType, { ComparisonResult, FormCard } from 'src/app/core/_model/form-structure';

@Component({
  selector: 'app-vaccination-form',
  templateUrl: './vaccination-form.component.html',
  styleUrls: ['./vaccination-form.component.css']
})
export class VaccinationFormComponent implements OnInit, AfterViewInit, OnDestroy {
  form!: FormGroup;

  private _vaccinationId = -1
  private _vaccineId = -1
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
    vaccineComparePipe: VaccinationComparePipe,
    @Optional() public _dialogRef: MatDialogRef<VaccinationFormComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: { patientId: number, vaccination?: VaccinationEvent, comparedVaccination?: VaccinationEvent, changePrimarySourceToFalse?: Boolean }) {
    if (data) {
      this.patientId = data.patientId;
      if (data.vaccination) {
        this._vaccinationId = data.vaccination.id ?? -1
        this._vaccineId = data.vaccination.vaccine?.id ?? -1
        this.vaccination = data.vaccination
        this.isEditionMode = true
        if (data.comparedVaccination) {
          this.compareTo = vaccineComparePipe.transform(this.vaccination, data.comparedVaccination)
        }
        if (data.changePrimarySourceToFalse === true) {
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
    const formerUpdatedDate = this.vaccination.vaccine.updatedDate
    this.vaccination.id = this._vaccinationId
    this.vaccination.patient = this.patientId
    this.vaccination.vaccine.id = this._vaccineId
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

  public referenceTableObservable: BehaviorSubject<CodeReferenceTable> = new BehaviorSubject<CodeReferenceTable>({});
  @ViewChild('vaccinationForm', { static: true }) vaccinationForm!: NgForm;

  public filteredCodeMapsOptions: { [key: string]: KeyValue<string, Code>[] } = {};
  private formChangesSubscription!: Subscription;

  ngOnInit() {
    this.formChangesSubscription = this.vaccinationForm.form.valueChanges.subscribe(x => {
      this.referenceTableObservable.next(this.referenceTableObservable.getValue())
    })
  }

  ngAfterViewInit() {

  }

  ngOnDestroy() {
    this.formChangesSubscription.unsubscribe();
  }

  referenceTableChanges(emitted: CodeReferenceTableMember, codeMapKey: string | undefined): void {
    if (codeMapKey) {
      let newRefList: { [key: string]: CodeReferenceTableMember } = JSON.parse(JSON.stringify(this.referenceTableObservable.value))
      if (emitted) {
        newRefList[codeMapKey] = emitted
      } else {
        delete newRefList[codeMapKey]
      }
      this.referenceTableObservable.next(newRefList)
      this.updateLotHint();
    }
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


  private _lot_hint_value: string = '';
  lotNumberHint = (value?: string): string => {
    // this._produce_hint_value = false
    // for (const codeReferenceTableMember of Object.values(this.referenceTableObservable.getValue())) {
    //   const exampleValidTemplate: string | undefined = this.invalidatingLotNumberTemplates(codeReferenceTableMember.reference, '  ')
    //   if (exampleValidTemplate) {
    //     return 'example: ' + randexp(exampleValidTemplate)
    //   }
    // }
    // }
    return this._lot_hint_value;
  }

  updateLotHint() {
    for (const codeReferenceTableMember of Object.values(this.referenceTableObservable.getValue())) {
      const exampleValidTemplate: string | undefined = this.invalidatingLotNumberTemplates(codeReferenceTableMember.reference, ' ')
      if (exampleValidTemplate) {
        this._lot_hint_value = ' example: ' + randexp(exampleValidTemplate)
      }
    }
  }

  lotNumberValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
    if (!control.value || control.value.length == 0) {
      return null
    }
    for (const codeReferenceTableMember of Object.entries(this.referenceTableObservable.getValue())) {
      let exampleValidTemplate: string | undefined = this.invalidatingLotNumberTemplates(codeReferenceTableMember[1].reference, control.value)
      if (exampleValidTemplate) {
        return { customIssue: codeReferenceTableMember[1].value + ' template: ' + exampleValidTemplate + this._lot_hint_value }
      }
    }
    return null;
  };


  /**
   * returns a valid template example if no templates are valid
   * @param reference
   * @returns
   */
  invalidatingLotNumberTemplates(reference: CodeReference, lotNumber: string | undefined): string | undefined {
    let latestScannedTemplate = undefined
    if (!lotNumber) {
      return undefined;
    }
    for (const ref of reference.linkTo) {
      if (ref.codeset == "VACCINATION_LOT_NUMBER_PATTERN") {
        latestScannedTemplate = ref.value
        if (new RegExp(ref.value).test(lotNumber)) {
          // Valid lotNumber
          return undefined
        }
      }
    }
    return latestScannedTemplate
  }


  readonly VACCINATION_FORM_CARDS: FormCard[] = [
    {
      title: "Vaccine", rows: 1, cols: 1, vaccineForms: [
        { type: FormType.date, title: "Administered", attributeName: "administeredDate", required: true },
        { type: FormType.text, title: "Amount Admininistered (mL)", attributeName: "administeredAmount" },
      ], vaccinationForms: [
        {
          type: FormType.select, title: "Record Nature", attributeName: "primarySource", options: [{ code: true, display: 'New Administration' }, { code: false, display: 'Historical' },]
          // , tooltip: 'Wether the '
        },
      ]
    },
    {
      title: "Codes", rows: 1, cols: 2, vaccineForms: [
        { type: FormType.code, title: "Vaccine type (CVX)", attributeName: "vaccineCvxCode", codeMapLabel: "VACCINATION_CVX_CODE", required: true },
        { type: FormType.code, title: "Unit of Use (NDC)", attributeName: "vaccineNdcCode", codeMapLabel: "VACCINATION_NDC_CODE_UNIT_OF_USE" },
      ]
    },
    {
      title: "Request", rows: 1, cols: 2, vaccineForms: [
        { type: FormType.code, title: "Information source", attributeName: "informationSource", codeMapLabel: "VACCINATION_INFORMATION_SOURCE", required: true },
        { type: FormType.code, title: "Action code", attributeName: "actionCode", codeMapLabel: "VACCINATION_ACTION_CODE" },
      ]
    },
    {
      title: "Lot", rows: 1, cols: 2, vaccineForms: [
        { type: FormType.code, title: "Manifacturer (MVX)", attributeName: "vaccineMvxCode", codeMapLabel: "VACCINATION_MANUFACTURER_CODE" },
        { type: FormType.text, title: "Lot number", attributeName: "lotNumber", codeMapLabel: "VACCINATION_LOT_NUMBER_PATTERN", customValidator: this.lotNumberValidator, hintProducer: this.lotNumberHint },
        { type: FormType.date, title: "Expiration date", attributeName: "expirationDate" },
      ]
    },
    {
      title: "Funding", rows: 1, cols: 1, vaccineForms: [
        { type: FormType.code, title: "Source", attributeName: "fundingSource", codeMapLabel: "VACCINATION_FUNDING_SOURCE" },
        { type: FormType.code, title: "Financial status (Dose level accountability)", attributeName: "financialStatus", codeMapLabel: "FINANCIAL_STATUS_CODE" },
      ]
    },
    {
      title: "Information Statement (VIS)", rows: 1, cols: 1, vaccineForms: [
        { type: FormType.code, title: "Information Statement Document", attributeName: "informationStatement", codeMapLabel: "VACCINATION_VIS_DOC_TYPE" },
        { type: FormType.date, title: "Presented date", attributeName: "informationStatementPresentedDate" },
        { type: FormType.code, title: "Information Statement Cvx", attributeName: "informationStatementCvx", codeMapLabel: "VACCINATION_VIS_CVX_CODE" },
        { type: FormType.date, title: "Published date", attributeName: "informationStatementPublishedDate" },
      ], toolTips: "Preferred method for VXU reporting includes Document type and Presented Date, supporting deprecated method with CVX alongside Published and Presented Date"
    },
    {
      title: "Injection route", rows: 1, cols: 1, vaccineForms: [
        { type: FormType.code, title: "Route", attributeName: "bodyRoute", codeMapLabel: "BODY_ROUTE" },
        { type: FormType.code, title: "Site", attributeName: "bodySite", codeMapLabel: "BODY_SITE" },
      ]
    },
    {
      title: "Injection status", rows: 1, cols: 1, vaccineForms: [
        { type: FormType.code, title: "Completion status", attributeName: "completionStatus", codeMapLabel: "VACCINATION_COMPLETION" },
        { type: FormType.code, title: "Refusal reason", attributeName: "refusalReasonCode", codeMapLabel: "VACCINATION_REFUSAL" },
      ]
    },
    {
      title: "Clinicians", rows: 1, cols: 1, vaccinationForms: [
        { type: FormType.clinician, title: "Entering", attributeName: "enteringClinician" },
        { type: FormType.clinician, title: "Ordering", attributeName: "orderingClinician" },
        { type: FormType.clinician, title: "Administering", attributeName: "administeringClinician" }
      ]
    },
    {
      title: "Update dates", rows: 1, cols: 1, vaccineForms: [
        { type: FormType.date, title: "Creation date", attributeName: "createdDate", disabled: true },
        { type: FormType.date, title: "Updated date", attributeName: "updatedDate", disabled: true },
      ]
    },
  ]




}
