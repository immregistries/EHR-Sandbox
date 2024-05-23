import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FhirBulkNdjsonManualComponent } from './fhir-bulk-ndjson-manual.component';

describe('FhirBulkNdjsonManualComponent', () => {
  let component: FhirBulkNdjsonManualComponent;
  let fixture: ComponentFixture<FhirBulkNdjsonManualComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FhirBulkNdjsonManualComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FhirBulkNdjsonManualComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
