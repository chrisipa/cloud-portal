variable "credentials-subscription-id-string" {}
variable "credentials-tenant-id-string" {}
variable "credentials-client-id-string" {}
variable "credentials-client-secret-string" {}
variable "general-region-string" {}
variable "vm-image-string" {}
variable "vm-size-string" {}
variable "vm-username-string" {}
variable "vm-password-string" {}
variable "storage-account-tier-string" {}
variable "storage-replication-type-string" {}
variable "network-vnet-address-space-string" {}
variable "network-subnet-address-space-string" {}
variable "network-incoming-port-range-string" {}
variable "bootstrap-public-key-file" {}
variable "bootstrap-private-key-file" {}
variable "bootstrap-script-file" {}
variable "title" {}
variable "description" {}

variable "image-names-map" {
  type = "map"
  default = {
    "Ubuntu Server 16.04" = "Canonical:UbuntuServer:16.04-LTS:latest"
    "Windows Server 2016" = "MicrosoftWindowsServer:WindowsServer:2016-Datacenter:latest"
  }
}