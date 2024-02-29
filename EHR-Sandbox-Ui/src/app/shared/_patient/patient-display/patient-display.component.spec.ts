import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PatientDisplayComponent } from './patient-display.component';

describe('PatientDisplayComponent', () => {
  let component: PatientDisplayComponent;
  let fixture: ComponentFixture<PatientDisplayComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PatientDisplayComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PatientDisplayComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
