import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { PracticeWord } from '../../model/PracticeWord';
import {LoadingSpinnerComponent} from '../../shared/loading-spinner/loading-spinner';
import {environment} from '../../../environments/environment';

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

  learnerAudioBlob: Blob | null = null;
  expectedText = '';
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

  private currentObjectUrl: string | null = null;
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


    this.http.get(`${environment.audioUrl}${audioUrl}`, {
      headers: new HttpHeaders({
        Authorization: `Bearer ${token}`
      }),
      responseType: 'blob'
    }).subscribe({
      next: (blob) => {
        this.isLoadingAudio = false;

        const objectUrl = URL.createObjectURL(blob);
        const audio = new Audio(objectUrl);
        this.currentObjectUrl=objectUrl;
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
/*async trimOnly() {
    if (!this.recordedBlob) {
      alert('Nothing to trim');
      return;
    }

    const regions = Object.values(this.regionsPlugin.getRegions());
    if (regions.length === 0) {
      alert('Please select a region on the waveform');
      return;
    }

    const { start, end }: any = regions[0];

    const ctx = new AudioContext();
    const buffer = await ctx.decodeAudioData(await this.recordedBlob.arrayBuffer());

    const startSample = Math.floor(start * buffer.sampleRate);
    const endSample = Math.floor(end * buffer.sampleRate);

    const trimmed = ctx.createBuffer(
      buffer.numberOfChannels,
      endSample - startSample,
      buffer.sampleRate
    );

    for (let ch = 0; ch < buffer.numberOfChannels; ch++) {
      trimmed
        .getChannelData(ch)
        .set(buffer.getChannelData(ch).slice(startSample, endSample));
    }

    this.trimmedBlob = this.encodeWav(trimmed);

    if (this.audioUrl) URL.revokeObjectURL(this.audioUrl);
    this.audioUrl = URL.createObjectURL(this.trimmedBlob);

    this.regionsPlugin.clearRegions();
    this.waveSurfer.load(this.audioUrl);

    this.hasRegion = false;
    this.nowPlaying = 'Trimmed';
  }

encodeWav(buffer: AudioBuffer): Blob {
    const samples = buffer.getChannelData(0);
    const ab = new ArrayBuffer(44 + samples.length * 2);
    const view = new DataView(ab);

    let o = 0;
    const w = (s: string) => [...s].forEach((c) => view.setUint8(o++, c.charCodeAt(0)));

    w('RIFF');
    view.setUint32(o, 36 + samples.length * 2, true);
    o += 4;
    w('WAVEfmt ');
    view.setUint32(o, 16, true);
    o += 4;
    view.setUint16(o, 1, true);
    o += 2;
    view.setUint16(o, 1, true);
    o += 2;
    view.setUint32(o, buffer.sampleRate, true);
    o += 4;
    view.setUint32(o, buffer.sampleRate * 2, true);
    o += 4;
    view.setUint16(o, 2, true);
    o += 2;
    view.setUint16(o, 16, true);
    o += 2;
    w('data');
    view.setUint32(o, samples.length * 2, true);
    o += 4;

    samples.forEach((s) => {
      view.setInt16(o, s * 0x7fff, true);
      o += 2;
    });

    return new Blob([view], { type: 'audio/wav' });
  }*/

}
