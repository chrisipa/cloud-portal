variable "title" {}
variable "description" {}
variable "access_key" {}
variable "secret_key" {}
variable "region" {}
variable "availability_zone" {}
variable "expiration_days" {}
variable "vm_size" {}
variable "password" {}
variable "storage_size" {}
variable "storage_type" {}
variable "incoming_ports" {}
variable "public_key_file" {}
variable "private_key_file" {}
variable "script_file" {}
variable "image_name" {}

variable "image_names_map" {
  type = "map"
  default = {
    "Ubuntu Server Linux 16.04" = "ubuntu/images/hvm-ssd/ubuntu-xenial-16.04-amd64-server-*"
    "Windows Server 2016" = "Windows_Server-2016-English-Full-Base-*"
  }
}

variable "image_owners_map" {
  type = "map"
  default = {
    "Ubuntu Server Linux 16.04" = "099720109477"
    "Windows Server 2016" = "801119661308"
  }
}