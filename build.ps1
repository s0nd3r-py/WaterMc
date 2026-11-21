param([switch]$Verify)

$ErrorActionPreference = 'Stop'
if (Test-Path -LiteralPath 'D:\water-mc\env.sh') {
    $env:GRADLE_USER_HOME = 'D:\water-mc\gradle-home'
    $env:TMP = 'D:\water-mc\tmp'
    $env:TEMP = 'D:\water-mc\tmp'
    $env:GRADLE_OPTS = '-Djava.io.tmpdir=D:/water-mc/tmp'
}

$buildTasks = @(':watermc-server:createWaterMcJar')
if ($Verify) {
    $buildTasks += @(':watermc-server:runWaterTests', ':watermc-server:runWaterCompatibilityTests', ':watermc-server:scanJarForBadCalls')
}
Push-Location -LiteralPath $PSScriptRoot
try {
    & (Join-Path $PSScriptRoot 'gradlew.bat') @buildTasks --console=plain
    $buildExitCode = $LASTEXITCODE
} finally {
    Pop-Location
}
exit $buildExitCode
