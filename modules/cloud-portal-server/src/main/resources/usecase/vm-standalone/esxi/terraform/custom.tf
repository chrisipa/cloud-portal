locals {
  
  linux_default_username = "devops"
  linux_default_password = "devops"
  windows_default_username = "Administrator"
  windows_default_password = "DevOps2017"
  
  image_templates_map = {
    "Ubuntu Server Linux 16.04" = "TPL_UBUNTU_SERVER_16_04.3_LTS/TPL_UBUNTU_SERVER_16_04.3_LTS.vmdk"
    "Windows Server 2016" = "TPL_WIN_2016/TPL_WIN_2016.vmdk"  
  }
  
  image_guest_id_map = {
    "Ubuntu Server Linux 16.04" = "ubuntu64Guest"
    "Windows Server 2016" = "windows9Server64Guest"  
  }
}