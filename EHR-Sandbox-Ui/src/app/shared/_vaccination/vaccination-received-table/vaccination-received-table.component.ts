import { trigger, state, style, transition, animate } from '@angular/animations';
import { AfterViewInit, Component, Input, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { VaccinationEvent, Vaccine } from 'src/app/core/_model/rest';
import { CodeBaseMap } from 'src/app/core/_model/structure';
import { CodeMapsService } from 'src/app/core/_services/code-maps.service';
import { VaccinationService } from 'src/app/core/_services/vaccination.service';
import { VaccinationFormComponent } from '../vaccination-form/vaccination-form.component';
import { PatientService } from 'src/app/core/_services/patient.service';
import { VaccinationComparePipe } from '../../_pipes/vaccination-compare.pipe';
import { MatTableDataSource } from '@angular/material/table';

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
export class VaccinationReceivedTableComponent implements OnInit, AfterViewInit {
  private codeBaseMap!: CodeBaseMap;

  columns: (keyof VaccinationEvent | keyof Vaccine  | "alerts" | "index")[] = [
    "vaccineCvxCode",
    "administeredDate",
    "lotNumber",
    "primarySource",
    // "index"
  ]

  @Input() title: string = 'Vaccinations received'
  differencesWithSelected: any = ''
  loading = false;

  expandedElement: VaccinationEvent | null = null;
  dataSource = new MatTableDataSource<VaccinationEvent>([]);
  // matchingMatrix: {[index: string]: string[]} = {}
  matchingMatrix: {}[][] = []


  private _localVaccinations!: VaccinationEvent[];
  @Input()
  set localVaccinations(values: VaccinationEvent[] | undefined) {
    this._localVaccinations = values ?? [];
    this.updateMatchingMatrix()
  }
  get localVaccinations() {return this._localVaccinations}


  @Input()
  set remoteVaccinations(values: VaccinationEvent[]) {
    this.loading = false
    this.dataSource.data = values;
    this.expandedElement = values.find((vaccinationEvent: VaccinationEvent) => {return vaccinationEvent.id == this.expandedElement?.id}) ?? null
    this.updateMatchingMatrix()
  }
  get remoteVaccinations(): VaccinationEvent[] {
    return this.dataSource.data
  }

  updateMatchingMatrix() {
    this.matchingMatrix = []
    this.remoteVaccinations?.forEach(element => {
      let length = this.matchingMatrix.push([]); // Adds new line and returns length
      this.localVaccinations?.forEach(local => {
        this.matchingMatrix[length -1].push(this.vaccinationComparePipe.transform(element,local))
      })
    });
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
    this.remoteVaccinations = []
  }
  public get patientId(): number {
    return this._patientId;
  }

  constructor(private dialog: MatDialog,
    public codeMapsService: CodeMapsService,
    public vaccinationService: VaccinationService,
    public patientService: PatientService,
    public vaccinationComparePipe: VaccinationComparePipe) { }
  ngOnInit(): void {
    this.patientService.getCurrentObservable().subscribe(patient => {
      this.vaccinationService.readVaccinations(patient?.id ?? -1).subscribe(res => {
        this.localVaccinations = res
      })
    })
  }

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

  openMerge(element: VaccinationEvent) {
    element.id = this.vaccinationToCompare ? this.vaccinationToCompare.id : undefined
    // element.primarySource = falset TODO talk about it in a meeting
    const dialogRef = this.dialog.open(VaccinationFormComponent, {
      maxWidth: '98vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'dialog-with-bar',
      data: {patientId: this.patientId, vaccination: element, comparedVaccination: this.vaccinationToCompare, changePrimarySourceToFalse: true},
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.vaccinationService.doRefresh()
      }
      // this.patientService.doRefresh()
    });
  }

  selectElement(element: VaccinationEvent | null) {
    this.expandedElement = this.expandedElement === element ? null : element
    this.updateDifferences()
  }

  isMatch(element: VaccinationEvent | null): boolean {
     return (this.expandedElement && this.differencesWithSelected == 'MATCH') ? true : false;
  }

  hasNoMatch(index: number): boolean {
     return !this.matchingMatrix[index]?.includes('MATCH');
  }

  private updateDifferences() {
    if (this._vaccinationToCompare && this.expandedElement) {
      this.differencesWithSelected = this.vaccinationComparePipe.transform(this.expandedElement, this._vaccinationToCompare)
    } else {
      this.differencesWithSelected = null
    }
  }

}
