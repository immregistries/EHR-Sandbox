import { animate, state, style, transition, trigger } from '@angular/animations';
import { Component, Input, OnInit } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { ImmunizationRecommendationRecommendation } from 'fhir/r5';

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
export class RecommendationComponentTableComponent implements OnInit {

  @Input()
  components!: ImmunizationRecommendationRecommendation[];

  columns: (keyof ImmunizationRecommendationRecommendation)[] = [
    "vaccineCode",
    "dateCriterion",
  ]
  constructor() { }

  ngOnInit(): void {
    this.dataSource.filterPredicate = (data, filter) => {
      return JSON.stringify(data).includes(filter)
    }
  }

  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }

  ngOnChanges() {
    this.dataSource.data = this.components
  }

  dataSource = new MatTableDataSource<ImmunizationRecommendationRecommendation>([]);
  expandedElement: ImmunizationRecommendationRecommendation | null = null;
  loading = false;

}
