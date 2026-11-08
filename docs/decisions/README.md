# Architectural decision records (ADRs)

Use short, versioned records to explain **why** a direction was chosen, not
just what code changed. Records are proposed until the maintainer accepts them.
Do not rewrite the history of an accepted decision; supersede it with a new ADR
and link the old record to the new one.

| ADR | Title | Status |
| --- | --- | --- |
| [0001](0001-unified-modding-api.md) | Unified API across Minecraft versions and loaders | **Proposed** |
| [0002](0002-history-cleanup.md) | Historical release binaries and history rewrite policy | **Proposed** |
| [0003](0003-version-neutral-default-branch.md) | Version-neutral default branch for governance and tooling | **Proposed** |

Copy [TEMPLATE.md](TEMPLATE.md) for future decisions. Suggested status values:
`Proposed`, `Accepted`, `Rejected`, `Superseded`. Include context, alternatives,
consequences, affected branches, evidence, and related issue/commit references.

A proposed ADR never authorises implementation on its own. Any accepted ADR
still requires a scoped issue and a reviewed patch to change code.
