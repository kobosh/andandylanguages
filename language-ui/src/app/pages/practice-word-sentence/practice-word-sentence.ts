import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { PracticeWord } from '../../model/PracticeWord';
import {LoadingSpinnerComponent} from '../../shared/loading-spinner/loading-spinner';

@Component({
  selector: 'app-practice-word-sentence',

  imports: [CommonModule,LoadingSpinnerComponent],
  templateUrl: './practice-word-sentence.html',
  styleUrls: ['./practice-word-sentence.css']
})
export class PracticeWordSentence {
  @Input() item: PracticeWord | null = null;
  @Input() currentIndex = 0;
  @Input() totalItems = 0;
  @Input() isWordMode = true;
  @Output() readyWord = new EventEmitter<void>();
  @Output() readySentence = new EventEmitter<void>();
  @Output() next = new EventEmitter<void>();
  @Output() prev = new EventEmitter<void>();
  isPreparingTranscript=false;


  error = '';
  nowPlaying: string | null = null;
  isLoadingAudio = false;
  loadingMessage = '';
  private currentAudio: HTMLAudioElement | null = null;
  private selectedWord: any;


  constructor(private http: HttpClient) {}

  playWord(): void {
    if (!this.item) return;
    this.playAudio('word', this.item.wordAudioUrl);
  }

  playSentence(): void {
    if (!this.item) return;
    this.playAudio('sentence', this.item.sentenceAudioUrl);
  }


 private playAudio(label: string, audioUrl: string): void {
    const token = localStorage.getItem('token');

    if (!token || !audioUrl) {
      this.error = 'Missing token or audio URL';
      return;
    }

    this.isLoadingAudio = true;
    this.loadingMessage = 'Loading audio... please wait';
    this.error = '';
    this.nowPlaying = label;
    this.stopCurrentAudio();

    this.http.get(`http://localhost:8083${audioUrl}`, {
      headers: new HttpHeaders({
        Authorization: `Bearer ${token}`
      }),
      responseType: 'blob'
    }).subscribe({
      next: (blob) => {
        this.isLoadingAudio = false;

        const objectUrl = URL.createObjectURL(blob);
        const audio = new Audio(objectUrl);
        this.currentAudio = audio;

        audio.onended = () => {
          this.nowPlaying = null;
          URL.revokeObjectURL(objectUrl);
          this.currentAudio = null;
        };

        audio.onerror = () => {
          this.error = 'Failed to play audio';
          this.nowPlaying = null;
          URL.revokeObjectURL(objectUrl);
          this.currentAudio = null;
        };

        audio.play().catch(err => {
          console.error(err);
          this.error = 'Could not play audio';
          this.nowPlaying = null;
          URL.revokeObjectURL(objectUrl);
          this.currentAudio = null;
        });
      },
      error: (err) => {
        console.error(err);
        this.isLoadingAudio = false;
        this.error = 'Failed to load audio';
        this.nowPlaying = null;
      }
    });
  }

  private stopCurrentAudio(): void {
    if (this.currentAudio) {
      this.currentAudio.pause();
      this.currentAudio.currentTime = 0;
      this.currentAudio = null;
    }
  }

}
