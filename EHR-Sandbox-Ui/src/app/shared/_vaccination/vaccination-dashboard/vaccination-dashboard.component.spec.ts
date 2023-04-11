import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VaccinationDashboardComponent } from './vaccination-dashboard.component';

describe('VaccinationDashboardComponent', () => {
  let component: VaccinationDashboardComponent;
  let fixture: ComponentFixture<VaccinationDashboardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ VaccinationDashboardComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VaccinationDashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
