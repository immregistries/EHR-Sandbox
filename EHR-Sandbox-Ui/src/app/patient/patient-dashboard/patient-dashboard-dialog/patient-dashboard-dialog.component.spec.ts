import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PatientDashboardDialogComponent } from './patient-dashboard-dialog.component';

describe('PatientDashboardDialogComponent', () => {
  let component: PatientDashboardDialogComponent;
  let fixture: ComponentFixture<PatientDashboardDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PatientDashboardDialogComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PatientDashboardDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
