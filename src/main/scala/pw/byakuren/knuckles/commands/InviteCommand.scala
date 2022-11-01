package pw.byakuren.knuckles.commands

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import pw.byakuren.knuckles.APIAnalytics

object InviteCommand extends BotCommand("invite", "Get an invite link for knuckles!") {

  def generateInvite(jda: JDA): String = {
    val botUser = jda.getSelfUser
    s"https://discord.com/api/oauth2/authorize?client_id=${botUser.getId}&permissions=34816&scope=bot%20applications.commands"
  }

  override def onSlash(event: SlashCommandInteractionEvent)(implicit analytics: APIAnalytics): Unit = {
    event.reply(generateInvite(event.getJDA)).queue()
  }

}
