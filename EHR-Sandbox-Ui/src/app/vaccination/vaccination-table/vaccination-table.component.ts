import { animate, state, style, transition, trigger } from '@angular/animations';
import { AfterViewInit, Component, Input, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatTableDataSource } from '@angular/material/table';
import { merge, startWith, switchMap } from 'rxjs';
import { VaccinationEvent, Vaccine } from 'src/app/core/_model/rest';
import { Code, CodeBaseMap } from 'src/app/core/_model/structure';
import { CodeMapsService } from 'src/app/core/_services/code-maps.service';
import { PatientService } from 'src/app/core/_services/patient.service';
import { VaccinationService } from 'src/app/core/_services/vaccination.service';
import { VaccinationCreationComponent } from '../vaccination-form/vaccination-creation/vaccination-creation.component';
import { FeedbackDialogComponent } from '../../shared/feedback-table/feedback-dialog/feedback-dialog.component';

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
    "alerts",
  ]

  // Allows Date type casting in HTML template
  asDate(val: any) : Date { return val; }

  dataSource = new MatTableDataSource<VaccinationEvent>([]);
  expandedElement: VaccinationEvent | null = null;

  @Input() patientId: number= -1
  @Input() title: string = 'Vaccination history'

  resultsLength = 0;
  isLoadingResults = true;

  constructor(private dialog: MatDialog,
    public codeMapsService: CodeMapsService,
    private vaccinationService: VaccinationService,
    private patientService: PatientService) { }

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

    this.vaccinationService.getRefresh().subscribe((refresh) => {
      this.patientService.getObservablePatient().subscribe((patient) => {
        if (patient.id) {
          this.patientId = patient.id
          this.vaccinationService.quickReadVaccinations(this.patientId).subscribe((res) => {
            this.dataSource.data = res
          })
        } else {
          this.dataSource.data = []
        }
      })
    })
    this.vaccinationService.quickReadVaccinations(this.patientId).subscribe((res) => {
      this.dataSource.data = res
    })
  }

  openCreation() {
    const dialogRef = this.dialog.open(VaccinationCreationComponent, {
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
    const dialogRef = this.dialog.open(FeedbackDialogComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'dialog-with-bar',
      data: {vaccination: element},
    });
  }

}
