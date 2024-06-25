import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PatientJsonFormComponent } from './patient-json-form.component';

describe('PatientJsonFormComponent', () => {
  let component: PatientJsonFormComponent;
  let fixture: ComponentFixture<PatientJsonFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PatientJsonFormComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PatientJsonFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
