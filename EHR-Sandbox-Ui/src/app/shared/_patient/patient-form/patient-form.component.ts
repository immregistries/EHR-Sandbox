import { Component, EventEmitter, Inject, Input, OnInit, Optional, Output } from '@angular/core';
import { EhrPatient } from 'src/app/core/_model/rest';
import FormType, { FormCard, CodeReference } from 'src/app/core/_model/structure';
import { PatientService } from 'src/app/core/_services/patient.service';
import { BehaviorSubject } from 'rxjs';
import { SnackBarService } from 'src/app/core/_services/snack-bar.service';
import { HttpResponse } from '@angular/common/http';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Dialog } from '@angular/cdk/dialog';

@Component({
  selector: 'app-patient-form',
  templateUrl: './patient-form.component.html',
  styleUrls: ['./patient-form.component.css'],

})
export class PatientFormComponent {
  @Input()
  patient: EhrPatient = { id: -1 };
  @Output()
  patientChange = new EventEmitter<EhrPatient>();
  @Output()
  savedEmitter = new EventEmitter<EhrPatient | number | string>();

  isEditionMode: boolean = false;
  populate = false

  /**
   * Currently unusused, just initialised
   */
  public references: BehaviorSubject<{ [key: string]: { reference: CodeReference, value: string } }> = new BehaviorSubject<{ [key: string]: { reference: CodeReference, value: string } }>({});

  constructor(private patientService: PatientService,
    private snackBarService: SnackBarService,
    @Optional() public _dialogRef?: MatDialogRef<PatientFormComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data?: { patient: EhrPatient }) {
    if (data && data.patient) {
      this.patient = data.patient;
      this.isEditionMode = true
    }
  }

  fillRandom(): void {
    this.patientService.readRandom().subscribe((res) => this.patient = res)
    console.log(this.patient.phones)
  }

  save(): void {
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

  closeDialog(res: EhrPatient | number | string) {
    this.savedEmitter.emit(res)
    if (this._dialogRef?.close) {
      this._dialogRef.close(res);
    }
  }


  readonly PATIENT_FORM_CARDS: FormCard[] = [
    {
      title: 'Name', cols: 3, rows: 1, patientForms: [
        { type: FormType.text, title: 'First name', attribute: 'nameFirst' },
        { type: FormType.text, title: 'Middle name', attribute: 'nameMiddle' },
        { type: FormType.text, title: 'Last name', attribute: 'nameLast' },
        { type: FormType.text, title: 'Suffix', attribute: 'nameSuffix' },
        { type: FormType.text, title: 'Mother maiden name', attribute: 'motherMaiden' },
      ]
    },
    {
      title: 'Medical Record Number', cols: 1, rows: 1, patientForms: [
        { type: FormType.text, title: 'Mrn Identifier', attribute: 'mrn' },
        { type: FormType.text, title: 'Mrn System', attribute: 'mrnSystem' },
        { type: FormType.identifiers, title: 'Identifier', attribute: 'identifiers' },
      ]
    },
    {
      title: 'Birth', cols: 1, rows: 1, patientForms: [
        { type: FormType.date, title: 'Birth date', attribute: 'birthDate', required: true },
        { type: FormType.yesNo, title: 'Multiple birth', attribute: 'birthFlag' },
        { type: FormType.short, title: 'Order', attribute: 'birthOrder' },
      ]
    },
    {
      title: 'Identity', cols: 1, rows: 1, patientForms: [
        { type: FormType.code, title: 'Sex', attribute: 'sex', codeMapLabel: "PATIENT_SEX" },
        { type: FormType.code, title: 'Ethnicity', attribute: 'ethnicity', codeMapLabel: "PATIENT_ETHNICITY" },
        { type: FormType.races, title: 'Race', attribute: 'races' },
      ]
    },
    {
      title: 'Address', cols: 1, rows: 2, patientForms: [
        { type: FormType.addresses, title: 'Address', attribute: 'addresses' },
        // { type: FormType.text, title: 'Line 1', attribute: 'addressLine1' },
        // { type: FormType.text, title: 'Line 2', attribute: 'addressLine2' },
        // { type: FormType.text, title: 'Zip code', attribute: 'addressZip' },
        // { type: FormType.text, title: 'City', attribute: 'addressCity' },
        // { type: FormType.text, title: 'County', attribute: 'addressCountyParish' },
        // { type: FormType.text, title: 'State', attribute: 'addressState' },
        // { type: FormType.text, title: 'Country', attribute: 'addressCountry' },
      ]
    },
    {
      title: 'Contact', cols: 1, rows: 1, patientForms: [
        { type: FormType.text, title: 'Email', attribute: 'email' },
        { type: FormType.phoneNumbers, title: 'Phone', attribute: 'phones' },
      ]
    },
    {
      title: 'Death', cols: 1, rows: 1, patientForms: [
        { type: FormType.yesNo, title: 'Death flag', attribute: 'deathFlag' },
        { type: FormType.date, title: 'Death date', attribute: 'deathDate' },
      ]
    },
    {
      title: 'Protection', cols: 1, rows: 1, patientForms: [
        { type: FormType.short, title: 'Indicator', attribute: 'protectionIndicator' },
        { type: FormType.date, title: 'Date', attribute: 'protectionIndicatorDate' },
      ]
    },
    {
      title: 'Registry', cols: 1, rows: 1, patientForms: [
        { type: FormType.short, title: 'Indicator', attribute: 'registryStatusIndicator' },
        { type: FormType.date, title: 'Date', attribute: 'registryStatusIndicatorDate' },
      ]
    },
    {
      title: 'Publicity', cols: 1, rows: 1, patientForms: [
        { type: FormType.short, title: 'Indicator', attribute: 'publicityIndicator' },
        { type: FormType.date, title: 'Date', attribute: 'publicityIndicatorDate' },
      ]
    },
    {
      title: 'Financial Status', cols: 1, rows: 1, patientForms: [
        { type: FormType.code, title: 'Financial status', attribute: 'financialStatus', codeMapLabel: "FINANCIAL_STATUS_CODE" },
      ]
    },
    {
      title: 'Guardian', cols: 3, rows: 1, patientForms: [
        { type: FormType.text, title: 'First name', attribute: 'guardianFirst' },
        { type: FormType.text, title: 'Middle name', attribute: 'guardianMiddle' },
        { type: FormType.text, title: 'Last name', attribute: 'guardianLast' },
        { type: FormType.text, title: 'Suffix', attribute: 'guardianSuffix' },
        { type: FormType.code, title: 'Relationship', attribute: 'guardianRelationship', codeMapLabel: "PERSON_RELATIONSHIP" },
      ]
    },
    {
      title: 'Next of Kin', cols: 3, rows: 1, patientForms: [
        { type: FormType.nextOfKinRelationships, title: 'Next of kin', attribute: 'nextOfKinRelationships' },
      ]
    },
  ]
}
