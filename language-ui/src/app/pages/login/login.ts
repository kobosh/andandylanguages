import { Component ,OnInit} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import {Router, ActivatedRoute} from '@angular/router';
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
export class Login implements OnInit{

  email: string = '';
  password: string = '';
  error: string = '';
  private role:string='';
   loginForm : FormGroup;


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
  ngOnInit() {
    this.idleService.stopWatching();

      this.route.queryParams.subscribe(params => {
        this.role = params['role'] || '';
        console.log('ROLE FROM URL:', this.role);
      });
    }

  login() {
    console.log("in log in");
    this.http.post('http://localhost:8082/api/auth/login',
      {
      email: this.email,
      password: this.password,
      role:this.role
    }).subscribe({
      next: (resp: any) => {
        console.log("IN LOG IN ??????",resp);
       localStorage.setItem('token', resp.accessToken);
        console.log('🔥 TOKEN AFTER LOGIN:', localStorage.getItem('token'));
        //localStorage.setItem('token', resp.accessToken);

        this.idleService.startWatching();
        setTimeout(() => {
          if (this.role === 'contrib') {
            console.log(this.role+"  demo record");
            this.router.navigate(['/demo-record']);
          } else if (this.role === 'learner') {
            this.router.navigate(['/listen']);
          } else if (this.role === 'admin') {
            this.router.navigate(['/login']); // or admin page later
          } else {
            this.router.navigate(['/login']);
          }

        }, 1500);

      },
      error: () => {
        this.error = 'Invalid email or password';
      }
    });
  }
}
