import { animate, state, style, transition, trigger } from '@angular/animations';
import { DatePipe } from '@angular/common';
import { Component, Input, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatTableDataSource } from '@angular/material/table';
import { ImmunizationRecommendationRecommendation } from 'fhir/r5';
import { AbstractDataTableComponent } from 'src/app/shared/_components/abstract-data-table/abstract-data-table.component';
import { VaccinationFormComponent } from '../../vaccination-form/vaccination-form.component';
import { PatientService } from 'src/app/core/_services/patient.service';
import { VaccinationEvent } from 'src/app/core/_model/rest';

@Component({
  selector: 'app-recommendation-component-table',
  templateUrl: './recommendation-component-table.component.html',
  styleUrls: ['./recommendation-component-table.component.css'],
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({ height: '0px', minHeight: '0' })),
      state('expanded', style({ height: '*' })),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ],
})
export class RecommendationComponentTableComponent extends AbstractDataTableComponent<ImmunizationRecommendationRecommendation> implements OnInit {

  columns: (keyof ImmunizationRecommendationRecommendation | "button")[] = [
    "vaccineCode",
    "forecastStatus",
    "dateCriterion",
    "button",
  ]

  constructor(private datePipe: DatePipe,
    private dialog: MatDialog,
    private patientService: PatientService,
  ) { super() }

  ngOnInit(): void {
    this.dataSource.filterPredicate = (data, filter) => {
      return JSON.stringify(data).includes(filter)
    }
  }

  override ngAfterViewInit(): void {
    super.ngAfterViewInit();
    this.dataSource.sortingDataAccessor = (data: ImmunizationRecommendationRecommendation, sortHeaderId: string) => {
      if (sortHeaderId === "vaccineCode") {
        return this.printVaccineCode(data)
      }
      if (sortHeaderId === "forecastStatus") {
        return this.printForecastStatus(data)
      }
      // if (sortHeaderId === "dateCriterion") {
      // }
      //@ts-ignore
      return data[sortHeaderId]
    }
  }

  printVaccineCode(element: ImmunizationRecommendationRecommendation): string {
    return element?.vaccineCode && element?.vaccineCode[0].coding ?
      element.vaccineCode[0].coding[0].display
      + ' (' + (element.vaccineCode[0].coding[0].code ?? "-None-") + ')' : "N/A"
  }

  extractVaccineCode(element: ImmunizationRecommendationRecommendation): string {
    return element?.vaccineCode && element?.vaccineCode[0].coding ? (element.vaccineCode[0].coding[0].code ?? "") : ""
  }

  extractForecastStatus(element: ImmunizationRecommendationRecommendation): string {
    return element?.forecastStatus?.coding ? (element.forecastStatus.coding[0].code ?? "") : ""
  }

  printForecastStatus(element: ImmunizationRecommendationRecommendation): string {
    return element?.forecastStatus?.coding ? element.forecastStatus.coding[0].display
      + ' (' + (element.forecastStatus.coding[0].code ?? "-None-") + ')' : "N/A"
  }

  /**
   *
   * @param element Currently not used
   * @returns
   */
  printDates(element: ImmunizationRecommendationRecommendation): string {
    let disp = "";
    let prevDate: string | null = null;
    if (element.dateCriterion) {
      for (const dateC of element.dateCriterion) {
        let currentDate = this.datePipe.transform(dateC.value, 'shortDate')
        if (dateC.code.coding) {
          if (currentDate != prevDate) {
            disp += "\n" + currentDate + ": "
          } else {
            disp += ", "
          }
          disp += dateC.code?.coding[0].display

        }
        prevDate = currentDate
      }
    }
    return disp;
  }

  openCreation(element: ImmunizationRecommendationRecommendation) {
    let vaccination: VaccinationEvent = {
      id: -1,
      vaccine: {
        vaccineCvxCode: this.extractVaccineCode(element),
        createdDate: new Date(),
        updatedDate: new Date(),
        administeredDate: new Date(),
        informationSource: '00',
      },
      primarySource: true,
    }
    const dialogRef = this.dialog.open(VaccinationFormComponent, {
      maxWidth: '99vw',
      maxHeight: '95vh',
      minHeight: 'fit-content',
      width: '90%',
      panelClass: 'dialog-with-bar',
      data: { patientId: this.patientService.getCurrentId(), vaccination: vaccination },
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.patientService.doRefresh()
      }
    });
  }


}
