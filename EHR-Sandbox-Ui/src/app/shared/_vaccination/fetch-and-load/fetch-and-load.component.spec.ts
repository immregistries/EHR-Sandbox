import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FetchAndLoadComponent } from './fetch-and-load.component';

describe('FetchAndLoadComponent', () => {
  let component: FetchAndLoadComponent;
  let fixture: ComponentFixture<FetchAndLoadComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FetchAndLoadComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FetchAndLoadComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
