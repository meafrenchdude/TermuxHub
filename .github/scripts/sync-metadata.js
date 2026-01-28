import fs from "fs";
import path from "path";
import https from "https";

const METADATA_PATH = "metadata/metadata.json";
const STATS_PATH = "metadata/stars.json";
const README_DIR = "metadata/readme";
const README_SIZE_LIMIT = 100 * 1024;
const TOKEN = process.env.GITHUB_TOKEN;

function info(m) { console.log(`[INFO] ${new Date().toISOString()} ${m}`); }
function warn(m) { console.warn(`[WARN] ${new Date().toISOString()} ${m}`); }
function error(m, e) {
  console.error(`[ERROR] ${new Date().toISOString()} ${m}`);
  if (e) console.error(e.stack || e.message || e);
}

if (!TOKEN) {
  error("GITHUB_TOKEN not set");
  process.exit(1);
}

function readJson(p) {
  info(`readJson start ${p}`);
  const data = JSON.parse(fs.readFileSync(p, "utf8"));
  info(`readJson success ${p}`);
  return data;
}

function ensureDir(d) {
  info(`ensureDir ${d}`);
  if (!fs.existsSync(d)) {
    fs.mkdirSync(d, { recursive: true });
    info(`directory created ${d}`);
  } else {
    info(`directory exists ${d}`);
  }
}

function parseRepo(url) {
  info(`parseRepo input ${url}`);
  if (!url) {
    warn("parseRepo url empty");
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
    warn(`parseRepo failed ${cleaned}`);
    return null;
  }
  info(`parseRepo success ${parts[0]}/${parts[1]}`);
  return { owner: parts[0], repo: parts[1] };
}

function github(pathname) {
  info(`GitHub request ${pathname}`);
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
        info(`GitHub response ${pathname} ${res.statusCode}`);
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(JSON.parse(data));
        } else {
          reject(new Error(`GitHub ${res.statusCode}: ${data}`));
        }
      });
    });
    req.on("error", e => {
      error(`GitHub request failed ${pathname}`, e);
      reject(e);
    });
    req.end();
  });
}

function rewriteUrls(md, o) {
  info(`rewriteUrls ${o.owner}/${o.repo}`);
  let out = md.replace(
    /!\[([^\]]*)\]\((?!https?:\/\/)([^)]+)\)/g,
    `![$1](https://raw.githubusercontent.com/${o.owner}/${o.repo}/${o.branch}/$2)`
  );
  out = out.replace(
    /\[([^\]]+)\]\((?!https?:\/\/)([^)]+)\)/g,
    `[$1](https://github.com/${o.owner}/${o.repo}/blob/${o.branch}/$2)`
  );
  return out;
}

function truncate(md) {
  info("truncate check");
  if (Buffer.byteLength(md, "utf8") <= README_SIZE_LIMIT) {
    info("truncate skipped");
    return md;
  }
  warn("README exceeded size limit");
  let t = md.slice(0, README_SIZE_LIMIT);
  t = t.slice(0, t.lastIndexOf("\n"));
  return `${t}\n\n---\n\nREADME truncated by TermuxHub\n`;
}

async function fetchReadme(o) {
  info(`fetchReadme ${o.owner}/${o.repo}`);
  try {
    const r = await github(`/repos/${o.owner}/${o.repo}/readme`);
    let c = Buffer.from(r.content, "base64").toString("utf8");
    c = rewriteUrls(c, o);
    info(`fetchReadme success ${o.owner}/${o.repo}`);
    return truncate(c);
  } catch (e) {
    warn(`fetchReadme failed ${o.owner}/${o.repo}`);
    return null;
  }
}

async function fetchPullRequestCount(owner, repo) {
  info(`fetchPullRequestCount ${owner}/${repo}`);
  let count = 0;
  let page = 1;
  while (true) {
    info(`PR page ${page}`);
    const prs = await github(`/repos/${owner}/${repo}/pulls?state=open&per_page=100&page=${page}`);
    count += prs.length;
    if (prs.length < 100) break;
    page++;
  }
  info(`PR count ${count}`);
  return count;
}

async function main() {
  info("main start");

  const metadata = readJson(METADATA_PATH);
  ensureDir(README_DIR);

  const statsOutput = {
    lastUpdated: Date.now(),
    stats: {}
  };

  let processed = 0;
  let skipped = 0;

  for (const tool of metadata.tools) {
    processed++;
    info(`tool start ${tool.id}`);

    const repo = parseRepo(tool.repo);
    if (!repo) {
      warn(`tool skipped invalid repo ${tool.id}`);
      skipped++;
      continue;
    }

    let repoData;
    try {
      repoData = await github(`/repos/${repo.owner}/${repo.repo}`);
    } catch (e) {
      error(`repo fetch failed ${tool.id}`, e);
      skipped++;
      continue;
    }

    const branch = repoData.default_branch || "main";
    const stars = repoData.stargazers_count || 0;
    const forks = repoData.forks_count || 0;
    const issues = repoData.open_issues_count || 0;
    const license = repoData.license?.spdx_id || "No license";
    const lastUpdated = new Date(repoData.pushed_at).getTime();

    let pullRequests = 0;
    try {
      pullRequests = await fetchPullRequestCount(repo.owner, repo.repo);
    } catch (e) {
      warn(`PR fetch failed ${tool.id}`);
    }

    statsOutput.stats[tool.id] = {
      stars,
      forks,
      issues,
      pullRequests,
      license,
      lastUpdated
    };

    const readme = await fetchReadme({ ...repo, branch });
    const content = readme || `# ${repo.repo}\n\nNo README found\n`;

    const outPath = path.join(README_DIR, `${tool.id}.md`);
    fs.writeFileSync(outPath, content);
    info(`README written ${outPath}`);
  }

  fs.writeFileSync(STATS_PATH, JSON.stringify(statsOutput, null, 2));
  info(`stats written ${STATS_PATH}`);

  info(`main complete processed=${processed} skipped=${skipped}`);
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
