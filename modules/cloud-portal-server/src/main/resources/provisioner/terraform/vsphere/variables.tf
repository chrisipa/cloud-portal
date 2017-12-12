variable "credentials-vcenter-hostname-string" {}
variable "credentials-vcenter-image-folder-string" {}
variable "credentials-vcenter-target-folder-string" {}
variable "credentials-vcenter-username-string" {}
variable "credentials-vcenter-password-string" {}
variable "vm-image-string" {}
variable "vm-vcores-string" {}
variable "vm-ram-string" {}
variable "bootstrap-username-string" {}
variable "bootstrap-password-string" {}
variable "bootstrap-script-file" {}
variable "title" {}
variable "description" {}

variable "image-templates-map" {
  type = "map"
  default = {
    "Ubuntu Server Linux 16.04" = "TPL_UBUNTU_SERVER_16.04.3_LTS"
    "Windows Server 2016" = "TPL_WIN_2016"
  }
}