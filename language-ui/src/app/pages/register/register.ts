import { Component } from '@angular/core';
import { FormsModule,NgForm } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';


@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './register.html',
  styleUrls: ['./register.css']
})
export class Register {
  name: string = '';
  email: string = '';
  password: string = '';
  error: string = '';
  success: string = '';

  constructor(
    private http: HttpClient,
    private router: Router
  ) {}

  register(form: NgForm) {
    this.error = '';
    this.success = '';

    if (form.invalid) {
      this.error = 'Please correct the errors above.';
      return;
    }

    this.http.post('http://localhost:8082/api/auth/register', {
      email: this.email,
      password: this.password,
      name: this.name
    }).subscribe({
      next: () => {
        this.success = 'Registration successful! Redirecting...';

        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 1500);
      },
      error: () => {
        this.error = 'Registration failed. Try a different email.';
      }
    });
  }
}
