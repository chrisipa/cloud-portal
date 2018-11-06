# install chocolatey package manager
Invoke-Expression ((New-Object System.Net.WebClient).DownloadString('https://chocolatey.org/install.ps1'))

# install jenkins package
choco install jenkins -y
