import { ComponentFixture, TestBed } from '@angular/core/testing';

import { IdentifierListFormComponent } from './identifier-list-form.component';

describe('IdentifierListFormComponent', () => {
  let component: IdentifierListFormComponent;
  let fixture: ComponentFixture<IdentifierListFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ IdentifierListFormComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(IdentifierListFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
