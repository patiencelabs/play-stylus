package patience.assets

import play.PlayExceptions.AssetCompilationException
import java.io.File
import scala.sys.process._

object StylusCompiler {

  def compile(stylFile: File, options: Seq[String]): (String, Option[String], Seq[File]) = {
    try {
      val parentPath = stylFile.getParentFile.getAbsolutePath
      val cssOutput = captureOutput((
        Seq("stylus -I", parentPath) ++ options
      ).mkString(" ") #< stylFile)
      val compressedCssOutput = captureOutput((
        Seq("stylus -c -I", parentPath) ++ options
      ).mkString(" ") #< stylFile)

      (cssOutput, Some(compressedCssOutput), Seq(stylFile))
    } catch {
      case e: StylusCompilationException => {
        throw AssetCompilationException(Some(stylFile), "Stylus compiler: " + e.message, Some(e.line), Some(e.column))
      }
    }
  }

  private def captureOutput(command: ProcessBuilder): String = {
    val err = new StringBuilder
    val out = new StringBuilder

    val capturer = ProcessLogger(
        (output: String) => out.append(output + "\n"),
        (error: String) => err.append(error + "\n"))

    val process = command.run(capturer)
    if (process.exitValue == 0)
      out.toString
    else
      throw new StylusCompilationException(err.toString())
  }

  private val MarkedLine = """\s*>\s*(\d+)\|.*?""".r
  private val EmptyLine = "".r

  private class StylusCompilationException(stderr: String) extends RuntimeException {

    val (line: Int, column: Int, message: String) = parseError(stderr)

    private def parseError(error: String): (Int, Int, String) = {
      var seen = 0
      var line = 0
      val column = 0
      var message = "Unknown error, try running stylus directly"
      for (errline: String <- augmentString(error).lines) {
        errline match {
          case MarkedLine(l) => { line = l.toInt; seen += 1 }
          case EmptyLine() if (seen == 1) => { seen += 1 }
          case other if (seen == 2) => { message = other; seen += 1 }
          case other => seen
        }
      }

      (line, column, message)
    }
  }

}
