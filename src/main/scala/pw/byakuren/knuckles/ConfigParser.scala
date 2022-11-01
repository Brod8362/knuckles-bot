package pw.byakuren.knuckles

import scala.io.{BufferedSource, Source}

object ConfigParser {

  def parse(filename: String): Map[String, String] = {
    parse(Source.fromFile(filename))
  }

  def parse(f: BufferedSource): Map[String, String] = {
    var line_count = 0
    (for (line <- f.getLines) yield {
      line_count += 1
      line.split("=", 2) match {
        case arr if arr.size == 2 =>
          arr(0) -> arr(1)
        case _ =>
          throw new RuntimeException(s"config syntax error at line $line_count")
      }
    }).toMap
  }

}
