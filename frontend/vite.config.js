import { defineConfig } from 'vite';

// Polyfill crypto.hash before Vite uses it
if (typeof require !== 'undefined') {
  try {
    const crypto = require('crypto');
    if (!crypto.hash) {
      const { createHash } = crypto;
      crypto.hash = function(algorithm, data) {
        const hash = createHash(algorithm);
        hash.update(data);
        return hash;
      };
    }
  } catch (e) {
    // Ignore if require is not available
  }
}

export default defineConfig({
  // Your vite config here
});

