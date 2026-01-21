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

<div align="center">

<small>
<em>Termux Hub is a read-only index for discovery only and does not host, distribute, execute, or endorse any third-party tools; all use is at your own risk, and you are solely responsible for reviewing code, licenses, security, and legal compliance. This project is provided “AS IS”, with no warranties and no liability for any damage, security issues, or legal consequences.</em>
</small>

</div>

## Data Sources

All data is **read-only** and sourced from:
https://github.com/maazm7d/TermuxHub

Consumed files:

### Tools
- Metadata index  
  `https://github.com/maazm7d/TermuxHub/metadata/metadata.json`
- Star counts  
  `https://github.com/maazm7d/TermuxHub/metadata/stars.json`
- Tool README  
  `https://github.com/maazm7d/TermuxHub/metadata/readme/<tool.id>.md`
- Thumbnails  
  `https://github.com/maazm7d/TermuxHub/metadata/thumbnail/<tool.id>.webp`

### Hall of Fame
- Index  
  `https://github.com/maazm7d/TermuxHub/metadata/halloffame/index.json`
- Member markdown  
  `https://github.com/maazm7d/TermuxHub/metadata/halloffame/<member.id>.md`


Only these resources are consumed.

## Contributing & Indexing Your Own Tool

See: [CONTRIBUTING.md](CONTRIBUTING.md)
