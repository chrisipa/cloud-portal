resource "azurerm_resource_group" "rg" {
  name     = "${var.general-hostname-string}-rg"
  location = "${var.general-location-string}"
}

resource "azurerm_virtual_network" "vnet" {
  name                = "${var.general-hostname-string}vnet"
  location            = "${var.general-location-string}"
  address_space       = ["${var.network-vnet-address-space-string}"]
  resource_group_name = "${azurerm_resource_group.rg.name}"
}

resource "azurerm_subnet" "subnet" {
  name                 = "${var.general-hostname-string}subnet"
  virtual_network_name = "${azurerm_virtual_network.vnet.name}"
  resource_group_name  = "${azurerm_resource_group.rg.name}"
  address_prefix       = "${var.network-subnet-address-space-string}"
}

resource "azurerm_network_security_group" "nsg" {
  name                = "${var.general-hostname-string}nsg"
  location            = "${var.general-location-string}"
  resource_group_name = "${azurerm_resource_group.rg.name}"
}

resource "azurerm_network_security_rule" "ruleports" {
  name                        = "${var.general-hostname-string}ruleports"
  priority                    = 100
  direction                   = "Inbound"
  access                      = "Allow"
  protocol                    = "Tcp"
  source_port_range           = "*"
  destination_port_range      = "${var.network-incoming-port-range-string}"
  source_address_prefix       = "*"
  destination_address_prefix  = "*"
  resource_group_name         = "${azurerm_resource_group.rg.name}"
  network_security_group_name = "${azurerm_network_security_group.nsg.name}"
}

resource "azurerm_network_interface" "nic" {
  name                      = "${var.general-hostname-string}nic"
  location                  = "${var.general-location-string}"
  resource_group_name       = "${azurerm_resource_group.rg.name}"
  network_security_group_id = "${azurerm_network_security_group.nsg.id}"

  ip_configuration {
    name                          = "${var.general-hostname-string}ipconfig"
    subnet_id                     = "${azurerm_subnet.subnet.id}"
    private_ip_address_allocation = "dynamic"
    public_ip_address_id          = "${azurerm_public_ip.pip.id}"
  }

  depends_on = ["azurerm_network_security_group.nsg"]
}

resource "azurerm_public_ip" "pip" {
  name                         = "${var.general-hostname-string}-ip"
  location                     = "${var.general-location-string}"
  resource_group_name          = "${azurerm_resource_group.rg.name}"
  public_ip_address_allocation = "dynamic"
  domain_name_label            = "${var.general-hostname-string}"
}

resource "azurerm_storage_account" "stor" {
  name                     = "${var.general-hostname-string}stor"
  location                 = "${var.general-location-string}"
  resource_group_name      = "${azurerm_resource_group.rg.name}"
  account_tier             = "${var.storage-account-tier-string}"
  account_replication_type = "${var.storage-replication-type-string}"  
}

resource "azurerm_storage_container" "storc" {
  name                  = "${var.general-hostname-string}-vhds"
  resource_group_name   = "${azurerm_resource_group.rg.name}"
  storage_account_name  = "${azurerm_storage_account.stor.name}"
  container_access_type = "private"
}

resource "azurerm_virtual_machine" "vm" {
  name                  = "${var.general-hostname-string}vm"
  location              = "${var.general-location-string}"
  resource_group_name   = "${azurerm_resource_group.rg.name}"
  vm_size               = "${var.vm-size-string}"
  network_interface_ids = ["${azurerm_network_interface.nic.id}"]

  storage_image_reference {
    publisher = "${var.image-publisher-string}"
    offer     = "${var.image-offer-string}"
    sku       = "${var.image-sku-string}"
    version   = "${var.image-version-string}"
  }

  storage_os_disk {
    name          = "${var.general-hostname-string}osdisk"
    vhd_uri       = "${azurerm_storage_account.stor.primary_blob_endpoint}${azurerm_storage_container.storc.name}/${var.general-hostname-string}osdisk.vhd"
    caching       = "ReadWrite"
    create_option = "FromImage"
  }

  os_profile {
    computer_name  = "${var.general-hostname-string}"
    admin_username = "${var.vm-username-string}"
    admin_password = "${var.vm-password-string}"
  }

  os_profile_linux_config {
    disable_password_authentication = "${var.vm-disable-password-authentication-boolean}"

    ssh_keys = [{
      path     = "/home/${var.vm-username-string}/.ssh/authorized_keys"
      key_data = "${file("${var.bootstrap-public-key-file}")}"
    }]
  }

  connection {
    type = "ssh"
    agent = false  
    host = "${azurerm_public_ip.pip.fqdn}"
    user = "${var.vm-username-string}"      
    private_key = "${file("${var.bootstrap-private-key-file}")}"
    timeout = "1m"      
  }

  provisioner "file" {
    source      = "${var.bootstrap-script-file}"
    destination = "/tmp/bootstrap.sh"                
  }

  provisioner "remote-exec" {
    inline = [
      "bash /tmp/bootstrap.sh",
      "rm /tmp/bootstrap.sh"
    ]
  }

  depends_on = ["azurerm_storage_account.stor"]
}

output "vm-username-string" {
  value = "${var.vm-username-string}"
}

output "vm_fqdn" {  
  value = "${azurerm_public_ip.pip.fqdn}"
}