import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup } from '@angular/forms';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';

import { environment } from '../../../environments/environment';
import { IdleService} from '../../services/IdleService'


@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, ReactiveFormsModule],
  templateUrl: './login.html',
  styleUrls: ['./login.css']
})
export class Login implements OnInit {

  email = '';
  password = '';
  role = '';

  loginForm: FormGroup;

  errorMessage = '';
  forgotPasswordMessage = '';
  //private resetPassword: boolean;

  constructor(
    private fb: FormBuilder,
    private http: HttpClient,
    private router: Router,
    private route: ActivatedRoute,
    private idleService: IdleService
  ) {
    this.loginForm = this.fb.group({
      email: [''],
      password: ['']
    });
  }

  ngOnInit(): void {
    this.idleService.stopWatching();
  }

  login(): void {
    this.errorMessage = '';

    this.http.post<any>(`${environment.authUrl}/api/auth/login`, {
      email: this.email,
      password: this.password
    }).subscribe({
      next: (resp) => {
        localStorage.setItem('token', resp.accessToken);
        localStorage.setItem('role', resp.role);
        localStorage.setItem("email",resp.email);

        if (resp.mustChangePassword) {
          this.router.navigate(['/change-password']);
          return;
        }

        this.idleService.startWatching();

        if (resp.role === 'contrib' || resp.role === 'CONTRIB' || resp.role === 'CONTRIBUTOR') {
          this.router.navigate(['/record']);
        } else if (resp.role === 'learner' || resp.role === 'LEARNER') {
          this.router.navigate(['/demorecord']);
        } else {
          this.router.navigate(['/login']);
        }
      },
      error: (err) => {
        console.log('LOGIN ERROR', err);
        this.errorMessage = `Login failed. Status=${err.status}`;
      }
    });
  }

  forgotPassword(): void {
    this.errorMessage = '';
    this.forgotPasswordMessage = '';
console.log("forgotpassword email",this.email);
    if (!this.email) {
      this.errorMessage = 'Please enter your email first.';
      return;
    }

    this.http.post(
      `${environment.authUrl}/api/auth/forgot-password`,
      { email: this.email },
      { responseType: 'text' }
    ).subscribe({
      next: (resp) => {
        this.forgotPasswordMessage = resp;
      },
      error: (err) => {
        console.log('FORGOT PASSWORD ERROR', err);
        this.errorMessage = `Forgot password failed. Status=${err.status}`;
      }
    });
  }
}
