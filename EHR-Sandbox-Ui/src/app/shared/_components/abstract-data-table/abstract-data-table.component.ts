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

  constructor() { }

  public dataSource = new MatTableDataSource<T>();

  @Input()
  public allow_create: boolean = true

  loading: boolean = false
  lastIdSelectedBeforeRefresh: number = -1;

  @Input()
  selectedElement: T | undefined;
  @Output() selectEmitter: EventEmitter<T | undefined> = new EventEmitter<T | undefined>();


  @Input()
  observableRefresh!: Observable<any>;
  @Input()
  observableSource!: Observable<T[]>;

  private _data_set_input: boolean = false
  @Input()
  public set data(value: T[] | undefined) {
    this._data_set_input = true
    if (value) {
      this.dataSource.data = value;
    }
  }

  ngAfterViewInit(): void {
    // Set filter rules for research
    this.dataSource.filterPredicate = (data: T, filter: string) => {
      return JSON.stringify(data).trim().toLowerCase().indexOf(filter) !== -1
    };
    if (!this._data_set_input) {
      this.observableRefresh.subscribe(() => {
        this.loading = true
        this.observableSource.subscribe((list) => {
          this.loading = false
          this.dataSource.data = list
          if (this.selectedElement) {
            this.onSelection(list.find((item: T) => { return item.id === this.selectedElement?.id }));
          }
        })
      })
    }
  }



  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }

  onSelection(event: T | undefined) {
    if (event && this.selectedElement?.id == event.id) {
      this.selectedElement = undefined
    } else {
      this.selectedElement = event
    }
    this.selectEmitter.emit(this.selectedElement);
  }


}
