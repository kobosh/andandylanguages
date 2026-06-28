import { Routes } from '@angular/router';


export const routes: Routes = [
  {
    path: 'contributor-content',
    loadComponent: () =>
      import('./pages/content-range/content-range')
        .then(m => m.ContentRangeComponent)
  },
  {
    path: 'learner-content',
    loadComponent: () =>
      import('./pages/content-range/content-range')
        .then(m => m.ContentRangeComponent)
  },
  {
    path: 'change-password',
    loadComponent: () =>
      import('./pages/change-password/change-password')
        .then(m => m.ChangePassword)
  },
{
    path: 'reset-password',
    loadComponent: () =>
      import('./pages/reset-password/reset-password')
        .then(m => m.ResetPassword)
  },
  {
    path: 'forgot-password',
    loadComponent: () =>
      import('./pages/reset-password/reset-password')
        .then(m => m.ResetPassword)
  },
{
    path: '',
    loadComponent: () =>
      import('./pages/landing/landing')
        .then(m => m.Landing)
  },
  {
    path: 'learner',
    loadComponent: () =>
      import('./pages/practice-word-list/practice-word-list')
        .then(m => m.PracticeWordListComponent)
  },

  { path: 'login', loadComponent: () =>
      import('./pages/login/login').then(m => m.Login) },
  {
    path: 'register',
    loadComponent: () => import('./pages/register/register').then(m => m.Register)
  },
  {
    path: 'demo-record',
    loadComponent: () =>
      import('./pages/demo-record/demo-record').then(m => m.DemoRecordComponent)
  },

  { path: 'record', loadComponent: () =>
      import('./pages/record/record').then(m => m.Record) },
  {path: 'demorecord',loadComponent:()=>import('./pages/demo-record/demo-record')
      .then(m=>m.DemoRecordComponent)},
  {path: 'practice',
    loadComponent:()=>import('./pages/practice-word-list/practice-word-list')
      .then(m=>m.PracticeWordListComponent)},
       {
         path: 'admin',
          loadComponent:()=>import('./pages/admin/admin')
            .then(m=>m.AdminComponent)
              }
];
