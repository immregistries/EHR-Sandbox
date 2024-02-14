import { AfterViewInit, Component, EventEmitter, Input, Output } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { Observable } from 'rxjs';
import { EhrPatient, Facility, ObjectWithID } from 'src/app/core/_model/rest';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { PatientService } from 'src/app/core/_services/patient.service';

@Component({
  template: ''
})
export class AbstractDataTableComponent<T extends ObjectWithID> implements AfterViewInit {

  constructor(public patientService: PatientService, public facilityService: FacilityService) { }

  private _dataSource = new MatTableDataSource<T>();
  public get dataSource() {
    return this._dataSource;
  }
  @Input()
  public set dataSource(value) {
    this._dataSource = value;
  }
  // @Input()
  // public set data(value: T[]) {
  //   this._dataSource.data = value;
  // }
  @Input() title: string = 'Patients'
  loading: boolean = false
  lastIdSelectedBeforeRefresh: number = -1;

  @Input()
  selectedElement: T | undefined;
  @Output() eventEmitter: EventEmitter<T | undefined> = new EventEmitter<T | undefined>();


  @Input()
  observableRefresh!: Observable<any>;
  @Input()
  observableSource!: Observable<T[]>;
  @Input()
  selectingElementObser!: Observable<T[]>;

  ngAfterViewInit(): void {
    // Set filter rules for research
    this.dataSource.filterPredicate = (data: T, filter: string) => {
      return JSON.stringify(data).trim().toLowerCase().indexOf(filter) !== -1
    };
    this.observableRefresh.subscribe(() => {
      this.loading = true
      this.observableSource.subscribe((list) => {
        this.loading = false
        this.dataSource.data = list
        this.onSelection(this.selectedElement ? list.find((item: T) => { return item.id === this.selectedElement?.id }): undefined);
      })
    })
  }



  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }

  onSelection(event: T | undefined) {
    if (event && this.selectedElement?.id == event.id ) {
      this.selectedElement = undefined
    } else {
      this.selectedElement = event
    }
    this.eventEmitter.emit(this.selectedElement);
  }


}
