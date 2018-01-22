provider "vsphere" {
  vsphere_server = "${var.vcenter_hostname}"
  user = "${var.vcenter_username}"
  password = "${var.vcenter_password}"
  allow_unverified_ssl = "true"
  version = "1.2.0"
}

locals {
  is_linux = "${replace(var.image_name, "Linux", "") != var.image_name ? 1 : 0}"
  linux_temp_folder_path = "/tmp"
  linux_script_folder_name = "linux_scripts"
  linux_script_folder_path = "${local.linux_temp_folder_path}/${local.linux_script_folder_name}"
  linux_prepare_script_path = "${local.linux_script_folder_path}/prepare.sh"
  linux_user_script_path = "${local.linux_script_folder_path}/user.sh"
  linux_cleanup_script_path = "${local.linux_script_folder_path}/cleanup.sh"
  
  is_windows = "${replace(var.image_name, "Windows", "") != var.image_name ? 1 : 0}"
  windows_temp_folder_path = "C:\\"
  windows_script_folder_name = "windows_scripts"      
  windows_script_folder_path = "${local.windows_temp_folder_path}\\${local.windows_script_folder_name}"
  windows_prepare_script_path = "${local.windows_script_folder_path}\\prepare.ps1"
  windows_user_script_path = "${local.windows_script_folder_path}\\user.ps1"
  windows_cleanup_script_path = "${local.windows_script_folder_path}\\cleanup.ps1"
}

data "vsphere_datacenter" "dc" {
  name = "${var.vcenter_datacenter}"
}

data "vsphere_datastore" "datastore" {
  name          = "${var.vcenter_datastore}"
  datacenter_id = "${data.vsphere_datacenter.dc.id}"
}

data "vsphere_resource_pool" "pool" {
  name          = "${var.vcenter_resource_pool}"
  datacenter_id = "${data.vsphere_datacenter.dc.id}"
}

data "vsphere_network" "network" {
  name          = "${var.vcenter_network}"
  datacenter_id = "${data.vsphere_datacenter.dc.id}"
}

data "vsphere_virtual_machine" "template" {
  name          = "${lookup(local.image_templates_map, var.image_name)}"
  datacenter_id = "${data.vsphere_datacenter.dc.id}"
}

resource "vsphere_virtual_machine" "linux" {

  count = "${local.is_linux}"
  name = "${var.random_id}"
  resource_pool_id = "${data.vsphere_resource_pool.pool.id}"
  datastore_id     = "${data.vsphere_datastore.datastore.id}"
  num_cpus = "${var.vm_cores}"
  memory = "${var.vm_ram}"
  guest_id = "${data.vsphere_virtual_machine.template.guest_id}"
  folder = "${var.vcenter_target_folder}"

  network_interface {
    network_id = "${data.vsphere_network.network.id}"
  }

  disk {
    name = "${var.random_id}.vmdk"
    size = "${var.vm_storage}"
  }

  clone {
    template_uuid = "${data.vsphere_virtual_machine.template.id}"    
  }  
  
  connection {
    type = "ssh"
    agent = false  
    host = "${vsphere_virtual_machine.linux.guest_ip_addresses.0}"
    user = "${local.linux_default_username}" 
    password = "${local.linux_default_password}"     
    timeout = "1m"      
  }
  
  provisioner "file" {
    source      = "${local.linux_script_folder_name}"
    destination = "${local.linux_temp_folder_path}"  
  }
  
  provisioner "file" {
    source      = "${var.script_file}"
    destination = "${local.linux_user_script_path}"  
  }
  
  provisioner "remote-exec" {
    inline = [
      "echo '${local.linux_default_password}' | sudo -S bash ${local.linux_prepare_script_path}",
      "echo '${local.linux_default_password}' | sudo -S bash ${local.linux_user_script_path}",
      "echo '${local.linux_default_password}' | sudo -S bash ${local.linux_cleanup_script_path} '${var.username}' '${var.password}' '${file("${var.public_key_file}")}'",
      "rm -rf ${local.linux_script_folder_path}"
    ]
  }  
}

resource "vsphere_virtual_machine" "windows" {

  count = "${local.is_windows}"
  name = "${var.random_id}"
  resource_pool_id = "${data.vsphere_resource_pool.pool.id}"
  datastore_id     = "${data.vsphere_datastore.datastore.id}"

  num_cpus = "${var.vm_cores}"
  memory = "${var.vm_ram}"
  guest_id = "${data.vsphere_virtual_machine.template.guest_id}"
  folder = "${var.vcenter_target_folder}"

  network_interface {
    network_id = "${data.vsphere_network.network.id}"
  }

  disk {
    name = "${var.random_id}.vmdk"
    size = "${var.vm_storage}"
  }

  clone {
    template_uuid = "${data.vsphere_virtual_machine.template.id}"    
  }  
  
  connection {
    type = "winrm"
    host = "${vsphere_virtual_machine.windows.guest_ip_addresses.0}"
    user = "${local.windows_default_username}" 
    password = "${local.windows_default_password}"          
    timeout = "10m"      
  }

  provisioner "file" {
    source      = "${local.windows_script_folder_name}"
    destination = "${local.windows_script_folder_path}"  
  }

  provisioner "file" {
    source = "${var.script_file}"
    destination = "${local.windows_user_script_path}" 
  }  

  provisioner "remote-exec" {
    inline = [
      "Powershell.exe -ExecutionPolicy Unrestricted -File ${local.windows_prepare_script_path}",      
      "Powershell.exe -ExecutionPolicy Unrestricted -File ${local.windows_user_script_path}",
      "Powershell.exe -ExecutionPolicy Unrestricted -File ${local.windows_cleanup_script_path} ${var.username} ${var.password}",
      "Powershell.exe -ExecutionPolicy Unrestricted -Command Remove-Item ${local.windows_script_folder_path} -Force -Recurse"      
    ]
  }
}