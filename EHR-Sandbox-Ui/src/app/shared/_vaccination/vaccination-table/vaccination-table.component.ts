import { animate, state, style, transition, trigger } from '@angular/animations';
import { AfterViewInit, Component, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { merge, tap } from 'rxjs';
import { VaccinationEvent, Vaccine } from 'src/app/core/_model/rest';
import { CodeBaseMap } from 'src/app/core/_model/structure';
import { CodeMapsService } from 'src/app/core/_services/code-maps.service';
import { PatientService } from 'src/app/core/_services/patient.service';
import { VaccinationService } from 'src/app/core/_services/vaccination.service';
import { VaccinationFormComponent } from '../vaccination-form/vaccination-form.component';
import { FeedbackTableComponent } from 'src/app/shared/_components/feedback-table/feedback-table.component';
import { VaccinationDashboardComponent } from '../vaccination-dashboard/vaccination-dashboard.component';
import { MatTableDataSource } from '@angular/material/table';

@Component({
  selector: 'app-vaccination-table',
  templateUrl: './vaccination-table.component.html',
  styleUrls: ['./vaccination-table.component.css'],
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({height: '0px', minHeight: '0'})),
      state('expanded', style({height: '*'})),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ],
})
export class VaccinationTableComponent implements AfterViewInit  {
  private codeBaseMap!: CodeBaseMap;

  columns: (keyof VaccinationEvent | keyof Vaccine  | "alerts")[] = [
    "vaccineCvxCode",
    "administeredDate",
    "lotNumber",
    "primarySource",
    "alerts",
  ]

  // Allows Date type casting in HTML template
  asDate(val: any) : Date { return val; }

  @Input() title: string = 'Vaccination history'
  @Input() allowCreation: boolean = true
  dataSource = new MatTableDataSource<VaccinationEvent>([]);
  loading = false;

  expandedElement: VaccinationEvent | null = null;
  @Output()
  selectedEmitter = new EventEmitter<VaccinationEvent | null>();

  private listManuallySet = false
  @Input()
  set vaccinations(values: VaccinationEvent[]) {
    this.listManuallySet = true
    this.setVaccinations(values)
  }
  private setVaccinations(values: VaccinationEvent[]) {
    this.loading = false
    this.dataSource.data = values;
    this.expandedElement = values.find((vaccinationEvent: VaccinationEvent) => {return vaccinationEvent.id == this.expandedElement?.id}) ?? null
  }

  selectElement(element: VaccinationEvent) {
    this.expandedElement = this.expandedElement === element ? null : element
    this.selectedEmitter.emit(this.expandedElement);
  }

  private _patientId: number = -1;
  @Input()
  public set patientId(value: number | undefined) {
    this._patientId = value ?? -1;
    if (this.listManuallySet == false) {
      this.loading = true
      this.vaccinationService.quickReadVaccinations(this.patientId).subscribe((res) => {
        this.setVaccinations(res)
      })
    }
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
    this.vaccinationService.getRefresh().subscribe(() => {
      this.patientId = this.patientId // trigger refresh through setter
    })
  }

  openCreation() {
    const dialogRef = this.dialog.open(VaccinationFormComponent, {
      maxWidth: '99vw',
      maxHeight: '95vh',
      minHeight: 'fit-content',
      width: '90%',
      panelClass: 'dialog-with-bar',
      data: {patientId: this.patientId},
    });
    dialogRef.afterClosed().subscribe(result => {
      this.ngAfterViewInit()
    });
  }


  openFeedback(element: VaccinationEvent) {
    const dialogRef = this.dialog.open(FeedbackTableComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'dialog-with-bar',
      data: {vaccination: element},
    });
  }

  openVaccination(vaccination: VaccinationEvent | number){
    const dialogRef = this.dialog.open(VaccinationDashboardComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'dialog-with-bar',
      data: {vaccination: vaccination},
    });
  }

}
