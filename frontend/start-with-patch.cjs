// Wrapper script to run Angular CLI with crypto.hash polyfill
require('./patch-crypto.js');

// Get all arguments passed to this script (everything after the script name)
const args = process.argv.slice(2);

// Execute the Angular CLI command
const { spawn } = require('child_process');
const ngProcess = spawn('ng', args, {
  stdio: 'inherit',
  shell: true
});

ngProcess.on('error', (error) => {
  console.error('Error starting ng:', error);
  process.exit(1);
});

ngProcess.on('exit', (code) => {
  process.exit(code || 0);
});

