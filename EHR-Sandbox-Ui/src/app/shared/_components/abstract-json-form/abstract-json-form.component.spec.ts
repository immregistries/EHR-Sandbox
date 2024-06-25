import { ComponentFixture, TestBed } from '@angular/core/testing';

import { JsonFormComponent } from './abstract-json-form.component';

describe('JsonFormComponent', () => {
  let component: JsonFormComponent;
  let fixture: ComponentFixture<JsonFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ JsonFormComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(JsonFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
