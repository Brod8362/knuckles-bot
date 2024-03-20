package pw.byakuren.knuckles.i18n

import net.dv8tion.jda.api.interactions.DiscordLocale
import pw.byakuren.knuckles.Implicits.StringImplicits

object Translations extends Enumeration {
  type Translations = String
  val MEME_COMMAND_NAME = "meme-command-name"
  val MEME_COMMAND_DESC = "meme-command-desc"
  val INVITE_COMMAND_NAME = "invite-command-name"
  val INVITE_COMMAND_DESC = "invite-command-desc"
  val DEBUG_COMMAND_NAME = "debug-command-name"
  val DEBUG_COMMAND_DESC = "debug-command-desc"
  val STOP_COMMAND_NAME = "stop-command-name"
  val STOP_COMMAND_DESC = "stop-command-desc"
  val UNHOME_COMMAND_NAME = "unhome-command-name"
  val UNHOME_COMMAND_DESC = "unhome-command-desc"
}

class Translations(defaultLocale: String, groups: Seq[TranslationGroup]) {

  private val translationGroups: Map[String, TranslationGroup] = groups.map(k => (k.messageId, k)).toMap

  def apply(messageId: String, substitutions: (String, String)*)(implicit locale: String): String = {
    translationGroups.get(messageId) match {
      case Some(group) =>
        // its safe to assume the default locale will *always* be available, as it is enforced at parse time
        group.apply(locale, defaultLocale).map(_.apply(substitutions:_*)).get
      case None =>
        throw MessageIdException(messageId)
    }
  }

  def default(messageId: String, substitutions: (String, String)*): String = {
    apply(messageId, substitutions:_* )(defaultLocale)
  }

  def all(messageId: String, substitutions: (String, String)*): Map[String, String] = {
    translationGroups.get(messageId) match {
      case Some(group) =>
        group.templates.map(pair => {
          (pair._1, pair._2.apply(substitutions:_*))
        })
      case None =>
        throw MessageIdException(messageId)
    }
  }

  def asDiscord(messageId: String, substitutions: (String, String)*): Map[DiscordLocale, String] = {
    all(messageId, substitutions:_*)
      .collect {
        case p if p._1.asDiscordLocale.isDefined => (p._1.asDiscordLocale.get, p._2)
      }
  }

  def exists(messageId: String, locale: String): Boolean = {
    translationGroups
      .get(messageId)
      .map(_.templates.get(locale))
      .isDefined
  }
}