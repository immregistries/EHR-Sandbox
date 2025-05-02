import { AfterViewInit, Directive, Input, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { ObjectWithID } from 'src/app/core/_model/rest';

@Directive()
export abstract class AbstractMergingTableComponent<T extends ObjectWithID> implements AfterViewInit {
  constructor() { }

  abstract paginator?: MatPaginator;
  abstract sort?: MatSort;


  public dataSource = new MatTableDataSource<T>([]);

  ngAfterViewInit(): void {
    // Set filter rules for research
    if (this.sort) {
      this.dataSource.sort = this.sort
    }
    if (this.paginator) {
      this.dataSource.paginator = this.paginator
    }
    // this.dataSource.sortingDataAccessor = this.sortingAccessor
    // this.dataSource.sort = new MatSort()
    // this.dataSource.sort?.register({ id: "match", start: 'desc', disableClear: false })
  }

  @Input()
  set remoteValues(values: T[]) {
    this.loading = false
    this.dataSource.data = values;
    this.expandedElement = values.find((val: T) => { return val.id == this.expandedElement?.id }) ?? null
    this.updateMatchingMatrix()
    // this.dataSource.sort?.sort({ id: "match", start: 'desc', disableClear: false })
  }
  get remoteValues(): T[] {
    return this.dataSource.data
  }

  private _localValues!: T[];
  @Input()
  set localValues(values: T[] | undefined | null) {
    this._localValues = values ?? [];
    this.updateMatchingMatrix()
  }
  get localValues() { return this._localValues }

  public matchingMatrix: {}[][] = []
  public loading = false;

  public expandedElement: T | null = null;

  public selectElement(element: T | null, index: number) {
    this.expandedElement = this.expandedElement === element ? null : element
  }

  // protected abstract updateDifferences(): void;


  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }



  protected abstract updateMatchingMatrix(): void;

  @Input()
  public localSelectedIndex?: number | undefined;


  comparisonWithSelected(index: number) {
    return (index && this.localSelectedIndex != undefined) ? this.matchingMatrix[index][this.localSelectedIndex] : null
  }

  comparedWith(): T | null {
    return this.localValues && this.localSelectedIndex != undefined ? this.localValues[this.localSelectedIndex] : null
  }

  isMatch(index: number): boolean {
    return this.comparisonWithSelected(index) == 'MATCH' ? true : false;
  }

  hasNoMatch(index: number): boolean {
    return !this.matchingMatrix[index]?.includes('MATCH');
  }

  hasNoMatchObject(data: T): boolean {
    return !this.matchingMatrix[this.dataSource.data.indexOf(data)]?.includes('MATCH');
  }

}
