# get script folder
$scriptFolder = Split-Path -Path $MyInvocation.MyCommand.Definition -Parent

# change hostname
& $scriptFolder\hostname.ps1 $args[0]