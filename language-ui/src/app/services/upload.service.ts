import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
/* 👇 Put it here (outside the class) */
export interface UploadWordResponse {
  id: number;
  word: string;
  meaning: string;
  userId: number;
  wordObjectKey: string;
}
@Injectable({ providedIn: 'root' })
export class UploadService {
  constructor(private http: HttpClient) {}

  uploadWord(
    word: string,
    meaning: string,
    blob: Blob,
    filename: string
  ): Observable<UploadWordResponse> {

    const formData = new FormData();
    formData.append('word', word);
    formData.append('meaning', meaning);
    formData.append('file', blob, filename);

    const token = localStorage.getItem('token');
    if (!token) throw new Error('Missing JWT token');

    return this.http.post<UploadWordResponse>(
      'http://localhost:8083/api/recordings/upload-word',
      formData,
      { headers: { Authorization: `Bearer ${token}` } }
    );
  }
  uploadSentence(
    recordId: number,
    sentence: string,
    sentenceMeaning: string,
    blob: Blob,
    filename: string
  ): Observable<object> {

    const formData = new FormData();

    // 🔥 MUST match backend param names exactly
    formData.append("recordingId", recordId.toString());
    formData.append("sentence", sentence);
    formData.append("sentenceMeaning", sentenceMeaning);
    formData.append("file", blob, filename);

    const token = localStorage.getItem('token');
    if (!token) throw new Error('Missing JWT token');

    return this.http.post(
      'http://localhost:8083/api/recordings/upload-sentence',
      formData,
      {
        headers: {
          Authorization: `Bearer ${token}`
        }
      }
    );
  }
 /* constructor(private http: HttpClient) {
  }
  upload(
    word: string | null,
    meaning: string | null,
    blob: Blob,
    filename: string
  ): Observable<Object> {

    const formData = new FormData();
    formData.append('word', word ?? '');
    formData.append('meaning', meaning ?? '');
    formData.append('file', blob, filename);

    const token = localStorage.getItem('token');
    //console.log('🔥 TOKEN AT REQUEST TIME (SERVICE):', token);


    return this.http.post(
      'http://localhost:8083/api/recordings/upload',
      formData,
      {
        headers: {
          Authorization: `Bearer ${token}`
        }
      }
    );
  }*/




}
