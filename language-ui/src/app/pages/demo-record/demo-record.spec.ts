import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DemoRecord } from './demo-record';

describe('DemoRecord', () => {
  let component: DemoRecord;
  let fixture: ComponentFixture<DemoRecord>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DemoRecord]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DemoRecord);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
