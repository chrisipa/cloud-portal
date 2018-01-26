provider "null" {
  version = "1.0.0"
}

provider "azurerm" {
  subscription_id = "${var.subscription_id}"
  tenant_id = "${var.tenant_id}"
  client_id = "${var.client_id}"
  client_secret = "${var.client_secret}"
  version = "0.3.0"
}

locals {

  image_names_map = {
    "Ubuntu Server Linux 16.04" = "Canonical:UbuntuServer:16.04-LTS:latest"
    "Windows Server 2016" = "MicrosoftWindowsServer:WindowsServer:2016-Datacenter:latest"
  }

  image_publisher = "${element(split(":", lookup(local.image_names_map, var.image_name)), 0)}"
  image_offer = "${element(split(":", lookup(local.image_names_map, var.image_name)), 1)}"
  image_sku = "${element(split(":", lookup(local.image_names_map, var.image_name)), 2)}"
  image_version = "${element(split(":", lookup(local.image_names_map, var.image_name)), 3)}"
  
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
  
  allow_win_rm_file = "allow-winrm.cmd"
  allow_win_rm_url = "https://raw.githubusercontent.com/chrisipa/cloud-portal/master/public/bootstrap/${local.allow_win_rm_file}"

  incoming_ports_list = "${split(",", var.incoming_ports)}"
}

resource "azurerm_resource_group" "rg" {
  name = "${var.random_id}rg"
  location = "${var.region}"
  
  tags {
    Name = "${var.title}"
    Description = "${var.description}"
  }
}

resource "azurerm_virtual_network" "vnet" {
  name = "${var.random_id}vnet"
  location = "${var.region}"
  address_space = ["${var.vnet_address_space}"]
  resource_group_name = "${azurerm_resource_group.rg.name}"
  
  tags {
    Name = "${var.title}"
    Description = "${var.description}"
  }
}

resource "azurerm_subnet" "subnet" {
  name = "${var.random_id}subnet"
  virtual_network_name = "${azurerm_virtual_network.vnet.name}"
  resource_group_name = "${azurerm_resource_group.rg.name}"
  address_prefix = "${var.subnet_address_space}"
}

resource "azurerm_network_security_group" "nsg" {
  name = "${var.random_id}nsg"
  location = "${var.region}"
  resource_group_name = "${azurerm_resource_group.rg.name}"
  
  tags {
    Name = "${var.title}"
    Description = "${var.description}"
  }
}

resource "azurerm_network_security_rule" "rulessh" {
  count = "${local.is_linux}"
  name = "${var.random_id}rulessh"
  priority = 100
  direction = "Inbound"
  access = "Allow"
  protocol = "Tcp"
  source_port_range = "*"
  destination_port_range = "22"
  source_address_prefix = "*"
  destination_address_prefix = "*"
  resource_group_name = "${azurerm_resource_group.rg.name}"
  network_security_group_name = "${azurerm_network_security_group.nsg.name}"
}

resource "azurerm_network_security_rule" "rulerdp" {
  count = "${local.is_windows}"
  name = "${var.random_id}rulerdp"
  priority = 101
  direction = "Inbound"
  access = "Allow"
  protocol = "Tcp"
  source_port_range = "*"
  destination_port_range = "3389"
  source_address_prefix = "*"
  destination_address_prefix = "*"
  resource_group_name = "${azurerm_resource_group.rg.name}"
  network_security_group_name = "${azurerm_network_security_group.nsg.name}"
}

resource "azurerm_network_security_rule" "rulerm" {
  count = "${local.is_windows}"
  name = "${var.random_id}rulerm"
  priority = 102
  direction = "Inbound"
  access = "Allow"
  protocol = "Tcp"
  source_port_range = "*"
  destination_port_range = "5985"
  source_address_prefix = "*"
  destination_address_prefix = "*"
  resource_group_name = "${azurerm_resource_group.rg.name}"
  network_security_group_name = "${azurerm_network_security_group.nsg.name}"
}

resource "azurerm_network_security_rule" "rulecustom" {
  count = "${var.incoming_ports != -1 ? length(local.incoming_ports_list) : 0}"
  name = "${var.random_id}rulecustom"
  priority = "${103 + count.index}"
  direction = "Inbound"
  access = "Allow"
  protocol = "Tcp"
  source_port_range = "*"
  destination_port_range = "${element(local.incoming_ports_list, count.index)}"
  source_address_prefix = "*"
  destination_address_prefix = "*"
  resource_group_name = "${azurerm_resource_group.rg.name}"
  network_security_group_name = "${azurerm_network_security_group.nsg.name}"
}

resource "azurerm_network_interface" "nic" {
  name = "${var.random_id}nic"
  location = "${var.region}"
  resource_group_name = "${azurerm_resource_group.rg.name}"
  network_security_group_id = "${azurerm_network_security_group.nsg.id}"

  ip_configuration {
    name = "${var.random_id}ipconfig"
    subnet_id = "${azurerm_subnet.subnet.id}"
    private_ip_address_allocation = "dynamic"
    public_ip_address_id = "${azurerm_public_ip.pip.id}"
  }
  
  tags {
    Name = "${var.title}"
    Description = "${var.description}"
  }

  depends_on = ["azurerm_network_security_group.nsg"]
}

resource "azurerm_public_ip" "pip" {
  name = "${var.random_id}ip"
  location = "${var.region}"
  resource_group_name = "${azurerm_resource_group.rg.name}"
  public_ip_address_allocation = "dynamic"
  domain_name_label = "d${var.random_id}"
  
  tags {
    Name = "${var.title}"
    Description = "${var.description}"
  }
}

resource "azurerm_storage_account" "stor" {
  name = "${var.random_id}stor"
  location = "${var.region}"
  resource_group_name = "${azurerm_resource_group.rg.name}"
  account_tier = "${var.storage_account_tier}"
  account_replication_type = "${var.storage_account_replication_type}"  
  
  tags {
    Name = "${var.title}"
    Description = "${var.description}"
  }
}

resource "azurerm_storage_container" "storc" {
  name = "${var.random_id}vhds"
  resource_group_name = "${azurerm_resource_group.rg.name}"
  storage_account_name = "${azurerm_storage_account.stor.name}"
  container_access_type = "private"
}

resource "azurerm_virtual_machine" "linux" {

  count = "${local.is_linux}"
  name = "${var.random_id}vm"
  location = "${var.region}"
  resource_group_name = "${azurerm_resource_group.rg.name}"
  vm_size = "${var.vm_size}"
  network_interface_ids = ["${azurerm_network_interface.nic.id}"]

  storage_image_reference {
    publisher = "${local.image_publisher}"
    offer = "${local.image_offer}"
    sku = "${local.image_sku}"
    version = "${local.image_version}"
  }

  storage_os_disk {
    name = "${var.random_id}osdisk"
    vhd_uri = "${azurerm_storage_account.stor.primary_blob_endpoint}${azurerm_storage_container.storc.name}/${var.random_id}osdisk.vhd"
    caching = "ReadWrite"
    create_option = "FromImage"
  }

  os_profile {
    computer_name = "${var.random_id}"
    admin_username = "${var.username}"
    admin_password = "${var.password}"
  }

  os_profile_linux_config {
    disable_password_authentication = "true"

    ssh_keys = [{
      path = "/home/${var.username}/.ssh/authorized_keys"
      key_data = "${file("${var.public_key_file}")}"
    }]
  }

  connection {
    type = "ssh"
    agent = false  
    host = "${azurerm_public_ip.pip.fqdn}"
    user = "${var.username}"      
    private_key = "${file("${var.private_key_file}")}"
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
      "bash '${local.linux_prepare_script_path}'",
      "bash '${local.linux_user_script_path}'",
      "bash '${local.linux_cleanup_script_path}'",
      "rm -rf ${local.linux_script_folder_path}"
    ]
  }  
  
  tags {
    Name = "${var.title}"
    Description = "${var.description}"
  }

  depends_on = ["azurerm_storage_account.stor"]
}

resource "azurerm_virtual_machine" "windows" {

  count = "${local.is_windows}"
  name = "${var.random_id}vm"
  location = "${var.region}"
  resource_group_name = "${azurerm_resource_group.rg.name}"
  vm_size = "${var.vm_size}"
  network_interface_ids = ["${azurerm_network_interface.nic.id}"]

  storage_image_reference {
    publisher = "${local.image_publisher}"
    offer = "${local.image_offer}"
    sku = "${local.image_sku}"
    version = "${local.image_version}"
  }

  storage_os_disk {
    name = "${var.random_id}osdisk"
    vhd_uri = "${azurerm_storage_account.stor.primary_blob_endpoint}${azurerm_storage_container.storc.name}/${var.random_id}osdisk.vhd"
    caching = "ReadWrite"
    create_option = "FromImage"
  }

  os_profile {
    computer_name = "${var.random_id}"
    admin_username = "${var.username}"
    admin_password = "${var.password}"
  }
  
  os_profile_windows_config {
    enable_automatic_upgrades = true
    provision_vm_agent = true
  }
  
  tags {
    Name = "${var.title}"
    Description = "${var.description}"
  }  

  depends_on = ["azurerm_storage_account.stor"]
}

resource "azurerm_virtual_machine_extension" "windowsvmext" {
  
  count = "${local.is_windows}"
  name = "${var.random_id}vmext"
  location = "${var.region}"
  resource_group_name = "${azurerm_resource_group.rg.name}"  
  virtual_machine_name = "${azurerm_virtual_machine.windows.name}"  
  publisher = "Microsoft.Compute"
  type = "CustomScriptExtension"
  type_handler_version = "1.8"
  
  settings = <<SETTINGS
  {
    "fileUris": [
      "${local.allow_win_rm_url}"
    ],
    "commandToExecute": "cmd.exe /c ${local.allow_win_rm_file}"
  }
SETTINGS

  depends_on = ["azurerm_virtual_machine.windows"]
}

resource "null_resource" "windowsprovisioning" {
  
  count = "${local.is_windows}"
  
  connection {
    type = "winrm"
    host = "${azurerm_public_ip.pip.fqdn}"
    user = "${var.username}"
    password = "${var.password}"          
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
      "Powershell.exe -ExecutionPolicy Unrestricted -File ${local.windows_cleanup_script_path}",
      "Powershell.exe -ExecutionPolicy Unrestricted -Command Remove-Item ${local.windows_script_folder_path} -Force -Recurse"      
    ]
  }  
  
  depends_on = ["azurerm_virtual_machine_extension.windowsvmext"]
}