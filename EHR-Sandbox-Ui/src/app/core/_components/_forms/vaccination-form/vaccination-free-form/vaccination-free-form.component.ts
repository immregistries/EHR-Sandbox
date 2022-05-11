import { Component, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import { NgForm } from '@angular/forms';
import { Subscription } from 'rxjs';
import { VaccinationEvent } from 'src/app/core/_model/rest';
import { FormCard } from 'src/app/core/_model/structure';

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

  @ViewChild('form', { static: true }) vaccinationFreeForm!: NgForm;


  constructor() { }

  private formChangesSubscription!: Subscription
  ngOnInit(): void {
    this.formChangesSubscription = this.vaccinationFreeForm.form.valueChanges.subscribe(x => {
      // console.log(x);
    })
  }

  ngOnDestroy() {
    this.formChangesSubscription.unsubscribe();
  }

}
