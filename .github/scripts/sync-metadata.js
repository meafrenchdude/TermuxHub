import fs from "fs";
import path from "path";
import https from "https";

const METADATA_PATH = "metadata/metadata.json";
const STARS_PATH = "metadata/stars.json";
const README_DIR = "metadata/readme";
const README_SIZE_LIMIT = 100 * 1024;
const TOKEN = process.env.GITHUB_TOKEN;

function info(m) { console.log(`[INFO] ${m}`); }
function warn(m) { console.warn(`[WARN] ${m}`); }
function error(m, e) {
  console.error(`[ERROR] ${m}`);
  if (e) console.error(e.stack || e.message || e);
}

if (!TOKEN) {
  error("GITHUB_TOKEN not set");
  process.exit(1);
}

function readJson(p) {
  info(`Reading file ${p}`);
  return JSON.parse(fs.readFileSync(p, "utf8"));
}

function ensureDir(d) {
  if (!fs.existsSync(d)) {
    info(`Creating directory ${d}`);
    fs.mkdirSync(d, { recursive: true });
  }
}

function parseRepo(url) {
  if (!url) return null;
  const cleaned = url
    .trim()
    .replace(/^git\+/, "")
    .replace(/^https?:\/\/github\.com\//, "")
    .replace(/\.git$/, "")
    .replace(/\/$/, "");
  const parts = cleaned.split("/");
  if (parts.length !== 2) return null;
  return { owner: parts[0], repo: parts[1] };
}

function github(pathname) {
  info(`GitHub API request ${pathname}`);
  return new Promise((resolve, reject) => {
    const req = https.request({
      hostname: "api.github.com",
      path: pathname,
      headers: {
        Authorization: `Bearer ${TOKEN}`,
        Accept: "application/vnd.github+json",
        "User-Agent": "termuxhub-ci"
      }
    }, res => {
      let data = "";
      res.on("data", c => data += c);
      res.on("end", () => {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(JSON.parse(data));
        } else {
          reject(new Error(`GitHub ${res.statusCode}: ${data}`));
        }
      });
    });
    req.on("error", reject);
    req.end();
  });
}

function rewriteUrls(md, o) {
  md = md.replace(
    /!\[([^\]]*)\]\((?!https?:\/\/)([^)]+)\)/g,
    `![$1](https://raw.githubusercontent.com/${o.owner}/${o.repo}/${o.branch}/$2)`
  );
  md = md.replace(
    /\[([^\]]+)\]\((?!https?:\/\/)([^)]+)\)/g,
    `[$1](https://github.com/${o.owner}/${o.repo}/blob/${o.branch}/$2)`
  );
  return md;
}

function truncate(md) {
  if (Buffer.byteLength(md, "utf8") <= README_SIZE_LIMIT) return md;
  warn("README exceeded size limit and was truncated");
  let t = md.slice(0, README_SIZE_LIMIT);
  t = t.slice(0, t.lastIndexOf("\n"));
  return `${t}\n\n---\n\nREADME truncated by TermuxHub\n`;
}

async function fetchReadme(o) {
  try {
    info(`Fetching README for ${o.owner}/${o.repo}`);
    const r = await github(`/repos/${o.owner}/${o.repo}/readme`);
    let c = Buffer.from(r.content, "base64").toString("utf8");
    c = rewriteUrls(c, o);
    return truncate(c);
  } catch (e) {
    warn(`README not found for ${o.owner}/${o.repo}`);
    return null;
  }
}

function footer(d) {
  return `
---

Repository information indexed by TermuxHub

Stars: ${d.stars}
Forks: ${d.forks}
License: ${d.license}
Maintained: ${d.maintained}
Last updated: ${d.updated}
`;
}

async function main() {
  info("Starting metadata synchronization");

  const metadata = readJson(METADATA_PATH);
  ensureDir(README_DIR);

  const starsOutput = {
    lastUpdated: new Date().toISOString().slice(0, 10),
    stars: {}
  };

  let processed = 0;
  let skipped = 0;

  for (const tool of metadata.tools) {
    processed++;
    info(`Processing tool ${tool.id}`);

    const repo = parseRepo(tool.repo);
    if (!repo) {
      warn(`Invalid repository URL for ${tool.id}`);
      skipped++;
      continue;
    }

    let repoData;
    try {
      repoData = await github(`/repos/${repo.owner}/${repo.repo}`);
    } catch (e) {
      error(`Failed fetching repo ${tool.id}`, e);
      skipped++;
      continue;
    }

    const branch = repoData.default_branch || "main";
    const stars = repoData.stargazers_count || 0;
    const forks = repoData.forks_count || 0;
    const license = repoData.license?.spdx_id || "No license";
    const maintained = repoData.archived ? "No" : "Yes";
    const updated = repoData.pushed_at;

    starsOutput.stars[tool.id] = stars;

    const readme = await fetchReadme({ ...repo, branch });
    const content = (readme || `# ${repo.repo}\n\nNo README found\n`) +
      footer({ stars, forks, license, maintained, updated });

    const outPath = path.join(README_DIR, `${tool.id}.md`);
    fs.writeFileSync(outPath, content);
    info(`README written ${outPath}`);
  }

  fs.writeFileSync(STARS_PATH, JSON.stringify(starsOutput, null, 2));
  info("stars.json written");

  info(`Completed. Processed ${processed}, skipped ${skipped}`);
}

process.on("unhandledRejection", e => {
  error("Unhandled rejection", e);
  process.exit(1);
});

process.on("uncaughtException", e => {
  error("Uncaught exception", e);
  process.exit(1);
});

main();
