package pw.byakuren.knuckles.i18n

import java.io.File
import java.nio.file.{Files, Path}
import scala.collection.mutable
import scala.jdk.CollectionConverters.CollectionHasAsScala


class TranslationParser(val defaultLocale: String) {

  private var currentMessageId: Option[String] = None
  private var currentMessageVariables: Option[Seq[String]] = None
  private val currentMessageSubstitutions: mutable.Map[String, Substitution] = mutable.HashMap()

  private val finalizedMessages = mutable.ArrayBuffer[TranslationGroup]()

  def parse(path: Path): Translations = {
    val lines = Files.readAllLines(path).asScala
    parse(lines)
  }

  def parse(file: File): Translations = {
    parse(file.toPath)
  }

  def parse(lines: Iterable[String]): Translations = {
    lines.zipWithIndex.foreach({k =>
      try {
        this.parseLine(k._1.strip())
      } catch {
        case inner: Throwable =>
          //rethrow, wrapped, with line #
          throw TranslationsParserException(inner, k._2+1)
      }
    })

    currentMessageId match {
      case Some(messageId) => pushCurrentStateAsTranslationGroup(messageId)
      case None =>
    }

    val t = new Translations(defaultLocale, finalizedMessages.toSeq)

    finalizedMessages.clear()
    currentMessageId = None
    currentMessageVariables = None
    currentMessageSubstitutions.clear()
    t
  }

  private def pushCurrentStateAsTranslationGroup(messageId: String): Unit = {
    val isMessageAvailableInDefaultLocale = currentMessageSubstitutions.contains(defaultLocale)
    if (!isMessageAvailableInDefaultLocale) {
      throw TranslationSyntaxError(s"missing translation in default locale ($defaultLocale) for $messageId")
    }

    val group = TranslationGroup(messageId, currentMessageSubstitutions.toMap)
    currentMessageId = None
    currentMessageVariables = None
    currentMessageSubstitutions.clear()
    finalizedMessages.append(group)
  }

  private def parseLine(line: String): Unit = {
    if (line.isEmpty) {
      return
    }

    if (line.startsWith("#")) { //new message ID
      currentMessageId match {
        // the current message ID is done parsing, so now we can move on to another one
        case Some(currentId) =>
          pushCurrentStateAsTranslationGroup(currentId)
          //create a TranslationGroup out of the current info and push it onto the buffer
        case None =>
          //this is the first id of the file
      }

      // parse new id, vars, etc
      val splitLine = line.substring(1).split(":", 2)
      if (splitLine.length != 2) {
          throw TranslationSyntaxError("expected message ID and vars (ex: '# id : <v1>,<v2>')")
      }

      val idRaw = splitLine.head.strip()
      val varsRaw = splitLine.last.strip()
      //TODO: probably enforce conformance as far as allowed message IDs
      currentMessageId = Some(idRaw)

      if (!varsRaw.startsWith("<")) {
        throw TranslationSyntaxError(s"< expected in '$varsRaw''")
      }

      if (!varsRaw.endsWith(">")) {
        throw TranslationSyntaxError(s"> expected in '$varsRaw'")
      }

      val varsProcessed = varsRaw // "<r1, r2, r3>"
        .substring(1, varsRaw.length-1)  // "r1, r2, r3"
        .split(",") // ["r1", " r2", " r3"]
        .map(_.strip()) // ["r1", "r2", "r3"]

      currentMessageVariables = Some(varsProcessed)
      return
    }

    // if it's not a blank line, and it's not a start of a new message, it must be a translation itself.
    // expected syntax is either:
    // locale: message
    // locale: @locale
    val splitLine = line.split(":", 2)

    if (splitLine.length != 2) {
      throw TranslationSyntaxError(s"Expected 'locale: message' or 'locale: @locale'")
    }

    val locale = splitLine.head.strip()
    val messageOrLocaleRef = splitLine.last.strip()

    if (messageOrLocaleRef.startsWith("@")) {
      //this is a locale ref
      currentMessageSubstitutions.get(messageOrLocaleRef.substring(1)) match {
        case Some(localeToReference) => currentMessageSubstitutions(locale) = localeToReference
        case None => throw TranslationSyntaxError(s"referenced locale $messageOrLocaleRef does not exist yet")
      }
    } else {
      //this is not a locale ref, but rather a message, so that needs to be parsed
      //primarily concerned with making sure all of the variables exist and that no extras are defined
      val variablePattern = "<\\w+>".r
      val matches = variablePattern.findAllIn(messageOrLocaleRef)
      val variableNames = matches.map(v => v.substring(1, v.length-1)).toSeq // turn "<v1>" → "v1"

      currentMessageVariables match {
        case Some(expectedVariables) =>
          val missing = expectedVariables.filter(v => !variableNames.contains(v)) //values which are expected but not provided
          if (missing.nonEmpty) {
            throw TranslationSyntaxError(s"message missing variable references '${missing.mkString(",")}'")
          }
          val extraneous = variableNames.filter(v => !expectedVariables.contains(v)) //values which are provided but not expected
          if (extraneous.nonEmpty) {
            throw TranslationSyntaxError(s"message has extraneous variable references '${extraneous.mkString(",")}'")
          }
          //variables line up... message should be ok
          currentMessageSubstitutions(locale) = Substitution(messageOrLocaleRef, variableNames)
        case None =>
          //variables not defined, this should not happen
          throw new RuntimeException("expected variables should have been defined, but aren't")
      }
    }
  }
}
