import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PracticeWordSentence } from './practice-word-sentence';

describe('PracticeWordSentence', () => {
  let component: PracticeWordSentence;
  let fixture: ComponentFixture<PracticeWordSentence>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PracticeWordSentence]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PracticeWordSentence);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
