import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddressListFormComponent } from './address-list-form.component';

describe('AddressListFormComponent', () => {
  let component: AddressListFormComponent;
  let fixture: ComponentFixture<AddressListFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AddressListFormComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AddressListFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
