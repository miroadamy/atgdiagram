// We expect:
// - command line arguments
// -ATG_ROOT=atgRoot
// Default - use $ATG_ROOT env variable


atgROOT = ""
modules = [:]


def processArguments(args) {
  def cli = new CliBuilder(usage: 'groovy listATGModules.groovy')
  def env = System.getenv()
  // Create the list of options.
  cli.with {
    h longOpt: 'help', 'Show usage information'
    e longOpt: 'env', 'Use environment variable ATG_ROOT'
    d longOpt: 'debug', 'Provide extra debug information'
    a longOpt: 'list-all', 'List all modules'
    p longOpt: 'gen-plantuml', 'Generate PlantUML format'
    m longOpt: 'mod-dep', args: 1, argName: 'module', 'Show recursive dependencies of a module and UM diagram for it'
    r longOpt: 'atg-root', args: 1, argName: 'atgRoot', 'Installation directory of the ATG - full path that contains DAS subdirectory'
  }

  def options = cli.parse(args)

  // Show usage text when -h or --help option is used.
  if (!options || options.h || options.options == 0) {
    cli.usage()
    System.exit(1)
  }

  if (options.'atg-root') {
    atgROOT = options.r
    println "Using ATG_ROOT from command line: ${atgROOT}"
  } else if (options.e) {
    if (env['ATG_ROOT']) {
      atgROOT = env['ATG_ROOT']
      println "Using ATG_ROOT from environment variable \$ATG_ROOT: ${atgROOT}"
    } else {
      println "Cannot determine ATG installation directory from environment - please set ATG_ROOT environment variable"
      cli.usage()
      System.exit(1)
    }
  } else {
    println "Cannot determine ATG installation directory "
    cli.usage()
    System.exit(1)
  }
  // Handle all non-option arguments.
  def extraArguments = options.arguments()
  if (extraArguments) {
    println "Got extra arguments: ${extraArguments}"
    // The rest of the arguments belong to the prefix.
  }
  return options
}

def validateATGRoot(atgroot) {
  File root = new File(atgroot)
  // println "testing ${atgroot}"
  if (root.isDirectory()) {
    // check if there is subdirectory
    // println "testing ${atgroot}/DafEar/META-INF"
    if (new File("${atgroot}/DafEar/META-INF").isDirectory())
      return true
  }
  return false
}


def goodGraphName(String s) {
   return s?.replaceAll('-','_')?.replaceAll(/\./, '_')
}

def processATGModule(String rootPath, String parentPath, String modulePath, moduleList, boolean debug = false) {
  String start = "$rootPath/${parentPath}/${modulePath}"
  if (debug)
    println "Called process $start"
  File f = new File("$start/META-INF/MANIFEST.MF")
  if (f.canRead()) {
    // got the module
    // load the Manifest file
    def lines = []
    def hasATGContent = false
    def fullLine = ""

    // TODO: process each line - if starts with space, concat with previous line (remove space)
    // ATG manifests contains broken lines
    f.readLines().each() { line ->
      if (line.startsWith("ATG-"))
        hasATGContent = true
      if (line.size() > 0 && line[0] == ' ' && fullLine.size() > 0) {  // this is a continuation line
        fullLine += line[1..-1]
      } else {
        if (fullLine.size() > 0)  // we have collected something before
          lines.add(fullLine)
        fullLine = line
      }
    }
    lines.add(fullLine)   // add last line
    if (lines.size() > 0 && hasATGContent ) {
      // create new ATG module with lines from manifest
      String n = createModuleName(parentPath, modulePath)
      if (debug)
        println "Got module MOD="+n
      def module = new ATGModule(n, lines, debug)
      moduleList[n] = module
    }

  }
  // now do for all
  new File(start).eachFile { dir ->
    if (dir.isDirectory() && !dir.name.startsWith('.')) {
      if (debug)
        println "Processing ${dir.canonicalPath}"
      processATGModule(atgROOT, "${parentPath}/${modulePath}", dir.name, modules, debug)
    }
  }
}

def createModuleName(String parentPath, String modulePath) {
  if (parentPath.startsWith('.'))
    parentPath = parentPath.replaceFirst('.', '')
  if (parentPath.startsWith('/'))
    parentPath = parentPath.replaceFirst('/','')
  def name = (parentPath != "") ? "${parentPath}.${modulePath}" : modulePath
  return name.replace('/','.');
}

def outputForPlant(mods, listOfIgnored) {
  def pairs = [:]

  println "@startuml"
  mods.each {key, val ->
    // key is module name
    println "Object ${goodGraphName(key)}"

    val.dependsOn().each { dep ->
      String pair = "${key}|${dep}"
      if (!pairs.containsKey(pair)) {
        pairs[pair] = dep
      }
    }
  }

  pairs.each {key, val ->
    def parts = key.tokenize('|')
    println "${goodGraphName(parts[0])} <|-- ${goodGraphName(parts[1])}"
  }

  println "@enduml"
}

def cliOptions = processArguments(args)

if (validateATGRoot(atgROOT) == false) {
  println "The \'${atgROOT}\' does not seem to be a valid ATG installation"
  return 1;
}
println "Using ${atgROOT} installation"


new File(atgROOT).eachFile { f ->
  if (f.isDirectory()) {
    processATGModule(atgROOT, ".", f.name, modules, cliOptions.d == true)
  }
}

if (cliOptions.a) {
  modules.each { key, val ->
    println "${key} => ${val.dependsOn()}"
  }
  return 0;
}


if (cliOptions.p) {
  outputForPlant(modules, [])
  return 0;
}

if (cliOptions.m) {
  def key = cliOptions.m

  if (modules.containsKey(key)) {
    // get the direct dependencies
    def mods = [:]
    Set<String> deps = modules[key].dependsOn()
    Set<String> added = modules[key].dependsOn()

    mods[key] = modules[key]
    while (added.size() > 0) {
        Set<String> toCheck = new HashSet<String>();
        toCheck.addAll(added)
        added.clear()
        toCheck.each { mod ->
          // get the deps
          mods[mod] = modules[mod]
          Set<String> modDeps = modules[mod].dependsOn()
          modDeps.each { dependant ->
            if (!deps.contains(dependant)) {
              added.add(dependant)
              deps.add(dependant)
            }
          }
        }
    }

    println "Dependants of module ${key} => ${deps}"
    outputForPlant(mods, null)

  } else {
    println "No information about ${key} in ${atgROOT}"
    return 1
  }

}