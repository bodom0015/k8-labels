# Kubernetes Node Label Synchronizer
This is a simple Groovy script that slurps up a JSON map of Kubernetes node hostname/IP -> role.
The script then uses `kubectl label` to apply the roles to Kubernetes nodes as labels.

## Prerequisites
* Docker

## Getting Started
Clone this git repo:

```bash
git clone https://github.com/nds-org/k8-labels.git
cd k8-labels/
```

This git repo includes the **roles.groovy** node labeler script.

### Groovy Grapes
"Grapes" is Groovy's dependency grabbing system which functions like an inline Maven package declaration. The first time Docker runs this image, it may take a minute to download all required JARs. They will be cached in the `.grapes` folder alongside this script, making subsequent runs of the script much faster.

To clear the cached dependencies and force them to re-download, you can delete the contents of the `.grapes` folder before running the Docker image.

## Usage
You can run the **roles.groovy** script using the following Docker command:
```bash
docker run --rm --net host -v /opt:/opt -v $(pwd):/source -v $(pwd)/.grapes:/graperoot -w /source webratio/groovy roles.groovy [-f <jsonFile>] [-l <labelName>] [-o]
```

The script will read the JSON file (-f) containing a mapping of node name -> value and apply the desired label (-l) to each node.
The user can also choose to overwrite (-o) the specified label if it already exists.

Arguments:
* -h : Print  out the script usage dialog
* -l : The label name to use when applying these values (default = **ndslabs-role**)
* -f : The JSON file containing the map of roles to apply to each node (default = **specs/label-name.json**)
* -v : The JSON file containing a list of valid values for the specified label (default = **specs/validate.label-name.json**)
* -o : Force overwrite of the label if it already exists (default = **disabled**)
* --force : Skip validation and force execution of the kubectl (default = **disabled**)

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
core@willis8-k8-test-1 ~/k8-labels $ docker run --rm -v /opt:/opt --net=host -v $(pwd):/source -v $(pwd)/.grapes:/graperoot -w /source webratio/groovy roles.groovy -l test-label
Skipping unrecognized role: 192.168.100.156 -> g
/opt/bin/kubectl label nodes 192.168.100.157 test-label=a 
Error from server: nodes "192.168.100.157" not found

/opt/bin/kubectl label nodes 192.168.100.64 test-label=d 
node "192.168.100.64" labeled

/opt/bin/kubectl label nodes 192.168.100.65 test-label=a 
node "192.168.100.65" labeled

/opt/bin/kubectl label nodes 192.168.100.66 test-label=c 
node "192.168.100.66" labeled

/opt/bin/kubectl label nodes 192.168.100.89 test-label=b 
node "192.168.100.89" labeled
```

## Future Plans
* ~~Clean up script output~~
* ~~Add option for --overwrite instead of assuming~~
* ~~Parameterize the label applied to each node~~
* ~~Parameterize the name of the input file~~
* Add support for multiple simultaneous label applications?
