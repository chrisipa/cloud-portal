output "random_id" {
  value = "${var.random_id}"
}

output "host" {
  value = "${data.local_file.ipfile.content}"  
}

output "username" {
  value = "${var.username}"
}