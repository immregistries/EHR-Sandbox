import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VaccinationDisplayComponent } from './vaccination-display.component';

describe('VaccinationDisplayComponent', () => {
  let component: VaccinationDisplayComponent;
  let fixture: ComponentFixture<VaccinationDisplayComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ VaccinationDisplayComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(VaccinationDisplayComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
