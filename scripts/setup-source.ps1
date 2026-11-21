# Builds watermc-server/src/minecraft: Leaf at the pinned commit decompiles Minecraft and applies its own patches,
# then WaterMc's patches are applied on top. No Mojang code is stored in this repository.
$ErrorActionPreference = 'Stop'

$Root = Split-Path -Parent $PSScriptRoot
$LeafCommit = (Get-Content "$Root\patches\LEAF_COMMIT" -Raw).Trim()
$LeafDir = "$Root\build\leaf-base"
$Target = "$Root\watermc-server\src\minecraft"
# paperweight commits inside its own work repos; give it an identity if git has none
if (-not $env:GIT_AUTHOR_NAME) { $env:GIT_AUTHOR_NAME = (git config user.name); if (-not $env:GIT_AUTHOR_NAME) { $env:GIT_AUTHOR_NAME = 'watermc-setup' } }
if (-not $env:GIT_AUTHOR_EMAIL) { $env:GIT_AUTHOR_EMAIL = (git config user.email); if (-not $env:GIT_AUTHOR_EMAIL) { $env:GIT_AUTHOR_EMAIL = 'setup@localhost' } }
$env:GIT_COMMITTER_NAME = $env:GIT_AUTHOR_NAME
$env:GIT_COMMITTER_EMAIL = $env:GIT_AUTHOR_EMAIL
# patches are LF; keep every git call (Leaf tooling included) from converting line endings
$env:GIT_CONFIG_COUNT = '1'; $env:GIT_CONFIG_KEY_0 = 'core.autocrlf'; $env:GIT_CONFIG_VALUE_0 = 'false'

if (-not (Test-Path "$LeafDir\.git")) {
    git clone --filter=blob:none https://github.com/Winds-Studio/Leaf.git $LeafDir
    if ($LASTEXITCODE -ne 0) { throw 'git clone failed' }
}
git -C $LeafDir checkout -q $LeafCommit
if ($LASTEXITCODE -ne 0) { throw 'git checkout failed' }
Push-Location $LeafDir
try {
    .\gradlew.bat applyAllPatches
    if ($LASTEXITCODE -ne 0) { throw 'Leaf applyAllPatches failed' }
} finally {
    Pop-Location
}

if (Test-Path $Target) { Remove-Item -Recurse -Force $Target }
New-Item -ItemType Directory -Force $Target | Out-Null
Copy-Item -Recurse "$LeafDir\leaf-server\src\minecraft\*" $Target
if (Test-Path "$Target\.git") { Remove-Item -Recurse -Force "$Target\.git" }

Get-Content "$Root\patches\minecraft-deleted.txt" | Where-Object { $_ } | ForEach-Object {
    $file = Join-Path $Target $_
    if (Test-Path $file) { Remove-Item -Force $file }
}

Push-Location $Root
try {
    # one combined file: thousands of paths would overflow the Windows command line, byte copy keeps UTF-8 intact
    $combined = "$Root\build\watermc-minecraft.patch"
    $out = [System.IO.File]::Create($combined)
    try {
        Get-ChildItem -Recurse "$Root\patches\minecraft" -Filter *.patch | Sort-Object FullName | ForEach-Object {
            $bytes = [System.IO.File]::ReadAllBytes($_.FullName)
            $out.Write($bytes, 0, $bytes.Length)
        }
    } finally {
        $out.Close()
    }
    git apply --unsafe-paths --directory=watermc-server/src/minecraft --whitespace=nowarn -p1 -- $combined
    if ($LASTEXITCODE -ne 0) { throw 'git apply failed' }
} finally {
    Pop-Location
}
Write-Host 'watermc-server/src/minecraft is ready'
