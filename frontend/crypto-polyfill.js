// Polyfill for crypto.hash to support Node.js < 20.12
const crypto = require('crypto');

if (!crypto.hash) {
  const { createHash } = crypto;
  
  crypto.hash = function(algorithm, data) {
    const hash = createHash(algorithm);
    hash.update(data);
    return hash;
  };
}

module.exports = crypto;

