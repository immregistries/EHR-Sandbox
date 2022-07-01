import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EmptyListMessageComponent } from './empty-list-message.component';

describe('EmptyListMessageComponent', () => {
  let component: EmptyListMessageComponent;
  let fixture: ComponentFixture<EmptyListMessageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EmptyListMessageComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EmptyListMessageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
