import { trigger, state, style, transition, animate } from '@angular/animations';
import { Component, Input, OnInit } from '@angular/core';
import { merge, startWith, tap } from 'rxjs';
import { Facility, EhrSubscription } from 'src/app/core/_model/rest';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { SubscriptionService } from 'src/app/fhir/_services/subscription.service';
import { FhirService } from '../../_services/fhir.service';
import { MatDialog } from '@angular/material/dialog';
import { MatTableDataSource } from '@angular/material/table';

@Component({
  selector: 'app-subscription-table',
  templateUrl: './subscription-table.component.html',
  styleUrls: ['./subscription-table.component.css'],
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({height: '0px', minHeight: '0'})),
      state('expanded', style({height: '*'})),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ],
})
export class SubscriptionTableComponent implements OnInit {
  @Input() facility: Facility = {id: -1};
  @Input() title: string = 'Registered Subscriptions'
  loading: boolean = false
  columns: (keyof EhrSubscription | "event_since_start")[] = [
    "name", "status", "identifier","notificationUrlLocation"
    , "event_since_start"
  ]

  dataSource = new MatTableDataSource<EhrSubscription>([]);
  expandedElement: EhrSubscription | null = null;

  constructor(
    private dialog: MatDialog,
    private facilityService: FacilityService,
    private subscriptionService: SubscriptionService,
    ) { }

  ngOnInit(): void {
    this.ngOnChanges()
  }
  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }
  ngAfterViewInit(): void {
    // Set filter rules for research
    this.dataSource.filterPredicate = (data: EhrSubscription | number, filter: string) =>{
      return JSON.stringify(data).trim().toLowerCase().indexOf(filter) !== -1
    };
  }

  ngOnChanges(): void {
    merge(this.subscriptionService.getRefresh(),
      this.facilityService.getObservableFacility().pipe(tap(facility =>{this.facility = facility}))
    ).pipe(startWith(tap(() => this.loading = true)))
    .subscribe(() => {
      this.subscriptionService.readSubscription().pipe(tap()).subscribe(res => {
        this.loading = false
        if (res) {
          this.dataSource.data = [res];
        } else {
          this.dataSource.data = [];
        }
      })
    })
  }

  onSelection(event: EhrSubscription) {
    if (this.expandedElement && this.expandedElement.identifier == event.identifier){
      this.expandedElement = null
    } else {
      this.expandedElement = event
    }
  }


}
