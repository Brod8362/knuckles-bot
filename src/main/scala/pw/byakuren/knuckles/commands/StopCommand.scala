package pw.byakuren.knuckles.commands

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import pw.byakuren.knuckles.external.APIAnalytics
import pw.byakuren.knuckles.i18n.Translations

class StopCommand(implicit i18n: Translations) extends
  BotCommand(Translations.STOP_COMMAND_NAME, Translations.STOP_COMMAND_DESC, restricted = true) {

  override def onSlash(event: SlashCommandInteractionEvent)(implicit analytics: APIAnalytics): Unit = {
    event.reply(s"😰").setEphemeral(true).queue()
    event.getJDA.shutdown()
  }
}
