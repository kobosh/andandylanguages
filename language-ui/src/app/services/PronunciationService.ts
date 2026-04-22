import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface TranscribeResponse {
  recordingId: string;
  transcript: string;
}

@Injectable({
  providedIn: 'root'
})
export class PronunciationService {
  private baseUrl = 'http://localhost:8084/api/pronunciation';

  constructor(private http: HttpClient) {}

  transcribeReferenceAudio(
    blob: Blob,
    recordingId: number,
    languageCode: string = 'auto'
  ): Observable<TranscribeResponse> {
    const formData = new FormData();
    formData.append('audio', blob, 'reference.webm');
    formData.append('recordingId', recordingId.toString());
    formData.append('languageCode', languageCode);

    return this.http.post<TranscribeResponse>(`${this.baseUrl}/transcribe`, formData);
  }
}
