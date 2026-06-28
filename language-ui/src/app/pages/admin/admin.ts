import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-admin',
  imports: [CommonModule, FormsModule],
  templateUrl: './admin.html',
  styleUrl: './admin.css'
})
export class AdminComponent {

  fullName = '';
  email = '';
  temporaryPassword = '';
  role = 'contrib';

  successMessage = '';
  errorMessage = '';

  constructor(private http: HttpClient) {}

  addContributor() {
    this.successMessage = '';
    this.errorMessage = '';

    const body = {
      fullName: this.fullName,
      email: this.email,
      temporaryPassword: this.temporaryPassword,
      role: this.role
    };

    this.http.post('/auth-api/api/auth/admin/create-user', body, {
      responseType: 'text'
    }).subscribe({
      next: (resp) => {
        this.successMessage = resp;
        this.fullName = '';
        this.email = '';
        this.temporaryPassword = '';
      },
      error: (err) => {
        console.log('ADMIN CREATE USER ERROR', err);
        this.errorMessage =
          err.error || 'Failed to create contributor';
      }
    });
  }
}
