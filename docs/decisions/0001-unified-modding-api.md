# ADR-0001: Unified API across Minecraft versions and loaders

- **Status:** Proposed (exploration only; no implementation authorised)
- **Date:** 2026-11-07
- **Decision owner:** ChessMod maintainer
- **Related issues:** To create if investigation is approved
- **Affected targets:** Existing Minecraft version branches; potential Forge,
  Fabric and NeoForge targets, subject to feasibility

## Context

ChessMod has historically used branches for Minecraft versions. This creates
repeated migration/maintenance work and can fragment fixes. The longer-term
idea is to let mod authors write a single mod against a unified interface,
while a build selects a compatible adapter for a Minecraft version/loader.
Adding a target to the compatibility layer could then make it available to
multiple mods without duplicating each mod's adaptation work.

The chess domain model appears substantially independent already, but that
observation alone does not establish that block entities, GUIs, rendering,
networking, assets, loader lifecycle or packaging can share an API cheaply.

## Proposed direction (not a decision)

Investigate a library/framework providing mod-facing concepts and target-specific
adapters, producing **target-specific artefacts from one mod source**. A single
universal binary is neither assumed nor required. Evaluate whether the API
should be source, compile-time, generated-code, or runtime oriented, and whether
adapters ship bundled or as an installable dependency.

## Alternatives

1. Keep branch-per-version and port fixes manually; simplest in the short term.
2. Share the chess-domain module but keep separate loader/version integration.
3. Use source sets/multiloader Gradle modules without introducing a public API.
4. Build a generic unified modding API (highest potential leverage and cost).

## Risks and trade-offs

- A lowest-common-denominator API could limit features or hide expensive
  compatibility work.
- Minecraft/loader lifecycle and metadata/resource formats may require
  incompatible abstractions, especially for old versions.
- The framework would add its own support matrix, regression tests, versioning,
  diagnostics, distribution obligations and possibly many adapter modules.
- A reusable API may be most valuable when another mod actually consumes it.

## Evidence to gather before acceptance

- Branch/build/test matrix and actual duplication by integration subsystem.
- One representative vertical slice (e.g., registering a block + persisted
  state + a client visual) across two versions and preferably two loaders.
- Build time, complexity, binary size, maintenance and user installation cost.
- Design for advanced features and escape hatches, rather than only the easy
  shared subset.

## Decision / exit criteria

No architectural commitment until the maintainer reviews a costed experiment
and explicitly changes status to **Accepted**, **Rejected**, or **Deferred**.
Until then, work under the current branch model.
