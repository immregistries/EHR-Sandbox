import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { VaccinationEvent } from 'src/app/_model/rest';
import { FormCard } from 'src/app/_model/structure';

@Component({
  selector: 'app-vaccination-free-form',
  templateUrl: './vaccination-free-form.component.html',
  styleUrls: ['./vaccination-free-form.component.css']
})
export class VaccinationFreeFormComponent implements OnInit {

  @Input()
  vaccination!: VaccinationEvent;
  @Output()
  vaccinationChange = new EventEmitter<VaccinationEvent>();

  @Input() formCards?: FormCard[]

  constructor() { }

  ngOnInit(): void {
  }

}
