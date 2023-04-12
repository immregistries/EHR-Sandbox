import { trigger, state, style, transition, animate } from '@angular/animations';
import { AfterViewInit, Component, Input, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatTableDataSource } from '@angular/material/table';
import { VaccinationEvent, Vaccine } from 'src/app/core/_model/rest';
import { CodeBaseMap } from 'src/app/core/_model/structure';
import { CodeMapsService } from 'src/app/core/_services/code-maps.service';
import { VaccinationService } from 'src/app/core/_services/vaccination.service';

@Component({
  selector: 'app-vaccination-received-table',
  templateUrl: './vaccination-received-table.component.html',
  styleUrls: ['./vaccination-received-table.component.css'],
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({height: '0px', minHeight: '0'})),
      state('expanded', style({height: '*', width: '80%'})),
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

  // Allows Date type casting in HTML template
  asDate(val: any) : Date { return val; }

  @Input() title: string = 'Vaccinations received'
  dataSource = new MatTableDataSource<VaccinationEvent>([]);
  expandedElement: VaccinationEvent | null = null;
  loading = false;

  @Input()
  vaccinationToCompare!: VaccinationEvent | null;

  @Input()
  set vaccinations(values: VaccinationEvent[]) {
    this.loading = false
    this.dataSource.data = values;
    this.expandedElement = values.find((vaccinationEvent: VaccinationEvent) => {return vaccinationEvent.id == this.expandedElement?.id}) ?? null
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
    private vaccinationService: VaccinationService) { }

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

}
