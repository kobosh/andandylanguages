import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import {PracticeWordSentence} from '../practice-word-sentence/practice-word-sentence';

import { AssessmentComponent } from '../assessment-component/assessment-component';

export interface PracticeWord {
  id: number;
  word: string;
  meaning: string;
  wordAudioUrl: string;
  passed?: boolean;
  sentence: string;
  sentenceMeaning: string;
  sentenceAudioUrl: string;
  author: string;
}

@Component({
  selector: 'app-practice-word-list',

  imports: [CommonModule, PracticeWordSentence, AssessmentComponent,FormsModule],
  templateUrl: './practice-word-list.html',
  styleUrls: ['./practice-word-list.css']
})
export class PracticeWordListComponent implements OnInit {
  practiceWords: PracticeWord[] = [];
  currentIndex = 0;
  selectedWord: PracticeWord | null = null;

  viewMode: 'practice' | 'assess' = 'practice';
  isWordMode = true;
  showRecorder = false;

  fullName = '';
  error = '';


  dialect: string | null = null;

  dialects: string[] = ['DONGOLAWI', 'KENZY', 'MAHASSI'];
  constructor(private http: HttpClient) {
    console.log('🔥 constructor fired');
  }

  ngOnInit(): void {
    console.log('🔥 ngOnInit fired');
    const token = localStorage.getItem('token');
    if (token) {
      const payload = JSON.parse(atob(token.split('.')[1]));
      this.fullName = payload.fullName ?? '';
    }
  // this.loadPracticeWords();
  }
  onDialectChange(): void {
    console.log('selected dialect:', this.dialect);

    if (this.dialect) {
      this.loadPracticeWords();
    }
  }
  loadPracticeWords(): void {
    console.log("calling loadpracticewords"+this.dialect);
    this.error = '';
    if (!this.dialect) {
      this.error = 'Please select a dialect first';
      return;
    }
    this.http.get<PracticeWord[]>(`http://localhost:8083/api/recordings/practice-words?dialect=${this.dialect}`)
      .subscribe({
        next: (items) => {
          this.practiceWords = items;
          if (items.length > 0) {
            this.currentIndex = 0;
            this.selectedWord = items[0];
          }
          else { alert(" Not available ");}


        },
        error: (err) => {
          console.error(err);
          this.error = 'Failed to load practice words';
        }
      });
  }

  get currentItem(): PracticeWord | null {
    if (!this.practiceWords.length) return null;
    return this.practiceWords[this.currentIndex];
  }

  selectWord(item: PracticeWord): void {
    this.selectedWord = item;
    this.error = '';
    this.showRecorder = false;
    this.viewMode = 'practice';
  }

  goToNext(): void {
    if (this.currentIndex < this.practiceWords.length - 1) {
      this.currentIndex++;
      this.selectedWord = this.currentItem;
      this.error = '';
      this.showRecorder = false;
      this.viewMode = 'practice';
    }

  }

  goToPrevious(): void {
    if (this.currentIndex > 0) {
      this.currentIndex--;
      this.selectedWord = this.currentItem;
      this.error = '';
      this.showRecorder = false;
      this.viewMode = 'practice';
    }

  }

  onReadyWord(): void {
    this.isWordMode = true;
    this.viewMode = 'assess';
    this.showRecorder = true;
  }

  onReadySentence(): void {
    this.isWordMode = false;
    this.viewMode = 'assess';
    this.showRecorder = true;
  }

  onBackToPractice(): void {
    this.viewMode = 'practice';
    this.showRecorder = false;
  }
}
