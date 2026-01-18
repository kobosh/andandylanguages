import {
  Component,
  AfterViewInit,
  ViewChild,
  ElementRef

} from '@angular/core';
import { CommonModule } from '@angular/common';

import WaveSurfer from 'wavesurfer.js';
import RegionsPlugin from 'wavesurfer.js/dist/plugins/regions';

@Component({
  selector: 'app-demo-record',
  standalone: true,
  imports:[ CommonModule],
  templateUrl: './demo-record.html',
  styleUrls: ['./demo-record.css']
})
export class DemoRecordComponent implements AfterViewInit {
  hasRecorded = false;
  hasSelectedRegion = false;
  hasTrimmed = false;
  showDragHint = true;
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

  ngAfterViewInit() {
    // ✅ create regions plugin
    this.regions = RegionsPlugin.create();

    // ✅ create WaveSurfer
    this.waveSurfer = WaveSurfer.create({
      container: this.waveform.nativeElement,
      waveColor: '#cfd8dc',
      progressColor: '#1976d2',
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
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true });

    this.recordedChunks = [];
    this.recorder = new MediaRecorder(stream);

    this.recorder.ondataavailable = e => this.recordedChunks.push(e.data);
   /* this.recorder.onstop = () => {
      this.recordedBlob = new Blob(this.recordedChunks, { type: 'audio/webm' });
      this.waveSurfer.loadBlob(this.recordedBlob);
    };*/
    this.recorder.onstop = () => {
      this.recordedBlob = new Blob(this.recordedChunks, { type: 'audio/webm' });
      this.waveSurfer.loadBlob(this.recordedBlob);

      this.canPlayOriginal = true;
      this.canPlayTrimmed = true; // reset if re-recording
    };


    this.recorder.start();
    this.isRecording = true;
  }
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
    console.log('in playtrimmed')
    if (!this.trimmedBlob) return;
    console.log('in playtrimmed size=',this.trimmedBlob.size)
    this.waveSurfer.loadBlob(this.trimmedBlob);
    this.waveSurfer.once('ready', () => {
      this.waveSurfer.play();
    });
  }


  stopRecording() {
    this.recorder?.stop();
    this.isRecording = false;
  }
  playOriginal() {
    if (!this.recordedBlob) return;

    this.waveSurfer.loadBlob(this.recordedBlob);
    this.waveSurfer.play();
  }

  async trim() {
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
  }
}
