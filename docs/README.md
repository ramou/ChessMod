# ChessMod development documentation

This directory is the **versioned handoff and engineering record**, not a replacement
for GitHub issues or the public [project README](../README.md). Begin with
[AI handoff](AI_HANDOFF.md) when opening a new AI-assisted development session.

## Document index

| File | Purpose | Maturity |
| --- | --- | --- |
| [AI_HANDOFF.md](AI_HANDOFF.md) | Minimal orientation and mandatory working constraints | Initial draft |
| [WORKFLOW.md](WORKFLOW.md) | Issue → patch → review → validation → commit protocol | Initial draft |
| [ISSUE_MANAGEMENT.md](ISSUE_MANAGEMENT.md) | Review-and-run GitHub CLI issue write protocol | Initial draft |
| [CONTEXT_EXPORT.md](CONTEXT_EXPORT.md) | Git bundles, worktree/stash ZIPs and GitHub issue snapshots | Initial draft |
| [RELEASING.md](RELEASING.md) | Versioned Git tags, binary release assets and migration checklist | Release workflow in use |
| [RELEASE_TOOL.md](RELEASE_TOOL.md) | Branch-aware Java preflight, verified builds, draft/publication safeguards (#205) | In use; safeguard details in tool documentation |
| [ARCHITECTURE.md](ARCHITECTURE.md) | Current structural map; verified facts vs hypotheses | Initial inventory |
| [TESTING.md](TESTING.md) | Verification responsibilities and build baselines | Initial draft |
| [ACCEPTANCE_TESTING.md](ACCEPTANCE_TESTING.md) | Manual feature scenarios, evidence rules and GTF mapping (#210) | Initial implementation |
| [ACCEPTANCE_MATRIX.md](ACCEPTANCE_MATRIX.md) | Canonical cross-version, feature-by-release acceptance matrix (#210) | Unverified baseline |
| [ROADMAP.md](ROADMAP.md) | Linked process roadmap and architectural investigations | Proposed |
| [decisions/README.md](decisions/README.md) | Architectural decision record (ADR) index and conventions | Initial draft |
| [decisions/0001-unified-modding-api.md](decisions/0001-unified-modding-api.md) | Cross-version/loader abstraction: **proposal only** | Proposed |
| [decisions/0002-history-cleanup.md](decisions/0002-history-cleanup.md) | Prune historical release binaries: alternatives and experiment | Proposed |
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
