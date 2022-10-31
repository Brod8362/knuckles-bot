package pw.byakuren.knuckles

import net.dv8tion.jda.api.entities.{Activity, Guild}
import net.dv8tion.jda.api.events.guild.update.GuildUpdateNameEvent
import net.dv8tion.jda.api.events.guild.{GuildJoinEvent, GuildLeaveEvent}
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.{ExceptionEvent, ReadyEvent}
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.{Commands, SlashCommandData}
import net.dv8tion.jda.api.{JDA, JDABuilder}

import java.io.File
import java.util.{Random, Scanner}
import scala.collection.mutable
import scala.jdk.CollectionConverters.CollectionHasAsScala

object KnucklesBot extends ListenerAdapter {

  val random = new Random()
  val prefix = "$"

  val analytics = new APIAnalytics("knuckles")

  def generateInvite(jda: JDA): String = {
    val botUser = jda.getSelfUser
    s"https://discord.com/api/oauth2/authorize?client_id=${botUser.getId}&permissions=34816&scope=bot%20applications.commands"
  }

  val uses: mutable.Map[Long, Int] = mutable.HashMap()
  val deprecatedReminder: mutable.HashMap[Long, Long] = mutable.HashMap()

  val MEME_COMMAND: SlashCommandData = Commands.slash("meme", "Submit your meme for knuckles to rate!")
    .addOption(OptionType.STRING, "the_meme", "What meme should knuckles rate?", false)

  val INVITE_COMMAND: SlashCommandData = Commands.slash("invite", "Get an invite link for knuckles!")

  val STOP_COMMAND: SlashCommandData = Commands.slash("stop", "stop bot")

  val STATS_COMMAND: SlashCommandData = Commands.slash("stats", "the data")

  val UNHOME_COMMAND: SlashCommandData = Commands.slash("unhome", "remove home commands from a previous home server")
    .addOption(OptionType.STRING, "server_id", "the old server id", true)

  def approveFiles: Array[File] = new File("approve").listFiles()

  def denyFiles: Array[File] = new File("deny").listFiles()

  def main(args: Array[String]): Unit = {
    val token = new Scanner(new File("token")).nextLine()
    JDABuilder.createLight(token).addEventListeners(this).build()
  }

  override def onReady(event: ReadyEvent): Unit = {
    event.getJDA.getPresence.setActivity(Activity.playing("/meme to submit meme, /invite"))
    event.getJDA.upsertCommand(MEME_COMMAND).queue()
    event.getJDA.upsertCommand(INVITE_COMMAND).queue()

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
        guild.upsertCommand(MEME_COMMAND).queue()
        guild.upsertCommand(INVITE_COMMAND).queue()
        guild.upsertCommand(STOP_COMMAND).queue()
        guild.upsertCommand(UNHOME_COMMAND).queue()
        guild.upsertCommand(STATS_COMMAND).queue()
    }
  }

  override def onSlashCommandInteraction(event: SlashCommandInteractionEvent): Unit = {
    event.getName match {
      case "meme" =>
        uses(event.getGuild.getIdLong) = uses.getOrElse(event.getGuild.getIdLong, 0)+1
        val file = if (random.nextBoolean()) {
          randomFromArray(approveFiles)
        } else {
          randomFromArray(denyFiles)
        }
        Option(event.getOption("the_meme")) match {
          case Some(param) =>
            event.reply(s"Knuckles has rated `${param.getAsString.replaceAll("`", "")}`").addFile(file).queue()
          case _ =>
            event.replyFile(file).queue()
        }
        try {
          analytics.updateUsage(event.getGuild.getIdLong, event.getChannel.getIdLong)
        } catch {
          case e: Exception =>
            println(s"couldn't update analytics: $e")
        }

      case "invite" =>
        event.reply(generateInvite(event.getJDA)).queue()
      case "stop" =>
        if (event.getUser.getIdLong == event.getJDA.retrieveApplicationInfo().complete().getOwner.getIdLong) {
          event.reply(s"${uses.values.sum} uses at time of shutdown").setEphemeral(true).queue()
          event.getJDA.shutdown()
        }
      case "unhome" if event.getUser.getIdLong == event.getJDA.retrieveApplicationInfo().complete().getOwner.getIdLong =>
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
      case _ =>
        //do nothing, invalid command
    }
  }

  def randomFromArray[T](array: Array[T]): T = {
    array((array.length * Math.random).toInt)
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