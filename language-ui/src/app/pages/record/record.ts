import {AfterViewInit, Component, ElementRef, ViewChild} from '@angular/core';
import {RecorderService} from '../../services/recorder.service';

import {AudioEditService} from '../../services/audio-edit.service';

import {UploadService} from '../../services/upload.service';
import {WaveformService} from '../../services/waveform.service';
import WaveSurfer from 'wavesurfer.js';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import RegionsPlugin from 'wavesurfer.js/dist/plugins/regions.js';
import {AuthInterceptor} from '../../auth.interceptor';


@Component({
  selector: 'app-record',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './record.html',
  styleUrls: ['./record.css']


})

export class Record implements AfterViewInit {

  @ViewChild('waveformContainer')
  waveformContainer!: ElementRef<HTMLDivElement>;

  waveSurfer!: WaveSurfer;
  regionsPlugin!: RegionsPlugin;

  recorder: MediaRecorder | null = null;
  recordedChunks: BlobPart[] = [];

  recordedBlob: Blob | null = null;
  trimmedBlob: Blob | null = null;

  audioUrl: string | null = null;

   word:string|null=null ;
  meaning:string|null=null ;
  constructor(private uploadSrvc: UploadService) {


  }


  ngAfterViewInit() {
    // 1️⃣ Create regions plugin (NO args)
    this.regionsPlugin = RegionsPlugin.create();

    // 2️⃣ Create WaveSurfer
    this.waveSurfer = WaveSurfer.create({
      container: this.waveformContainer.nativeElement,
      waveColor: '#cfd8dc',
      progressColor: '#1976d2',
      height: 80,
      normalize: false,
      plugins: [this.regionsPlugin]
    });

    // 3️⃣ Enable drag selection (v7+ API)
    this.regionsPlugin.enableDragSelection({
      color: 'rgba(25, 118, 210, 0.2)'
    });
  }


  async start() {
    console.log('START clicked');

    this.recordedChunks = [];
    this.recordedBlob = null;
    this.trimmedBlob = null;

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
    await new Promise<void>(r => this.recorder!.onstop = () => r());

    this.recordedBlob = new Blob(this.recordedChunks, { type: 'audio/webm' });

    if (this.audioUrl) URL.revokeObjectURL(this.audioUrl);
    this.audioUrl = URL.createObjectURL(this.recordedBlob);

    // load waveform
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
      audioBitsPerSecond: 32000 // 👈 adjust (24k–48k is good for speech)
    });

    const chunks: BlobPart[] = [];
    recorder.ondataavailable = e => chunks.push(e.data);

    recorder.start();
    source.start();

    await new Promise<void>(resolve => {
      source.onended = () => recorder.stop();
      recorder.onstop = () => resolve();
    });

    return new Blob(chunks, { type: 'audio/webm' });
  }

  async uploadTrimmedCompressed() {
    if (!this.trimmedBlob) {
      alert('Trim first');
      return;
    }

    console.log('Compressing trimmed audio…');

    const compressedBlob = await this.compressToWebM(this.trimmedBlob);

    console.log(
      'Trimmed WAV:',
      this.formatBytes(this.trimmedBlob.size),
      '→ Compressed:',
      this.formatBytes(compressedBlob.size)
    );
    console.log('🔥 UPLOAD METHOD ENTERED');
    const filename = `${this.word ?? 'audio'}-${Date.now()}.webm`;
    const token=localStorage.getItem('token');
    console.log('🔥 TOKEN AT REQUEST TIME (SERVICE):', token);
    this.uploadSrvc.upload(
      this.word,
      this.meaning,
      this.trimmedBlob!,
      filename
    ).subscribe({
      next: resp => {
        console.log('Upload success', resp);
      },
      error: err => {
        console.error('Upload failed', err);
      }
    });

    this.uploadSrvc.upload(

      this.word,
      this.meaning,
      compressedBlob,
      `${Date.now()}-trimmed.webm`
    ).subscribe({
      next: () => console.log('Compressed trimmed uploaded'),
      error: (e: any) => console.error('Upload failed', e)
    });
  }

  async trimOnly() {
    if (!this.recordedBlob) {
      alert('Nothing to trim');
      return;
    }
    console.log(
      'ORIGINAL size:',
      this.recordedBlob.size,
      'bytes'
    );



    const regions = Object.values(this.regionsPlugin.getRegions());
    if (regions.length === 0) {
      alert('Please select a region on the waveform');
      return;
    }

    const { start, end } = regions[0];
    console.log('Trimming', start, end);

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
      trimmed.getChannelData(ch)
        .set(buffer.getChannelData(ch).slice(startSample, endSample));
    }

    this.trimmedBlob = this.encodeWav(trimmed);

    if (this.audioUrl) URL.revokeObjectURL(this.audioUrl);
    this.audioUrl = URL.createObjectURL(this.trimmedBlob);

    this.regionsPlugin.clearRegions();
    this.waveSurfer.load(this.audioUrl);
    console.log(
      'TRIMMED size:',
      this.trimmedBlob.size,
      'bytes'
    );
  }

  encodeWav(buffer: AudioBuffer): Blob {
    const samples = buffer.getChannelData(0);
    const ab = new ArrayBuffer(44 + samples.length * 2);
    const view = new DataView(ab);

    let o = 0;
    const w = (s: string) => [...s].forEach(c => view.setUint8(o++, c.charCodeAt(0)));

    w('RIFF'); view.setUint32(o, 36 + samples.length * 2, true); o += 4;
    w('WAVEfmt '); view.setUint32(o, 16, true); o += 4;
    view.setUint16(o, 1, true); o += 2;
    view.setUint16(o, 1, true); o += 2;
    view.setUint32(o, buffer.sampleRate, true); o += 4;
    view.setUint32(o, buffer.sampleRate * 2, true); o += 4;
    view.setUint16(o, 2, true); o += 2;
    view.setUint16(o, 16, true); o += 2;
    w('data'); view.setUint32(o, samples.length * 2, true); o += 4;

    samples.forEach(s => {
      view.setInt16(o, s * 0x7fff, true);
      o += 2;
    });

    return new Blob([view], { type: 'audio/wav' });
  }

  private formatBytes(size: number): string {
    if (size === 0) return '0 B';

    const k = 1024;
    const units = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(size) / Math.log(k));

    return `${(size / Math.pow(k, i)).toFixed(i === 0 ? 0 : 1)} ${units[i]}`;
  }



}








