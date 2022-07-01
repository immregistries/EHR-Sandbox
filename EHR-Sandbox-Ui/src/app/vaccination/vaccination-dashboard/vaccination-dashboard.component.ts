import { Component, Input, OnInit } from '@angular/core';
import { VaccinationEvent } from 'src/app/core/_model/rest';

@Component({
  selector: 'app-vaccination-dashboard',
  templateUrl: './vaccination-dashboard.component.html',
  styleUrls: ['./vaccination-dashboard.component.css']
})
export class VaccinationDashboardComponent implements OnInit {
  @Input() vaccination!: VaccinationEvent

  constructor() { }

  ngOnInit(): void {
  }

}
