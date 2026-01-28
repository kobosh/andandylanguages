import { Injectable } from '@angular/core';
import {
  HttpInterceptor,
  HttpRequest,
  HttpHandler,
  HttpEvent
} from '@angular/common/http';
import { Router } from '@angular/router';
import { catchError } from 'rxjs/operators';

import { throwError } from 'rxjs';


@Injectable()
export class AuthenInterceptor implements HttpInterceptor {
  constructor(private router: Router) {}

  intercept(req: HttpRequest<any>, next: HttpHandler) {
    const token = localStorage.getItem('token');

    const authReq = token
      ? req.clone({
        setHeaders: { Authorization: `Bearer ${token}` }
      })
      : req;

    return next.handle(authReq).pipe(
      catchError(err => {
        if (err.status === 401) {
          localStorage.removeItem('token');
          alert('🔐 Session expired. Please log in again.');
          this.router.navigate(['/login']);
        }
        return throwError(() => err);
      })
    );
  }
}
