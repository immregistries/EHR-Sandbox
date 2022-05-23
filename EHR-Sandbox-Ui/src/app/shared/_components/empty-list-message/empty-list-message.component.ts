import { Component, OnInit } from '@angular/core';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { TenantService } from 'src/app/core/_services/tenant.service';

@Component({
  selector: 'app-empty-list-message',
  templateUrl: './empty-list-message.component.html',
  styleUrls: ['./empty-list-message.component.css']
})
export class EmptyListMessageComponent implements OnInit {

  constructor(
    public tenantService: TenantService,
    public facilityService: FacilityService,
    ) { }

  ngOnInit(): void {
  }

}
