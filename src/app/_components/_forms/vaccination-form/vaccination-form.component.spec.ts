import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VaccinationFormComponent } from './vaccination-form.component';

describe('VaccinationFormComponent', () => {
  let component: VaccinationFormComponent;
  let fixture: ComponentFixture<VaccinationFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ VaccinationFormComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VaccinationFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
