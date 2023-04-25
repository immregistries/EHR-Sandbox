import { Component, Input, OnInit } from '@angular/core';
import { ImmunizationRegistry } from '../../_model/rest';
import { ImmunizationRegistryService } from '../../_services/immunization-registry.service';

@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.css']
})
export class SettingsComponent implements OnInit {
  // imm!: ImmunizationRegistry

  constructor(private immRegistryService: ImmunizationRegistryService) { }

  ngOnInit(): void {
  }
}
