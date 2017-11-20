variable "credentials-subscription-id-string" {
  description = "Azure Subscription ID."
}

variable "credentials-tenant-id-string" {
  description = "Azure Tenant ID."
}

variable "credentials-client-id-string" {
  description = "Azure Client ID."
}

variable "credentials-client-secret-string" {
  description = "Azure Client Secret."
}

variable "general-hostname-string" {
  description = "Name of the machine to create."
}

variable "general-region-string" {
  description = "Region where the resources should exist."
  default = "westeurope"
}

variable "vm-size-string" {
  description = "Size of the vm to create."
  default = "Standard_A0"
}

variable "vm-username-string" {
  description = "Administrator user name."
  default = "vmadmin"
}

variable "vm-password-string" {
  description = "Administrator password (recommended to disable password auth)."
  default = "notused"
}

variable "vm-disable-password-authentication-boolean" {
  description = "Disable password authentication for vm (recommended to keep enabled)."
  default = true
}

variable "image-publisher-string" {
  description = "Name of the publisher of the image (az vm image list)."
  default = "Canonical"
}

variable "image-offer-string" {
  description = "The name of the offer (az vm image list)."
  default = "UbuntuServer"
}

variable "image-sku-string" {
  description = "Image sku to apply (az vm image list)."
  default = "16.04-LTS"
}

variable "image-version-string" {
  description = "Version of the image to apply (az vm image list)."
  default = "latest"
}

variable "storage-account-tier-string" {
  description = "Storage account Tier for the cluster (e.g. Standard or Premium)."
  default = "Standard"
}

variable "storage-replication-type-string" {
  description = "Storage account replication type for the cluster (e.g. LRS, GRS etc)."
  default = "LRS"
}

variable "network-vnet-address-space-string" {
  description = "Full address space allowed to the virtual network."
  default = "10.0.0.0/16"
}

variable "network-subnet-address-space-string" {
  description = "The subset of the virtual network for this subnet."
  default = "10.0.10.0/24"
}

variable "network-incoming-port-range-string" {
  description = "The allowed port change for incoming connections."
  default = "1-1024"
}

variable "bootstrap-public-key-file" {
  description = "Public key file for SSH access."
}

variable "bootstrap-private-key-file" {
  description = "Private key file for SSH access."
}

variable "bootstrap-script-file" {
  description = "Bootstrap script file to execute after vm has been created."
}