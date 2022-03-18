import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Facility, VaccinationEvent, Tenant } from 'src/app/_model/rest';
import { FormCard, formType } from 'src/app/_model/structure';
import { Breakpoints, BreakpointObserver } from '@angular/cdk/layout';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-vaccination-form',
  templateUrl: './vaccination-form.component.html',
  styleUrls: ['./vaccination-form.component.css']
})
export class VaccinationFormComponent implements OnInit {

  @Input()
  vaccination: VaccinationEvent = {id: -1};
  @Output()
  vaccinationChange = new EventEmitter<VaccinationEvent>();

  constructor(private breakpointObserver: BreakpointObserver,
    private _snackBar: MatSnackBar) { }

  ngOnInit(): void {
  }

  // Allows Date type casting in HTML template
  asDate(val: any) : Date { return val; }
  formCards: FormCard[] = [
    {title: "Vaccine",rows: 1, cols: 1, forms: [
      {type: formType.text, title : "", attribute: "addressCity"}
    ]}
  ]

}
