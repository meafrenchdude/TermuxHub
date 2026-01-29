import fs from "fs";
import path from "path";
import https from "https";

const METADATA_PATH = "metadata/metadata.json";
const STARS_PATH = "metadata/stars.json";
const STATS_PATH = "metadata/repo_stats.json";
const README_DIR = "metadata/readme";
const README_SIZE_LIMIT = 100 * 1024;
const TOKEN = process.env.GITHUB_TOKEN;

function info(m) {
  console.log(`[INFO] ${new Date().toISOString()} ${m}`);
}

function warn(m) {
  console.warn(`[WARN] ${new Date().toISOString()} ${m}`);
}

function error(m, e) {
  console.error(`[ERROR] ${new Date().toISOString()} ${m}`);
  if (e) console.error(e.stack || e.message || e);
}

if (!TOKEN) {
  error("GITHUB_TOKEN not set");
  process.exit(1);
}

function readJson(p) {
  info(`Reading JSON: ${p}`);
  return JSON.parse(fs.readFileSync(p, "utf8"));
}

function ensureDir(d) {
  if (!fs.existsSync(d)) {
    info(`Creating directory: ${d}`);
    fs.mkdirSync(d, { recursive: true });
  }
}

function parseRepo(url) {
  if (!url) {
    warn("Empty repo URL");
    return null;
  }
  const cleaned = url
    .trim()
    .replace(/^git\+/, "")
    .replace(/^https?:\/\/github\.com\//, "")
    .replace(/\.git$/, "")
    .replace(/\/$/, "");
  const parts = cleaned.split("/");
  if (parts.length !== 2) {
    warn(`Invalid repo URL: ${url}`);
    return null;
  }
  return { owner: parts[0], repo: parts[1] };
}

function github(pathname) {
  info(`GitHub API request: ${pathname}`);
  return new Promise((resolve, reject) => {
    const req = https.request(
      {
        hostname: "api.github.com",
        path: pathname,
        headers: {
          Authorization: `Bearer ${TOKEN}`,
          Accept: "application/vnd.github+json",
          "User-Agent": "termuxhub-ci"
        }
      },
      res => {
        let data = "";
        res.on("data", c => (data += c));
        res.on("end", () => {
          if (res.statusCode >= 200 && res.statusCode < 300) {
            resolve(JSON.parse(data));
          } else {
            reject(new Error(`GitHub ${res.statusCode}: ${data}`));
          }
        });
      }
    );
    req.on("error", reject);
    req.end();
  });
}

function rewriteUrls(md, o) {
  let out = md.replace(
    /!\[([^\]]*)\]\((?!https?:\/\/)([^)]+)\)/g,
    `![$1](https://raw.githubusercontent.com/${o.owner}/${o.repo}/${o.branch}/$2)`
  );
  out = out.replace(
    /\[([^\]]+)\]\((?!https?:\/\/)([^)]+)\)/g,
    `[$1](https://raw.githubusercontent.com/${o.owner}/${o.repo}/${o.branch}/$2)`
  );
  return out;
}

function stripBadges(md) {
  return md
    .replace(/!\[.*?\]\(https?:\/\/img\.shields\.io\/.*?\)/gi, "")
    .replace(/!\[.*?\]\(https?:\/\/github\.com\/.*?\/workflows\/.*?\)/gi, "")
    .replace(/!\[.*?\]\(https?:\/\/travis-ci\..*?\)/gi, "")
    .replace(/!\[.*?\]\(https?:\/\/codecov\.io\/.*?\)/gi, "")
    .replace(/!\[.*?\]\(https?:\/\/badge\.fury\.io\/.*?\)/gi, "")
    .replace(/\n{3,}/g, "\n\n");
}

function truncate(md) {
  if (Buffer.byteLength(md, "utf8") <= README_SIZE_LIMIT) return md;
  warn("README size limit exceeded, truncating");
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
    c = stripBadges(c);
    return truncate(c);
  } catch (e) {
    warn(`README not found for ${o.owner}/${o.repo}`);
    return null;
  }
}

async function fetchPullRequestCount(owner, repo) {
  info(`Counting pull requests for ${owner}/${repo}`);
  let count = 0;
  let page = 1;
  while (true) {
    const prs = await github(`/repos/${owner}/${repo}/pulls?state=open&per_page=100&page=${page}`);
    count += prs.length;
    if (prs.length < 100) break;
    page++;
  }
  return count;
}

async function main() {
  info("Starting metadata fetch");
  const metadata = readJson(METADATA_PATH);
  ensureDir(README_DIR);

  const starsMap = {};
  const statsMap = {};
  const today = new Date().toISOString().slice(0, 10);

  for (const tool of metadata.tools) {
    info(`Processing tool: ${tool.id}`);
    const repo = parseRepo(tool.repo);
    if (!repo) continue;

    let repoData;
    try {
      repoData = await github(`/repos/${repo.owner}/${repo.repo}`);
    } catch (e) {
      warn(`Failed to fetch repo: ${repo.owner}/${repo.repo}`);
      continue;
    }

    const branch = repoData.default_branch || "main";
    const stars = repoData.stargazers_count || 0;
    const forks = repoData.forks_count || 0;
    const issues = repoData.open_issues_count || 0;
    const license = repoData.license?.spdx_id || null;
    const lastUpdated = new Date(repoData.pushed_at).getTime();

    let pullRequests = 0;
    try {
      pullRequests = await fetchPullRequestCount(repo.owner, repo.repo);
    } catch {
      warn(`Failed to fetch PR count for ${repo.owner}/${repo.repo}`);
    }

    starsMap[tool.id] = stars;
    statsMap[tool.id] = {
      forks,
      issues,
      pullRequests,
      license,
      lastUpdated
    };

    const readme = await fetchReadme({ ...repo, branch });
    const content = readme || `# ${repo.repo}\n\nNo README found\n`;
    fs.writeFileSync(path.join(README_DIR, `${tool.id}.md`), content);
    info(`Saved README for tool ${tool.id}`);
  }

  fs.writeFileSync(
    STARS_PATH,
    JSON.stringify({ lastUpdated: today, stars: starsMap }, null, 2)
  );
  info("Stars metadata written");

  fs.writeFileSync(
    STATS_PATH,
    JSON.stringify({ lastUpdated: today, stats: statsMap }, null, 2)
  );
  info("Repo stats metadata written");

  info("Metadata fetch completed successfully");
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
