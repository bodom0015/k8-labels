# Kubernetes Node Labeler
This is a simple Groovy script that slurps up a JSON map of hostname/IP -> role.
The script then uses `kubectl label` to apply the roles to Kubernetes nodes as labels.

## Prerequisites
* Docker

## Getting Started
Clone this git repo:

```bash
git clone https://github.com/bodom0015/k8-labels.git
cd k8-labels/
```

Then run the roles.groovy script using Docker:
```bash
docker run -v $(pwd):/source -v $(pwd)/.grapes:/graperoot -w /source webratio/groovy roles.groovy
```

### About Grapes
"Grapes" is Groovy dependency grabbing system. The first time Docker runs this image, it may take a minute to download all required JARs. They will be cached in the `.grapes` folder alongside this script.

To force dependencies to re-download, you can delete the `.grapes` folder.

## Input Format
```javascript
{
  "nodeName": "nodeRole",
  "192.168.100.64": "ingress",
  "192.168.100.65": "compute",
  "192.168.100.89": "storage"
}
```
