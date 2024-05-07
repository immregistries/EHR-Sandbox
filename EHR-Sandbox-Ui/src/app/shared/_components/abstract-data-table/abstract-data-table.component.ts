import { trigger, state, style, transition, animate } from '@angular/animations';
import { AfterViewInit, Component, EventEmitter, Input, Output } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { Observable } from 'rxjs';
import { ObjectWithID } from 'src/app/core/_model/rest';

@Component({
  template: '',
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({ height: '0px', minHeight: '0' })),
      state('expanded', style({ height: '*' })),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ],

})
export class AbstractDataTableComponent<T> implements AfterViewInit {

  constructor() { }

  public dataSource = new MatTableDataSource<T>();

  @Input()
  public allow_create: boolean = true
  @Input()
  public allow_populate: boolean = false;

  loading: boolean = false
  lastIdSelectedBeforeRefresh: number = -1;

  @Input()
  selectedElement: T | undefined;
  @Output() selectEmitter: EventEmitter<T | undefined> = new EventEmitter<T | undefined>();


  @Input()
  observableRefresh?: Observable<any>;
  @Input()
  observableSource!: Observable<T[]>;

  public _data_set_input: boolean = false
  @Input()
  public set dataArray(value: T[] | undefined | null) {
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
      this.observableRefresh?.subscribe(() => {
        this.loading = true
        this.observableSource.subscribe((list) => {
          this.loading = false
          this.dataSource.data = list
          if (this.hasIdElement(this.selectedElement)) {
            // @ts-ignore
            this.selectedElement = list.find((item: T) => { return item.id === this.selectedElement?.id });
          }
        })
      })
    }
  }

  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }

  onSelection(event: T | undefined, index?: number) {
    if (event && this.selectedElement && this.hasIdElement(this.selectedElement) && this.hasIdElement(event)) {
      // @ts-ignore
      if (event && this.selectedElement?.id == event.id) {
        this.selectedElement = undefined
      } else {
        this.selectedElement = event
      }
    } else {
      this.selectedElement = event
    }
    this.selectEmitter.emit(this.selectedElement);
  }


  private hasIdElement(t: T | undefined) {
    if (this.selectedElement && Object.keys(this.selectedElement).includes('id')) {
      return true
    } else {
      return false
    }
  }


}
