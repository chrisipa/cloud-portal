# get max partition size
$size = Get-PartitionSupportedSize -DriveLetter C

# resize partition to 100% disk space
Resize-Partition -DriveLetter C -Size $size.SizeMax