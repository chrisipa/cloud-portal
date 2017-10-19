# required variables
variable "general-hostname-string" {
  description = "Name of the machine to create."
}

variable "general-location-string" {
  description = "Region where the resources should exist."
  default     = "westeurope"
}

variable "general-name-prefix-string" {
  description = "Unique part of the name to give to resources."
}

variable "network-vnet-address-space-string" {
  description = "full address space allowed to the virtual network"
  default     = "10.0.0.0/16"
}

variable "network-subnet-address-space-string" {
  description = "the subset of the virtual network for this subnet"
  default     = "10.0.10.0/24"
}

variable "storage-account-tier-string" {
  description = "Storage account Tier for the cluster (e.g. Standard or Premium)"
  default     = "Standard"
}

variable "storage-replication-type-string" {
  description = "Storage account replication type for the cluster (e.g. LRS, GRS etc)"
  default     = "LRS"
}

variable "image-publisher-string" {
  description = "Name of the publisher of the image (az vm image list)."
  default     = "Canonical"
}

variable "image-offer-string" {
  description = "The name of the offer (az vm image list)."
  default     = "UbuntuServer"
}

variable "image-sku-string" {
  description = "Image sku to apply (az vm image list)."
  default     = "16.04-LTS"
}

variable "image-version-string" {
  description = "Version of the image to apply (az vm image list)."
  default     = "latest"
}

variable "vm-size-string" {
  description = "Size of the vm to create."
  default     = "Standard_A0"
}

variable "vm-username-string" {
  description = "Administrator user name."
  default     = "vmadmin"
}

variable "vm-password-string" {
  description = "Administrator password (recommended to disable password auth)."
  default     = "notused"
}

variable "vm-disable-password-authentication-boolean" {
  description = "Disable password authentication for vm (recommended to keep enabled)."
  default     = true
}

variable "bootstrap-public-key-file" {
  description = "public key file for ssh access"
}

variable "bootstrap-private-key-file" {
  description = "private key file for ssh access"
}

variable "bootstrap-script-file" {
  description = "bootstrap script file to execute after vm has been created"
}