# Kubernetes Node Labeler
This is a simple Groovy script that slurps up a JSON map of Kubernetes node hostname/IP -> role.
The script then uses `kubectl label` to apply the roles to Kubernetes nodes as labels.

## Prerequisites
* Docker

## Getting Started
Clone this git repo:

```bash
git clone https://github.com/bodom0015/k8-labels.git
cd k8-labels/
```

### About Grapes
"Grapes" is Groovy dependency grabbing system. The first time Docker runs this image, it may take a minute to download all required JARs. They will be cached in the `.grapes` folder alongside this script.

To force dependencies to re-download, you can delete the `.grapes` folder before running the Docker image.

## Usage
You can run the **roles.groovy** script using the following Docker command:
```bash
docker run --rm -v $(pwd):/source -v $(pwd)/.grapes:/graperoot -w /source webratio/groovy roles.groovy
```
### Input
The **roles.json** input file should sit alongside the script.

The input file should be formatted as follows:
```javascript
{
  "nodeName": "nodeRole",
  "192.168.100.64": "ingress",
  "192.168.100.65": "compute",
  "192.168.100.89": "storage"
}
```

### Output
```bash
core@lambert-test ~/groovy $ docker run --rm -v $(pwd):/source -v $(pwd)/.grapes:/graperoot -w /source webratio/groovy roles.groovy
Map parsed successfully!
 
Applying 192.168.100.156=storage
192.168.100.156 -> storage
Executing: kubectl label 192.168.100.156 ndslabs-role=storage --overwrite
 
Applying 192.168.100.64=ingress
192.168.100.64 -> ingress
Executing: kubectl label 192.168.100.64 ndslabs-role=ingress --overwrite
 
Applying 192.168.100.65=compute
192.168.100.65 -> compute
Executing: kubectl label 192.168.100.65 ndslabs-role=compute --overwrite
 
Applying 192.168.100.66=compute
192.168.100.66 -> compute
Executing: kubectl label 192.168.100.66 ndslabs-role=compute --overwrite
 
Applying 192.168.100.89=storage
192.168.100.89 -> storage
Executing: kubectl label 192.168.100.89 ndslabs-role=storage --overwrite
 
Map applied successfully!
```
