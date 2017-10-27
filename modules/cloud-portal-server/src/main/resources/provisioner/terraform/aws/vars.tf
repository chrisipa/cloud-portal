variable "credentials-access-key-string" {
  description = "AWS access key."
}

variable "credentials-secret-key-string" {
  description = "AWS secret key."
}

variable "general-name-string" {
  description = "Name of the virtual machine instance."
}

variable "general-region-string" {
  description = "Region where the resources should exist."
  default     = "eu-central-1"
}

variable "general-availability-zone-string" {
  description = "Availability zone where the resources should exist."
  default     = "eu-central-1a"
}

variable "vm-size-string" {
  description = "Size of the virtual machine to create."
  default     = "t2.micro"
}

variable "image-ami-string" {
  description = "Amazon machine image to use."
  default     = "ami-1e339e71"
}

variable "storage-size-string" {
  description = "Size of the virtual machine storage in GB."
  default     = "8"
}

variable "storage-type-string" {
  description = "Type of the virtual machine storage."
  default     = "standard"
}

variable "network-incoming-port-start-string" {
  description = "Start port for incoming connections."
  default     = "1"
}

variable "network-incoming-port-end-string" {
  description = "End port for incoming connections."
  default     = "1024"
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