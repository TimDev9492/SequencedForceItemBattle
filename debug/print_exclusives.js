const fs = require('fs');

// Load the two JSON files
const file1 = JSON.parse(fs.readFileSync('./materials.json', 'utf-8'));
const file2 = JSON.parse(fs.readFileSync('./survival-obtainable-materials.json', 'utf-8'));

// Convert arrays to Sets for efficient lookup
const set1 = new Set(file1);
const set2 = new Set(file2);

// Compute values only in file1
const onlyIn1 = file1.filter(item => !set2.has(item));

// Compute values only in file2
const onlyIn2 = file2.filter(item => !set1.has(item));

// Combine both sets of unique values
const symmetricDifference = [...onlyIn1, ...onlyIn2].map(item => item.toUpperCase());

// Print the result
console.log(JSON.stringify(symmetricDifference));
