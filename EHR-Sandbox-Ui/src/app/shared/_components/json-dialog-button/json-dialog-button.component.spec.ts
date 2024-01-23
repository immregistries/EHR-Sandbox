import { ComponentFixture, TestBed } from '@angular/core/testing';

import { JsonDialogButtonComponent } from './json-dialog-button.component';

describe('JsonDialogButtonComponent', () => {
  let component: JsonDialogButtonComponent;
  let fixture: ComponentFixture<JsonDialogButtonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ JsonDialogButtonComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(JsonDialogButtonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
