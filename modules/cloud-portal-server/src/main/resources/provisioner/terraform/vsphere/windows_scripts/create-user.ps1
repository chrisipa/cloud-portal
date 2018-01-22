# read paramters
$username=$args[0]
$password=$args[1]

# configuration
$group = "Administrators"

# create user
Write-Host "Creating new local user $username."
& NET USER $username $password /add /y /expires:never
    
#  add user to admin group
Write-Host "Adding local user $username to $group."
& NET LOCALGROUP $group $username /add

# set password expire to false
Write-Host "Ensuring password for $username never expires."
& WMIC USERACCOUNT WHERE "Name='$username'" SET PasswordExpires=FALSE