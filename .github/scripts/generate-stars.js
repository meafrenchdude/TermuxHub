import fs from "fs";
import path from "path";

const METADATA_PATH = "metadata/metadata.json";
const OUTPUT_PATH = "metadata/stars.json";
const TOKEN = process.env.GITHUB_TOKEN;

if (!TOKEN) {
  console.error("GITHUB_TOKEN missing");
  process.exit(1);
}

const headers = {
  Authorization: `Bearer ${TOKEN}`,
  Accept: "application/vnd.github+json"
};

function parseRepo(url) {
  if (!url) return null;

  const cleaned = url.replace(/\.git$/, "").replace(/\/$/, "");
  const match = cleaned.match(/github\.com\/([^/]+)\/([^/]+)/);
  if (!match) return null;

  return { owner: match[1], repo: match[2] };
}

async function fetchStars(owner, repo) {
  const res = await fetch(
    `https://api.github.com/repos/${owner}/${repo}`,
    { headers }
  );

  if (!res.ok) return null;

  const data = await res.json();
  return data.stargazers_count ?? null;
}

async function main() {
  const metadata = JSON.parse(fs.readFileSync(METADATA_PATH, "utf8"));

  const stars = {};

  for (const tool of metadata.tools) {
    const parsed = parseRepo(tool.repo);
    if (!parsed) continue;

    const count = await fetchStars(parsed.owner, parsed.repo);
    if (count !== null) {
      stars[tool.id] = count;
    }
  }

  const output = {
    lastUpdated: new Date().toISOString().slice(0, 10),
    stars
  };

  fs.writeFileSync(OUTPUT_PATH, JSON.stringify(output, null, 2));
}

main().catch(() => process.exit(1));
