import { Component, OnInit } from '@angular/core';
import { FhirService } from '../../_services/fhir.service';

@Component({
  selector: 'app-subscription-dashboard',
  templateUrl: './subscription-dashboard.component.html',
  styleUrls: ['./subscription-dashboard.component.css']
})
export class SubscriptionDashboardComponent implements OnInit {

  constructor(private fhir: FhirService) { }

  ngOnInit(): void {
  }

  subscribeToIIS() {
    console.log("subscribe")
    this.fhir.createSubscription().subscribe(res => {
      console.log(res)
    })
  }

}
