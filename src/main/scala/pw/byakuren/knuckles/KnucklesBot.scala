package pw.byakuren.knuckles

import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.{Activity, Guild}
import net.dv8tion.jda.api.events.guild.update.GuildUpdateNameEvent
import net.dv8tion.jda.api.events.guild.{GuildJoinEvent, GuildLeaveEvent}
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.{ExceptionEvent, ReadyEvent}
import net.dv8tion.jda.api.hooks.ListenerAdapter
import pw.byakuren.knuckles.commands.{InviteCommand, MemeCommand, StopCommand, UnhomeCommand}

import java.io.File
import java.util.Scanner

object KnucklesBot extends ListenerAdapter {

  implicit val analytics: APIAnalytics = new APIAnalytics("knuckles")

  val commandsSeq = Seq(
    InviteCommand,
    MemeCommand,
    StopCommand,
    UnhomeCommand
  )

  def main(args: Array[String]): Unit = {
    val token = new Scanner(new File("token")).nextLine()
    JDABuilder.createLight(token).addEventListeners(this).build()
  }

  override def onReady(event: ReadyEvent): Unit = {
    event.getJDA.getPresence.setActivity(Activity.playing("/meme to submit meme, /invite"))
    commandsSeq.filter(!_.restricted).foreach(cmd => event.getJDA.upsertCommand(cmd.commandData).queue())

    event.getJDA.updateCommands().addCommands(
      commandsSeq
        .filter(!_.restricted)
        .map(_.commandData): _*
    ).queue()

    val homeIdOpt: Option[String] = new File("home") match {
      case f if f.exists() =>
        Some(new Scanner(f).nextLine())
      case _ =>
        None
    }
    homeIdOpt match {
      case Some(homeId) if Option(event.getJDA.getGuildById(homeId)).isDefined =>
        val guild = event.getJDA.getGuildById(homeId)
        println(s"home server set to ${guild.getName}")
        guild.updateCommands().addCommands(commandsSeq.map(_.commandData):_*).queue()
    }
  }

  override def onSlashCommandInteraction(event: SlashCommandInteractionEvent): Unit = {
    commandsSeq.find(_.name == event.getName) match {
      case Some(command) =>
        if (command.restricted && event.getUser.getIdLong != event.getJDA.retrieveApplicationInfo().complete().getOwner.getIdLong) {
          //insufficient permissions
          return
        }
        command.onSlash(event)
      case _ =>
        event.reply("Sorry, command not found. This is an error and should be reported.").setEphemeral(true).queue()
    }

  }

  override def onGuildJoin(event: GuildJoinEvent): Unit = {
    guildUpdate(joined = true, event.getGuild)
  }

  override def onGuildLeave(event: GuildLeaveEvent): Unit = {
    guildUpdate(joined = false, event.getGuild)
  }

  def guildUpdate(joined: Boolean, guild: Guild): Unit = {
    val owner = guild.getJDA.retrieveApplicationInfo().complete().getOwner
    val txt = s"${if (joined) "joined" else "left"} guild `${guild.getName}`:${guild.getId} (owned by <@${guild.getOwnerId}>) " +
      s"(${guild.getMemberCount} members) [${guild.getJDA.getGuilds.size()} servers]"
    val dm = owner.openPrivateChannel().complete()
    dm.sendMessage(txt).queue()
    try {
      analytics.updateGuilds(guild.getJDA.getGuilds.size())
      analytics.updateGuildName(guild.getIdLong, guild.getName)
      analytics.log(txt)
    } catch {
      case e: Exception =>
        dm.sendMessage(s"couldn't update guild stats: $e").queue()
    }
    println(txt)
  }

  override def onGuildUpdateName(event: GuildUpdateNameEvent): Unit = {
    analytics.updateGuildName(event.getGuild.getIdLong, event.getNewName)
  }

  override def onException(event: ExceptionEvent): Unit = {
    analytics.error(event.getCause.toString)
  }
}