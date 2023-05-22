import { Component, Input, OnInit } from '@angular/core';
import { animate, state, style, transition, trigger } from '@angular/animations';
import { MatTableDataSource } from '@angular/material/table';
import { MatDialog } from '@angular/material/dialog';
import { CodeMapsService } from 'src/app/core/_services/code-maps.service';
import { VaccinationService } from 'src/app/core/_services/vaccination.service';
import { PatientService } from 'src/app/core/_services/patient.service';
import { CodeBaseMap } from 'src/app/core/_model/structure';
import { merge, tap } from 'rxjs';
import { RecommendationService } from 'src/app/core/_services/recommendation.service';
import { ImmunizationRecommendation } from 'fhir/r5';

@Component({
  selector: 'app-recommendation-table',
  templateUrl: './recommendation-table.component.html',
  styleUrls: ['./recommendation-table.component.css'],
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({height: '0px', minHeight: '0'})),
      state('expanded', style({height: '*'})),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ],
})
export class RecommendationTableComponent implements OnInit {

  columns: (keyof ImmunizationRecommendation)[] = [
    "identifier",
    "date",
    "authority",
    "recommendation"
  ]


  constructor(private dialog: MatDialog,
    public codeMapsService: CodeMapsService,
    private recommendationService: RecommendationService,
    private vaccinationService: VaccinationService,
    private patientService: PatientService) { }

  ngOnInit(): void {

  }

  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }

  dataSource = new MatTableDataSource<ImmunizationRecommendation>([]);
  expandedElement: ImmunizationRecommendation | null = null;
  loading = false;

  @Input() patientId: number = -1

  ngAfterViewInit(): void {
    // Set filter rules for research
    // this.dataSource.filterPredicate = this.vaccinationFilterPredicate()

    merge(
      // this.vaccinationService.getRefresh(),
      this.patientService.getCurrentObservable().pipe(tap((patient) => {this.patientId = patient.id? patient.id : -1}))
    ).subscribe(() => {
      this.loading = true
      this.recommendationService.readRecommendations(this.patientId).subscribe((res) => {
        this.loading = false
        this.dataSource.data = res
        this.expandedElement = res.find((reco: ImmunizationRecommendation) => {return reco.id == this.expandedElement?.id}) ?? null
      })
    })
  }

}
