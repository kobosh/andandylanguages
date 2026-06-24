import { Component ,OnInit } from '@angular/core';
import {environment} from '../../../environments/environment';
import { CommonModule} from '@angular/common';

import {FormsModule} from '@angular/forms'
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { ActivatedRoute } from '@angular/router';
@Component({
  selector: 'app-reset-password',
  imports: [CommonModule,FormsModule],
  templateUrl: './reset-password.html',
  styleUrl: './reset-password.css',
})
export class ResetPassword  implements OnInit{
  oldPassword = '';
  newPassword = '';
  confirmPassword = '';

  successMessage = '';
  errorMessage = '';

  email = localStorage.getItem('email') || '';
  private token: any;

  constructor(
    private http: HttpClient,
    private router: Router,
    private  route: ActivatedRoute
  ) {}
  ngOnInit(): void {
    this.token = this.route.snapshot.queryParamMap.get('token') || '';
    console.log('RESET TOKEN =', this.token);
  }
  resetPassword(): void {

    console.log("email ",this.email,'TOKEN=', this.token);
    console.log('NEW PASSWORD=', this.newPassword);

    this.http.post(
      `${environment.authUrl}/api/auth/reset-password`,
      {
        token: this.token,
        newPassword: this.newPassword,
        email: this.email
      },
      { responseType: 'text' }
    ).subscribe({
      next: (resp) => {
        console.log('RESET SUCCESS', resp);
        this.successMessage = resp;
      },
      error: (err) => {
        console.log('RESET ERROR', err);
        this.errorMessage = 'Password reset failed';
      }
    });
  }
}
