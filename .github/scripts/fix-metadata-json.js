#!/usr/bin/env node

const fs = require("fs");
const path = require("path");
const {
  parse,
  ParseErrorCode,
  printParseErrorCode,
} = require("jsonc-parser");

const FILE = path.resolve("metadata/metadata.json");

function fatal(msg) {
  console.error("❌", msg);
  process.exit(1);
}

if (!fs.existsSync(FILE)) {
  fatal("metadata.json not found");
}

const raw = fs.readFileSync(FILE, "utf8");

let errors = [];
let data;

// Parse with recovery
data = parse(raw, errors, {
  allowTrailingComma: true,
  allowEmptyContent: false,
  disallowComments: false,
});

// If parsing failed badly
if (!data || typeof data !== "object") {
  console.error("⚠️ JSON was severely broken, attempting fallback recovery");
  try {
    // brute-force fallback
    const repaired = raw
      .replace(/,\s*}/g, "}")
      .replace(/,\s*]/g, "]")
      .replace(/([}\]])\s*([{[])/g, "$1,$2");

    data = parse(repaired, errors, {
      allowTrailingComma: true,
    });
  } catch (e) {
    fatal("Unrecoverable JSON");
  }
}

// Log errors (for PR transparency)
if (errors.length > 0) {
  console.log("⚠️ JSON issues detected:");
  for (const err of errors) {
    console.log("-", printParseErrorCode(err.error));
  }
}

// Canonical rewrite
const formatted = JSON.stringify(data, null, 2) + "\n";

// Write back
fs.writeFileSync(FILE, formatted, "utf8");

console.log("✅ metadata.json repaired and normalized");
