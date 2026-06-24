import { Component,OnInit } from '@angular/core';
import { FormsModule,NgForm } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router ,ActivatedRoute} from '@angular/router';

import {environment} from '../../../environments/environment';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './register.html',
  styleUrls: ['./register.css']
})
export class Register implements OnInit{
  name: string = '';
  email: string = '';
  password: string = '';
  error: string = '';
  success: string = '';
  role:string='';

  constructor(
    private http: HttpClient,
    private router: Router,
    private route: ActivatedRoute
  ) {}


  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.role = params['role'] || '';


    });
  }
  register(form: NgForm) {
    this.error = '';
    this.success = '';

    if (form.invalid) {
      this.error = 'Please correct the errors above.';
      return;
    }
     const loggedInRole = localStorage.getItem('role')?.toUpperCase();

      const newUserRole = loggedInRole === 'ADMIN'
        ? 'contrib'
        : 'learner';

    this.http.post(`${environment.authUrl}/api/auth/register`, {
      email: this.email,
      password: this.password,
      name: this.name,
      role: newUserRole
    }).subscribe({
      next: () => {

        this.success = 'Registration successful! Redirecting...';

        setTimeout(() => {

            this.router.navigate(['/login']
            );

        }, 1500);
      },
      error: () => {
        this.error = 'Registration failed. Try a different email.';
      }
    });
  }
}
