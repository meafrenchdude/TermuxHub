<h1 align="center">TermuxHub</h1>

<p align="center">
Android application that indexes Termux tools using metadata.<br/>
</p>

<p align="center">
  <a href="https://github.com/maazm7d/TermuxHub/releases/latest/download/app-release.apk">
    <img src="https://raw.githubusercontent.com/NeoApplications/Neo-Backup/034b226cea5c1b30eb4f6a6f313e4dadcbb0ece4/badge_github.png" alt="Get it on GitHub" height="80"/>
  </a>
  <a href="https://f-droid.org/packages/com.maazm7d.termuxhub/">
    <img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" alt="Get it on F-Droid" height="80"/>
  </a>
</p>

## How It Works

- Fetches static metadata from GitHub
- Resolves tools, stars, and markdown independently
- Persists validated data into local Room database
- Local database is the source of truth after sync
- Falls back to cached or bundled data on failure
- **Stars and tool README update automatically at 00:00 UTC**


## Data Sources

All data is **read-only** and sourced from:
https://github.com/maazm7d/TermuxHub

Consumed files:

### Tools
- Metadata index  
  `https://github.com/maazm7d/TermuxHub/blob/main/metadata/metadata.json`
- Star counts  
  `https://github.com/maazm7d/TermuxHub/blob/main/metadata/stars.json`
- Tool README  
  `https://github.com/maazm7d/TermuxHub/blob/main/metadata/readme/<tool.id>.md`
- Thumbnails  
  `https://github.com/maazm7d/TermuxHub/blob/main/metadata/thumbnail/<tool.id>.webp`

### Hall of Fame
- Index  
  `https://github.com/maazm7d/TermuxHub/blob/main/metadata/halloffame/index.json`
- Member markdown  
  `https://github.com/maazm7d/TermuxHub/blob/main/metadata/halloffame/<member.id>.md`


Only these resources are consumed.

## Contributing & Indexing Your Own Tool

See: [CONTRIBUTING.md](CONTRIBUTING.md)
