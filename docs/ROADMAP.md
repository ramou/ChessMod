# Development process roadmap (proposed)

This is a **linked engineering roadmap**, not a second feature backlog. The
[root README roadmap](../README.md#roadmap) expresses public feature goals;
[GitHub issues](https://github.com/ramou/ChessMod/issues) track real work and
status. Refresh issue snapshots before prioritising work.

| Horizon | Focus | Where to track it | Current status |
| --- | --- | --- | --- |
| 0 | Context scripts, handoff docs, ADR process, reviewed issue writes | [WORKFLOW.md](WORKFLOW.md), [ISSUE_MANAGEMENT.md](ISSUE_MANAGEMENT.md) | Initial workflow; issue writes proposed |
| Next | Baseline builds/tests per Minecraft branch | [TESTING.md](TESTING.md), GitHub issue to create | Not started |
| Next | Chess rules regression bugs, targeted JUnit cases | Existing/new GitHub issues | Not yet selected |
| Later | Branch compatibility inventory and migration effort | [ARCHITECTURE.md](ARCHITECTURE.md), GitHub issue to create | Investigation |
| Later | Unified loader/version API proof of concept | [ADR-0001](decisions/0001-unified-modding-api.md) | Proposed only |
| Optional | Repository-history size experiment (no rewrite approved) | [ADR-0002](decisions/0002-history-cleanup.md) | Proposed only |
| Later | Feature work from established public roadmap | [Root roadmap](../README.md#roadmap) | Issue-driven |

**Prioritisation rule:** bugs and small user-visible fixes need not wait for a
large architecture effort. Pursue the unified framework only after an explicit
decision and an evidence-backed comparison with current branch maintenance.

When an item is adopted, add a real issue link and update this table. Don't
invent issue numbers or treat an architectural proposal as approved work.
