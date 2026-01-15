import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class RecorderService {
  private mediaRecorder!: MediaRecorder;
  private chunks: BlobPart[] = [];
  private stream!: MediaStream;

  async start(): Promise<void> {
    this.stream = await navigator.mediaDevices.getUserMedia({ audio: true });
    this.chunks = [];

    this.mediaRecorder = new MediaRecorder(this.stream);

    this.mediaRecorder.ondataavailable = (e: BlobEvent) => {
      if (e.data.size > 0) {
        this.chunks.push(e.data);
      }
    };

    this.mediaRecorder.start();
  }

  stop(): Promise<Blob> {
    return new Promise((resolve) => {
      this.mediaRecorder.onstop = () => {
        // ✅ stop microphone (important)
        this.stream.getTracks().forEach(track => track.stop());

        resolve(new Blob(this.chunks, { type: 'audio/webm' }));
      };

      this.mediaRecorder.stop();
    });
  }
}
