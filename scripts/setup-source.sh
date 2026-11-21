#!/usr/bin/env bash
# Builds watermc-server/src/minecraft: Leaf at the pinned commit decompiles Minecraft and applies its own patches,
# then WaterMc's patches are applied on top. No Mojang code is stored in this repository.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
LEAF_COMMIT="$(cat "$ROOT/patches/LEAF_COMMIT")"
LEAF_DIR="$ROOT/build/leaf-base"
TARGET="$ROOT/watermc-server/src/minecraft"
# paperweight commits inside its own work repos; give it an identity if git has none
export GIT_AUTHOR_NAME="${GIT_AUTHOR_NAME:-$(git config user.name || echo watermc-setup)}"
export GIT_AUTHOR_EMAIL="${GIT_AUTHOR_EMAIL:-$(git config user.email || echo setup@localhost)}"
export GIT_COMMITTER_NAME="$GIT_AUTHOR_NAME" GIT_COMMITTER_EMAIL="$GIT_AUTHOR_EMAIL"
# patches are LF; keep every git call (Leaf tooling included) from converting line endings
export GIT_CONFIG_COUNT=1 GIT_CONFIG_KEY_0=core.autocrlf GIT_CONFIG_VALUE_0=false

if [ ! -d "$LEAF_DIR/.git" ]; then
    git clone --filter=blob:none https://github.com/Winds-Studio/Leaf.git "$LEAF_DIR"
fi
git -C "$LEAF_DIR" checkout -q "$LEAF_COMMIT"
(cd "$LEAF_DIR" && ./gradlew applyAllPatches)

rm -rf "$TARGET"
mkdir -p "$TARGET"
cp -r "$LEAF_DIR/leaf-server/src/minecraft/." "$TARGET/"
rm -rf "$TARGET/.git"

while IFS= read -r path; do
    [ -n "$path" ] && rm -f "$TARGET/$path"
done < "$ROOT/patches/minecraft-deleted.txt"

find "$ROOT/patches/minecraft" -name '*.patch' -print0 | sort -z \
    | xargs -0 git apply --unsafe-paths --directory="watermc-server/src/minecraft" --whitespace=nowarn -p1 --
echo "watermc-server/src/minecraft is ready"
