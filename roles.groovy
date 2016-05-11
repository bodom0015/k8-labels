@Grapes(
	@Grab(group='org.codehaus.groovy', module='groovy-json', version='2.0.0-rc-4')
)

import groovy.json.*

// Read in our roles from the fs
def roles = new File("roles.json")

// Parse the JSON from the file
def object = new JsonSlurper().parseText(roles.text)

// Make sure our test case passes
assert object instanceof Map

// Test String values
assert object["192.168.100.64"] instanceof String
assert object["192.168.100.64"] == "ingress"

assert object["192.168.100.65"] instanceof String
assert object["192.168.100.65"] == "compute"

assert object["192.168.100.66"] instanceof String
assert object["192.168.100.66"] == "compute"

assert object["192.168.100.89"] instanceof String
assert object["192.168.100.89"] == "storage"

assert object["192.168.100.156"] instanceof String
assert object["192.168.100.156"] == "storage"

println "Map parsed successfully!\n"

object.each{
  println "Applying " + it
  def (name, role) = it.toString().tokenize( '=' )
  println name + " -> " + role
  def command = String.format("kubectl label %s ndslabs-role=%s --overwrite", name, role)
  println "Executing: " + command + "\n"
}

println "Map applied successfully!\n"
