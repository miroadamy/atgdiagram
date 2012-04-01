import java.util.regex.Matcher
/**
 * Created by IntelliJ IDEA.
 * User: miro
 * Date: 12-04-01
 * Time: 12:21 AM
 * To change this template use File | Settings | File Templates.
 */
class ATGModule {

  String moduleName
  String atgProduct
  String atgProductFull
  String atgConfigPath
  String atgInstallUnit
  String atgInstallVersion
  String atgClientClassPath
  String atgClassPath
  String atgRequired
  String atgDbSetupInitialDataPath
  String atgDbSetupInitialDataRepositories

  String atgJ2EE
  String atgEarModule


  List<String> dependsOn

  public ATGModule (String name, List<String> manifestLines, boolean debug = false) {
    moduleName = name

    // process manifest lines

    manifestLines.each { line ->

      switch (line) {
        case ~/^ATG-Product: (.*)/:
          def m = Matcher.lastMatcher
          atgProduct = m[0][1]
          break;

        case ~/^ATG-Product-Full: (.*)/:
          def m = Matcher.lastMatcher
          atgProduct = m[0][1]
          break;

        case ~/^ATG-Config-Path: (.*)$/:
          def m = Matcher.lastMatcher
          atgConfigPath = m[0][1]
          break;

        case ~/^ATG-Install-Unit: (.*)$/:
          def m = Matcher.lastMatcher
          atgInstallUnit = m[0][1]
          break;

        case ~/^ATG-Install-Version: (.*)$/:
          def m = Matcher.lastMatcher
          atgInstallVersion = m[0][1]
          break;

        case ~/^ATG-Client-Class-Path: (.*)$/:
          def m = Matcher.lastMatcher
          atgClientClassPath = m[0][1]
          break;


        case ~/^ATG-Class-Path: (.*)$/:
          def m = Matcher.lastMatcher
          atgClassPath = m[0][1]
          break;

        case ~/^ATG-Required: (.*)$/:
          def m = Matcher.lastMatcher
          atgRequired = m[0][1]
          break;

        case ~/^ATG-DBSetup-InitialDataPath: (.*)$/:
          def m = Matcher.lastMatcher
          atgDbSetupInitialDataPath = m[0][1]
          break;

        case ~/^ATG-EAR-Module: (.*)$/:
          def m = Matcher.lastMatcher
          atgEarModule = m[0][1]
          break;

        case ~/^ATG-DBSetup-InitialDataRepositories: (.*)$/:
          def m = Matcher.lastMatcher
          atgDbSetupInitialDataRepositories = m[0][1]
          break;

      }

      if (debug)
        println "MF-> (${line})"

    }
  }

  def dependsOn() {
    if (atgRequired == null || atgRequired.trim().size() == 0)
      return []
    return atgRequired.trim().split(' ')
  }

  public String toString() {
    java.lang.StringBuilder sb = new StringBuilder("ATGModule ${moduleName} ");
    if (atgProduct)
      sb.append(", ATGProduct = ${atgProduct}")
    if (atgConfigPath)
      sb.append(", config = ${atgConfigPath}")

  }
}
