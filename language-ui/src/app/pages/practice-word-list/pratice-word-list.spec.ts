import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PraticeWordList } from './practice-word-list';

describe('PraticeWordList', () => {
  let component: PraticeWordList;
  let fixture: ComponentFixture<PraticeWordList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PraticeWordList]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PraticeWordList);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
