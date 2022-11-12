package pw.byakuren.knuckles.commands

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import pw.byakuren.knuckles.external.APIAnalytics

object StopCommand extends BotCommand("stop", "stop bot", restricted = true) {

  override def onSlash(event: SlashCommandInteractionEvent)(implicit analytics: APIAnalytics): Unit = {
    event.reply(s"😰").setEphemeral(true).queue()
    event.getJDA.shutdown()
  }
}
