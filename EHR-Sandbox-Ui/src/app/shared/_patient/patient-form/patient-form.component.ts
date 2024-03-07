import { Component, EventEmitter, Inject, Input, OnInit, Optional, Output } from '@angular/core';
import { EhrPatient } from 'src/app/core/_model/rest';
import FormType, { FormCard, CodeReference } from 'src/app/core/_model/structure';
import { PatientService } from 'src/app/core/_services/patient.service';
import { BreakpointObserver } from '@angular/cdk/layout';
import { BehaviorSubject } from 'rxjs';
import { SnackBarService } from 'src/app/core/_services/snack-bar.service';
import { HttpResponse } from '@angular/common/http';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';

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

  isEditionMode: boolean = false;

  /**
   * Currently unusused, just initialised
   */
  public references: BehaviorSubject<{ [key: string]: { reference: CodeReference, value: string } }> = new BehaviorSubject<{ [key: string]: { reference: CodeReference, value: string } }>({});

  constructor(private patientService: PatientService,
    private breakpointObserver: BreakpointObserver,
    private snackBarService: SnackBarService,
    @Optional() public _dialogRef: MatDialogRef<PatientFormComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: { patient: EhrPatient }) {
    if (data && data.patient) {
      this.patient = data.patient;
      this.isEditionMode = true
    }
  }

  fillRandom(): void {
    this.patientService.readRandom().subscribe((res) => this.patient = res)
  }

  save(): void {
    if (this.isEditionMode) {
      this.patientService.quickPutPatient(this.patient).subscribe({
        next: (res: EhrPatient) => {
          this._dialogRef?.close(res)
        },
        error: (err) => {
          console.log(err.error)
          this.snackBarService.errorMessage(err.error.error);
        }
      });
    } else {
      this.patientService.quickPostPatient(this.patient).subscribe({
        next: (res: HttpResponse<string>) => {
          this._dialogRef?.close(res)
        },
        error: (err) => {
          console.log(err.error)
          this.snackBarService.errorMessage(err.error.error);
        }
      });
    }
  }


  formCards: FormCard[] = [
    {
      title: 'Name', cols: 3, rows: 1, patientForms: [
        { type: FormType.text, title: 'First name', attribute: 'nameFirst' },
        { type: FormType.text, title: 'Middle name', attribute: 'nameMiddle' },
        { type: FormType.text, title: 'Last name', attribute: 'nameLast' },
        { type: FormType.text, title: 'Mother maiden name', attribute: 'motherMaiden' },

      ]
    },
    {
      title: 'Medical Record Number', cols: 1, rows: 1, patientForms: [
        { type: FormType.text, title: 'Mrn Identifier', attribute: 'mrn' },
        { type: FormType.text, title: 'Mrn System', attribute: 'mrnSystem' },
      ]
    },
    {
      title: 'Birth', cols: 1, rows: 1, patientForms: [
        { type: FormType.date, title: 'Birth date', attribute: 'birthDate' },
        { type: FormType.yesNo, title: 'Multiple birth', attribute: 'birthFlag' },
        { type: FormType.short, title: 'Order', attribute: 'birthOrder' },
      ]
    },
    {
      title: 'Identity', cols: 1, rows: 1, patientForms: [
        { type: FormType.code, title: 'Sex', attribute: 'sex', codeMapLabel: "PATIENT_SEX", options: [{ value: 'M' }, { value: 'F' }] },
        { type: FormType.code, title: 'Ethnicity', attribute: 'ethnicity', codeMapLabel: "PATIENT_ETHNICITY" },
        { type: FormType.code, title: 'Race', attribute: 'race', codeMapLabel: "PATIENT_RACE" },
      ]
    },
    {
      title: 'Address', cols: 1, rows: 2, patientForms: [
        { type: FormType.text, title: 'Line 1', attribute: 'addressLine1' },
        { type: FormType.text, title: 'Line 2', attribute: 'addressLine2' },
        { type: FormType.text, title: 'Zip code', attribute: 'addressZip' },
        { type: FormType.text, title: 'City', attribute: 'addressCity' },
        { type: FormType.text, title: 'County', attribute: 'addressCountyParish' },
        { type: FormType.text, title: 'State', attribute: 'addressState' },
        { type: FormType.text, title: 'Country', attribute: 'addressCountry' },
      ]
    },
    {
      title: 'Contact', cols: 1, rows: 1, patientForms: [
        { type: FormType.text, title: 'Phone', attribute: 'phone' },
        { type: FormType.text, title: 'Email', attribute: 'email' },
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
      title: 'Guardian', cols: 3, rows: 1, patientForms: [
        { type: FormType.text, title: 'First name', attribute: 'guardianFirst' },
        { type: FormType.text, title: 'Middle name', attribute: 'guardianMiddle' },
        { type: FormType.text, title: 'Last name', attribute: 'guardianLast' },
        { type: FormType.code, title: 'Relationship', attribute: 'guardianRelationship', codeMapLabel: "PERSON_RELATIONSHIP" },
      ]
    },
  ]
}
