import { Component, EventEmitter, Input, Output, ViewChild } from '@angular/core';
import { NgForm } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { Subscription, concat } from 'rxjs';
import { Clinician } from 'src/app/core/_model/rest';
import { ClinicianService } from 'src/app/core/_services/clinician.service';
import { TenantService } from 'src/app/core/_services/tenant.service';
import { ClinicianFormComponent } from '../../../_clinician/clinician-form/clinician-form.component';
import { CardFormComponent } from '../card-form.component';
import { BaseForm } from 'src/app/core/_model/structure';
import { MatAutocomplete } from '@angular/material/autocomplete';
import { AbstractBaseFormComponent } from '../abstract-base-form/abstract-base-form.component';

@Component({
  selector: 'app-clinician-select',
  templateUrl: './clinician-select.component.html',
  styleUrls: ['./clinician-select.component.css']
})
export class ClinicianSelectComponent extends AbstractBaseFormComponent {
  @ViewChild('auto')
  matAutoComplete!: MatAutocomplete;

  constructor(public clinicianService: ClinicianService,
    private tenantService: TenantService,
    private dialog: MatDialog) {
    super()
  }

  @ViewChild('selectClinicianForm', { static: true }) selectClinicianForm!: NgForm;

  private formChangesSubscription!: Subscription
  ngOnDestroy() {
    this.formChangesSubscription.unsubscribe();
  }

  private _model?: Clinician;
  private _isFirstInit: boolean = true;
  @Input()
  baseForm!: BaseForm;

  @Input()
  set model(clinician: Clinician | number | undefined) {
    if (!clinician) {
      this._model = undefined
      this.filterChange('')
    } else if (typeof clinician === "number" || typeof clinician === "string") {
      if (this._isFirstInit) {
        this.clinicianService.readClinician(this.tenantService.getCurrentId(), clinician).subscribe((res) => {
          this._model = res
        })
      } else {
        this._model = this.options.find((opt) => opt.id == clinician)
      }
    } else {
      // console.log('clinician', clinician)
      this._model = clinician
    }
    this._isFirstInit = false
  }

  get model(): Clinician | number | undefined {
    return this._model
  }

  @Output() modelChange: EventEmitter<Clinician> = new EventEmitter()

  private options: Clinician[] = []
  filteredOptions: Clinician[] = []
  ngOnInit() {
    concat(
      this.clinicianService.getRefresh(),
      this.tenantService.getCurrentObservable()
    ).subscribe(() => {
      this.clinicianService.readClinicians(this.tenantService.getCurrentId()).subscribe((res) => {
        this.options = res
        this.filterChange('')
      })
    })
    this.formChangesSubscription = this.selectClinicianForm.form.valueChanges.subscribe((value) => {
      this.valueChanged('sub')
    })
  }

  displayFn(clinician: Clinician): string {
    let selected: Clinician | undefined = this.options.find((opt) => opt.id == clinician)
    if (!selected) {
      selected = clinician
    }
    return selected && selected.nameFirst ? selected.nameFirst + ' ' + selected.nameLast : '';
  }

  filterChange(event: string) {
    console.log("event", event)
    let filterValue = ((event ?? '') + '').toLowerCase();
    this.filteredOptions = this.options.filter(
      option => {
        return JSON.stringify(option).toLowerCase().includes(filterValue) || this.displayFn(option).toLowerCase().includes(filterValue)
      }
    )
  }

  valueChanged(log?: string) {
    console.log('log', log, this._model)
    this.modelChange.emit(this._model)
  }

  add() {
    const dialogRef = this.dialog.open(ClinicianFormComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      minWidth: '30vw',
      panelClass: 'dialog-with-bar',
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.model = result
        this.valueChanged()
      }
      this.clinicianService.doRefresh()
    });

  }

  openEdition(cli: Clinician) {
    const dialogRef = this.dialog.open(ClinicianFormComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      minWidth: '30vw',
      panelClass: 'dialog-with-bar',
      data: { clinician: cli }
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.model = result
      }
      this.clinicianService.doRefresh()
    });

  }

  @Input() overrideNoFieldsRequired: boolean = false
  @Input() overrideAllFieldsRequired: boolean = false

  /**
   * On press enter key select first visible option
   */
  submit() {
    if (this.matAutoComplete.options && this.matAutoComplete.options.first) {
      this.model = this.matAutoComplete.options.first?.value ?? this.matAutoComplete.options.first?.value.code ?? this.model
      this.valueChanged('enter')
    } else {
      this.valueChanged('enter')
    }
  }




}
