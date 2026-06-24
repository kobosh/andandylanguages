import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-change-password',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './change-password.html',
  styleUrls: ['./change-password.css']
})
export class ChangePassword {

  oldPassword = '';
  newPassword = '';
  confirmPassword = '';

  successMessage = '';
  errorMessage = '';

  email = localStorage.getItem('email') || '';

  constructor(
    private http: HttpClient,
    private router: Router
  ) {}

  changePassword(): void {
    this.errorMessage = '';
    this.successMessage = '';

    if (this.newPassword !== this.confirmPassword) {
      this.errorMessage = 'Passwords do not match';
      return;
    }
console.log("sending password  and email",this.newPassword,this.email)
    this.http.post(
      `${environment.authUrl}/api/auth/change-password`,
      {
        email: localStorage.getItem("email"),
        oldPassword: this.oldPassword,
        newPassword: this.newPassword
      },
      { responseType: 'text' }
    ).subscribe({
      next: (resp) => {
        this.successMessage = resp;
        this.router.navigate(['/login']);
      },
      error: (err) => {
        console.log('CHANGE PASSWORD ERROR', err);
        this.errorMessage = 'Password change failed';
      }
    });
  }
}
