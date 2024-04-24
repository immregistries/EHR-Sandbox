import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VaccinationCompareComponent } from './vaccination-compare.component';

describe('VaccinationCompareComponent', () => {
  let component: VaccinationCompareComponent;
  let fixture: ComponentFixture<VaccinationCompareComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ VaccinationCompareComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(VaccinationCompareComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
