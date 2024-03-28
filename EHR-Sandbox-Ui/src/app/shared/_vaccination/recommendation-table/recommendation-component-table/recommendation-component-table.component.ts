import { animate, state, style, transition, trigger } from '@angular/animations';
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
      state('collapsed', style({height: '0px', minHeight: '0'})),
      state('expanded', style({height: '*'})),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ],
})
export class RecommendationComponentTableComponent extends AbstractDataTableComponent<ImmunizationRecommendationRecommendation> implements OnInit {
  columns: (keyof ImmunizationRecommendationRecommendation)[] = [
    "vaccineCode",
    "dateCriterion",
  ]
  constructor() { super() }

  ngOnInit(): void {
    this.dataSource.filterPredicate = (data, filter) => {
      return JSON.stringify(data).includes(filter)
    }
  }
}
