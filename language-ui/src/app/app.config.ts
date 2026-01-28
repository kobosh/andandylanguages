import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import {
  provideBrowserGlobalErrorListeners,
  provideZoneChangeDetection
} from '@angular/core';
//import { provideHttpClient } from '@angular/common/http';
import { provideHttpClient, withInterceptors } from '@angular/common/http';

import {AuthenInterceptor} from './interceptors/AuthenInterceptor';
import {authInterceptor} from './auth.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(),
    provideHttpClient(
      withInterceptors([authInterceptor])
    )
  ]
};
