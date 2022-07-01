import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VaccinationDashboardDialogComponent } from './vaccination-dashboard-dialog.component';

describe('VaccinationDashboardDialogComponent', () => {
  let component: VaccinationDashboardDialogComponent;
  let fixture: ComponentFixture<VaccinationDashboardDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ VaccinationDashboardDialogComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VaccinationDashboardDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
