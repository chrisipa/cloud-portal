output "id" {
  value = "${random_id.id.hex}"
}

output "host" {
  value = "${element(flatten(list(aws_instance.linux.*.public_dns, aws_instance.windows.*.public_dns)), 0)}"  
}

output "username" {
  value = "${replace(var.image-ami-string, "Linux", "") != var.image-ami-string ? "ubuntu" : "Administrator"}"
}