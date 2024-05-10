import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RecommendationDownloadComponent } from './recommendation-download.component';

describe('RecommendationDownloadComponent', () => {
  let component: RecommendationDownloadComponent;
  let fixture: ComponentFixture<RecommendationDownloadComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RecommendationDownloadComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RecommendationDownloadComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
