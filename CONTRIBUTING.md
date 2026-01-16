# Contributing

Contributions are accepted only if they respect data integrity and architectural boundaries.

Anything else is rejected.


## Acceptable Changes

- Adding new tools via metadata
- UI polish that does not affect data flow
- Metadata sync correctness
- Repository logic fixes
- Performance improvements


## Rules

- Domain layer is Android-free
- DTOs stay in the data layer
- Room entities are persistence-only
- Repositories control sync behavior
- Local-only state remains local-only
- No partial writes or silent failures

Break a rule. PR closed.


## Commits

- One concern per commit
- No mixed refactors
- Messages explain intent


## Pull Requests

State what changed, why it exists, and what data it touches.

That is all.
