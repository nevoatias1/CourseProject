@echo off
setlocal
set "MAVEN_VERSION=3.9.9"
set "MAVEN_HOME=%TEMP%\courseproject-maven-%MAVEN_VERSION%\apache-maven-%MAVEN_VERSION%"
if not exist "%MAVEN_HOME%\bin\mvn.cmd" powershell -NoProfile -ExecutionPolicy Bypass -Command "$v='%MAVEN_VERSION%'; $base=Join-Path $env:TEMP ('courseproject-maven-'+$v); $home=Join-Path $base ('apache-maven-'+$v); if (-not (Test-Path (Join-Path $home 'bin/mvn.cmd'))) { New-Item -ItemType Directory -Path $base -Force | Out-Null; $archive=Join-Path $base ('apache-maven-'+$v+'-bin.zip'); Invoke-WebRequest -Uri ('https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/'+$v+'/apache-maven-'+$v+'-bin.zip') -OutFile $archive; Expand-Archive -LiteralPath $archive -DestinationPath $base -Force; Remove-Item -LiteralPath $archive -Force }"
if errorlevel 1 exit /b %errorlevel%
call "%MAVEN_HOME%\bin\mvn.cmd" %*
exit /b %errorlevel%
