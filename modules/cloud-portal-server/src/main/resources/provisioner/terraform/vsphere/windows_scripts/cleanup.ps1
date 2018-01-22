# read parameters
$username=$args[0]
$password=$args[1]

# get script folder
$scriptFolder = Split-Path -Path $MyInvocation.MyCommand.Definition -Parent

# create user
& $scriptFolder\create-user.ps1 $username $password

# get max partition size
$size = Get-PartitionSupportedSize -DriveLetter C

# resize partition to 100% disk space
Resize-Partition -DriveLetter C -Size $size.SizeMax