import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FhirBulkComponent } from './fhir-bulk.component';

describe('FhirBulkComponent', () => {
  let component: FhirBulkComponent;
  let fixture: ComponentFixture<FhirBulkComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FhirBulkComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FhirBulkComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
