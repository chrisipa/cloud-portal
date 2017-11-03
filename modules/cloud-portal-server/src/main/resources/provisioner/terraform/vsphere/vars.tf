variable "credentials-vcenter-hostname-string" {
  description = "VMWare vCenter hostname."
}

variable "credentials-vcenter-username-string" {
  description = "VMWare vCenter username."
}

variable "credentials-vcenter-password-string" {
  description = "VMWare vCenter password."
}


variable "vm-name-string" {
  description = "Name of the virtual machine."  
}

variable "vm-image-string" {
  description = "Virtual machine image to use."
  default     = "DevOps EMEA POC/templates/TPL_UBUNTU_SERVER_16.04.3_LTS"
}

variable "vm-vcores-string" {
  description = "Number of virtual cores of the virtual machine."  
  default     = "1"
}

variable "vm-ram-string" {
  description = "RAM (in MB) of the virtual machine."
  default     = "1024"  
}