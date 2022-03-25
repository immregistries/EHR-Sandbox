import { animate, state, style, transition, trigger } from '@angular/animations';
import { AfterViewInit, Component, Input, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { merge, startWith, switchMap } from 'rxjs';
import { VaccinationEvent, Vaccine } from 'src/app/_model/rest';
import { Code } from 'src/app/_model/structure';
import { CodeMapsService } from 'src/app/_services/code-maps.service';
import { PatientService } from 'src/app/_services/patient.service';
import { VaccinationService } from 'src/app/_services/vaccination.service';
import { Hl7MessagingComponent } from '../../_dialogs/hl7-messaging/hl7-messaging.component';
import { VaccinationCreationComponent } from '../../_dialogs/vaccination-creation/vaccination-creation.component';

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
  private codeBaseMap!:  {[key:string]: {[key:string]: Code}};

  columns: (keyof VaccinationEvent | keyof Vaccine )[] = [
    "vaccineCvxCode",
    "administeredDate",
    "lotNumber",
  ]

  // Allows Date type casting in HTML template
  asDate(val: any) : Date { return val; }

  dataSource = new MatTableDataSource<VaccinationEvent>([]);
  expandedElement: VaccinationEvent | null = null;

  @Input() patientId: number= -1

  resultsLength = 0;
  isLoadingResults = true;

  constructor(private dialog: MatDialog,
    public codeMapsService: CodeMapsService,
    private vaccinationService: VaccinationService,
    private patientService: PatientService) { }

  refreshCodeMaps() {
    this.codeMapsService.getObservableCodeBaseMap().subscribe((codeBaseMap) => {
      this.codeBaseMap = codeBaseMap
    });
  }

  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }

  ngAfterViewInit(): void {
    this.refreshCodeMaps()
    // Set filter rules for research
    this.dataSource.filterPredicate = (data: VaccinationEvent, filter: string) =>{
      return JSON.stringify(data).trim().toLowerCase().indexOf(filter) !== -1
    };
    this.patientService.getObservablePatient().subscribe((patient) => {
      if (patient.id) {
        this.patientId = patient.id
        this.vaccinationService.quickReadVaccinations(this.patientId).subscribe((res) => {
          this.dataSource.data = res
        })
      }
    })
    this.vaccinationService.quickReadVaccinations(this.patientId).subscribe((res) => {
      this.dataSource.data = res
    })
  }

  openCreation() {
    const dialogRef = this.dialog.open(VaccinationCreationComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '90%',
      panelClass: 'full-screen-modal',
      data: {patientId: this.patientId},
    });
    dialogRef.afterClosed().subscribe(result => {
      this.ngAfterViewInit()
    });
  }

  openEdition(element: VaccinationEvent) {
    const dialogRef = this.dialog.open(VaccinationCreationComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'full-screen-modal',
      data: {patientId: this.patientId, vaccination: element},
    });
    dialogRef.afterClosed().subscribe(result => {
      this.ngAfterViewInit()
    });
  }

  openHl7(element: VaccinationEvent) {
    const dialogRef = this.dialog.open(Hl7MessagingComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'full-screen-modal',
      data: {patientId: this.patientId, vaccinationId: element.id},
    });
  }

}
