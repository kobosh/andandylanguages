import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
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

  imports: [CommonModule, PracticeWordSentence, AssessmentComponent],
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

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    const token = localStorage.getItem('token');
    if (token) {
      const payload = JSON.parse(atob(token.split('.')[1]));
      this.fullName = payload.fullName ?? '';
    }
    this.loadPracticeWords();
  }

  loadPracticeWords(): void {
    this.error = '';

    this.http.get<PracticeWord[]>('http://localhost:8083/api/recordings/practice-words')
      .subscribe({
        next: (items) => {
          this.practiceWords = items;
          if (items.length > 0) {
            this.currentIndex = 0;
            this.selectedWord = items[0];
          }
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
    console.log("next current index "+this.currentIndex);
  }

  goToPrevious(): void {
    if (this.currentIndex > 0) {
      this.currentIndex--;
      this.selectedWord = this.currentItem;
      this.error = '';
      this.showRecorder = false;
      this.viewMode = 'practice';
    }
    console.log("prev current index "+this.currentIndex);
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
/*import { Component, ElementRef, OnInit, ViewChild,AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import WaveSurfer from 'wavesurfer.js';
import RegionsPlugin from 'wavesurfer.js/dist/plugins/regions.esm.js';
import {UploadService} from '../../services/upload.service';
import {PronunciationService} from '../../services/PronunciationService';
import { Observable, throwError } from 'rxjs';
import { map } from 'rxjs/operators';
export interface PracticeWord {
  id: number;
  word: string;
  meaning: string;
  wordAudioUrl: string;
  passed?: boolean;
  sentence:string;
  sentenceMeaning: string;
  sentenceAudioUrl:string;
  author: string;
}
interface AssessmentResponse {
  recognizedText: string;
  accuracyScore: number;
  completenessScore: number;
  overallScore: number;
  feedback?: string[];
}

@Component({
  selector: 'app-practice-word-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './practice-word-list.html',
  styleUrls: ['./practice-word-list.css']
})
export class PracticeWordList implements OnInit,AfterViewInit {
  viewMode: 'practice' | 'assess' = 'practice';
  @ViewChild('waveformContainer', { static: false })
  waveformContainer!: ElementRef<HTMLDivElement>;
  practiceWords: PracticeWord[] = [];
  selectedWord: PracticeWord | null = null;
  nowPlaying: string | null = null;
  learnerfullname: string | null = null;
  error = '';
  isWordMode:boolean=true;
  showRecorder:boolean=false;
  private currentAudio: HTMLAudioElement | null = null;

    wordSelected:  any|null=null;// = new EventEmitter<PracticeWord | null>();
   // ready = new EventEmitter<PracticeWord | null>();
  waveSurfer!: WaveSurfer;
  regionsPlugin!: any;

  recorder: MediaRecorder | null = null;
  recordedChunks: BlobPart[] = [];

  recordedBlob: Blob | null = null;
  trimmedBlob: Blob | null = null;

  audioUrl: string | null = null;
   hasRegion:boolean|null =null;
   fullName='';
   isLoadingAudio=false;
  loadingMessage= '';
  expectedWordText: string = '';
  expectedSentenceText: string = '';

  isPreparingWordTranscript = false;
  isPreparingSentenceTranscript = false;
  isPreparingTranscript = false;
  currentBlob: Blob|null=null;

  constructor(private http: HttpClient, private pronunciationService: PronunciationService
) {
  }
  goToAssessment(): void {
    this.viewMode = 'assess';
    // this.assessmentError = '';
    //this.assessmentResult = null;
  }

  goBackToPractice(): void {
    this.viewMode = 'practice';
  }
  ngAfterViewInit(): void {
        //throw new Error("Method not implemented.");
    }

  ngOnInit(): void {
    const token = localStorage.getItem('token');
    if (token) {
      const payload = JSON.parse(atob(token.split('.')[1]));
      this.fullName = payload.fullName ?? null;


      console.log('payload', payload);
      this.loadPracticeWords();
    }
  }
  initWaveSurfer(): void {
    if (!this.waveformContainer?.nativeElement) {
      return;
    }

    if (this.waveSurfer) {
      this.waveSurfer.destroy();
    }

    this.waveformContainer.nativeElement.innerHTML = '';

    this.regionsPlugin = RegionsPlugin.create();

    this.waveSurfer = WaveSurfer.create({
      container: this.waveformContainer.nativeElement,
      waveColor: '#cfd8dc',
      progressColor: '#1976d2',
      height: 80,
      plugins: [this.regionsPlugin]
    });

    this.regionsPlugin.enableDragSelection({
      color: 'rgba(25, 118, 210, 0.2)'
    });

    this.regionsPlugin.on('region-created', () => {
      this.hasRegion = true;
      this.trimmedBlob = null;
    });

    this.regionsPlugin.on('region-updated', () => {
      this.hasRegion = Object.keys(this.regionsPlugin.getRegions()).length > 0;
    });

    this.regionsPlugin.on('region-removed', () => {
      this.hasRegion = Object.keys(this.regionsPlugin.getRegions()).length > 0;
    });
  }

  selectWord(item: PracticeWord): void {
    this.selectedWord = item;
    this.error = '';
    this.stopCurrentAudio();
    this.showRecorder = false;
    this.assessmentResult = null;
    this.assessmentError = '';
    this.recognizedText = '';
  }
  readyToRecordWord(): void {
    this.isWordMode = true;
    console.log('Learner chose WORD mode:', this.isWordMode);
    this.assessmentResult = null;
    this.assessmentError = '';
    this.recognizedText = '';

    if (this.selectedWord) {
      this.startLearnerRecording(this.selectedWord);
    }
  }

  readyToRecordSentence(): void {
    this.isWordMode = false;
    console.log('Learner chose SENTENCE mode:', this.isWordMode);
    this.assessmentResult = null;
    this.assessmentError = '';
    this.recognizedText = '';

    if (this.selectedWord) {
      this.startLearnerRecording(this.selectedWord);
    }
  }

  playAudio(label: string, audioUrl: string,recordingId:number): void {
    const token = localStorage.getItem('token');
    console.log('TOKEN:', localStorage.getItem('token'));
    console.log('AUDIO URL:', audioUrl);

    if (!token || !audioUrl) {
      this.error = 'Missing token or audio URL';
      return;
    }
    this.isLoadingAudio = true;
    this.loadingMessage = "Loading audio... please wait";

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
        this.isLoadingAudio=false;
        this.currentBlob = blob;
        const objectUrl = URL.createObjectURL(blob);
        const audio = new Audio(objectUrl);
        this.currentAudio = audio;
// save audio
      //  this.transcribeReferenceAudio(blob,label,recordingId)  // add this function to transcribe word/sentence audio
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
        this.error = 'Failed to load audio';
        this.nowPlaying = null;
      }
    });
  }




  backToWordList(): void {
    this.selectedWord = null;
    this.nowPlaying = null;
    this.error = '';
    this.stopCurrentAudio();
    this.showRecorder=false;
    this.wordSelected.emit(null);
  }
  currentIndex = 0;

  get currentItem(): PracticeWord | null {
    if (!this.practiceWords.length) return null;
    return this.practiceWords[this.currentIndex];
  }
  goToNext(): void {
    if (this.currentIndex < this.practiceWords.length - 1) {
      this.currentIndex++;
      this.selectedWord = this.currentItem;
      this.error = '';
      this.nowPlaying = null;
      this.stopCurrentAudio();
      this.showRecorder = false;
    }
  }

  goToPrevious(): void {
    if (this.currentIndex > 0) {
      this.currentIndex--;
      this.selectedWord = this.currentItem;
      this.error = '';
      this.nowPlaying = null;
      this.stopCurrentAudio();
      this.showRecorder = false;
    }
  }
  loadPracticeWords(): void {
    this.error = '';

    this.http.get<PracticeWord[]>('http://localhost:8083/api/recordings/practice-words')
      .subscribe({
        next: (items) => {
          console.log('practice words:', items);
          this.practiceWords = items;

          if (items.length > 0) {
            this.currentIndex = 0;
            this.selectedWord = items[0];
          }
        },
        error: (err) => {
          console.error('Failed to load practice words', err);
          this.error = 'Failed to load practice words';
        }
      });
  }
  playReference(item: PracticeWord): void {
    const token = localStorage.getItem('token');

    if (!token || !item?.wordAudioUrl) {
      this.error = 'Missing token or audio URL';
      return;
    }

    this.error = '';
    this.nowPlaying = item.word;
    this.stopCurrentAudio();

    this.http.get(`http://localhost:8083${item.wordAudioUrl}`, {
      headers: new HttpHeaders({
        Authorization: `Bearer ${token}`
      }),
      responseType: 'blob'
    }).subscribe({
      next: (blob) => {
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

//recording

  async start() {
    console.log('START clicked');

    this.recordedChunks = [];
    this.recordedBlob = null;
    this.trimmedBlob = null;
    this.hasRegion = false;
    this.nowPlaying = null;

    const stream = await navigator.mediaDevices.getUserMedia({audio: true});

    this.recorder = new MediaRecorder(stream, {mimeType: 'audio/webm'});

    this.recorder.ondataavailable = (e) => {
      if (e.data.size > 0) {
        this.recordedChunks.push(e.data);
      }
    };

    this.recorder.start();
  }

  async stop() {
    if (!this.recorder || this.recorder.state !== 'recording') return;

    this.recorder.stop();
    await new Promise<void>((r) => (this.recorder!.onstop = () => r()));

    this.recordedBlob = new Blob(this.recordedChunks, {type: 'audio/webm'});

    if (this.audioUrl) URL.revokeObjectURL(this.audioUrl);
    this.audioUrl = URL.createObjectURL(this.recordedBlob);

    this.trimmedBlob = null;
    this.hasRegion = false;
    this.nowPlaying = null;

    this.regionsPlugin.clearRegions();
    this.waveSurfer.load(this.audioUrl);
  }

  async compressToWebM(wavBlob: Blob): Promise<Blob> {
    const audioCtx = new AudioContext();
    const buffer = await audioCtx.decodeAudioData(await wavBlob.arrayBuffer());

    const dest = audioCtx.createMediaStreamDestination();
    const source = audioCtx.createBufferSource();
    source.buffer = buffer;
    source.connect(dest);

    const recorder = new MediaRecorder(dest.stream, {
      mimeType: 'audio/webm;codecs=opus',
      audioBitsPerSecond: 32000
    });

    const chunks: BlobPart[] = [];
    recorder.ondataavailable = (e) => chunks.push(e.data);

    recorder.start();
    source.start();

    await new Promise<void>((resolve) => {
      source.onended = () => recorder.stop();
      recorder.onstop = () => resolve();
    });

    return new Blob(chunks, {type: 'audio/webm'});
  }

  reset() {
    console.log('RESET clicked');

    if (this.recorder && this.recorder.state === 'recording') {
      this.recorder.stop();
    }

    if (this.recorder?.stream) {
      this.recorder.stream.getTracks().forEach((t) => t.stop());
    }

    this.recorder = null;
    this.recordedChunks = [];
    this.recordedBlob = null;
    this.trimmedBlob = null;
    this.hasRegion = false;
    this.nowPlaying = null;

    if (this.audioUrl) {
      URL.revokeObjectURL(this.audioUrl);
      this.audioUrl = null;
    }

    this.waveSurfer?.stop();
    this.waveSurfer?.empty();
    this.regionsPlugin?.clearRegions();

    console.log('RESET complete');
  }

  setAudio(blob: Blob, label: 'Original' | 'Trimmed') {
    if (!this.waveSurfer) {
      console.error('WaveSurfer not initialized');
      return;
    }

    this.nowPlaying = label;

    const url = URL.createObjectURL(blob);
    this.waveSurfer.stop();
    this.waveSurfer.load(url);

    this.waveSurfer.once('ready', () => {
      this.waveSurfer.play();
    });
  }

  protected startLearnerRecording(word:PracticeWord |null): void {
    this.showRecorder = true;

    setTimeout(() => {
      this.initWaveSurfer();
    })
  }

  protected playTrimmed() {
    if (!this.trimmedBlob) return;
    this.setAudio(this.trimmedBlob, 'Trimmed');
  }


  playOriginal() {
    if (!this.recordedBlob) return;
    this.setAudio(this.recordedBlob, 'Original');
  }
  async trimOnly() {
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
  }
  private resetRecordingState(): void {
    this.recordedBlob = null;
    this.trimmedBlob = null;
    this.hasRegion = false;
    this.nowPlaying = '';
  }
  //Assessment
  assessmentLoading = false;
  assessmentError = '';
 // assessmentResult: any = null;
  recognizedText = '';
  assessmentResult: AssessmentResponse | null = null;
  assessRecording(blob: Blob, id: number): void {

    console.log('currentBlob:', this.currentBlob);
    console.log('recordingId:', this.selectedWord!.id);
    if (!this.recordedBlob && !this.trimmedBlob) {
      this.assessmentError = 'Please record audio first.';
      return;
    }

    if (!this.selectedWord) {
      this.assessmentError = 'No practice item selected.';
      return;
    }

    const audioToSend = this.trimmedBlob ?? this.recordedBlob;
    if (!audioToSend) {
      this.assessmentError = 'No audio available.';
      return;
    }

    if (!blob || !id) {
      this.assessmentError = 'Reference audio is missing.';
      return;
    }

    this.assessmentLoading = true;
    this.assessmentError = '';
    this.assessmentResult = null;
    this.isPreparingTranscript = true;

    this.transcribeReferenceAudio(blob, id).subscribe({
      next: (transcript: string) => {
        const clean = transcript?.trim() ?? '';

        if (!clean || clean.toLowerCase().startsWith('error:')) {
          this.assessmentError = 'Reference transcription failed.';
          this.isPreparingTranscript = false;
          this.assessmentLoading = false;
          return;
        }



        if (this.isWordMode) {
          this.expectedWordText = clean;
        } else {
          this.expectedSentenceText = clean;
        }

        console.log('Transcript:', transcript);
        this.isPreparingTranscript = false;

        const formData = new FormData();
        formData.append('audio', audioToSend, 'learner-recording.webm');
        formData.append('expectedText', clean);
        formData.append('languageCode', 'en-US');
        formData.append('recordingId', String(id));

        const token = localStorage.getItem('token');

        this.http.post<any>(
          'http://localhost:8084/api/pronunciation/assess',
          formData,
          token
            ? {
              headers: new HttpHeaders({
                Authorization: `Bearer ${token}`
              })
            }
            : {}
        ).subscribe({
          next: (result) => {
            console.log('Assessment result:', result);
            this.assessmentResult = result;
            this.recognizedText = result.recognizedText ?? '';
            this.assessmentLoading = false;
          },
          error: (err) => {
            console.error('Assessment failed', err);
            this.assessmentError = err?.error?.message || 'Assessment failed.';
            this.assessmentLoading = false;
          }
        });
      },
      error: (err) => {
        console.error('Transcription failed', err);
        this.assessmentError = 'Transcription failed';
        this.isPreparingTranscript = false;
        this.assessmentLoading = false;
      }
    });
  }

  //transcribe audio
  transcribeReferenceAudio(
    blob: Blob,
    recordingId: number,
    languageCode: string = 'auto'
  ): Observable<string> {

    if (!blob) {
      return throwError(() => new Error('No audio blob provided'));
    }

    const formData = new FormData();
    formData.append('audio', blob, 'reference.webm');
    formData.append('recordingId', recordingId.toString());
    formData.append('languageCode', languageCode);

    const token = localStorage.getItem('token');

    return this.http.post<{ recordingId: string; transcript: string }>(
      'http://localhost:8084/api/pronunciation/transcribe',
      formData,
      token
        ? {
          headers: new HttpHeaders({
            Authorization: `Bearer ${token}`
          })
        }
        : {}
    ).pipe(
      map(res => res.transcript?.trim() ?? '')
    );
  }
  transcribeWordOrSentenceAudio(blob: Blob, label: string, recordingId: number): void {
    this.isPreparingTranscript = true;

    this.pronunciationService.transcribeReferenceAudio(blob, recordingId, 'auto')
      .subscribe({
        next: (res) => {
          const transcript = res.transcript ?? '';

          if (label.toLowerCase().includes('sentence')) {
            this.expectedSentenceText = transcript;
            console.log('transcribe word or senetence expectedSentenceText:', this.expectedSentenceText);
          } else {
            this.expectedWordText = transcript;
            console.log('transcribe word or senetence expectedWordText:', this.expectedWordText);
          }

          this.isPreparingTranscript = false;
        },
        error: (err) => {
          console.error('Transcription failed', err);
          this.isPreparingTranscript = false;
        }
      });
  }
}*/
