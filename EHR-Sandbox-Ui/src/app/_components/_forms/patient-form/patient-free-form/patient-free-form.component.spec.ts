import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PatientFreeFormComponent } from './patient-free-form.component';

describe('PatientFreeFormComponent', () => {
  let component: PatientFreeFormComponent;
  let fixture: ComponentFixture<PatientFreeFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PatientFreeFormComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PatientFreeFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
