@Grapes(
	@Grab(group='org.codehaus.groovy', module='groovy-json', version='2.0.0-rc-4')
)

import groovy.json.*

def KUBECTL_BASE_PATH = '/opt/bin/'

/** Helper function to apply a CLI option or use the default value */
def getArgOrDefault(def arg, def defaultValue) {
  if (!arg) {
    return defaultValue;
  }

  return arg;
}

// Parse command line to retrieve arguments
def cli = new CliBuilder(usage:'roles.groovy -f path/to/roles.json')
cli.h(longOpt:'help', args:0, 'print script usage')
cli.f(longOpt:'file', args:1, argName:'path/to/roles.json',
       'provide a JSON file of label values to apply')
cli.v(longOpt:'validate', args:1, argName:'path/to/roles.json',
       'provide a JSON file of valid values')
cli.o(longOpt:'overwrite', args:0, 'overwrite any previous role present')
cli.l(longOpt:'label-name', args:1, argName:'target label', 
       'provide a target label name that the JSON should be applied under - default=ndslabs-role')
cli._(longOpt:'force', args:0, 'force the label application (WARNING: this skips validation)')

def options = cli.parse(args)
assert options

if (options.h) {
  cli.usage()
  return
}

// Read provided CLI arguments out of parsed object
def overwrite = getArgOrDefault(options.o, false)

def defaultLabel = 'ndslabs-role'

// Define what roles we expect to see, where to find them, and which label to post them under
def targetLabel = getArgOrDefault(options.l, defaultLabel)
def filename = getArgOrDefault(options.f, 'specs/' + targetLabel + '.json')
def validateFilename = getArgOrDefault(options.v, 'specs/validate.' + targetLabel + '.json')

def overwriteLabel = overwrite ? '--overwrite' : ''

assert filename
assert targetLabel
assert validateFilename

// Parse the JSON from the file
def object = new JsonSlurper().parseText(new File(filename).text)

def acceptedValues = null
def validate = new File(validateFilename)
if (validate.exists()) {
  acceptedValues = new JsonSlurper().parseText(validate.text)
} else if (!options.force) {
  println String.format("ERROR: Validation file %s was not found.", validateFilename)
  println "Please provide a validation file, or specify --force to skip validation."
  return
}

def roles = new LinkedHashMap();

// Validate our JSON map first
assert object instanceof Map
object.each{
  def (name, role) = it.toString().tokenize( '=' )
  
  if (role && acceptedValues && !acceptedValues.contains(role)) {
    println String.format("Skipping unrecognized role: %s -> %s", name, role)
    return
  }
 
  // Cache this value in our lookup table
  roles[name] = role;
}

// Then apply the role's we've validated using 'kubectl label'
roles.each{ name, role ->
  // Build up the command we plan to execute
  def command = KUBECTL_BASE_PATH + 'kubectl label nodes'
  if (role == null) {
    // Remove this label if no value is specified
    command += String.format(" %s %s-", name, targetLabel)
  } else {
    // Add / overwrite this label if a value was specified
    command += String.format(" %s %s=%s %s", name, targetLabel, role, overwriteLabel)
  }

  // Execute the kubectl command we just built
  println command
  command.execute().waitForProcessOutput(System.out, System.err)
  println ""
}
