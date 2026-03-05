import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';


export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);

  // Don't attach tokens to auth endpoints (prevents expired-token lockout)
  const isAuthEndpoint =
    req.url.includes('/api/auth/login') ||
    req.url.includes('/api/auth/register') ||
    req.url.includes('/.well-known/jwks.json');

  let authReq = req;

  if (!isAuthEndpoint) {
    const token = localStorage.getItem('token');
    if (token) {
      authReq = req.clone({
        setHeaders: { Authorization: `Bearer ${token}` }
      });
    }
  }

  return next(authReq).pipe(
    catchError(err => {
      // If token is expired/invalid, clear it and go to login
      // But don't keep redirecting if we're already on login/register calls
      if (err.status === 401 && !isAuthEndpoint) {
        localStorage.removeItem('token');
        router.navigate(['/login']);
      }
      return throwError(() => err);
    })
  );
};
