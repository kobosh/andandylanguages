import { Injectable } from '@angular/core';
import { FFmpeg } from '@ffmpeg/ffmpeg';
import { fetchFile } from '@ffmpeg/util';

@Injectable({ providedIn: 'root' })
export class FfmpegService {

  private ffmpeg = new FFmpeg();
  private loaded = false;

  async load() {
    if (this.loaded) return;
    await this.ffmpeg.load();
    this.loaded = true;
  }

  async trim(
    inputBlob: Blob,
    startSec: number,
    endSec: number
  ): Promise<Blob> {

    await this.load();

    const inputName = 'input.webm';
    const outputName = 'output.webm';
    const duration = Math.max(0, endSec - startSec);

    // write input file
    await this.ffmpeg.writeFile(
      inputName,
      await fetchFile(inputBlob)
    );

    // trim command
    await this.ffmpeg.exec([
      '-ss', String(startSec),
      '-t', String(duration),
      '-i', inputName,
      '-c:a', 'libopus',
      '-c:v', 'copy',
      outputName
    ]);

    // read output
    const data = await this.ffmpeg.readFile(outputName);

    // IMPORTANT: readFile returns Uint8Array
    // @ts-ignore
    return new Blob([data as Uint8Array], {
      type: 'audio/webm'
    });
  }
}
