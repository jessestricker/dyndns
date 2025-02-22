# dyndns

## dyndns-broker

### Install

```shell
version='1.0.0'
cd "$(mktemp --directory)" 
wget "https://github.com/jessestricker/dyndns/releases/download/v${version}/dyndns-broker-${version}.zip"
sudo unzip "dyndns-broker-${version}.zip" -d /opt
sudo mv "/opt/dyndns-broker-${version}" /opt/dyndns-broker

sudo adduser --system dyndns-broker
sudo systemctl enable --now /opt/dyndns-broker/etc/dyndns-broker.service
```

### Uninstall

```shell
sudo systemctl disable --now dyndns-broker.service
sudo rm -rf /opt/dyndns-broker
sudo deluser --system dyndns-broker
```
