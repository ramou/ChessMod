#!/usr/bin/env bash
# Source this file in Bash: source scripts/env.sh [Minecraft branch]
# Do not execute it: a child process cannot change its parent's environment.
if [[ "${BASH_SOURCE[0]}" == "$0" ]]; then
  echo 'Source this helper instead: source scripts/env.sh [branch]' >&2
  exit 2
fi
_chessmod_env() {
  local script_dir root config target selected branch major compiler home java_major javac_major
  local requested="${1:-}"
  script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)" || return 1
  root="$(cd -- "$script_dir/../.." && pwd -P)" || return 1
  config="$root/.conf/app.conf"
  if [[ ! -r "$config" ]]; then
    printf 'ChessMod: configure local JDK paths: cp %q %q (then edit).\n' "$root/.conf/app.conf.example" "$config" >&2
    return 1
  fi
  if ! command -v python3 >/dev/null 2>&1; then
    echo 'ChessMod: Python 3 is required to read scripts/transient/release_targets.json.' >&2
    return 1
  fi
  # The bootstrap checkout is authoritative even when sourced from elsewhere.
  local checkout="$root"
  if [[ -z "$requested" ]]; then
    requested="$(git -C "$checkout" symbolic-ref --quiet --short HEAD 2>/dev/null)" || {
      echo 'ChessMod: detached HEAD; pass an explicit Minecraft branch (e.g. source scripts/env.sh 1.19.4).' >&2
      return 1
    }
  fi
  # Output is sourced from the canonical release registry, not a duplicate table.
  selected="$(python3 - "$root/scripts/transient/release_targets.json" "$requested" <<'PYREG'
import json,sys
try:
    with open(sys.argv[1], encoding='utf-8') as f: registry=json.load(f)
    matches=[t for t in registry['targets'] if t['branch']==sys.argv[2]]
    if len(matches)!=1: raise ValueError('unknown target branch: '+sys.argv[2])
    t=matches[0]
    launcher=int(t['gradle_jvm']); compiler=int(t['compiler_jdk'])
    if launcher!=compiler: raise ValueError('different launcher and compiler JDKs are not supported by this helper; use release-manager tooling')
    print(f"{t['branch']}|{launcher}|{compiler}")
except (OSError,KeyError,ValueError,TypeError) as e:
    print('ChessMod: '+str(e),file=sys.stderr)
    sys.exit(1)
PYREG
)" || return 1
  IFS='|' read -r branch major compiler <<< "$selected"
  # Execute trusted local configuration ONLY in a subshell. Do not let local
  # configuration mutate the caller environment on a failed lookup.
  local homes
  homes="$(bash -c 'source "$1" || exit 1; printf "%s\n" "${JAVA_HOME_8-}" "${JAVA_HOME_16-}" "${JAVA_HOME_17-}" "${JAVA_HOME_21-}"' _ "$config")" || {
    echo "ChessMod: could not load $config" >&2; return 1;
  }
  local -a choices=()
  mapfile -t choices <<< "$homes"
  case "$major" in
    8) home="${choices[0]:-}";; 16) home="${choices[1]:-}";;
    17) home="${choices[2]:-}";; 21) home="${choices[3]:-}";;
    *) echo "ChessMod: unsupported Java major $major" >&2; return 1;;
  esac
  if [[ -z "$home" || ! -x "$home/bin/java" || ! -x "$home/bin/javac" ]]; then
    echo "ChessMod: JAVA_HOME_$major must name a JDK with executable bin/java and bin/javac; edit $config" >&2
    return 1
  fi
  java_major="$("$home/bin/java" -version 2>&1 | python3 -c 'import re,sys; s=sys.stdin.read(); m=re.search(r"(?:openjdk|java) version [\"]?(1\.\d+|\d+)",s,re.I); print(m.group(1) if m else "")' )" || return 1
  javac_major="$("$home/bin/javac" -version 2>&1 | python3 -c 'import re,sys; s=sys.stdin.read(); m=re.search(r"javac (1\.\d+|\d+)",s,re.I); print(m.group(1) if m else "")' )" || return 1
  [[ "$java_major" == '1.8' ]] && java_major=8
  [[ "$javac_major" == '1.8' ]] && javac_major=8
  if [[ "$java_major" != "$major" || "$javac_major" != "$major" ]]; then
    printf 'ChessMod: expected Java %s for %s, got java=%s javac=%s from %s; edit %s\n' "$major" "$branch" "${java_major:-unknown}" "${javac_major:-unknown}" "$home" "$config" >&2
    return 1
  fi
  # Validation complete. Remove only the bin path this helper previously added.
  local clean_path=":$PATH:" previous="${CHESSMOD_ENV_BIN-}"
  if [[ -n "$previous" ]]; then
    # Filter all occurrences of the previously injected directory.
    local -a original_parts=() remaining_parts=()
    local segment
    IFS=: read -r -a original_parts <<< "$PATH"
    for segment in "${original_parts[@]}"; do
      [[ "$segment" == "$previous" ]] || remaining_parts+=("$segment")
    done
    clean_path="$(IFS=:; echo "${remaining_parts[*]}")"
  fi
  clean_path="${clean_path#:}"; clean_path="${clean_path%:}"
  export JAVA_HOME="$home"
  export PATH="$home/bin:$clean_path"
  export CHESSMOD_ENV_BIN="$home/bin"
  printf 'ChessMod: %s -> Java %s (%s)\n' "$branch" "$major" "$JAVA_HOME"
}
_chessmod_env "$@"
_chessmod_result=$?
unset -f _chessmod_env
return "$_chessmod_result"
