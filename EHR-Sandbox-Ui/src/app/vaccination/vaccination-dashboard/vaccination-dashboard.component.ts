import { Component, Input, OnInit } from '@angular/core';
import { VaccinationEvent } from 'src/app/core/_model/rest';
import { CodeMapsService } from 'src/app/core/_services/code-maps.service';

@Component({
  selector: 'app-vaccination-dashboard',
  templateUrl: './vaccination-dashboard.component.html',
  styleUrls: ['./vaccination-dashboard.component.css']
})
export class VaccinationDashboardComponent implements OnInit {
  @Input() vaccination!: VaccinationEvent


  constructor(public codeMapsService: CodeMapsService) { }

  ngOnInit(): void {
    this.codeMapsService.getCodeMap("cvx")
  }

}
