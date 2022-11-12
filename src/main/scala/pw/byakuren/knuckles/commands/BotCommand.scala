package pw.byakuren.knuckles.commands

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.{Commands, SlashCommandData}
import pw.byakuren.knuckles.external.APIAnalytics

abstract case class BotCommand(name: String, description: String = "", restricted: Boolean = false) {

  def onSlash(event: SlashCommandInteractionEvent)(implicit analytics: APIAnalytics): Unit

  def commandData: SlashCommandData = Commands.slash(this.name, this.description)

}
