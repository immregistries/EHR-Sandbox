import { trigger, state, style, transition, animate } from '@angular/animations';
import { AfterViewInit, Component, EventEmitter, Input, Output, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { Observable, of } from 'rxjs';

/**
 * Abstract component to harmonize basic table functionalities across the UI
 */
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

  @ViewChild(MatPaginator) paginator?: MatPaginator;
  @ViewChild(MatSort) sort?: MatSort;


  /**
   * Show Create button
   */
  @Input()
  public allow_create: boolean = true
  /**
   * Show Populate button
   */
  @Input()
  public allow_populate: boolean = false;

  @Input()
  /**
   * controls loading bar
   */
  loading: boolean = false

  /**
   * Selected element from table
   */
  @Input()
  selectedElement: T | undefined;
  @Output() selectEmitter: EventEmitter<T | undefined> = new EventEmitter<T | undefined>();
  @Output() selectIndexEmitter: EventEmitter<number | undefined> = new EventEmitter<number | undefined>();


  /**
   * Observable to call for to fill datasource object if not filled manually
   * DEPRECATED AS INPUT, Not the preferred way, unless specific refresh needed
   */
  @Input()
  observableSource?: Observable<T[]>;
  /**
   * Observable controlling when to refresh if observableSource set
   * DEPRECATED AS INPUT, Not the preferred way, unless specific refresh needed
   */
  @Input()
  observableRefresh: Observable<any> = of(true);

  /**
   * boolean used to detect and notifuy compoenet not to use observables, as the datasource was overriden by another component
   */
  public _data_set_input: boolean = false
  @Input()
  public set dataArray(value: T[] | undefined | null) {
    this._data_set_input = true
    if (value) {
      this.dataSource.data = value;
    }
  }

  /**
   * Settings for dataSource Object
   * Subscription to Input Observables if data not set through other mean
   *
   * Override here to customize sortingDataAccessor
   */
  ngAfterViewInit(): void {
    // Set filter rules for research
    this.dataSource.filterPredicate = (data: T, filter: string) => {
      return JSON.stringify(data).trim().toLowerCase().indexOf(filter) !== -1
    };
    if (this.sort) {
      this.dataSource.sort = this.sort
    }
    if (this.paginator) {
      this.dataSource.paginator = this.paginator
    }
    if (!this._data_set_input) {
      this.observableRefresh?.subscribe(() => {
        this.loading = true
        this.observableSource?.subscribe((list) => {
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
    // console.log(event, this.selectedElement)
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
    this.selectIndexEmitter.emit(index)
  }


  /**
   * Method used to defer semantic check for automatic selection
   * checks if the element has an Id to compare to
   * @param t
   * @returns
   */
  private hasIdElement(t: T | undefined) {
    if (this.selectedElement && Object.keys(this.selectedElement).includes('id')) {
      return true
    } else {
      return false
    }
  }


}
