package pw.byakuren.knuckles.commands

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import pw.byakuren.knuckles.external.APIAnalytics
import pw.byakuren.knuckles.i18n.Translations

import scala.jdk.CollectionConverters.CollectionHasAsScala

class UnhomeCommand(implicit i18n: Translations) extends
  BotCommand(Translations.UNHOME_COMMAND_NAME, Translations.UNHOME_COMMAND_DESC, restricted = true) {
  override def onSlash(event: SlashCommandInteractionEvent)(implicit analytics: APIAnalytics): Unit = {
    val id: String = event.getOption("server_id").getAsString
    Option(event.getJDA.getGuildById(id)) match {
      case Some(guild) if guild.getIdLong != event.getGuild.getIdLong =>
        val restricted_commands = Seq("unhome", "stop")
        guild.retrieveCommands().complete().asScala.filter(c => restricted_commands.contains(c.getName))
          .foreach(c => c.delete().queue())
        event.reply(s"removed private commands from guild w/ id ${guild.getIdLong}")
      case _ =>
        event.reply(s"Guild with id $id not found").queue()
    }
  }

  override def commandData: SlashCommandData = super.commandData.addOption(OptionType.STRING, "server_id", "the old server id", true)
}
