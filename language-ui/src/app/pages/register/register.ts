import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

@Component({
  selector: 'app-register',
  //standalone: true,
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

  constructor(private http: HttpClient, private router: Router) {}

  register() {
    //🔑 clear previous state
    this.error = '';
    this.success = '';

    this.http.post('http://localhost:8082/api/auth/register', {   // ✅ FIX
      //name: this.name,
      email: this.email,
      password: this.password
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
