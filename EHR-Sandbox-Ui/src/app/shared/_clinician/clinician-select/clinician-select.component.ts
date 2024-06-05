import { Component, EventEmitter, Input, Output, ViewChild } from '@angular/core';
import { NgForm } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { Subscription, concat } from 'rxjs';
import { Clinician } from 'src/app/core/_model/rest';
import { ClinicianService } from 'src/app/core/_services/clinician.service';
import { TenantService } from 'src/app/core/_services/tenant.service';
import { ClinicianFormComponent } from '../clinician-form/clinician-form.component';
import { CardFormComponent } from '../../_components/card-form/card-form.component';
import { BaseForm } from 'src/app/core/_model/structure';

@Component({
  selector: 'app-clinician-select',
  templateUrl: './clinician-select.component.html',
  styleUrls: ['./clinician-select.component.css']
})
export class ClinicianSelectComponent {

  constructor(public clinicianService: ClinicianService,
    private tenantService: TenantService,
    private dialog: MatDialog) { }

  @ViewChild('selectClinicianForm', { static: true }) selectClinicianForm!: NgForm;

  private formChangesSubscription!: Subscription
  ngOnDestroy() {
    this.formChangesSubscription.unsubscribe();
  }

  private _clinician?: Clinician;
  firstInit: boolean = true;
  @Input()
  form!: BaseForm;
  @Input()
  set model(clinician: Clinician | number | undefined) {
    if (!clinician) {
      this._clinician = undefined
      this.filterChange('')
    } else if (typeof clinician === "number" || typeof clinician === "string") {
      if (this.firstInit) {
        this.clinicianService.readClinician(this.tenantService.getCurrentId(), clinician).subscribe((res) => {
          this._clinician = res
        })
      } else {
        this._clinician = this.options.find((opt) => opt.id == clinician)
      }
    } else {
      this._clinician = clinician
    }
    this.firstInit = false
  }

  get model(): Clinician | number | undefined {
    return this._clinician
  }


  @Output() modelChange: EventEmitter<Clinician> = new EventEmitter()

  options: Clinician[] = []
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
      this.valueChanged()
    })
  }

  onSelect(c: number) {
    this.model = c
    this.filterChange('')
  }

  displayFn(clinician: Clinician): string {
    let selected: Clinician | undefined = this.options.find((opt) => opt.id == clinician)
    if (!selected) {
      selected = clinician
    }
    return selected && selected.nameFirst ? selected.nameFirst + ' ' + selected.nameLast : '';
  }

  filterChange(event: string) {
    let filterValue = ''
    if (typeof event == 'string') {
      if (event) {
        filterValue = event.toLowerCase();
      }
      this.filteredOptions = this.options.filter(
        option => {
          return JSON.stringify(option).toLowerCase().includes(filterValue)
        }
      )
    }
  }

  valueChanged() {
    this.modelChange.emit(this._clinician)
  }

  openEdition() {

  }

  add() {
    const dialogRef = this.dialog.open(ClinicianFormComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '30vw',
      panelClass: 'dialog-with-bar',
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.model = result
      }
      this.clinicianService.doRefresh()
    });

  }

  edit(cli: Clinician) {
    const dialogRef = this.dialog.open(ClinicianFormComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '30vw',
      panelClass: 'dialog-with-bar',
      data: { clinician: cli }
    });
    dialogRef.afterClosed().subscribe(result => {
      this.clinicianService.doRefresh()
      // this.ngOnInit()
      this.model = result
    });

  }

  @Input() overrideNoFieldsRequired: boolean = false
  @Input() overrideAllFieldsRequired: boolean = false
  isRequired(): 'true' | 'false' {
    if (this.overrideNoFieldsRequired) {
      return 'false'
    } else if (this.overrideAllFieldsRequired) {
      return 'true'
    } else if (this.form.required) {
      return 'true'
    } else {
      return 'false'
    }
  }




}
