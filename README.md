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

This git repo includes the **roles.groovy** node labeler script.

### Groovy Grapes
"Grapes" is Groovy's dependency grabbing system which functions like an inline Maven package declaration. The first time Docker runs this image, it may take a minute to download all required JARs. They will be cached in the `.grapes` folder alongside this script, making subsequent runs of the script much faster.

To clear the cached dependencies and force them to re-download, you can delete the contents of the `.grapes` folder before running the Docker image.

## Usage
You can run the **roles.groovy** script using the following Docker command:
```bash
docker run --rm -v $(pwd):/source -v $(pwd)/.grapes:/graperoot -w /source webratio/groovy roles.groovy [-f <jsonFile>] [-l <labelName>] [-o]
```

The script will read the JSON file (-f) containing a mapping of node name -> value and apply the desired label (-l) to each node.
The user can also choose to overwrite (-o) the specified label if it already exists.

Arguments:
* -f : The JSON file containing the map of roles to apply to each node (default = **roles.json**)
* -l : The label name to use when applying these values (default = **ndslabs-role**)
* -o : Force overwrite of the label if it already exists (default = **false**)

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
core@lambert-test ~/groovy $ docker run -v $(pwd):/source -v $(pwd)/.grapes:/graperoot -w /source webratio/groovy roles.groovy -f roles.json -l label-maker -o
Skipping unrecognized role: 192.168.100.157 -> asdf
Executing: kubectl label 192.168.100.156 label-maker=storage --overwrite
Executing: kubectl label 192.168.100.64 label-maker=ingress --overwrite
Executing: kubectl label 192.168.100.65 label-maker=compute --overwrite
Executing: kubectl label 192.168.100.66 label-maker=compute --overwrite
Executing: kubectl label 192.168.100.89 label-maker=storage --overwrite

5 roles applied successfully!
```

## Future Plans
* ~~Clean up script output~~
* ~~Add option for --overwrite instead of assuming~~
* ~~Parameterize the label applied to each node~~
* ~~Parameterize the name of the input file~~
* Add support for multiple simultaneous label applications?
