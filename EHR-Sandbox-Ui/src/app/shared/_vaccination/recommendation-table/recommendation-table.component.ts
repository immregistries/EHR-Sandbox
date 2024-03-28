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
import { AbstractDataTableComponent } from '../../_components/abstract-data-table/abstract-data-table.component';

@Component({
  selector: 'app-recommendation-table',
  templateUrl: './recommendation-table.component.html',
  styleUrls: ['./recommendation-table.component.css'],
})
export class RecommendationTableComponent extends AbstractDataTableComponent<ImmunizationRecommendation> implements OnInit {

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
    private patientService: PatientService) {
      super()
    }

  ngOnInit(): void {

  }

  expandedElement: ImmunizationRecommendation | null = null;

  @Input() patientId: number = -1

  // ngAfterViewInit(): void {
  //   // Set filter rules for research
  //   // this.dataSource.filterPredicate = this.vaccinationFilterPredicate()

  //   merge(
  //     // this.vaccinationService.getRefresh(),
  //     this.patientService.getCurrentObservable().pipe(tap((patient) => {this.patientId = patient.id? patient.id : -1}))
  //   ).subscribe(() => {
  //     this.loading = true
  //     this.recommendationService.readRecommendations(this.patientId).subscribe((res) => {
  //       this.loading = false
  //       this.dataSource.data = res
  //       this.expandedElement = res.find((reco: ImmunizationRecommendation) => {return reco.id == this.expandedElement?.id}) ?? null
  //     })
  //   })
  // }

}
