import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FhirV2CompareComponent } from './fhir-v2-compare.component';

describe('FhirV2CompareComponent', () => {
  let component: FhirV2CompareComponent;
  let fixture: ComponentFixture<FhirV2CompareComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FhirV2CompareComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(FhirV2CompareComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
