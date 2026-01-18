import fs from "fs";
import path from "path";
import https from "https";

const METADATA_PATH = "metadata/metadata.json";
const STARS_PATH = "metadata/stars.json";
const README_DIR = "metadata/readme";

const README_SIZE_LIMIT = 100 * 1024; // 100 KB
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
            reject(new Error(`GitHub API ${res.statusCode}`));
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


function rewriteRelativeUrls(markdown, { owner, repo, branch }) {
  // Images
  markdown = markdown.replace(
    /!\[([^\]]*)\]\((?!https?:\/\/)([^)]+)\)/g,
    `![$1](https://raw.githubusercontent.com/${owner}/${repo}/${branch}/$2)`
  );

  // Links
  markdown = markdown.replace(
    /\[([^\]]+)\]\((?!https?:\/\/)([^)]+)\)/g,
    `[$1](https://github.com/${owner}/${repo}/blob/${branch}/$2)`
  );

  return markdown;
}

function truncateReadme(content) {
  if (Buffer.byteLength(content, "utf-8") <= README_SIZE_LIMIT) {
    return content;
  }

  let truncated = content.slice(0, README_SIZE_LIMIT);
  truncated = truncated.slice(0, truncated.lastIndexOf("\n"));

  return `${truncated}

---

> ⚠️ **README truncated by Termux Hub**  
> The original README exceeds the supported size limit.
`;
}

async function fetchRepoReadme({ owner, repo, branch }) {
  try {
    const readme = await githubRequest(
      `/repos/${owner}/${repo}/readme`
    );

    let content = Buffer
      .from(readme.content, "base64")
      .toString("utf-8");

    content = rewriteRelativeUrls(content, { owner, repo, branch });
    content = truncateReadme(content);

    return content;
  } catch {
    return null;
  }
}


function generateFooter({ stars, forks, license, updated, maintained }) {
  return `
---

## 📦 Repository Info (Termux Hub)

- ⭐ **Stars:** ${stars}
- 🍴 **Forks:** ${forks}
- 📄 **License:** ${license}
- 🛠 **Maintained:** ${maintained}
- 🔄 **Last updated:** ${updated}

---

> Indexed by **Termux Hub** for legitimate educational and authorized research purposes.  
> Termux Hub does not host, modify, or endorse third-party projects.
`;
}


function writeSummary({ processed, updated, skipped }) {
  const summaryPath = process.env.GITHUB_STEP_SUMMARY;
  if (!summaryPath) return;

  const now = formatDate(new Date().toISOString());

  fs.appendFileSync(
    summaryPath,
    `
## TermuxHub – README Sync Summary

| Metric | Count |
|------|------:|
| Tools processed | ${processed} |
| READMEs written | ${updated} |
| Skipped | ${skipped} |

**Run time:** ${now}

---
`
  );
}


async function main() {
  const metadata = readJson(METADATA_PATH);
  const starsJson = readJson(STARS_PATH);

  ensureDir(README_DIR);

  let processed = 0;
  let updated = 0;
  let skipped = 0;

  for (const tool of metadata.tools) {
    processed++;

    const repoInfo = parseRepo(tool.repo);
    if (!repoInfo) {
      skipped++;
      continue;
    }

    let repoData;
    try {
      repoData = await githubRequest(
        `/repos/${repoInfo.owner}/${repoInfo.repo}`
      );
    } catch {
      skipped++;
      continue;
    }

    const branch = repoData.default_branch || "main";

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

    const updatedAt = formatDate(repoData.pushed_at);
    const maintained = repoData.archived ? "No (Archived)" : "Yes";

    const upstreamReadme = await fetchRepoReadme({
      owner: repoInfo.owner,
      repo: repoInfo.repo,
      branch
    });

    let finalReadme = upstreamReadme
      ? upstreamReadme
      : `# ${repoInfo.repo}

> No README found in the upstream repository.
`;

    finalReadme += generateFooter({
      stars,
      forks,
      license,
      updated: updatedAt,
      maintained
    });

    fs.writeFileSync(
      path.join(README_DIR, `${tool.id}.md`),
      finalReadme
    );

    updated++;
  }

  writeSummary({ processed, updated, skipped });
}

main().catch(() => process.exit(1));
