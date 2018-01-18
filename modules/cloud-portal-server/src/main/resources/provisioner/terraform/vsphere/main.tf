provider "vsphere" {
  vsphere_server = "${var.vcenter_hostname}"
  user = "${var.vcenter_username}"
  password = "${var.vcenter_password}"
  allow_unverified_ssl = "true"
  version = "1.2.0"
}

locals {

  image_templates_map = {
    "Ubuntu Server Linux 16.04" = "TPL_UBUNTU_SERVER_16.04.3_LTS"
    "Windows Server 2016" = "TPL_WIN_2016"  
  }

  is_linux = "${replace(var.image_name, "Linux", "") != var.image_name ? 1 : 0}"
  is_windows = "${replace(var.image_name, "Windows", "") != var.image_name ? 1 : 0}"
  linux_script_path = "/tmp/bootstrap.sh"
  windows_script_path = "C:\\bootstrap.ps1"    
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
    user = "${var.username}" 
    password = "${var.password}"     
    timeout = "1m"      
  }
  
  provisioner "file" {
    source      = "${var.script_file}"
    destination = "${local.linux_script_path}"  
  }

  provisioner "remote-exec" {
    inline = [
      "echo '${var.password}' | sudo -S bash ${local.linux_script_path}",
      "rm ${local.linux_script_path}"
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
    user = "${var.username}"
    password = "${var.password}"          
    timeout = "10m"      
  }

  provisioner "file" {
    source = "${var.script_file}"
    destination = "${local.windows_script_path}" 
  }  

  provisioner "remote-exec" {
    inline = [
      "Powershell.exe -ExecutionPolicy Unrestricted -File ${local.windows_script_path}",
      "del ${local.windows_script_path}"      
    ]
  }
}