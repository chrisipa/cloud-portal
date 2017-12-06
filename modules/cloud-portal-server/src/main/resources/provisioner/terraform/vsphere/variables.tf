variable "credentials-vcenter-hostname-string" {}
variable "credentials-vcenter-username-string" {}
variable "credentials-vcenter-password-string" {}
variable "vm-name-string" {}
variable "vm-resource-pool-string" {}
variable "vm-image-string" {}
variable "vm-vcores-string" {}
variable "vm-ram-string" {}
variable "bootstrap-username-string" {}
variable "bootstrap-password-string" {}
variable "bootstrap-script-file" {}

variable "image-templates-map" {
  type = "map"
  default = {
    "Ubuntu Server 16.04" = "TPL_UBUNTU_SERVER_16.04.3_LTS"
    "Windows Server 2016" = "TPL_WIN_2016"
  }
}