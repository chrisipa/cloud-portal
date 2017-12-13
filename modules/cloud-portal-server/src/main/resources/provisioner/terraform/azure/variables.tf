variable "title" {}
variable "description" {}
variable "subscription_id" {}
variable "tenant_id" {}
variable "client_id" {}
variable "client_secret" {}
variable "region" {}
variable "image_name" {}
variable "vm_size" {}
variable "username" {}
variable "password" {}
variable "storage_account_tier" {}
variable "storage_account_replication_type" {}
variable "vnet_address_space" {}
variable "subnet_address_space" {}
variable "incoming_ports" {}
variable "public_key_file" {}
variable "private_key_file" {}
variable "script_file" {}

variable "image_names_map" {
  type = "map"
  default = {
    "Ubuntu Server Linux 16.04" = "Canonical:UbuntuServer:16.04-LTS:latest"
    "Windows Server 2016" = "MicrosoftWindowsServer:WindowsServer:2016-Datacenter:latest"
  }
}