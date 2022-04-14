import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Hl7MessagingComponent } from './hl7-messaging.component';

describe('Hl7MessagingComponent', () => {
  let component: Hl7MessagingComponent;
  let fixture: ComponentFixture<Hl7MessagingComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ Hl7MessagingComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(Hl7MessagingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
