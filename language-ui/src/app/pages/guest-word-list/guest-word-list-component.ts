import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';

interface PublicWord {
  id: number;
  word: string;
  meaning: string;
  audioUrl: string;
}

@Component({
  selector: 'app-guest-word-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './guest-word-list-component.html',
  styleUrls: ['./guest-word-list.css']
})
export class GuestWordListComponent implements OnInit {
  words: PublicWord[] = [];
  loading = false;
  error = '';

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadWords();
  }

  loadWords(): void {
    this.loading = true;
    this.error = '';

    this.http.get<PublicWord[]>('http://localhost:8083/api/public/words').subscribe({
      next: (data) => {
        this.words = data;
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.error = 'Failed to load words';
        this.loading = false;
      }
    });
  }
}
