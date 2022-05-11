import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FeedbackTableComponent } from './feedback-table.component';

describe('FeedbackTableComponent', () => {
  let component: FeedbackTableComponent;
  let fixture: ComponentFixture<FeedbackTableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FeedbackTableComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FeedbackTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
