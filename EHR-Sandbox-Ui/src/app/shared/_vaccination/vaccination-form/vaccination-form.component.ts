import { AfterViewInit, Component, EventEmitter, Inject, Input, OnDestroy, OnInit, Optional, Output, ViewChild } from '@angular/core';
import { VaccinationEvent } from 'src/app/core/_model/rest';
import { Code, CodeReference, CodeReferenceTable, CodeReferenceTableMember } from "src/app/core/_model/code-base-map";
import { CodeMapsService } from 'src/app/core/_services/code-maps.service';
import { VaccinationService } from 'src/app/core/_services/vaccination.service';
import { KeyValue } from '@angular/common';
import { BehaviorSubject, catchError, firstValueFrom, map, Observable, of, Subscription } from 'rxjs';
import { AbstractControl, AsyncValidatorFn, FormGroup, NgForm, ValidationErrors, ValidatorFn } from '@angular/forms';
import { randexp } from 'randexp';
import { SnackBarService } from 'src/app/core/_services/snack-bar.service';
import { HttpResponse } from '@angular/common/http';
import { VaccinationComparePipe } from '../../_pipes/vaccination-compare.pipe';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import FormType, { ComparisonResult, FormCard } from 'src/app/core/_model/form-structure';
import { TenantService } from 'src/app/core/_services/tenant.service';

@Component({
  selector: 'app-vaccination-form',
  templateUrl: './vaccination-form.component.html',
  styleUrls: ['./vaccination-form.component.css']
})
export class VaccinationFormComponent implements OnInit, AfterViewInit, OnDestroy {
  form!: FormGroup;

  private _vaccinationId = -1
  private _vaccineId = -1
  private _vaccination: VaccinationEvent = { id: -1, vaccine: { updatedDate: new Date() } };
  @Input()
  public set vaccination(v: VaccinationEvent) {
    this._vaccination = v;
    if (this._vaccination) {
      if (!this._vaccination.vaccine) {
        this._vaccination.vaccine = { updatedDate: new Date() };
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
    private tenantService: TenantService,
    private vaccinationService: VaccinationService,
    vaccineComparePipe: VaccinationComparePipe,
    @Optional() public _dialogRef: MatDialogRef<VaccinationFormComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: { patientId: number, vaccination?: VaccinationEvent, comparedVaccination?: VaccinationEvent }) {
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
    // this.vaccination.patient = undefined
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
          this.snackBarService.errorMessage(err.error.error ?? err.error)
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
          this.snackBarService.errorMessage(err.error.error ?? err.error)
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

  lotNumberValidatorAsync: AsyncValidatorFn = (control: AbstractControl): Promise<ValidationErrors | null> | Observable<ValidationErrors | null> => {
    if (!control.value || control.value.length == 0) {
      return firstValueFrom(of(null))
    }
    if (this.tenantService.getCurrent().nameDisplay?.includes('LOTTERY')) {
      if (!this.vaccination.vaccine.vaccineCvxCode || !this.vaccination.vaccine.vaccineMvxCode) {
        return of({ customIssue: "CVX & MVX Required to verify" })
      } else {
        return firstValueFrom(
          this.vaccinationService.lotNumberValidation(control.value, this.vaccination.vaccine.vaccineCvxCode, this.vaccination.vaccine.vaccineMvxCode)
            .pipe(map((res) => {
              if (res.status == 200) {
                return null
              }
              else if (res.status == 202) { // TODO find better distinction for when result needs to be printed
                return { customIssue: res.body }
              }
              return null
            }), catchError((err, caught) => {
              // return of({ customIssue: "Service Unreachable" }) TODO currently dealt with by API
              return of({ customIssue: "Error in verification Query" })
            })))
      }
    } else {
      of(this.lotNumberValidator(control))
    }
    return of(null)
  }

  private lotNumberValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
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
        latestScannedTemplate = ref.value.replace("\\", "\\\\")
        latestScannedTemplate = latestScannedTemplate.replace("\\\\", "\\")
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
      title: "Vaccine", vaccineForms: [
        {
          type: FormType.date, title: "Administered", attributeName: "administeredDate", required: true,
          hl7Location: {
            segmentId: "RXA",
            componentNumber: 3
          }
        },
        {
          type: FormType.text, title: "Amount Admininistered (mL)", attributeName: "administeredAmount",
          hl7Location: {
            segmentId: "RXA",
            componentNumber: 6
          }
        },
      ], vaccinationForms: [
        {
          type: FormType.select, title: "Record Nature", attributeName: "primarySource",
          options: [
            { code: true, display: 'New Administration' },
            { code: false, display: 'Historical' },
          ],
          // hl7Location: {
          //   segmentId: "",
          //   componentNumber:
          // }
        },
      ]
    },
    {
      title: "Codes", vaccineForms: [
        {
          type: FormType.code, title: "Vaccine type (CVX)", attributeName: "vaccineCvxCode", codeMapLabel: "VACCINATION_CVX_CODE", required: true,
          hl7Location: {
            segmentId: "RXA",
            componentNumber: 5
          }
        },
        {
          type: FormType.code, title: "Unit of Use (NDC)", attributeName: "vaccineNdcCode", codeMapLabel: "VACCINATION_NDC_CODE_UNIT_OF_USE",
          hl7Location: {
            segmentId: "RXA",
            componentNumber: 5
          }
        },
      ]
    },
    {
      title: "Request", vaccineForms: [
        {
          type: FormType.code, title: "Information source", attributeName: "informationSource", codeMapLabel: "VACCINATION_INFORMATION_SOURCE", required: true,
          hl7Location: {
            segmentId: "RXA",
            componentNumber: 9
          }
        },
        {
          type: FormType.code, title: "Action code", attributeName: "actionCode", codeMapLabel: "VACCINATION_ACTION_CODE",
          hl7Location: {
            segmentId: "RXA",
            componentNumber: 21
          }
        },
      ]
    },
    {
      title: "Lot", vaccineForms: [
        {
          type: FormType.code, title: "Manifacturer (MVX)", attributeName: "vaccineMvxCode", codeMapLabel: "VACCINATION_MANUFACTURER_CODE",
          hl7Location: {
            segmentId: "RXA",
            componentNumber: 17
          }
        },
        {
          type: FormType.text, title: "Lot number", attributeName: "lotNumber", codeMapLabel: "VACCINATION_LOT_NUMBER_PATTERN",
          customValidatorAsync: this.lotNumberValidatorAsync,
          hintProducer: this.lotNumberHint,
          hl7Location: {
            segmentId: "RXA",
            componentNumber: 15
          }
        },
        {
          type: FormType.date, title: "Expiration date", attributeName: "expirationDate",
          hl7Location: {
            segmentId: "RXA",
            componentNumber: 16
          }
        },
      ]
    },
    {
      title: "Funding", vaccineForms: [
        {
          type: FormType.code, title: "Source", attributeName: "fundingSource", codeMapLabel: "VACCINATION_FUNDING_SOURCE",
          hl7Location: {
            segmentId: "OBX",
            componentNumber: 5
          }
        },
        {
          type: FormType.code, title: "Financial status (Dose level accountability)", attributeName: "financialStatus", codeMapLabel: "FINANCIAL_STATUS_CODE",
          hl7Location: {
            segmentId: "OBX",
            componentNumber: 5
          }
        },
      ]
    },
    {
      title: "Information Statement (VIS)", vaccineForms: [
        {
          type: FormType.code, title: "Information Statement Document", attributeName: "informationStatement", codeMapLabel: "VACCINATION_VIS_DOC_TYPE",
          hl7Location: {
            segmentId: "OBX",
            componentNumber: 5
          }
        },
        {
          type: FormType.date, title: "Presented date", attributeName: "informationStatementPresentedDate",
          hl7Location: {
            segmentId: "OBX",
            componentNumber: 5
          }
        },
        {
          type: FormType.code, title: "Information Statement Cvx", attributeName: "informationStatementCvx", codeMapLabel: "VACCINATION_VIS_CVX_CODE",
          hl7Location: {
            segmentId: "OBX",
            componentNumber: 5
          }
        },
        {
          type: FormType.date, title: "Published date", attributeName: "informationStatementPublishedDate",
          hl7Location: {
            segmentId: "OBX",
            componentNumber: 5
          }
        },
      ], toolTips: "Preferred method for VXU reporting includes Document type and Presented Date, supporting deprecated method with CVX alongside Published and Presented Date"
    },
    {
      title: "Injection route", vaccineForms: [
        {
          type: FormType.code, title: "Route", attributeName: "bodyRoute", codeMapLabel: "BODY_ROUTE",
          hl7Location: {
            segmentId: "RXR",
            componentNumber: 1
          }
        },
        {
          type: FormType.code, title: "Site", attributeName: "bodySite", codeMapLabel: "BODY_SITE",
          hl7Location: {
            segmentId: "RXR",
            componentNumber: 2
          }
        },
      ]
    },
    {
      title: "Injection status", vaccineForms: [
        {
          type: FormType.code, title: "Completion status", attributeName: "completionStatus", codeMapLabel: "VACCINATION_COMPLETION",
          hl7Location: {
            segmentId: "RXA",
            componentNumber: 20
          }
        },
        {
          type: FormType.code, title: "Refusal reason", attributeName: "refusalReasonCode", codeMapLabel: "VACCINATION_REFUSAL",
          hl7Location: {
            segmentId: "RXA",
            componentNumber: 18
          }
        },
      ]
    },
    {
      title: "Clinicians", vaccinationForms: [
        {
          type: FormType.clinician, title: "Entering", attributeName: "enteringClinician",
          hl7Location: {
            segmentId: "ORC",
            componentNumber: 10
          }
        },
        {
          type: FormType.clinician, title: "Ordering", attributeName: "orderingClinician",
          hl7Location: {
            segmentId: "ORC",
            componentNumber: 12
          }
        },
        {
          type: FormType.clinician, title: "Administering", attributeName: "administeringClinician",
          hl7Location: {
            segmentId: "RXA",
            componentNumber: 10
          }
        }
      ]
    },
    {
      title: "Update dates", vaccineForms: [
        { type: FormType.date, title: "Creation date", attributeName: "createdDate", disabled: true },
        { type: FormType.date, title: "Updated date", attributeName: "updatedDate", disabled: true },
      ]
    },
  ]




}
