import { AfterViewInit, Component, ElementRef, EventEmitter, Input, Output, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import WaveSurfer from 'wavesurfer.js';
import RegionsPlugin from 'wavesurfer.js/dist/plugins/regions';
import { PracticeWord } from '../../model/PracticeWord'
import {environment} from '../../../environments/environment';

interface AssessmentResponse {
  recognizedText: string;
  expectedText: string;   // ✅ ADD THIS
  accuracyScore: number;
  completenessScore: number;
  overallScore: number;
  feedback?: string[];
}

@Component({
  selector: 'app-assessment',

  imports: [CommonModule],
  templateUrl: './assessment-component.html',
  styleUrls: ['./assessment-component.css']
})
export class AssessmentComponent implements AfterViewInit {
  @Input() item: PracticeWord | null = null;
  @Input() isWordMode = true;
  @Output() back = new EventEmitter<void>();

  @ViewChild('waveformContainer', { static: false })
  waveformContainer!: ElementRef<HTMLDivElement>;

  waveSurfer!: WaveSurfer;
  regionsPlugin!: any;

  recorder: MediaRecorder | null = null;
  recordedChunks: BlobPart[] = [];
  recordedBlob: Blob | null = null;
  trimmedBlob: Blob | null = null;
  audioUrl: string | null = null;
  nowPlaying: string | null = null;
  hasRegion: boolean | null = null;

  assessmentLoading = false;
  assessmentError = '';
  assessmentResult: AssessmentResponse | null = null;

  isPreparingTranscript = false;
  expectedWordText = '';
  expectedSentenceText = '';
  startedRecording=true;

  wordTranscriptCache: Record<number, string> = {};
  sentenceTranscriptCache: Record<number, string> = {};
  constructor( private  http: HttpClient) {}

  ngAfterViewInit(): void {
    this.initWaveSurfer();
  }
  reset(): void {
    // stop recording if still active
    if (this.recorder && this.recorder.state === 'recording') {
      this.recorder.stop();
    }

    // release microphone
    if (this.recorder?.stream) {
      this.recorder.stream.getTracks().forEach(track => track.stop());
    }

    this.recorder = null;
    this.recordedChunks = [];

    // clear learner audio
    this.recordedBlob = null;
    this.trimmedBlob = null;

    // reset UI state
    this.hasRegion = false;
    this.nowPlaying = null;
    //this.error = '';
    this.assessmentResult = null;
    this.assessmentLoading = false;

    // clear local audio URL
    if (this.audioUrl) {
      URL.revokeObjectURL(this.audioUrl);
      this.audioUrl = null;
    }

    // stop and clear waveform
    if (this.waveSurfer) {
      this.waveSurfer.stop();
      this.waveSurfer.empty();
    }

    // clear selected regions
    this.regionsPlugin?.clearRegions();
  }
  initWaveSurfer(): void {
    if (!this.waveformContainer?.nativeElement) return;

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
  recordingDebug = '';
  recordingError = '';
  //startrecording = false;



  private getSupportedMimeType(): string {
    const types = [
      'audio/webm;codecs=opus',
      'audio/webm',
      'audio/mp4',
      'audio/aac'
    ];

    for (const type of types) {
      if (MediaRecorder.isTypeSupported(type)) {
        return type;
      }
    }

    return '';
  }

  async start() {
    this.startedRecording = true;
    this.recordingError = '';
    this.recordingDebug = '';

     try {

      if (!navigator.mediaDevices?.getUserMedia) {
        throw new Error('getUserMedia is not available. Use Safari with HTTPS or localhost.');
      }

      if (!window.MediaRecorder) {
        throw new Error('MediaRecorder is not supported on this iPhone/Safari.');
      }

      const mimeType = this.getSupportedMimeType();


      this.recordedChunks = [];
      this.recordedBlob = null;
      this.trimmedBlob = null;
      this.hasRegion = false;
      this.nowPlaying = null;
      this.assessmentError = '';
      this.assessmentResult = null;


      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });


      this.recorder = mimeType
        ? new MediaRecorder(stream, { mimeType })
        : new MediaRecorder(stream);


      this.recorder.ondataavailable = (e) => {

        if (e.data.size > 0) {
          this.recordedChunks.push(e.data);
        }
      };

      this.recorder.onerror = (e: any) => {
        this.recordingError = 'Recorder error: ' + (e.error?.message || e.message || e);

      };

      this.recorder.start();

    } catch (err: any) {
      this.recordingError = err?.message || String(err);

      this.startedRecording = false;
    }
  }

  async stop() {
    try {


      if (!this.recorder) {
        throw new Error('No recorder exists.');
      }

      if (this.recorder.state !== 'recording') {
        throw new Error('Recorder is not recording. Current state: ' + this.recorder.state);
      }

      this.recorder.stop();

      await new Promise<void>((resolve) => {
        this.recorder!.onstop = () => {

          resolve();
        };
      });

      const type = this.recorder.mimeType || 'audio/webm';
      this.recordedBlob = new Blob(this.recordedChunks, { type });



      if (this.audioUrl) URL.revokeObjectURL(this.audioUrl);
      this.audioUrl = URL.createObjectURL(this.recordedBlob);

      this.trimmedBlob = null;
      this.hasRegion = false;
      this.nowPlaying = null;

      this.regionsPlugin.clearRegions();
      this.waveSurfer.load(this.audioUrl);

      this.recorder.stream.getTracks().forEach(t => t.stop());

    } catch (err: any) {
      this.recordingError = err?.message || String(err);

    }
  }


  playOriginal() {
    if (!this.recordedBlob) return;
    this.setAudio(this.recordedBlob, 'Original');
  }

  playTrimmed() {
    if (!this.trimmedBlob) return;
    this.setAudio(this.trimmedBlob, 'Trimmed');
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


  assess(): void {
    if (!this.item) {
      this.assessmentError = 'No practice item selected.';
      return;
    }

    const audioToSend = this.trimmedBlob ?? this.recordedBlob;
    if (!audioToSend) {
      this.assessmentError = 'Please record audio first.';
      return;
    }

    this.assessmentLoading = true;
    this.assessmentError = '';
    this.assessmentResult = null;
    const audioToAssess = this.trimmedBlob ?? this.recordedBlob;
    this.fetchReferenceAudioBlobAndTranscribe();

  }







  private fetchReferenceAudioBlobAndTranscribe(): void {
    if (!this.item) {
      this.assessmentError = 'No practice item selected.';
      return;
    }

    const audioUrl = this.isWordMode ? this.item.wordAudioUrl : this.item.sentenceAudioUrl;
    const token = localStorage.getItem('token');

    if (!token || !audioUrl) {
      this.assessmentError = 'Missing token or reference audio URL.';
      return;
    }

    this.isPreparingTranscript = true;

    this.http.get(`${environment.audioUrl}${audioUrl}`, {
      headers: new HttpHeaders({
        Authorization: `Bearer ${token}`
      }),
      responseType: 'blob'
    }).subscribe({
      next: (blob) => {


        this.transcribeReferenceAudio(blob, this.item!.id);
      },
      error: (err) => {
        console.error('Failed to load reference audio', err);
        this.assessmentError = 'Failed to load reference audio.';
        this.isPreparingTranscript = false;
        this.assessmentLoading = false;
      }
    });
  }
  private transcribeReferenceAudio(
    blob: Blob,
    recordingId: number,
    languageCode: string = 'sw'
  ): void {
    const isSentence = !this.isWordMode;

    const cachedTranscript = isSentence
      ? this.sentenceTranscriptCache[recordingId]
      : this.wordTranscriptCache[recordingId];

    if (cachedTranscript) {
      if (isSentence) {
        this.expectedSentenceText = cachedTranscript;
      } else {
        this.expectedWordText = cachedTranscript;
      }

      this.isPreparingTranscript = false;
      this.submitAssessment(cachedTranscript);
      return;
    }

    const formData = new FormData();
    formData.append('audio', blob, 'reference.webm');
    formData.append('recordingId', recordingId.toString());
    formData.append('languageCode', languageCode);

    const token = localStorage.getItem('token');

    this.http.post<{ recordingId: string; transcript: string }>(
      `${environment.pronunciationUrl}/api/pronunciation/transcribe`,
      formData,
      token
        ? {
          headers: new HttpHeaders({
            Authorization: `Bearer ${token}`
          })
        }
        : {}
    ).subscribe({
      next: (res) => {
        const transcript = res.transcript?.trim() ?? '';

        if (!transcript) {
          this.assessmentError = 'Reference transcription failed.';
          this.isPreparingTranscript = false;
          this.assessmentLoading = false;
          return;
        }

        if (isSentence) {
          this.sentenceTranscriptCache[recordingId] = transcript;
          this.expectedSentenceText = transcript;
        } else {
          this.wordTranscriptCache[recordingId] = transcript;
          this.expectedWordText = transcript;
        }

        this.isPreparingTranscript = false;
        this.submitAssessment(transcript);
      },
      error: (err) => {
        console.error('Transcription failed', err);
        console.error('🔥 Transcription failed', err);

        console.log('STATUS', err.status);
        console.log('MESSAGE', err.message);
        console.log('ERROR', err.error);


        this.assessmentError = 'Transcription failed.';
        this.isPreparingTranscript = false;
        this.assessmentLoading = false;
      }
    });
  }
  private submitAssessment(expectedText: string): void {
    if (!this.item) {
      this.assessmentError = 'No practice item selected.';
      this.assessmentLoading = false;
      return;
    }

    const audioToSend = this.trimmedBlob ?? this.recordedBlob;
    if (!audioToSend) {
      this.assessmentError = 'No learner audio available.';
      this.assessmentLoading = false;
      return;
    }

    const formData = new FormData();
    formData.append('audio', audioToSend, 'learner-recording.webm');
    formData.append('expectedText', expectedText);
    formData.append('languageCode', 'sw');
    formData.append('recordingId', String(this.item.id));

    const token = localStorage.getItem('token');

    this.http.post<AssessmentResponse>(
      `${environment.pronunciationUrl}/api/pronunciation/assess`,
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
        console.log("expectedtext ",result);

        this.assessmentResult = result;
       // this.recognizedText = result.recognizedText ?? '';
        this.assessmentLoading = false;
        this.clearWaveform();
      },
      error: (err) => {
        console.error('Assessment failed', err);
        this.assessmentError = err?.error?.message || 'Assessment failed.';
        this.assessmentLoading = false;
      }
    });
  }
  private clearWaveform(): void {
    // stop playback
    this.waveSurfer?.stop();

    // clear waveform display
    this.waveSurfer?.empty();

    // remove regions
    this.regionsPlugin?.clearRegions();

    // reset blobs
    this.recordedBlob = null;
    this.trimmedBlob = null;

    // reset flags
    this.hasRegion = false;
    this.nowPlaying = null;

    // clear URL
    if (this.audioUrl) {
      URL.revokeObjectURL(this.audioUrl);
      this.audioUrl = null;
    }


  }
}
