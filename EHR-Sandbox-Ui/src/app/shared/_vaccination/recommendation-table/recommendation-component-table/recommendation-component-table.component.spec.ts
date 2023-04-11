import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RecommendationComponentTableComponent } from './recommendation-component-table.component';

describe('RecommendationComponentTableComponent', () => {
  let component: RecommendationComponentTableComponent;
  let fixture: ComponentFixture<RecommendationComponentTableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RecommendationComponentTableComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(RecommendationComponentTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
