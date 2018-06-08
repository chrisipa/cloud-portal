locals {
  
  linux_default_username = "devops"
  linux_default_password = "devops"
  windows_default_username = "Administrator"
  windows_default_password = "DevOps2017"
  
  image_templates_map = {
    "Ubuntu Server Linux 16.04" = "output-ubuntu-template/disk.vmdk"
    "Windows Server 2016" = "output-windows-template/disk.vmdk"  
  }
  
  image_guest_id_map = {
    "Ubuntu Server Linux 16.04" = "ubuntu64Guest"
    "Windows Server 2016" = "windows9Server64Guest"  
  }
}