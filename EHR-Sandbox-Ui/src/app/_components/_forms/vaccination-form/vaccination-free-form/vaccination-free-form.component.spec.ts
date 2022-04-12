import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VaccinationFreeFormComponent } from './vaccination-free-form.component';

describe('VaccinationFreeFormComponent', () => {
  let component: VaccinationFreeFormComponent;
  let fixture: ComponentFixture<VaccinationFreeFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ VaccinationFreeFormComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VaccinationFreeFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
