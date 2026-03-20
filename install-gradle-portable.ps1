# Gradle Portable Installer for Daily Report Plugin
# This script downloads and configures Gradle 8.5 automatically

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Gradle Portable Installer" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$gradleVersion = "8.5"
$gradleDir = Join-Path $PSScriptRoot "gradle-$gradleVersion"
$downloadPath = Join-Path $env:TEMP "gradle-$gradleVersion-bin.zip"
$mirrorUrl = "https://mirrors.cloud.tencent.com/gradle/gradle-$gradleVersion-bin.zip"
$officialUrl = "https://services.gradle.org/distributions/gradle-$gradleVersion-bin.zip"

Write-Host "[INFO] Will install to: $gradleDir" -ForegroundColor Green
Write-Host ""

# Step 1: Download
Write-Host "[Step 1] Downloading Gradle $gradleVersion..." -ForegroundColor Yellow
try {
    Write-Host "Trying Tencent mirror..." -ForegroundColor Gray
    Invoke-WebRequest -Uri $mirrorUrl -OutFile $downloadPath -UseBasicParsing -ErrorAction Stop
    Write-Host "Download successful!" -ForegroundColor Green
} catch {
    Write-Host "Mirror failed, trying official source..." -ForegroundColor Gray
    try {
        Invoke-WebRequest -Uri $officialUrl -OutFile $downloadPath -UseBasicParsing -ErrorAction Stop
        Write-Host "Download successful!" -ForegroundColor Green
    } catch {
        Write-Host "[ERROR] Download failed!" -ForegroundColor Red
        Write-Host "Please download manually from:" -ForegroundColor Yellow
        Write-Host "  $mirrorUrl" -ForegroundColor Cyan
        pause
        exit 1
    }
}

# Step 2: Extract
Write-Host ""
Write-Host "[Step 2] Extracting files..." -ForegroundColor Yellow
if (Test-Path $gradleDir) {
    Remove-Item -Path $gradleDir -Recurse -Force
}

Expand-Archive -Path $downloadPath -DestinationPath $PSScriptRoot -Force
Write-Host "Extraction completed!" -ForegroundColor Green

# Step 3: Create launcher
Write-Host ""
Write-Host "[Step 3] Creating launcher script..." -ForegroundColor Yellow

$launcherContent = @"
@echo off
setlocal

set GRADLE_HOME=%~dp0gradle-$gradleVersion
set PATH=%GRADLE_HOME%\bin;%PATH%

"%GRADLE_HOME%\bin\gradle.bat" %*
"@

$launcherPath = Join-Path $PSScriptRoot "my-gradle.bat"
$launcherContent | Out-File -FilePath $launcherPath -Encoding ASCII
Write-Host "Launcher created: my-gradle.bat" -ForegroundColor Green

# Step 4: Cleanup
Write-Host ""
Write-Host "[Step 4] Cleaning up..." -ForegroundColor Yellow
if (Test-Path $downloadPath) {
    Remove-Item -Path $downloadPath -Force
}

# Final message
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Installation Complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Gradle Portable installed to:" -ForegroundColor White
Write-Host "  $gradleDir" -ForegroundColor Cyan
Write-Host ""
Write-Host "Usage:" -ForegroundColor White
Write-Host "  Double-click: my-gradle.bat" -ForegroundColor Cyan
Write-Host "  Or command: .\my-gradle.bat buildPlugin" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next step:" -ForegroundColor White
Write-Host "  .\my-gradle.bat clean buildPlugin" -ForegroundColor Cyan
Write-Host ""

pause
