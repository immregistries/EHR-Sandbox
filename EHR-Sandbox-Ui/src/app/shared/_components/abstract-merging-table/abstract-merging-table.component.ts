import { Directive, Input } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { ObjectWithID } from 'src/app/core/_model/rest';

@Directive()
export abstract class AbstractMergingTableComponent<T extends ObjectWithID> {
  constructor() { }

  public dataSource = new MatTableDataSource<T>([]);
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
  public differencesWithSelected: any = ''

  isMatch(element: T | null): boolean {
    return (this.expandedElement && this.differencesWithSelected == 'MATCH') ? true : false;
  }

  hasNoMatch(index: number): boolean {
    return !this.matchingMatrix[index]?.includes('MATCH');
  }

  hasNoMatchObject(data: T): boolean {
    return !this.matchingMatrix[this.dataSource.data.indexOf(data)]?.includes('MATCH');
  }

  public expandedElement: T | null = null;

  public selectElement(element: T | null) {
    this.expandedElement = this.expandedElement === element ? null : element
    this.updateDifferences()
  }

  protected abstract updateDifferences(): void;
  protected abstract updateMatchingMatrix(): void;

  private _valueToCompare!: T | null;
  @Input()
  public get valueToCompare(): T | null {
    return this._valueToCompare;
  }
  public set valueToCompare(value: T | null) {
    this._valueToCompare = value;
    this.updateDifferences()
  }


  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }

}
