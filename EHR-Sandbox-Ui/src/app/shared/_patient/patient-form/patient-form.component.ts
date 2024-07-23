import { Component, EventEmitter, Inject, Input, OnInit, Optional, Output } from '@angular/core';
import { EhrPatient } from 'src/app/core/_model/rest';
import { CodeReferenceTable } from "src/app/core/_model/code-base-map";
import { PatientService } from 'src/app/core/_services/patient.service';
import { BehaviorSubject } from 'rxjs';
import { SnackBarService } from 'src/app/core/_services/snack-bar.service';
import { HttpResponse } from '@angular/common/http';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { FacilityService } from 'src/app/core/_services/facility.service';
import FormType, { FormCardGeneric } from 'src/app/core/_model/form-structure';

@Component({
  selector: 'app-patient-form',
  templateUrl: './patient-form.component.html',
  styleUrls: ['./patient-form.component.css'],

})
export class PatientFormComponent {
  private _patientId: number = -1;

  @Input()
  public patient: EhrPatient = { id: -1 };
  @Output()
  patientChange = new EventEmitter<EhrPatient>();
  @Output()
  savedEmitter = new EventEmitter<EhrPatient | number | string>();

  isEditionMode: boolean = false;
  populate = false

  constructor(private patientService: PatientService,
    private facilityService: FacilityService,
    private snackBarService: SnackBarService,
    @Optional() public _dialogRef?: MatDialogRef<PatientFormComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data?: { patient: EhrPatient }) {
    if (data && data.patient) {
      this.patient = data.patient;
      this._patientId = data.patient.id ?? -1
      this.isEditionMode = true
    }
  }

  fillRandom(): void {
    this.patientService.readRandom(this.facilityService.getCurrentId()).subscribe((res) => this.patient = res)
  }

  save(): void {
    // Just making sure this right id is used
    this.patient.id = this._patientId
    if (this.isEditionMode) {
      this.patientService.quickPutPatient(this.patient).subscribe({
        next: (res: EhrPatient) => {
          this.closeDialog(res)
        },
        error: (err) => {
          console.log(err.error)
          this.snackBarService.errorMessage(err.error);
        }
      });
    } else {
      this.patientService.quickPostPatient(this.patient).subscribe({
        next: (res: HttpResponse<string>) => {
          if (res.body && this.populate) {
            this.patientService.populatePatient(+res.body).subscribe((res2) => {
              this.closeDialog(+(res.body ?? -1))
            })
          } else {
            this.closeDialog(+(res.body ?? -1))
          }
        },
        error: (err) => {
          console.log(err.error)
          this.snackBarService.errorMessage(err.error);
        }
      });
    }
  }

  jsonChange(value: EhrPatient) {
    this.patient = value
  }

  closeDialog(res: EhrPatient | number | string) {
    this.savedEmitter.emit(res)
    if (this._dialogRef?.close) {
      this._dialogRef.close(res);
    }
  }


  readonly PATIENT_FORM_CARDS: FormCardGeneric<EhrPatient>[] = [
    {
      title: 'Name', cols: 3, rows: 1, forms: [
        { type: FormType.text, title: 'First name', attributeName: 'nameFirst' },
        { type: FormType.text, title: 'Middle name', attributeName: 'nameMiddle' },
        { type: FormType.text, title: 'Last name', attributeName: 'nameLast' },
        { type: FormType.text, title: 'Suffix', attributeName: 'nameSuffix' },
        { type: FormType.text, title: 'Mother maiden name', attributeName: 'motherMaiden' },
      ]
    },
    {
      title: 'Medical Record Number', cols: 1, rows: 1, forms: [
        // { type: FormType.text, title: 'Mrn Identifier', attributeName: 'mrn' },
        // { type: FormType.text, title: 'Mrn System', attributeName: 'mrnSystem' },
        { type: FormType.identifiers, title: 'Identifier', attributeName: 'identifiers', defaultListEmptyValue: JSON.stringify({ value: "", system: "", type: "MR" }) },
      ]
    },
    {
      title: 'Birth', cols: 1, rows: 1, forms: [
        { type: FormType.date, title: 'Birth date', attributeName: 'birthDate', required: true },
        { type: FormType.yesNo, title: 'Multiple birth', attributeName: 'birthFlag' },
        { type: FormType.short, title: 'Order', attributeName: 'birthOrder' },
      ]
    },
    {
      title: 'Identity', cols: 1, rows: 1, forms: [
        { type: FormType.code, title: 'Sex', attributeName: 'sex', codeMapLabel: "PATIENT_SEX" },
        { type: FormType.code, title: 'Ethnicity', attributeName: 'ethnicity', codeMapLabel: "PATIENT_ETHNICITY" },
        { type: FormType.races, title: 'Race', attributeName: 'races', defaultListEmptyValue: '{}' },
      ]
    },
    {
      title: 'Address', cols: 1, rows: 2, forms: [
        { type: FormType.addresses, title: 'Address', attributeName: 'addresses', defaultListEmptyValue: '{}' },
      ]
    },
    {
      title: 'Contact', cols: 1, rows: 1, forms: [
        { type: FormType.text, title: 'Email', attributeName: 'email' },
        { type: FormType.phoneNumbers, title: 'Phone', attributeName: 'phones', defaultListEmptyValue: '{}' },
      ]
    },
    {
      title: 'Death', cols: 1, rows: 1, forms: [
        { type: FormType.yesNo, title: 'Death flag', attributeName: 'deathFlag' },
        { type: FormType.date, title: 'Death date', attributeName: 'deathDate' },
      ]
    },
    {
      title: 'Publicity', cols: 1, rows: 1,
      toolTips: "Indicates reminder/recall intentions. A blank value will default to ‘Y’ in CAIR.",
      forms: [

        { type: FormType.code, title: 'Indicator', attributeName: 'publicityIndicator', codeMapLabel: 'PATIENT_PUBLICITY' },
        { type: FormType.date, title: 'Date', attributeName: 'publicityIndicatorDate' },
      ]
    },
    {
      title: 'Protection', cols: 1, rows: 1,
      toolTips: "’Y’, ‘N’. Indicates whether patient data should be ‘locked’ from view of CAIR2 providers outside of the facility that locked the record.",
      forms: [
        { type: FormType.yesNo, title: 'Indicator', attributeName: 'protectionIndicator' },
        { type: FormType.date, title: 'Date', attributeName: 'protectionIndicatorDate' },
      ]
    },
    {
      title: 'Registry', cols: 1, rows: 1,
      toolTips: 'Current status of the patient in relation to the sending provider organization',
      forms: [
        { type: FormType.code, title: 'Indicator', attributeName: 'registryStatusIndicator', codeMapLabel: 'REGISTRY_STATUS' },
        { type: FormType.date, title: 'Date', attributeName: 'registryStatusIndicatorDate' },
      ]
    },

    {
      title: 'Immunization Financial Status', cols: 1, rows: 1, forms: [
        { type: FormType.code, title: 'Financial status', attributeName: 'financialStatus', codeMapLabel: "FINANCIAL_STATUS_CODE" },
      ]
    },
    {
      title: 'Next of Kin', cols: 3, rows: 1, forms: [
        { type: FormType.nextOfKinRelationships, title: 'Next of kin', attributeName: 'nextOfKinRelationships' },
      ]
    },
  ]
}
