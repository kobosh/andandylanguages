import { Component,OnInit } from '@angular/core';
import { FormsModule,NgForm } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router ,ActivatedRoute} from '@angular/router';



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

    this.http.post('http://localhost:8082/api/auth/register', {
      email: this.email,
      password: this.password,
      name: this.name,
      role: this.role
    }).subscribe({
      next: () => {

        this.success = 'Registration successful! Redirecting...';

        setTimeout(() => {
          if (this.role === 'contrib') {
            console.log(this.role+"  demo record");
            this.router.navigate(['/demo-record']);
           }

          else if (this.role === 'learner') {
            this.router.navigate(['/login'], {
              queryParams: { role: 'learner' }
            });}

        }, 1500);
      },
      error: () => {
        this.error = 'Registration failed. Try a different email.';
      }
    });
  }
}
