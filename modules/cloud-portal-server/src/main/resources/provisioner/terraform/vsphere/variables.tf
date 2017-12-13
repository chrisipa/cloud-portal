variable "title" {}
variable "description" {}
variable "vcenter_hostname" {}
variable "vcenter_image_folder" {}
variable "vcenter_target_folder" {}
variable "vcenter_username" {}
variable "vcenter_password" {}
variable "image_name" {}
variable "vm_cores" {}
variable "vm_ram" {}
variable "username" {}
variable "password" {}
variable "script_file" {}

variable "image_templates_map" {
  type = "map"
  default = {
    "Ubuntu Server Linux 16.04" = "TPL_UBUNTU_SERVER_16.04.3_LTS"
    "Windows Server 2016" = "TPL_WIN_2016"
  }
}