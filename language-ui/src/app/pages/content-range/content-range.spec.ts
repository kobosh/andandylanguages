import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ContentRange } from './content-range';

describe('ContentRange', () => {
  let component: ContentRange;
  let fixture: ComponentFixture<ContentRange>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ContentRange]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ContentRange);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
