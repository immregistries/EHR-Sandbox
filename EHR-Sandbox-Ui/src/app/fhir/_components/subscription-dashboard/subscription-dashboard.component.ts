import { Component, OnInit } from '@angular/core';
import { SubscriptionStore } from 'src/app/core/_model/rest';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { SubscriptionService } from 'src/app/fhir/_services/subscription.service';
import { FhirService } from '../../_services/fhir.service';

@Component({
  selector: 'app-subscription-dashboard',
  templateUrl: './subscription-dashboard.component.html',
  styleUrls: ['./subscription-dashboard.component.css']
})
export class SubscriptionDashboardComponent implements OnInit {

  constructor(private fhir: FhirService,
    private subscriptionService: SubscriptionService,
    public facilityService: FacilityService) { }

  subscription?: SubscriptionStore;
  error: string = "";

  ngOnInit(): void {
    this.facilityService.getRefresh().subscribe(res => {
      this.subscriptionService.readSubscription().subscribe(res => {
        this.subscription = res
        this.subscriptionService.doRefresh()
        this.error = ""
      },
      err => {
        this.error = `${err.status} : ${err.error}`
      })
    })
  }

  subscribeToIIS() {
    console.log("subscribe")
    this.subscriptionService.createSubscription().subscribe(res => {
      this.ngOnInit()
    })
  }

}
