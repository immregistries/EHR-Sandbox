import { animate, state, style, transition, trigger } from '@angular/animations';
import { DatePipe } from '@angular/common';
import { Component, Input, OnInit } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { ImmunizationRecommendationRecommendation } from 'fhir/r5';
import { AbstractDataTableComponent } from 'src/app/shared/_components/abstract-data-table/abstract-data-table.component';

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
  columns: (keyof ImmunizationRecommendationRecommendation)[] = [
    "vaccineCode",
    "forecastStatus",
    "dateCriterion",
  ]
  constructor(private datePipe: DatePipe) { super() }

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
}
