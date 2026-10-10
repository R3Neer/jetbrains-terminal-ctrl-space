param([Parameter(Mandatory)][string]$IdePath, [ValidateSet('test','buildPlugin','runIde')][string]$Task = 'buildPlugin')
$ErrorActionPreference = 'Stop'
$priorJava = $env:JAVA_HOME
$priorGradle = $env:GRADLE_USER_HOME
Push-Location $PSScriptRoot
try {
  $runtime = Join-Path $IdePath 'jbr'
  if (-not (Test-Path -LiteralPath (Join-Path $runtime 'bin\java.exe'))) { throw 'IDE path must contain its jbr runtime.' }
  $env:JAVA_HOME = $runtime
  $env:GRADLE_USER_HOME = Join-Path $PSScriptRoot '.gradle-user-home'
  & "$runtime\bin\java.exe" -classpath gradle/wrapper/gradle-wrapper.jar org.gradle.wrapper.GradleWrapperMain $Task --console=plain "-PplatformLocalPath=$IdePath"
  if ($LASTEXITCODE) { throw "Gradle exited $LASTEXITCODE" }
} finally { $env:JAVA_HOME=$priorJava; $env:GRADLE_USER_HOME=$priorGradle; Pop-Location }
