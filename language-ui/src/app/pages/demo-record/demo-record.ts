import {
  Component,OnInit,

  AfterViewInit,
  ViewChild,
  ElementRef

} from '@angular/core';
import {ActivatedRoute,RouterModule} from '@angular/router';
import { CommonModule } from '@angular/common';
//import { ActivatedRoute} from '@angular/core'
import WaveSurfer from 'wavesurfer.js';
import RegionsPlugin from 'wavesurfer.js/dist/plugins/regions.esm.js' ;

@Component({
  selector: 'app-demo-record',
  standalone: true,
  imports:[ CommonModule,RouterModule],
  templateUrl: './demo-record.html',
  styleUrls: ['./demo-record.css']
})
export class DemoRecordComponent implements AfterViewInit,OnInit {

  canPlayOriginal = false;
  canPlayTrimmed = false;
  @ViewChild('waveform') waveform!: ElementRef<HTMLDivElement>;
  waveSurfer!: WaveSurfer;
  regions!: RegionsPlugin;
  recorder: MediaRecorder | null = null;
  recordedChunks: BlobPart[] = [];
  recordedBlob: Blob | null = null;
  trimmedBlob: Blob | null = null;
  isRecording = false;

  constructor(private readonly route: ActivatedRoute) {}
  role: 'contrib' | 'learner' = 'learner';

  ngOnInit(): void {
    const savedRole = localStorage.getItem('role');

      this.role =
      savedRole === 'contrib' || savedRole === 'contrib'
          ? 'contrib'
          : 'learner';

  }
  ngAfterViewInit() {
    //console.log('waveform native element:', this.waveform?.nativeElement);
    // ✅ create regions plugin
    this.regions = RegionsPlugin.create();

    // ✅ create WaveSurfer
    this.waveSurfer = WaveSurfer.create({
      container: this.waveform.nativeElement,
      waveColor: '#111' ,//'#cfd8dc',
      progressColor:'#111',// '#1976d2',
      height: 80,
      plugins: [this.regions]
    });

    // ✅ ENABLE drag selection (THIS IS REQUIRED)
    this.regions.enableDragSelection({
      color: 'rgba(25, 118, 210, 0.25)'
    });
    // auto-remove previous regions
    this.regions.on('region-created', (newRegion) => {
      Object.values(this.regions.getRegions()).forEach(region => {
        if (region.id !== newRegion.id) {
          region.remove();
        }
      });
    });
  }
async startRecording() {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true });

    this.recordedChunks = [];
    this.recordedBlob = null;
    this.trimmedBlob = null;

    this.recorder = new MediaRecorder(stream);

    this.recorder.ondataavailable = e => {
      if (e.data.size > 0) {
        this.recordedChunks.push(e.data);
      }
    };

    this.recorder.onstop = () => {
      this.recordedBlob = new Blob(this.recordedChunks, {
        type: this.recorder?.mimeType || 'audio/mp4'
      });

      console.log('DEMO RECORDED BLOB', this.recordedBlob);

      this.waveSurfer.loadBlob(this.recordedBlob);

      this.canPlayOriginal = true;
      this.canPlayTrimmed = false;
    };

    this.recorder.start();
    this.isRecording = true;

  } catch (err) {
    console.error('Microphone recording failed', err);
    alert('Recording failed. Check microphone permission.');
  }
}
 /* async startRecording() {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true });

    this.recordedChunks = [];
    this.recorder = new MediaRecorder(stream);

    this.recorder.ondataavailable = e => this.recordedChunks.push(e.data);

    this.recorder.onstop = () => {
      this.recordedBlob = new Blob(this.recordedChunks, { type: 'audio/webm' });
      this.waveSurfer.loadBlob(this.recordedBlob);

      this.canPlayOriginal = true;
      this.canPlayTrimmed = true; // reset if re-recording
    };


    this.recorder.start();
    this.isRecording = true;
  }*/
  resetDemo() {

    this.waveSurfer.stop();
    this.waveSurfer.empty();

    this.recordedBlob = null;
    this.trimmedBlob = null;
    this.recordedChunks = [];

    this.canPlayOriginal = false;
    this.canPlayTrimmed = false;

    // clear regions
    Object.values(this.regions.getRegions()).forEach(r => r.remove());
  }
  playTrimmed() {

    if (!this.trimmedBlob) return;

    this.waveSurfer.loadBlob(this.trimmedBlob);
    this.waveSurfer.once('ready', () => {
      this.waveSurfer.play();
    });
  }
stopRecording() {
  if (!this.recorder || this.recorder.state !== 'recording') {
    return;
  }

  this.recorder.stop();
  this.recorder.stream.getTracks().forEach(track => track.stop());

  this.isRecording = false;
}

 /* stopRecording() {
    this.recorder?.stop();
    this.isRecording = false;
  }*/
  playOriginal() {
    if (!this.recordedBlob) return;

    this.waveSurfer.loadBlob(this.recordedBlob);
    this.waveSurfer.play();
  }
 async trim() {
   const regions = Object.values(this.regions.getRegions()) as any[];

   if (!regions.length || !this.recordedBlob) {
     alert('Select a region by clicking and dragging on the waveform');
     return;
   }

   const { start, end } = regions[0];

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

   this.waveSurfer.loadBlob(this.trimmedBlob);

   this.canPlayTrimmed = true;
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
 /* async trim() {
    const regions = Object.values(this.regions.getRegions());

    if (!regions.length || !this.recordedBlob) {
      alert('Select a region by clicking and dragging on the waveform');
      return;
    }

    const { start, end } = regions[0];

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

    const dest = ctx.createMediaStreamDestination();
    const source = ctx.createBufferSource();
    source.buffer = trimmed;
    source.connect(dest);
    source.start();

    const mediaRecorder = new MediaRecorder(dest.stream);
    const chunks: BlobPart[] = [];

    mediaRecorder.ondataavailable = e => chunks.push(e.data);
    mediaRecorder.onstop = () => {
      this.trimmedBlob = new Blob(chunks, { type: 'audio/webm' });
      this.waveSurfer.loadBlob(this.trimmedBlob);
    };

    mediaRecorder.start();
    setTimeout(() => mediaRecorder.stop(), (end - start) * 1000);
  }*/
}
