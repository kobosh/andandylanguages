import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import {Router, RouterOutlet} from '@angular/router';
import { RouterLink } from '@angular/router';
import {FormBuilder,FormGroup,ReactiveFormsModule} from "@angular/forms"
import {DemoRecordComponent} from '../demo-record/demo-record';
import {IdleService} from '../../services/IdleService';
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule,RouterLink,ReactiveFormsModule, DemoRecordComponent],
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
     private router: Router, private idleService: IdleService
  ) {
    this.loginForm = this.fb.group({
      email: [''],
      password: ['']
     });
  }
  ngOnInit() {
    this.idleService.stopWatching();
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
        //localStorage.setItem('token', resp.accessToken);

        this.idleService.startWatching();
        this.router.navigate(['/record']);
      },
      error: () => {
        this.error = 'Invalid email or password';
      }
    });
  }
}
