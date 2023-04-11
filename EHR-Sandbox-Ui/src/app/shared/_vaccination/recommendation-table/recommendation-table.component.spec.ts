import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RecommendationTableComponent } from './recommendation-table.component';

describe('RecommandationTableComponent', () => {
  let component: RecommendationTableComponent;
  let fixture: ComponentFixture<RecommendationTableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RecommendationTableComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(RecommendationTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
