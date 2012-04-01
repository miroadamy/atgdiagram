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
  String atgConfigPath
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

        case ~/^ATG-Config-Path: (.*)$/:
          def m = Matcher.lastMatcher
          atgConfigPath = m[0][1]
          break;

      }

      if (debug)
        println "MF-> (${line})"

    }
  }

  public String toString() {
    java.lang.StringBuilder sb = new StringBuilder("ATGModule ${moduleName} ");
    if (atgProduct)
      sb.append(", ATGProduct = ${atgProduct}")
    if (atgConfigPath)
      sb.append(", config = ${atgConfigPath}")

  }
}
