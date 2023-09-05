Vagrant.configure("2") do |config|
config.vm.box = "ubuntu/focal64"
config.vm.provision "shell", inline: <<-SHELL
apt-get update
# if using python
apt-get install -y python3-pip
pip3 install tree_sitter
# if using java
apt-get install -y openjdk-11-jdk gradle
SHELL
end
