@echo off
setlocal
set MAVEN_VERSION=3.9.9
set MAVEN_CACHE=%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MAVEN_VERSION%
set MAVEN_HOME=%MAVEN_CACHE%\apache-maven-%MAVEN_VERSION%
set MAVEN_URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MAVEN_VERSION%/apache-maven-%MAVEN_VERSION%-bin.zip

if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
    where powershell >nul 2>nul || (echo PowerShell is required to download Maven %MAVEN_VERSION%. 1>&2 & exit /b 1)
    if not exist "%MAVEN_CACHE%" mkdir "%MAVEN_CACHE%"
    powershell -NoProfile -Command "$ErrorActionPreference='Stop'; $zip=Join-Path $env:TEMP 'exercise-hash-map-maven.zip'; Invoke-WebRequest -Uri '%MAVEN_URL%' -OutFile $zip; Expand-Archive -Path $zip -DestinationPath '%MAVEN_CACHE%' -Force; Remove-Item $zip"
    if errorlevel 1 exit /b 1
)

call "%MAVEN_HOME%\bin\mvn.cmd" %*
exit /b %ERRORLEVEL%
