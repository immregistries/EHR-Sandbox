import { Component, EventEmitter, Inject, Input, OnInit, Optional, Output } from '@angular/core';
import { EhrPatient, Feedback } from 'src/app/core/_model/rest';
import { PatientService } from 'src/app/core/_services/patient.service';
import { SnackBarService } from 'src/app/core/_services/snack-bar.service';
import { HttpResponse } from '@angular/common/http';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { FacilityService } from 'src/app/core/_services/facility.service';
import FormType, { FormCardGeneric, GenericForm } from 'src/app/core/_model/form-structure';

@Component({
  selector: 'app-patient-form',
  templateUrl: './patient-form.component.html',
  styleUrls: ['./patient-form.component.css'],

})
export class PatientFormComponent {
  private _patientId: number = -1;

  @Input()
  public patient: EhrPatient = { id: -1, names: [] };
  @Output()
  patientChange = new EventEmitter<EhrPatient>();
  @Output()
  savedEmitter = new EventEmitter<EhrPatient | number | string>();

  @Input()
  public issues?: Feedback[];

  isEditionMode: boolean = false;
  populate = false

  constructor(private patientService: PatientService,
    private facilityService: FacilityService,
    private snackBarService: SnackBarService,
    @Optional() public _dialogRef?: MatDialogRef<PatientFormComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data?: { patient: EhrPatient, issues?: Feedback[] }) {
    if (data && data.patient) {
      this.patient = data.patient;
      this._patientId = data.patient.id ?? -1
      this.isEditionMode = true
      this.issues = data.issues
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

  change(form: GenericForm<EhrPatient>, event: any) {
    //@ts-ignore
    this.patient[form.attributeName] = event
  }


  readonly PATIENT_FORM_CARDS: FormCardGeneric<EhrPatient>[] = [
    {
      title: 'Name',
      forms: [
        {
          type: FormType.names, title: 'Name', attributeName: 'names', defaultListEmptyValue: JSON.stringify({
            namePrefix: "",
            nameFirst: "",
            nameLast: "",
            nameMiddle: "",
            nameSuffix: "",
            nameType: "L"
          }),
          hl7Location: {
            segmentId: "PID",
            componentNumber: 5
          }
        },
        {
          type: FormType.text, title: 'Mother maiden name', attributeName: 'motherMaiden',
          hl7Location: {
            segmentId: "PID",
            componentNumber: 6
          }
        },
        {
          type: FormType.clinician, title: 'General Practitioner', attributeName: 'generalPractitioner',
          hl7Location: {
            segmentId: "PD1",
            componentNumber: 4
          }
        },
      ],
      // hl7Location: "PID^5"
    },
    {
      title: 'Identifiers / Medical Record Number', forms: [
        // { type: FormType.text, title: 'Mrn Identifier', attributeName: 'mrn' },
        // { type: FormType.text, title: 'Mrn System', attributeName: 'mrnSystem' },
        {
          type: FormType.identifiers, title: 'Identifier', attributeName: 'identifiers', defaultListEmptyValue: JSON.stringify({ value: "", system: "", type: "MR" }),
          hl7Location: {
            segmentId: "PID",
            componentNumber: 3
          }
        },
      ],
    },
    {
      title: 'Birth', forms: [
        {
          type: FormType.date, title: 'Birth date', attributeName: 'birthDate', required: true,
          hl7Location: {
            segmentId: "PID",
            componentNumber: 7
          }
        },
        {
          type: FormType.yesNo, title: 'Multiple birth', attributeName: 'birthFlag',
          hl7Location: {
            segmentId: "PID",
            componentNumber: 24
          }
        },
        {
          type: FormType.short, title: 'Order', attributeName: 'birthOrder',
          hl7Location: {
            segmentId: "PID",
            componentNumber: 25
          }
        },
      ]
    },
    {
      title: 'Identity', forms: [
        {
          type: FormType.code, title: 'Sex', attributeName: 'sex', codeMapLabel: "PATIENT_SEX",
          hl7Location: {
            segmentId: "PID",
            componentNumber: 8
          }
        },
        {
          type: FormType.code, title: 'Ethnicity', attributeName: 'ethnicity', codeMapLabel: "PATIENT_ETHNICITY",
          hl7Location: {
            segmentId: "PID",
            componentNumber: 22
          }
        },
        {
          type: FormType.races, title: 'Race', attributeName: 'races', defaultListEmptyValue: '{}',
          hl7Location: {
            segmentId: "PID",
            componentNumber: 10
          }
        },
      ], hl7Location: {
        segmentId: "PID",
        componentNumber: 3,
      }
    },
    {
      title: 'Address', forms: [
        {
          type: FormType.addresses, title: 'Address', attributeName: 'addresses', defaultListEmptyValue: '{}',
          hl7Location: {
            segmentId: "PID",
            componentNumber: 11
          }
        },
      ]
    },
    {
      title: 'Contact', forms: [
        {
          type: FormType.text, title: 'Email', attributeName: 'email',
          hl7Location: {
            segmentId: "PID",
            componentNumber: 10,
            segmentSequence: 1
          }
        },
        {
          type: FormType.phoneNumbers, title: 'Phone', attributeName: 'phones', defaultListEmptyValue: '{}',
          hl7Location: {
            segmentId: "PID",
            componentNumber: 10,
          }
        },
      ],
      hl7Location: {
        segmentId: "PID",
        componentNumber: 10,
      }
    },
    {
      title: 'Death', forms: [
        { type: FormType.yesNo, title: 'Death flag', attributeName: 'deathFlag' },
        { type: FormType.date, title: 'Death date', attributeName: 'deathDate' },
      ]
    },
    {
      title: 'Publicity',
      toolTips: "Indicates reminder/recall intentions. A blank value will default to ‘Y’ in CAIR.",
      forms: [
        {
          type: FormType.code, title: 'Indicator', attributeName: 'publicityIndicator', codeMapLabel: 'PATIENT_PUBLICITY',
          hl7Location: {
            segmentId: "PD1",
            componentNumber: 11
          }
        },
        {
          type: FormType.date, title: 'Date', attributeName: 'publicityIndicatorDate',
          hl7Location: {
            segmentId: "PD1",
            componentNumber: 18
          }
        },
      ]
    },
    {
      title: 'Protection',
      toolTips: "’Y’, ‘N’. Indicates whether patient data should be ‘locked’ from view of CAIR2 providers outside of the facility that locked the record.",
      forms: [
        {
          type: FormType.yesNo, title: 'Indicator', attributeName: 'protectionIndicator',
          hl7Location: {
            segmentId: "PD1",
            componentNumber: 12
          }
        },
        {
          type: FormType.date, title: 'Date', attributeName: 'protectionIndicatorDate',
          hl7Location: {
            segmentId: "PD1",
            componentNumber: 13
          }
        },
      ]
    },
    {
      title: 'Registry',
      toolTips: 'Current status of the patient in relation to the sending provider organization',
      forms: [
        {
          type: FormType.code, title: 'Indicator', attributeName: 'registryStatusIndicator', codeMapLabel: 'REGISTRY_STATUS',
          hl7Location: {
            segmentId: "PD1",
            componentNumber: 16
          }
        },
        {
          type: FormType.date, title: 'Date', attributeName: 'registryStatusIndicatorDate',
          hl7Location: {
            segmentId: "PD1",
            componentNumber: 17
          }
        },
      ]
    },

    {
      title: 'Immunization Financial Status', forms: [
        { type: FormType.code, title: 'Financial status', attributeName: 'financialStatus', codeMapLabel: "FINANCIAL_STATUS_CODE" },
      ]
    },
    {
      title: 'Next of Kin', forms: [
        { type: FormType.nextOfKinRelationships, title: 'Next of kin', attributeName: 'nextOfKinRelationships' },
      ], hl7Location: {
        segmentId: "NK1"
      }
    },
  ]
}
