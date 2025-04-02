import { Component, Input, OnInit } from '@angular/core';
import { animate, state, style, transition, trigger } from '@angular/animations';
import { MatTableDataSource } from '@angular/material/table';
import { MatDialog } from '@angular/material/dialog';
import { CodeMapsService } from 'src/app/core/_services/code-maps.service';
import { VaccinationService } from 'src/app/core/_services/vaccination.service';
import { PatientService } from 'src/app/core/_services/patient.service';
import { CodeBaseMap } from "src/app/core/_model/code-base-map";
import { merge, tap } from 'rxjs';
import { RecommendationService } from 'src/app/core/_services/recommendation.service';
import { ImmunizationRecommendation } from 'fhir/r5';
import { AbstractDataTableComponent } from '../../_components/abstract-data-table/abstract-data-table.component';
import { RecommendationDownloadComponent } from '../recommendation-download/recommendation-download.component';

@Component({
  selector: 'app-recommendation-table',
  templateUrl: './recommendation-table.component.html',
  styleUrls: ['./recommendation-table.component.css'],
})
export class RecommendationTableComponent extends AbstractDataTableComponent<ImmunizationRecommendation> implements OnInit {

  private _patientId: number = -1;
  public get patientId(): number {
    return this._patientId;
  }
  @Input()
  public set patientId(value: number) {
    this._patientId = value;
    // if (this.patientId)
    //   this.recommendationService.doRefresh()
    // this.refreshReco();
  }

  columns: (keyof ImmunizationRecommendation)[] = [
    // "identifier",
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

  private refreshReco() {
    // this.loading = true;
    this.recommendationService.doRefresh()
  }

  ngOnInit(): void {
    // this.loading = true;
    // this.recommendationService.quickReadRecommendations().pipe(tap(() => this.loading = false)).subscribe((res) => {
    //   this.dataArray = res;
    //   this.selectedElement = res.find((reco: ImmunizationRecommendation) => { return reco.id == this.selectedElement?.id; }) ?? undefined;
    // });
  }


  openFetch() {
    this.dialog.open(RecommendationDownloadComponent, { data: { 'patientId': this.patientService.getCurrentId() } }).afterClosed().subscribe((res) => {
      if (res) {
        // this.patientService.doRefresh();
        // this.refreshReco()
      }
    });
  }
}
