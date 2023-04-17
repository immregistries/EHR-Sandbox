import { trigger, state, style, transition, animate } from '@angular/animations';
import { AfterViewInit, Component, Input, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatTableDataSource } from '@angular/material/table';
import { VaccinationEvent, Vaccine } from 'src/app/core/_model/rest';
import { CodeBaseMap } from 'src/app/core/_model/structure';
import { CodeMapsService } from 'src/app/core/_services/code-maps.service';
import { VaccinationService } from 'src/app/core/_services/vaccination.service';
import { VaccinationFormComponent } from '../vaccination-form/vaccination-form.component';
import { PatientService } from 'src/app/core/_services/patient.service';
import { VaccinationComparePipe } from '../../_pipes/vaccination-compare.pipe';

@Component({
  selector: 'app-vaccination-received-table',
  templateUrl: './vaccination-received-table.component.html',
  styleUrls: ['./vaccination-received-table.component.scss'],
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({height: '0px', minHeight: '0'})),
      state('expanded', style({height: '*'})),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ],
})
export class VaccinationReceivedTableComponent implements AfterViewInit {
  private codeBaseMap!: CodeBaseMap;

  columns: (keyof VaccinationEvent | keyof Vaccine  | "alerts")[] = [
    "vaccineCvxCode",
    "administeredDate",
    "lotNumber",
    "primarySource",
  ]

  @Input() title: string = 'Vaccinations received'
  differences: any = ''
  loading = false;

  expandedElement: VaccinationEvent | null = null;
  dataSource = new MatTableDataSource<VaccinationEvent>([]);
  @Input()
  set vaccinations(values: VaccinationEvent[]) {
    this.loading = false
    this.dataSource.data = values;
    this.expandedElement = values.find((vaccinationEvent: VaccinationEvent) => {return vaccinationEvent.id == this.expandedElement?.id}) ?? null
  }

  private _vaccinationToCompare!: VaccinationEvent | null;
  @Input()
  public get vaccinationToCompare(): VaccinationEvent | null {
    return this._vaccinationToCompare;
  }
  public set vaccinationToCompare(value: VaccinationEvent | null) {
    this._vaccinationToCompare = value;
    this.updateDifferences()
  }

  private _patientId: number = -1;
  @Input()
  public set patientId(value: number | undefined) {
    this._patientId = value ?? -1;
    this.vaccinations = []
  }
  public get patientId(): number {
    return this._patientId;
  }

  constructor(private dialog: MatDialog,
    public codeMapsService: CodeMapsService,
    public vaccinationService: VaccinationService,
    public patientService: PatientService,
    public vaccinationComparePipe: VaccinationComparePipe) { }

  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }

  vaccinationFilterPredicate() {
    return (data: VaccinationEvent, filter: string): boolean => {
      if (JSON.stringify(data).trim().toLowerCase().indexOf(filter) !== -1) {
        return true
      }
      if (data.vaccine["vaccineCvxCode"] &&
        JSON.stringify(this.codeBaseMap["VACCINATION_CVX_CODE"][data.vaccine["vaccineCvxCode"]])
          .trim().toLowerCase().indexOf(filter) !== -1) {
        return true
      }
      return false
    }
  }

  ngAfterViewInit(): void {
    this.codeMapsService.getObservableCodeBaseMap().subscribe((codeBaseMap) => {
      this.codeBaseMap = codeBaseMap
    });
    // Set filter rules for research
    this.dataSource.filterPredicate = this.vaccinationFilterPredicate()
  }

  merge(element: VaccinationEvent) {
    element.id = this.vaccinationToCompare?.id
    const dialogRef = this.dialog.open(VaccinationFormComponent, {
      maxWidth: '98vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'dialog-with-bar',
      data: {patientId: this.patientId, vaccination: element, comparedVaccination: this.vaccinationToCompare},
    });
    dialogRef.afterClosed().subscribe(result => {
      this.patientService.doRefresh()
    });
  }

  selectElement(element: VaccinationEvent | null) {
    this.expandedElement = this.expandedElement === element ? null : element
    this.updateDifferences()
  }

  isMatch(element: VaccinationEvent | null): boolean {
     return (this.expandedElement && this.differences == 'MATCH') ? true : false;
  }

  private updateDifferences() {
    if (this._vaccinationToCompare && this.expandedElement) {
      this.differences = this.vaccinationComparePipe.transform(this.expandedElement, this._vaccinationToCompare)
    } else {
      this.differences = null
    }
  }

}
