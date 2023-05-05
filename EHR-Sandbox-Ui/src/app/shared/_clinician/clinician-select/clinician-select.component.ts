import { Component, EventEmitter, Input, Output, ViewChild } from '@angular/core';
import { NgForm } from '@angular/forms';
import { Subscription } from 'rxjs';
import { Clinician } from 'src/app/core/_model/rest';
import { ClinicianService } from 'src/app/core/_services/clinician.service';
import { TenantService } from 'src/app/core/_services/tenant.service';

@Component({
  selector: 'app-clinician-select',
  templateUrl: './clinician-select.component.html',
  styleUrls: ['./clinician-select.component.css']
})
export class ClinicianSelectComponent {
  @ViewChild('selectClinicianForm', { static: true }) selectClinicianForm!: NgForm;

  private formChangesSubscription!: Subscription
  ngOnDestroy() {
    this.formChangesSubscription.unsubscribe();
  }

  // @Input() model!: string;
  filter: string = ''

  private _clinician?: Clinician;
  @Input()
  set model(clinician: Clinician | number | undefined) {
    if (!clinician) {
      this._clinician = undefined
      this.filter = ''
    } else if (typeof clinician === "number" || typeof clinician ===  "string") {
      this.clinicianService.readClinician(this.tenantService.getTenantId(),clinician).subscribe((res) => {
        this._clinician = res
        this.filter = this.displayFn(this._clinician)

      })
    } else {
      this._clinician = clinician
      this.filter = this.displayFn(this._clinician)

    }
  }

  get model(): Clinician | number | undefined {
    return this._clinician
  }


  @Output() modelChange: EventEmitter<Clinician> = new EventEmitter()

  constructor(public clinicianService: ClinicianService,
    private tenantService: TenantService) {

  }


  options: Clinician[] = []
  filteredOptions: Clinician[] = []
  ngOnInit() {
    this.tenantService.getObservableTenant().subscribe((tenant) => {
      this.clinicianService.readClinicians(tenant.id).subscribe((res) => {
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
  }

  displayFn(clinician: Clinician): string {
    let selected: Clinician | undefined = this.options.find((opt) => opt.id == clinician)
    if (!selected) {
      selected = clinician
    }
    return selected && selected.nameFirst ? selected.nameFirst + ' ' + selected.nameLast : '';
  }

  filterChange(event: string){
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



  valueChanged(){
    this.modelChange.emit(this._clinician)

  }


}
