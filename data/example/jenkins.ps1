# install chocolatey package manager
Set-ExecutionPolicy Bypass -Scope Process -Force; iex ((New-Object System.Net.WebClient).DownloadString('https://chocolatey.org/install.ps1'))

# install jenkins package
Start-Process -FilePath "C:\ProgramData\chocolatey\choco.exe" -ArgumentList "install jenkins"