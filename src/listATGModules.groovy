// We expect:
// - command line arguments
// -ATG_ROOT=atgRoot
// Default - use $ATG_ROOT env variable


atgROOT = ""
modules = [:]


def processArguments(args) {
  def cli = new CliBuilder(usage: 'listATGModules.groovy -[ha] ')
  def env = System.getenv()
  // Create the list of options.
  cli.with {
    h longOpt: 'help', 'Show usage information'
    e longOpt: 'env', 'Use environment variable ATG_ROOT'
    d longOpt: 'debug', 'Provide extra debug information'
    a longOpt: 'atg-root', args: 1, argName: 'atgRoot', 'Installation directory of the ATG - full path that contains DAS subdirectory'
  }

  def options = cli.parse(args)
  if (!options) {
    println 'No options'
    return
  }
  // Show usage text when -h or --help option is used.
  if (options.h) {
    cli.usage()
    return
  }

  if (options.'atg-root') {
    atgROOT = options.a
    println "Using ATG_ROOT from command line: ${atgROOT}"
  } else if (options.e) {
    if (env['ATG_ROOT']) {
      atgROOT = env['ATG_ROOT']
      println "Using ATG_ROOT from environment variable \$ATG_ROOT: ${atgROOT}"
    } else {
      println "Cannot determine ATG installation directory from environment - please set ATG_ROOT environment variable"
      cli.usage()
      return
    }
  } else {
    println "Cannot determine ATG installation directory "
    cli.usage()
    return
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

def processATGModule(String rootPath, String parentPath, String modulePath, moduleList, boolean debug = false) {
  String start = "$rootPath/${parentPath}/${modulePath}"
  if (debug)
    println "Called process $start"
  File f = new File("$start/META-INF/MANIFEST.MF")
  if (f.canRead()) {
    // got the module
    // load the Manifest file
    def lines = []
    f.readLines().each() { line ->
      if (line.startsWith("ATG-"))
        lines.add(line)
    }
    if (lines.size() > 0) {
      // create new ATG module with lines from manifest
      String n = createModuleName(parentPath, modulePath)
      println "Got module MOD="+n
      def module = new ATGModule(n, lines)
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

def cliOptions = processArguments(args)

if (validateATGRoot(atgROOT) == false) {
  println "The ${atgROOT} does not seem to be a valid ATG installation"
  return 1;
}
println "Using ${atgROOT} installation"


new File(atgROOT).eachFile { f ->
  if (f.isDirectory()) {
    println "\nProcessing ${f.canonicalPath}"
    processATGModule(atgROOT, ".", f.name, modules, cliOptions.d)
  }
}

modules.each { key, val ->
  println "Module ${key} => ${val.toString()}"

}
