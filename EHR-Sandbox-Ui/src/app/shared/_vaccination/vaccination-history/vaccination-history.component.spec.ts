import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VaccinationHistoryComponent } from './vaccination-history.component';

describe('VaccinationHistoryComponent', () => {
  let component: VaccinationHistoryComponent;
  let fixture: ComponentFixture<VaccinationHistoryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ VaccinationHistoryComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VaccinationHistoryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
