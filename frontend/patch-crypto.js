// Patch crypto.hash before any modules load
const Module = require('module');
const originalRequire = Module.prototype.require;

// Patch the require function to intercept crypto module loading
Module.prototype.require = function(...args) {
  const module = originalRequire.apply(this, args);
  
  // Patch crypto module when it's first required
  if (args[0] === 'crypto' && module && typeof module === 'object' && !module.hash) {
    const { createHash } = module;
    module.hash = function(algorithm, data) {
      const hash = createHash(algorithm);
      hash.update(data);
      return hash;
    };
  }
  
  return module;
};

// Also patch immediately if crypto is already loaded
try {
  const crypto = require('crypto');
  if (crypto && typeof crypto === 'object' && !crypto.hash) {
    const { createHash } = crypto;
    crypto.hash = function(algorithm, data) {
      const hash = createHash(algorithm);
      hash.update(data);
      return hash;
    };
  }
} catch (e) {
  // Ignore if crypto is not available
}

