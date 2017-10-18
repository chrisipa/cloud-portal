# required variables
variable "hostname" {
  description = "name of the machine to create"
}

variable "name_prefix" {
  description = "unique part of the name to give to resources"
}

variable "ssh_public_key_file" {
  description = "public key file for ssh access"
}

variable "ssh_private_key_file" {
  description = "private key file for ssh access"
}

variable "bootstrap_script_file" {
  description = "bootstrap script file to execute after vm has been created"
}

# optional variables
variable "location" {
  description = "region where the resources should exist"
  default     = "westeurope"
}

variable "vnet_address_space" {
  description = "full address space allowed to the virtual network"
  default     = "10.0.0.0/16"
}

variable "subnet_address_space" {
  description = "the subset of the virtual network for this subnet"
  default     = "10.0.10.0/24"
}

variable "storage_account_tier" {
  description = "Storage account Tier for the cluster (e.g. Standard or Premium)"
  default     = "Standard"
}

variable "storage_replication_type" {
  description = "Storage account replication type for the cluster (e.g. LRS, GRS etc)"
  default     = "LRS"
}

variable "vm_size" {
  description = "size of the vm to create"
  default     = "Standard_A0"
}

variable "image_publisher" {
  description = "name of the publisher of the image (az vm image list)"
  default     = "Canonical"
}

variable "image_offer" {
  description = "the name of the offer (az vm image list)"
  default     = "UbuntuServer"
}

variable "image_sku" {
  description = "image sku to apply (az vm image list)"
  default     = "16.04-LTS"
}

variable "image_version" {
  description = "version of the image to apply (az vm image list)"
  default     = "latest"
}

variable "admin_username" {
  description = "administrator user name"
  default     = "vmadmin"
}

variable "admin_password" {
  description = "administrator password (recommended to disable password auth)"
  default     = "notused"
}

variable "disable_password_authentication" {
  description = "toggle for password auth (recommended to keep disabled)"
  default     = true
}