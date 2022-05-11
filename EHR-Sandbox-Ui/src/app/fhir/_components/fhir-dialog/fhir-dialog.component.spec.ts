import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FhirDialogComponent } from './fhir-dialog.component';

describe('FhirDialogComponent', () => {
  let component: FhirDialogComponent;
  let fixture: ComponentFixture<FhirDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FhirDialogComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FhirDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
