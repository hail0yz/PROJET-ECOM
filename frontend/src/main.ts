import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { App } from './app/app';
import 'webcrypto-shim';

bootstrapApplication(App, appConfig)
  .then(() => {
    try {
      const el = document.getElementById('app-loading');
      if (el && el.parentNode) el.parentNode.removeChild(el);
    } catch (e) {
      // NO OP
    }
  })
  .catch((err) => {
    try {
      const el = document.getElementById('app-loading');
      if (el && el.parentNode) el.parentNode.removeChild(el);
    } catch (e) {
      // NO OP
    }
    console.error(err);
  });
