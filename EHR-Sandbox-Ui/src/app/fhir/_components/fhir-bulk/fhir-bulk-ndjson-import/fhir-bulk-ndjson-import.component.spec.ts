import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FhirBulkNdjsonImportComponent } from './fhir-bulk-ndjson-import.component';

describe('FhirBulkNdjsonImportComponent', () => {
  let component: FhirBulkNdjsonImportComponent;
  let fixture: ComponentFixture<FhirBulkNdjsonImportComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FhirBulkNdjsonImportComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FhirBulkNdjsonImportComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
