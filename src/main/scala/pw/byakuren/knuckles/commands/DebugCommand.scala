package pw.byakuren.knuckles.commands
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import pw.byakuren.knuckles.{APIAnalytics, KnucklesBot}

class DebugCommand(config: Map[String, String]) extends BotCommand("debug", "for debugging purposes", true) {
  override def onSlash(event: SlashCommandInteractionEvent)(implicit analytics: APIAnalytics): Unit = {
    val feature = event.getOption("feature").getAsString
    feature match {
      case "association" =>
        Option(event.getOption("arg1")) match {
          case Some(userId) =>
            Option(event.getGuild.retrieveMemberById(userId.getAsString).complete()) match {
              case Some(member) =>
                val assoc = KnucklesBot.userHasAssociation(member, config("home"))
                event.reply(s"$assoc").queue()
              case _ => //user not found
                event.reply("User not found.").queue()
            }
          case _ => //arg1 not supplied
            event.reply("`arg1` -> user id").queue()
        }
      case _ => //this debug feature not found
        event.reply(s"feature '$feature' unavailable").queue()
    }
  }

  override def commandData: SlashCommandData = {
    super.commandData
      .addOption(OptionType.STRING, "feature", "Feature to test/debug", true)
      .addOption(OptionType.STRING, "arg1", "-", false)
  }
}
