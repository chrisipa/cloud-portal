# get script folder
$scriptFolder = Split-Path -Path $MyInvocation.MyCommand.Definition -Parent

# resize disk
& $scriptFolder\disk-resize.ps1