import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import {Router, RouterOutlet} from '@angular/router';
import { RouterLink } from '@angular/router';
import {FormBuilder,FormGroup,ReactiveFormsModule} from "@angular/forms"
import {DemoRecordComponent} from '../demo-record/demo-record';
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule,RouterLink,ReactiveFormsModule,DemoRecordComponent],
  templateUrl: './login.html',
  styleUrls: ['./login.css']
})
export class Login {

  email: string = '';
  password: string = '';
  error: string = '';
   loginForm : FormGroup;

  constructor(
     private fb: FormBuilder,
     private http: HttpClient,
     private router: Router
  ) {
    this.loginForm = this.fb.group({
      email: [''],
      password: ['']
     });
  }

  login() {
    console.log("in log in");
    this.http.post('http://localhost:8082/api/auth/login',
      {
      email: this.email,
      password: this.password
    }).subscribe({
      next: (resp: any) => {
        console.log("IN LOG IN ??????",resp);
       localStorage.setItem('token', resp.accessToken);
        console.log('🔥 TOKEN AFTER LOGIN:', localStorage.getItem('token'));

        this.router.navigate(['/record']);
      },
      error: () => {
        this.error = 'Invalid email or password';
      }
    });
  }
}
