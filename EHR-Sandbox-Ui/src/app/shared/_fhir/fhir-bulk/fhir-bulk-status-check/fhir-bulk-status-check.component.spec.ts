import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FhirBulkStatusCheckComponent } from './fhir-bulk-status-check.component';

describe('FhirBulkStatusCheckComponent', () => {
  let component: FhirBulkStatusCheckComponent;
  let fixture: ComponentFixture<FhirBulkStatusCheckComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FhirBulkStatusCheckComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FhirBulkStatusCheckComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
