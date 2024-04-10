import { animate, state, style, transition, trigger } from '@angular/animations';
import { AfterViewInit, Component, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { VaccinationEvent, Vaccine } from 'src/app/core/_model/rest';
import { CodeBaseMap } from 'src/app/core/_model/structure';
import { CodeMapsService } from 'src/app/core/_services/code-maps.service';
import { VaccinationService } from 'src/app/core/_services/vaccination.service';
import { VaccinationFormComponent } from '../vaccination-form/vaccination-form.component';
import { FeedbackTableComponent } from 'src/app/shared/_data-quality-issues/feedback-table/feedback-table.component';
import { VaccinationDashboardComponent } from '../vaccination-dashboard/vaccination-dashboard.component';
import { AbstractDataTableComponent } from '../../_components/abstract-data-table/abstract-data-table.component';
import { PatientService } from 'src/app/core/_services/patient.service';

@Component({
  selector: 'app-vaccination-table',
  templateUrl: './vaccination-table.component.html',
  styleUrls: ['./vaccination-table.component.css']
})
export class VaccinationTableComponent extends AbstractDataTableComponent<VaccinationEvent> implements AfterViewInit {
  private codeBaseMap!: CodeBaseMap;

  columns: (keyof VaccinationEvent | keyof Vaccine | "alerts")[] = [
    "vaccineCvxCode",
    "administeredDate",
    "lotNumber",
    "primarySource",
    "alerts",
  ]

  @Input() title: string = 'Vaccination history'

  @Input()
  patientId!: number;

  constructor(private dialog: MatDialog,
    public codeMapsService: CodeMapsService,
    private vaccinationService: VaccinationService,
    private patientService: PatientService) {
      super()
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

  override ngAfterViewInit(): void {
    this.codeMapsService.getObservableCodeBaseMap().subscribe((codeBaseMap) => {
      this.codeBaseMap = codeBaseMap
    });
    super.ngAfterViewInit()

    // Set filter rules for research
    this.dataSource.filterPredicate = this.vaccinationFilterPredicate()
    this.vaccinationService.getRefresh().subscribe(() => {
    })
  }

  openCreation() {
    const dialogRef = this.dialog.open(VaccinationFormComponent, {
      maxWidth: '99vw',
      maxHeight: '95vh',
      minHeight: 'fit-content',
      width: '90%',
      panelClass: 'dialog-with-bar',
      data: { patientId: this.patientId },
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.ngAfterViewInit()
        this.onSelection(result)
      }
    });
  }

  openFeedback(element: VaccinationEvent) {
    const dialogRef = this.dialog.open(FeedbackTableComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'dialog-with-bar',
      data: { vaccination: element },
    });
  }

  openVaccination(vaccination: VaccinationEvent | number) {
    const dialogRef = this.dialog.open(VaccinationDashboardComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'dialog-with-bar',
      data: { vaccination: vaccination },
    });
  }

  populate() {
    if (this.patientId) {
      this.patientService.populatePatient(this.patientId).subscribe((res) => {
        this.patientService.doRefresh()
      })
    }
  }

}
