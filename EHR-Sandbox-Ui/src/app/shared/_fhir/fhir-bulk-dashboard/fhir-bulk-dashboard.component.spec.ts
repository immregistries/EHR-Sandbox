import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FhirBulkDashboardComponent } from './fhir-bulk-dashboard.component';

describe('FhirBulkDashboardComponent', () => {
  let component: FhirBulkDashboardComponent;
  let fixture: ComponentFixture<FhirBulkDashboardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FhirBulkDashboardComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FhirBulkDashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
