import { Component, EventEmitter, Inject, Input, Optional, Output } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { BehaviorSubject } from 'rxjs';
import { Clinician } from 'src/app/core/_model/rest';
import FormType, { CodeReference, FormCard } from 'src/app/core/_model/structure';
import { ClinicianService } from 'src/app/core/_services/clinician.service';
import { TenantService } from 'src/app/core/_services/tenant.service';

@Component({
  selector: 'app-clinician-form',
  templateUrl: './clinician-form.component.html',
  styleUrls: ['./clinician-form.component.css']
})
export class ClinicianFormComponent {

  constructor(private tenantService: TenantService,
    private clinicianService: ClinicianService,
    @Optional() public _dialogRef: MatDialogRef<ClinicianFormComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: { clinician: Clinician }) {
    if (data && data.clinician) {
      this.model = data.clinician
    }
  }

  /**
   * Currently unusused, just initialised
   */
  public references: BehaviorSubject<{ [key: string]: { reference: CodeReference, value: string } }> = new BehaviorSubject<{ [key: string]: { reference: CodeReference, value: string } }>({});

  @Input()
  model: Clinician = {}

  @Output() modelChange = new EventEmitter<Clinician>();

  formCards: FormCard[] = [
    {
      title: "Clinician Name", rows: 1, cols: 1, clinicianForms: [
        { type: FormType.text, title: "First name", attribute: "nameFirst" },
        { type: FormType.text, title: "Middle name", attribute: "nameMiddle" },
        { type: FormType.text, title: "Last name", attribute: "nameLast" },
        { type: FormType.text, title: "Last suffix", attribute: "nameSuffix" },
        // { type: FormType.text, title: "Title", attribute: "title" }
      ]
    },
    {
      title: "Identifiers", rows: 1, cols: 1, clinicianForms: [
        { type: FormType.identifiers, title: "Identifier", attribute: "identifiers" },
      ]
    }
  ]

  save() {
    if (this.model.id && this.model.id > -1) {
      this.clinicianService.putClinician(this.tenantService.getCurrentId(), this.model).subscribe((res) => {
        this._dialogRef?.close(res)
      })
    } else {
      this.clinicianService.postClinician(this.tenantService.getCurrentId(), this.model).subscribe((res) => {
        this._dialogRef?.close(res)
      })
    }
  }

  fillRandom() {
    this.clinicianService.random(this.tenantService.getCurrentId()).subscribe((res) => {
      this.model = res
    })
  }

}
