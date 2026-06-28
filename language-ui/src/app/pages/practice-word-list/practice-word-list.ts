import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import {PracticeWordSentence} from '../practice-word-sentence/practice-word-sentence';

import { AssessmentComponent } from '../assessment-component/assessment-component';
import {environment} from '../../../environments/environment';

export interface PracticeWordAndSentence {
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
  practiceWords: PracticeWordAndSentence[] = [];
  currentIndex = 0;
  selectedWord: PracticeWordAndSentence | null = null;

  viewMode: 'practice' | 'assess' = 'practice';
  isWordMode = true;
  showRecorder = false;

  fullName = '';
  error = '';
numberOfRecordings: number | null = null;

  dialect: string | null = null;

  dialects: string[] = ['DONGOLAWI', 'KENZY', 'MAHASSI'];
  constructor(private http: HttpClient) {
    console.log('🔥 constructor fired');
  }
  isUploading=false;
  ngOnInit(): void {

    const token = localStorage.getItem('token');
    if (token) {
      const payload = JSON.parse(atob(token.split('.')[1]));
      this.fullName = payload.fullName ?? '';
    }

  }
  onDialectChange(): void {


    if (this.dialect) {
      this.loadPracticeWords();
      localStorage.setItem("selectedDialect",this.dialect)
    }
  }
 onNumberChange(event: Event): void {
   const inputElement = event.target as HTMLInputElement;

   this.numberOfRecordings = +  inputElement.value || 0;

   localStorage.setItem(
     'numberOfRecordings',
     this.numberOfRecordings.toString()
   );

   console.log('Current numeric value:', this.numberOfRecordings);
 }
  loadPracticeWords(): void {
    console.log("calling loadpracticewords"+this.dialect);

    const token = localStorage.getItem('token');
    console.log('TOKEN SENT TO AUDIO SERVICE:', token);
    this.error = '';
    if (!this.dialect) {
      this.error = 'Please select a dialect first';
      return;
    }
    this.isUploading=true;
    this.http.get<PracticeWordAndSentence[]>(`${environment.audioUrl}/api/recordings/practice-words?dialect=${this.dialect}`,
      {
        headers:{
          Authorization: `Bearer ${token}`
        }
      })
      .subscribe({

        next: (items) => {
          this.practiceWords = items;
          if (items.length > 0) {
            this.currentIndex = 0;
            this.selectedWord = items[0];
            this.isUploading=false;
          }
          else {  this.error=" Dialect Not available";}


        },
        error: (err) => {
          console.error(err);
          this.error = 'Failed to load practice words';
        },
        complete: ()=>{this.isUploading=false;}
      });
  }

  get currentItem(): PracticeWordAndSentence | null {
    if(this.isUploading) return  null;
    if (!this.practiceWords.length) return null;
    return this.practiceWords[this.currentIndex];
  }

  selectWord(item: PracticeWordAndSentence): void {
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
