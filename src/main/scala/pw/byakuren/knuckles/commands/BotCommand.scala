package pw.byakuren.knuckles.commands

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.{Commands, SlashCommandData}
import pw.byakuren.knuckles.external.APIAnalytics
import pw.byakuren.knuckles.i18n.Translations

import scala.jdk.CollectionConverters.MapHasAsJava

abstract case class BotCommand(nameTranslationId: String, descriptionTranslationId: String, restricted: Boolean = false)
                              (implicit i18n: Translations) {

  def onSlash(event: SlashCommandInteractionEvent)(implicit analytics: APIAnalytics): Unit

  def name: String = i18n.default(nameTranslationId)

  def commandData: SlashCommandData = {
    val defaultName = i18n.default(nameTranslationId)
    val defaultDescription = i18n.default(descriptionTranslationId)
    Commands.slash(defaultName, defaultDescription).setLocalizationFunction(i18n)

  }

}
