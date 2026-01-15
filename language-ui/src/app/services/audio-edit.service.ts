import {Injectable} from '@angular/core'
@Injectable({ providedIn: 'root' })
export class AudioEditService {

  trim(buffer: AudioBuffer, start: number, end: number): AudioBuffer {
    const sampleRate = buffer.sampleRate;

    const startSample = Math.floor(start * sampleRate);
    const endSample = Math.floor(end * sampleRate);

    const frameCount = endSample - startSample;

    const audioContext = new AudioContext();

    const trimmedBuffer = audioContext.createBuffer(
      buffer.numberOfChannels,
      frameCount,
      sampleRate
    );

    for (let ch = 0; ch < buffer.numberOfChannels; ch++) {
      const channelData = buffer.getChannelData(ch);
      const trimmedData = trimmedBuffer.getChannelData(ch);

      trimmedData.set(
        channelData.subarray(startSample, endSample)
      );
    }

    return trimmedBuffer;
  }

  toWav(buffer: AudioBuffer): Blob {
    const wav = this.encode(buffer);
    return new Blob([wav], { type: 'audio/wav' });
  }
// ✅ WAV encoder (keep this)
  private encode(buffer: AudioBuffer): ArrayBuffer {
    const samples = buffer.getChannelData(0);
    const sampleRate = buffer.sampleRate;

    const ab = new ArrayBuffer(44 + samples.length * 2);
    const view = new DataView(ab);

    let offset = 0;
    const writeString = (s: string) => {
      for (let i = 0; i < s.length; i++) {
        view.setUint8(offset++, s.charCodeAt(i));
      }
    };

    writeString('RIFF');
    view.setUint32(offset, 36 + samples.length * 2, true); offset += 4;
    writeString('WAVE');
    writeString('fmt ');
    view.setUint32(offset, 16, true); offset += 4;
    view.setUint16(offset, 1, true); offset += 2; // PCM
    view.setUint16(offset, 1, true); offset += 2; // mono
    view.setUint32(offset, sampleRate, true); offset += 4;
    view.setUint32(offset, sampleRate * 2, true); offset += 4;
    view.setUint16(offset, 2, true); offset += 2;
    view.setUint16(offset, 16, true); offset += 2;
    writeString('data');
    view.setUint32(offset, samples.length * 2, true); offset += 4;

    for (let i = 0; i < samples.length; i++) {
      const s = Math.max(-1, Math.min(1, samples[i]));
      view.setInt16(offset, s * 0x7fff, true);
      offset += 2;
    }

    return ab;
  }
}






