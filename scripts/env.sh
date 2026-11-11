#!/usr/bin/env bash
# Source from any version branch: source scripts/env.sh [version]
# The canonical implementation and JDK registry live on committed 1.20.1.
if [[ "${BASH_SOURCE[0]}" == "$0" ]]; then
  echo 'ChessMod: source scripts/env.sh [version] (do not execute it)' >&2
  exit 2
fi
_chessmod_bootstrap() {
  local root transient tmp_core tmp_registry source_branch
  root="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd -P)" || return 1
  transient="$root/scripts/transient"
  source_branch="1.20.1"
  if ! command -v git >/dev/null 2>&1; then
    echo 'ChessMod: Git is required to load canonical Java tooling.' >&2; return 1
  fi
  if ! git -C "$root" rev-parse --verify "refs/heads/$source_branch^{commit}" >/dev/null 2>&1; then
    echo "ChessMod: local $source_branch branch is missing; fetch it before sourcing scripts/env.sh." >&2; return 1
  fi
  mkdir -p -- "$transient" || return 1
  tmp_core="$(mktemp "$transient/.env_core.XXXXXXXX")" || return 1
  tmp_registry="$(mktemp "$transient/.release_targets.XXXXXXXX")" || { rm -f -- "$tmp_core"; return 1; }
  if ! git -C "$root" show "$source_branch:scripts/env_core.sh" > "$tmp_core" ||
     ! git -C "$root" show "$source_branch:scripts/release_targets.json" > "$tmp_registry"; then
    rm -f -- "$tmp_core" "$tmp_registry"
    echo 'ChessMod: canonical Java tooling missing from committed 1.20.1; commit it there first.' >&2
    return 1
  fi
  mv -f -- "$tmp_core" "$transient/env_core.sh" &&
  mv -f -- "$tmp_registry" "$transient/release_targets.json" || return 1
  # Source in the current shell so exports persist, but isolate bootstrap variables.
  source "$transient/env_core.sh" "$@"
}
_chessmod_bootstrap "$@"
_chessmod_bootstrap_result=$?
unset -f _chessmod_bootstrap
return "$_chessmod_bootstrap_result"
