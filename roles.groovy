@Grapes(
	@Grab(group='org.codehaus.groovy', module='groovy-json', version='2.0.0-rc-4')
)

import groovy.json.*

/** Helper function to apply a CLI option or use the default value */
def getArgOrDefault(def arg, def defaultValue) {
  if (!arg) {
    return defaultValue;
  }

  return arg;
}

// Define what roles we expect to see / apply for validation
def acceptedRoles = [
  'ingress',
  'compute',
  'etcd',
  'storage'
];

// Parse command line to retrieve arguments
def cli = new CliBuilder(usage:'roles.groovy -f path/to/roles.json')
cli.h(longOpt:'help', args:0, 'print script usage')
cli.f(longOpt:'file', args:1, argName:'path/to/roles.json',
       'provide a JSON file of roles to apply - default=roles.json')
cli.o(longOpt:'overwrite', args:0, 'overwrite any previous role present')
cli.l(longOpt:'label-name', args:1, argName:'target label', 
       'provide a target label name that the JSON should be applied under - default=ndslabs-role')

def options = cli.parse(args)
assert options

if (options.h) {
  cli.usage()
  return
}

// Read provided CLI arguments out of parsed object
def filename = getArgOrDefault(options.f, 'roles.json')
def overwrite = getArgOrDefault(options.o, false)
def targetLabel = getArgOrDefault(options.l, 'ndslabs-role')

def overwriteLabel = overwrite ? '--overwrite' : ''

assert filename
assert targetLabel

// Parse the JSON from the file
def object = new JsonSlurper().parseText(new File(filename).text)

def roles = new LinkedHashMap();

// Validate our JSON map first
assert object instanceof Map
object.each{
  def (name, role) = it.toString().tokenize( '=' )
  
  if (role && !acceptedRoles.contains(role)) {
    println String.format("Skipping unrecognized role: %s -> %s", name, role)
    return
  }
 
  // Cache this value in our lookup table
  roles[name] = role;
}

def count = 0

// Then apply the role's we've validated using 'kubectl label'
roles.each{ name, role ->
  def command
  if (role == null) {
    // Remove this label if no value is specified
    command = String.format("kubectl label %s %s-", name, targetLabel)
  } else {
    // Add / overwrite this label if a value was specified
    command = String.format("kubectl label %s %s=%s %s", name, targetLabel, role, overwriteLabel)
  }

  def sout = new StringBuffer(), serr = new StringBuffer()

  println "Executing: " + command
  def proc = command.execute()
  proc.consumeProcessOutput(sout, serr)
  proc.waitForKill(1000)
  println "out> $sout"
  println "err> $serr"

  count++;
}

println "\n" + count + " roles applied successfully!"
