import { Injectable } from '@angular/core';
import WaveSurfer from 'wavesurfer.js';
import RegionsPlugin from 'wavesurfer.js/dist/plugins/regions';

@Injectable({ providedIn: 'root' })
export class WaveformService {
  wavesurfer!: WaveSurfer;
  regions!: RegionsPlugin;
  selectedRegion: any = null;

  init(container: HTMLElement) {
    // 1️⃣ Create WaveSurfer
    this.wavesurfer = WaveSurfer.create({
      container,
      waveColor: '#9ca3af',
      progressColor: '#2563eb',
      backend: 'WebAudio',
    });

    // 2️⃣ Create Regions plugin (NO options passed!)
    this.regions = RegionsPlugin.create();

    // 3️⃣ Register plugin
    this.wavesurfer.registerPlugin(this.regions);

    // 4️⃣ Enable drag selection
    this.regions.enableDragSelection({
      color: 'rgba(37, 99, 235, 0.3)',
    });

    // 5️⃣ Listen to region events ON THE PLUGIN
    this.regions.on('region-created', region => {
      console.log('Region created:', region);
      this.selectedRegion = region;
    });

    this.regions.on('region-updated', region => {
      console.log('Region updated:', region);
      this.selectedRegion = region;
    });

    this.regions.on('region-removed', () => {
      console.log('Region removed');
      this.selectedRegion = null;
    });
  }

  // ✅ Must receive a Blob (NOT string)
  load(blob: Blob) {
    this.wavesurfer.loadBlob(blob);
  }

  playPause() {
    this.wavesurfer.playPause();
  }
}
