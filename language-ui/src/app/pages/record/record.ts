import {AfterViewInit, Component, ElementRef, ViewChild} from '@angular/core';

import {UploadService} from '../../services/upload.service';
import WaveSurfer from 'wavesurfer.js';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import RegionsPlugin from 'wavesurfer.js/dist/plugins/regions.js';
import {AuthService} from '../../services/auth.service';

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
  nowPlaying: "Original" | "Trimmed" | null = null;
   uploading = false;
  hasRegion: boolean | null=null;

  constructor(private uploadSrvc: UploadService,private authservice:AuthService) {


  }

  fullName: string | null = null;
  //String token = this.authservice.generateToken(user, expirationMs);

  ngOnInit() {
    const token = localStorage.getItem('token');
    if (token) {
      const payload = JSON.parse(atob(token.split('.')[1]));

      this.fullName = payload.fullName;
      console.log("full name is not null ?????? ",this.fullName);
    }
  }
  ngAfterViewInit() {
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

    // 🔥 IMPORTANT: region state tracking


    this.regionsPlugin.on('region-created', () => {
      this.hasRegion = true;
      this.trimmedBlob = null;     // 🔥 invalidate old trim
    });
    this.regionsPlugin.on('region-updated', () => {
      this.hasRegion = Object.keys(this.regionsPlugin.getRegions()).length > 0;
    });

    this.regionsPlugin.on('region-removed', () => {
      this.hasRegion = Object.keys(this.regionsPlugin.getRegions()).length > 0;
    });
  }


  async start() {
    console.log('START clicked');

    this.recordedChunks = [];
    this.recordedBlob = null;
    this.trimmedBlob = null;
    this.trimmedBlob = null;   // 🔥 REQUIRED
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
    await new Promise<void>(r => this.recorder!.onstop = () => r());

    this.recordedBlob = new Blob(this.recordedChunks, { type: 'audio/webm' });

    if (this.audioUrl) URL.revokeObjectURL(this.audioUrl);
    this.audioUrl = URL.createObjectURL(this.recordedBlob);
    this.trimmedBlob = null;   // 🔥
    this.hasRegion = false;
    this.nowPlaying = null;

    this.regionsPlugin.clearRegions();
    this.waveSurfer.load(this.audioUrl);
  }


  /*async stop() {
    if (!this.recorder || this.recorder.state !== 'recording') return;

    this.recorder.stop();
    await new Promise<void>(r => this.recorder!.onstop = () => r());

    this.recordedBlob = new Blob(this.recordedChunks, {type: 'audio/webm'});

    if (this.audioUrl) URL.revokeObjectURL(this.audioUrl);
    this.audioUrl = URL.createObjectURL(this.recordedBlob);

    // load waveform
    this.regionsPlugin.clearRegions();
    this.waveSurfer.load(this.audioUrl);
  }*/


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

    return new Blob(chunks, {type: 'audio/webm'});
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
    //console.log('🔥 UPLOAD METHOD ENTERED');
    const filename = `${this.word ?? 'audio'}-${Date.now()}.webm`;
    const token = localStorage.getItem('token');
   // console.log('🔥 TOKEN AT REQUEST TIME (SERVICE):', token);
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

    const {start, end} = regions[0];
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
    this.hasRegion = false; // reset after trim
    this.nowPlaying = 'Trimmed';

  }

  encodeWav(buffer: AudioBuffer): Blob {
    const samples = buffer.getChannelData(0);
    const ab = new ArrayBuffer(44 + samples.length * 2);
    const view = new DataView(ab);

    let o = 0;
    const w = (s: string) => [...s].forEach(c => view.setUint8(o++, c.charCodeAt(0)));

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

    samples.forEach(s => {
      view.setInt16(o, s * 0x7fff, true);
      o += 2;
    });

    return new Blob([view], {type: 'audio/wav'});
  }

  private formatBytes(size: number): string {
    if (size === 0) return '0 B';

    const k = 1024;
    const units = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(size) / Math.log(k));

    return `${(size / Math.pow(k, i)).toFixed(i === 0 ? 0 : 1)} ${units[i]}`;
  }


  reset() {
    console.log('RESET clicked');

    // 🛑 Stop recording if active
    if (this.recorder && this.recorder.state === 'recording') {
      this.recorder.stop();
    }

    // 🛑 Stop microphone tracks
    if (this.recorder?.stream) {
      this.recorder.stream.getTracks().forEach(t => t.stop());
    }

    this.recorder = null;

    // 🧹 Clear blobs
    this.recordedChunks = [];
    this.recordedBlob = null;
    this.trimmedBlob = null;

    // 🧹 Clear audio URL
    if (this.audioUrl) {
      URL.revokeObjectURL(this.audioUrl);
      this.audioUrl = null;
    }

    // 🧹 Clear waveform + regions
    this.waveSurfer.stop();
    this.waveSurfer.empty();
    this.regionsPlugin.clearRegions();


    console.log('RESET complete');
  }
  playOriginal() {
    if (!this.recordedBlob) return;
    this.setAudio(this.recordedBlob, 'Original');
    this.trimmedBlob = null;   // 🔥 invalidate old trim
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

}








