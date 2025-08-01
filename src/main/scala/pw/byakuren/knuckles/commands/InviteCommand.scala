package pw.byakuren.knuckles.commands

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import pw.byakuren.knuckles.external.APIAnalytics
import pw.byakuren.knuckles.i18n.Translations

class InviteCommand()(implicit i18n: Translations)
  extends BotCommand(Translations.INVITE_COMMAND_NAME, Translations.INVITE_COMMAND_DESC) {

  def generateInvite(jda: JDA): String = {
    val botUser = jda.getSelfUser
    s"https://discord.com/api/oauth2/authorize?client_id=${botUser.getId}&permissions=34816&scope=bot%20applications.commands"
  }

  override def onSlash(event: SlashCommandInteractionEvent)(implicit analytics: APIAnalytics): Unit = {
    event.reply(generateInvite(event.getJDA)).queue()
  }

}
