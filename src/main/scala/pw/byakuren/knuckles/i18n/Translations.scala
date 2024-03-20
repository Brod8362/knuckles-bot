package pw.byakuren.knuckles.i18n

import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction
import pw.byakuren.knuckles.Implicits.StringImplicits

import java.util
import scala.jdk.CollectionConverters.MapHasAsJava

object Translations extends Enumeration {
  type Translations = String
  val MEME_COMMAND_NAME = "meme.name"
  val MEME_COMMAND_DESC = "meme.description"
  val MEME_COMMAND_ARGUMENT_NAME = "meme.the_meme.name"
  val MEME_COMMAND_ARGUMENT_DESC = "meme.the_meme.description"
  val INVITE_COMMAND_NAME = "invite.name"
  val INVITE_COMMAND_DESC = "invite.description"
  val DEBUG_COMMAND_NAME = "debug.name"
  val DEBUG_COMMAND_DESC = "debug.description"
  val STOP_COMMAND_NAME = "stop.name"
  val STOP_COMMAND_DESC = "stop.description"
  val UNHOME_COMMAND_NAME = "unhome.name"
  val UNHOME_COMMAND_DESC = "unhome.description"
  val CMD_PERMISSION_DENIED = "cmd-permission-denied"
  val CMD_NOT_FOUND = "cmd-not-found"
  val GUILD_JOIN_MSG_HEADER = "guild-join-msg-header"
  val GUILD_JOIN_MSG_BODY = "guild-join-msg-body"
  val SUPPORT_SERVER_TEXT = "support-server-text"
  val SOURCE_CODE_TEXT = "source-code-text"
  val MEME_COMMAND_REPLY = "meme-command-reply"
}

class Translations(defaultLocale: String, groups: Seq[TranslationGroup]) extends LocalizationFunction {

  private val translationGroups: Map[String, TranslationGroup] = groups.map(k => (k.messageId, k)).toMap

  def sub(messageId: String, substitutions: (String, String)*)(implicit locale: String): String = {
    translationGroups.get(messageId) match {
      case Some(group) =>
        // its safe to assume the default locale will *always* be available, as it is enforced at parse time
        group.apply(locale, defaultLocale).map(_.apply(substitutions:_*)).get
      case None =>
        throw MessageIdException(messageId)
    }
  }

  def default(messageId: String, substitutions: (String, String)*): String = {
    sub(messageId, substitutions:_* )(defaultLocale)
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

  override def apply(localizationKey: String): util.Map[DiscordLocale, String] = {
    asDiscord(localizationKey).asJava
  }
}