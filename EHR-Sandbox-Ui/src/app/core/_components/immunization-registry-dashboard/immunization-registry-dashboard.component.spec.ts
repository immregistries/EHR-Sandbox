import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ImmunizationRegistryDashboardComponent } from './immunization-registry-dashboard.component';

describe('ImmunizationRegistryDashboardComponent', () => {
  let component: ImmunizationRegistryDashboardComponent;
  let fixture: ComponentFixture<ImmunizationRegistryDashboardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ImmunizationRegistryDashboardComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ImmunizationRegistryDashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
