import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
@Injectable({
  providedIn: 'root'
})
export class ExpectedTextService {
  private expectedWord: BehaviorSubject<string> = new BehaviorSubject<string>('');
  private expectedSentence: BehaviorSubject<string> = new BehaviorSubject<string>('');
  constructor() {}
  getExpectedWord(): BehaviorSubject<string> {
    return this.expectedWord;
  }
  getExpectedSentence(): BehaviorSubject<string> {
    return this.expectedSentence;
  }
}
