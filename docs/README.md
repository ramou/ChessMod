# ChessMod development documentation

This directory is the **versioned handoff and engineering record**, not a replacement
for GitHub issues or the public [project README](../README.md). Begin with
[AI handoff](AI_HANDOFF.md) when opening a new AI-assisted development session.

## Document index

| File | Purpose | Maturity |
| --- | --- | --- |
| [AI_HANDOFF.md](AI_HANDOFF.md) | Minimal orientation and mandatory working constraints | Initial draft |
| [WORKFLOW.md](WORKFLOW.md) | Issue → patch → review → validation → commit protocol | Initial draft |
| [CONTEXT_EXPORT.md](CONTEXT_EXPORT.md) | Git bundles, worktree/stash ZIPs and GitHub issue snapshots | Initial draft |
| [ARCHITECTURE.md](ARCHITECTURE.md) | Current structural map; verified facts vs hypotheses | Initial inventory |
| [TESTING.md](TESTING.md) | Verification responsibilities and build baselines | Initial draft |
| [ROADMAP.md](ROADMAP.md) | Linked process roadmap and architectural investigations | Proposed |
| [decisions/README.md](decisions/README.md) | Architectural decision record (ADR) index and conventions | Initial draft |
| [decisions/0001-unified-modding-api.md](decisions/0001-unified-modding-api.md) | Cross-version/loader abstraction: **proposal only** | Proposed |
| [decisions/TEMPLATE.md](decisions/TEMPLATE.md) | Template for recording future decisions | Template |

## Sources of truth

- **Code and history:** Git repository and branch-specific commits.
- **Work items and acceptance:** GitHub issues; exported ZIPs are *snapshots*.
- **Development mechanics:** [WORKFLOW.md](WORKFLOW.md).
- **Architectural decisions and rationale:** `decisions/` (status matters).
- **Public feature aspirations:** [root README roadmap](../README.md#roadmap).

When an issue or decision conflicts with a document, raise the conflict rather than
silently changing scope. Changes to this folder follow the same review/patch
process as code.
