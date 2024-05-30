import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PhoneListFormComponent } from './phone-list-form.component';

describe('PhoneListFormComponent', () => {
  let component: PhoneListFormComponent;
  let fixture: ComponentFixture<PhoneListFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PhoneListFormComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PhoneListFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
