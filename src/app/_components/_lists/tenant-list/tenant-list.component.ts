import { Component, Input, OnInit } from '@angular/core';
import { Tenant } from 'src/app/_model/rest';

@Component({
  selector: 'app-tenant-list',
  templateUrl: './tenant-list.component.html',
  styleUrls: ['./tenant-list.component.css']
})
export class TenantListComponent implements OnInit {

  @Input() tenantList?: Tenant[];

  constructor() { }

  ngOnInit(): void {
    // this.tenant
  }
}
