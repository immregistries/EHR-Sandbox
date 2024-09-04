import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FhirQrCodeComponent } from './fhir-qr-code.component';

describe('FhirQrCodeComponent', () => {
  let component: FhirQrCodeComponent;
  let fixture: ComponentFixture<FhirQrCodeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FhirQrCodeComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(FhirQrCodeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
