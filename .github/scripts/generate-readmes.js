import fs from "fs";
import path from "path";
import https from "https";

const METADATA_PATH = "metadata/metadata.json";
const STARS_PATH = "metadata/stars.json";
const README_DIR = "metadata/readme";

const GITHUB_TOKEN = process.env.GITHUB_TOKEN;

if (!GITHUB_TOKEN) {
  console.error("GITHUB_TOKEN is required");
  process.exit(1);
}

function readJson(file) {
  return JSON.parse(fs.readFileSync(file, "utf-8"));
}

function ensureDir(dir) {
  if (!fs.existsSync(dir)) {
    fs.mkdirSync(dir, { recursive: true });
  }
}

function parseRepo(repo) {
  if (!repo || typeof repo !== "string") return null;

  const cleaned = repo
    .trim()
    .replace(/^git\+/, "")
    .replace(/^git:\/\//, "https://")
    .replace(/^https?:\/\/github\.com\//, "")
    .replace(/\.git$/, "")
    .replace(/\/$/, "");

  const parts = cleaned.split("/");
  if (parts.length !== 2) return null;

  return { owner: parts[0], repo: parts[1] };
}

function githubRequest(pathname) {
  return new Promise((resolve, reject) => {
    const req = https.request(
      {
        hostname: "api.github.com",
        path: pathname,
        method: "GET",
        headers: {
          "User-Agent": "termuxhub-readme-bot",
          "Authorization": `Bearer ${GITHUB_TOKEN}`,
          "Accept": "application/vnd.github+json"
        }
      },
      res => {
        let data = "";
        res.on("data", c => (data += c));
        res.on("end", () => {
          if (res.statusCode >= 200 && res.statusCode < 300) {
            resolve(JSON.parse(data));
          } else {
            reject(new Error(`GitHub API ${res.statusCode}: ${data}`));
          }
        });
      }
    );

    req.on("error", reject);
    req.end();
  });
}

function formatDate(iso) {
  if (!iso) return "Unknown";

  return new Intl.DateTimeFormat(undefined, {
    year: "numeric",
    month: "short",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
    hour12: true,
    timeZoneName: "short"
  }).format(new Date(iso));
}

function generateReadme({ stars, forks, license, updated, maintained }) {
  return `
---

- **Stars:** ${stars}
- **Forks:** ${forks}
- **License:** ${license}
- **Maintained:** ${maintained}
- **Last updated:** ${updated}

---

> *This repository is indexed by **Termux Hub** for legitimate *educational* and authorized security *research* purposes only.*  
> **Termux Hub** does not *host*, *modify*, or *endorse* third-party projects.*

---

<sub>
This README is automatically generated and continuously refreshed by Termux Hub.
</sub>
`;
}

function writeSummary({ processed, updated, skipped }) {
  const summaryPath = process.env.GITHUB_STEP_SUMMARY;
  if (!summaryPath) return;

  const now = formatDate(new Date().toISOString());

  const summary = `
## TermuxHub – README Generation Summary

| Metric | Count |
|------|------:|
| Tools processed | ${processed} |
| READMEs generated | ${updated} |
| Skipped | ${skipped} |

**Run time:** ${now}

---
`;

  fs.appendFileSync(summaryPath, summary);
}

async function main() {
  const metadata = readJson(METADATA_PATH);
  const starsJson = readJson(STARS_PATH);

  ensureDir(README_DIR);

  let processedCount = 0;
  let updatedCount = 0;
  let skippedCount = 0;

  for (const tool of metadata.tools) {
    processedCount++;

    const repoInfo = parseRepo(tool.repo);
    if (!repoInfo) {
      skippedCount++;
      continue;
    }

    let repoData;
    try {
      repoData = await githubRequest(
        `/repos/${repoInfo.owner}/${repoInfo.repo}`
      );
    } catch {
      skippedCount++;
      continue;
    }

    const stars =
      starsJson[tool.repo]?.stars ??
      repoData.stargazers_count ??
      0;

    const forks = repoData.forks_count ?? 0;

    const license =
      repoData.license?.spdx_id &&
      repoData.license.spdx_id !== "NOASSERTION"
        ? repoData.license.spdx_id
        : "No license";

    const updated = formatDate(repoData.pushed_at);

    const maintained = repoData.archived ? "No (Archived)" : "Yes";

    const readmePath = path.join(README_DIR, `${tool.id}.md`);
    const content = generateReadme({
      stars,
      forks,
      license,
      updated,
      maintained
    });

    fs.writeFileSync(readmePath, content);
    updatedCount++;
  }

  writeSummary({
    processed: processedCount,
    updated: updatedCount,
    skipped: skippedCount
  });
}

main().catch(() => process.exit(1));
