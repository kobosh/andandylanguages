
import { AfterViewInit, Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

import WaveSurfer from 'wavesurfer.js';
import RegionsPlugin from 'wavesurfer.js/dist/plugins/regions.esm.js';

import { UploadService } from '../../services/upload.service';
import { AuthService } from '../../services/auth.service';


@Component({
  selector: 'app-record',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './record.html',
  styleUrls: ['./record.css']
})
export class Record implements OnInit, AfterViewInit {
  @ViewChild('waveformContainer', { static: false })
  waveformContainer!: ElementRef<HTMLDivElement>;

  waveSurfer!: WaveSurfer;
  regionsPlugin!: any;

  recorder: MediaRecorder | null = null;
  recordedChunks: BlobPart[] = [];

  recordedBlob: Blob | null = null;
  trimmedBlob: Blob | null = null;

  audioUrl: string | null = null;
  text: string = '';
  meaningText: string = '';

  word: string | null = null;
  meaning: string | null = null;

  nowPlaying: string | null = null;
  uploading = false;
  hasRegion: boolean | null = null;

  isWordMode: boolean = true;
  currentRecordingId: number | null = null;

  isUploading=false;
  fullName: string ='';
  role: string | null = '';
  isLearner = false;
  isContributor = false;
  author:string|null=null;

  currentStep: 'list' | 'listen' | 'record' | 'result' = 'list';
  dialects = ['dongolawi', 'kenzy', 'mahassi'];
  dialect: string | null=null;

  constructor(
    private http: HttpClient,
    private uploadSrvc: UploadService,
    private authservice: AuthService
  ) {}

  ngOnInit(): void {
    const token = localStorage.getItem('token');
    if (token) {
      const payload = JSON.parse(atob(token.split('.')[1]));
      this.fullName = payload.fullName ?? null;

      this.role =localStorage.getItem('role');//  payload.role ?? '';
      this.isLearner = this.role === 'learner';
      this.isContributor = true;// this.role === 'contrib' || this.role === 'contributor';


      console.log('payload', payload);
    }
  }

  ngAfterViewInit(): void {
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

  async start() {


    this.recordedChunks = [];
    this.recordedBlob = null;
    this.trimmedBlob = null;
    this.hasRegion = false;
    this.nowPlaying = null;

    const stream = await navigator.mediaDevices.getUserMedia({ audio: true });

    this.recorder = new MediaRecorder(stream, { mimeType: 'audio/webm' });

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

    this.recordedBlob = new Blob(this.recordedChunks, { type: 'audio/webm' });

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

    return new Blob(chunks, { type: 'audio/webm' });
  }

  async uploadTrimmedCompressed() {
    this.isUploading=false;

    if (!this.trimmedBlob) {
      alert('Trim first');
      return;
    }

    if (!this.text || !this.meaningText) {
      alert(this.isWordMode ? 'Enter word + meaning' : 'Enter sentence + sentence meaning');
      return;
    }

    if (!this.isWordMode && !this.currentRecordingId) {
      alert('Missing recording id. Upload the word first.');
      return;
    }

    const compressedBlob = await this.compressToWebM(this.trimmedBlob);

    const safeBase = this.text.trim().replace(/\s+/g, '_');
    const filename = `${this.isWordMode ? 'word' : 'sentence'}-${safeBase}-${Date.now()}.webm`;

    this.isUploading = true;

    const req$ = this.isWordMode

        ? this.uploadSrvc.uploadWord(

          this.text,
          this.meaningText,
          compressedBlob,
          filename,
          this.fullName,
        this.dialect

        )
      : this.uploadSrvc.uploadSentence(
        this.currentRecordingId!,
        this.text,
        this.meaningText,
    compressedBlob, filename



      );

    req$.subscribe({
      next: (resp: any) => {



        if (this.isWordMode) {
          const id = Number(resp?.id);
          if (!id) {
                       return;
          }

          this.currentRecordingId = id;
          this.isWordMode = false;

          // clear fields so user can type sentence next
          this.text = '';
          this.meaningText = '';
        } else {
          this.isWordMode = true;
          this.currentRecordingId = null;
          this.text = '';
          this.meaningText = '';
        }

        this.trimmedBlob = null;
        this.recordedBlob = null;
        this.recordedChunks = [];
        this.hasRegion = false;
        this.nowPlaying = null;

        if (this.audioUrl) {
          URL.revokeObjectURL(this.audioUrl);
          this.audioUrl = null;
        }

        this.regionsPlugin?.clearRegions?.();
        this.waveSurfer?.empty?.();

      },
      error: (err: any) => {

        alert('Upload failed (check console / backend logs).');
        this.isUploading = false;
      },
      complete: () => {
        this.isUploading = false;
      }
    });
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

  reset() {


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

  playFromServer(recordingId: number) {
    const audio = new Audio();
    const audioUrl = `http://localhost:8083/api/recordings/${recordingId}/stream`;

    audio.src = audioUrl;
    audio.load();
    audio.play().catch((err) => {
      console.error('Audio play failed', err);
    });
  }

  submitAudio(): void {

    if (this.isContributor) {

      this.uploadTrimmedCompressed();
      return;
    }


  }


  private resetRecordingState(): void {
    this.recordedBlob = null;
    this.trimmedBlob = null;
    this.hasRegion = false;
    this.nowPlaying = '';
  }




}
