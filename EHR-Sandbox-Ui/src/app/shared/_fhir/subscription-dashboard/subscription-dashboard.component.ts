import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, ViewChild } from '@angular/core';
import { MatTabGroup } from '@angular/material/tabs';
import { merge } from 'rxjs';
import { EhrSubscription } from 'src/app/core/_model/rest';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { ImmunizationRegistryService } from 'src/app/core/_services/immunization-registry.service';
import { SubscriptionService } from 'src/app/core/_services/_fhir/subscription.service';

@Component({
  selector: 'app-subscription-dashboard',
  templateUrl: './subscription-dashboard.component.html',
  styleUrls: ['./subscription-dashboard.component.css']
})
export class SubscriptionDashboardComponent implements OnInit {

  constructor(public subscriptionService: SubscriptionService,
    public facilityService: FacilityService,
    public registryService: ImmunizationRegistryService) { }

  subscription?: EhrSubscription;
  error: string = "";
  loading: boolean = false;
  sample!: string;

  @ViewChild('tabs', {static: false}) tabGroup!: MatTabGroup;

  ngAfterViewInit(): void {
    this.tabGroup.selectedIndex = 1
  }

  ngOnInit(): void {
    this.subscriptionService.readSample().subscribe((res) => {
      this.sample = JSON.stringify(res,undefined,4)
    })
    merge(
      this.facilityService.getCurrentObservable(),
      this.registryService.getCurrentObservable(),
    ).subscribe(res => {
      this.subscriptionService.readSubscription().subscribe({
        next: res => {
          this.subscription = res
          this.subscriptionService.doRefresh()
          this.error = ""
        },
        error: err => {
          this.error = `${err.status} : ${err.error}`
        }})
    })

  }

  subscribeToIIS() {
    this.loading = true
    this.subscriptionService.createSubscription().subscribe({
      next: (res) => {
        this.loading = false
        this.ngOnInit()
      },
      error: (err: HttpErrorResponse) => {
        this.loading = false
        this.error = `${err.status} : ${err.statusText}`
      }})
  }

}
