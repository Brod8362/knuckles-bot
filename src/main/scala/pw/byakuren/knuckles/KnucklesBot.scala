package pw.byakuren.knuckles

import net.dv8tion.jda.api.entities.{Activity, Guild}
import net.dv8tion.jda.api.events.ExceptionEvent
import net.dv8tion.jda.api.events.guild.update.GuildUpdateNameEvent
import net.dv8tion.jda.api.events.guild.{GuildJoinEvent, GuildLeaveEvent}
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.session.{ReadyEvent, ShutdownEvent}
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.{EmbedBuilder, JDA, JDABuilder}
import pw.byakuren.knuckles.commands.{InviteCommand, MemeCommand, StopCommand, UnhomeCommand}
import pw.byakuren.knuckles.external.{APIAnalytics, DummyAPIAnalytics, PhonyShardAPIWrapper, ShardAPIWrapper}

import java.awt.Color
import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit}
import scala.util.control.Breaks.break

object KnucklesBot extends ListenerAdapter {

  val BOT_VERSION = "v0.8"
  val DEFAULT_CONFIG_PATH = "./config"

  val configPath: String = Option(System.getenv("KNUCKLES_CONFIG_PATH")).getOrElse(DEFAULT_CONFIG_PATH)

  val botConfig: Map[String, String] = ConfigParser.parse(configPath)

  implicit val analytics: APIAnalytics = botConfig.get("solo") match {
    case Some(_) =>
      println("using dummy analytics")
      DummyAPIAnalytics
    case _ => new APIAnalytics("knuckles", botConfig.getOrElse("analytics", "http://localhost:8086"))
  }

  val commandsSeq = Seq(
    InviteCommand,
    MemeCommand,
    StopCommand,
    UnhomeCommand
  )

  def main(args: Array[String]): Unit = {
    val kubePodName = Option(System.getenv("KNUCKLES_POD_NAME")) match {
      case Some(t) => t
      case _ =>
        println("environment variable KNUCKLES_POD_NAME not specified, exiting")
        return
    }
    println(kubePodName)

    val maxShards = Option(System.getenv("KNUCKLES_MAX_SHARDS")) match {
      case Some(t) if t.toIntOption.isDefined => t.toIntOption.get
      case Some(t) =>
        println(s"failed to decode KNUCKLES_MAX_SHARDS=$t into int")
        return
      case _ =>
        println("KNUCKLES_MAX_SHARDS not specified, exiting")
        return
    }

    val shardId = kubePodName.split("-").lastOption match {
      case Some(v) if v.toIntOption.isDefined => v.toIntOption.get
      case Some(v) =>
        println(s"failed to determine shard id from $v")
        return
      case _ =>
        println("shard id doesn't seem to to be of the correct format (expected something like knuckles-shard-0)")
        return
    }
    //this must not be nul
    println(s"logging in as $shardId/$maxShards")
    JDABuilder.createLight(botConfig("token"))
      .addEventListeners(this)
      .useSharding(shardId, maxShards)
      .build()
  }

  override def onShutdown(event: ShutdownEvent): Unit = {
    analytics.log("Shutdown issued")
  }

  override def onReady(event: ReadyEvent): Unit = {
    event.getJDA.getPresence.setActivity(Activity.playing(s"now v2! (shard ${event.getJDA.getShardInfo.getShardId})"))

    event.getJDA.updateCommands().addCommands(
      commandsSeq
        .filter(!_.restricted)
        .map(_.commandData): _*
    ).queue()


    val homeIdOpt = botConfig.get("home")
    homeIdOpt match {
      case Some(homeId) if Option(event.getJDA.getGuildById(homeId)).isDefined =>
        val guild = event.getJDA.getGuildById(homeId)
        analytics.log(s"home server set to ${guild.getName}")
        guild.updateCommands().addCommands(commandsSeq.map(_.commandData):_*).queue()
      case _ =>
        analytics.log("No home available")
    }
  }

  override def onSlashCommandInteraction(event: SlashCommandInteractionEvent): Unit = {
    commandsSeq.find(_.name == event.getName) match {
      case Some(command) =>
        if (command.restricted && event.getUser.getIdLong != event.getJDA.retrieveApplicationInfo().complete().getOwner.getIdLong) {
          //insufficient permissions
          event.reply("Knuckles says: no way!").setEphemeral(true).queue()
          return
        }
        command.onSlash(event)
      case _ =>
        event.reply("Sorry, command not found. This is an error and should be reported.").setEphemeral(true).queue()
    }

  }

  override def onGuildJoin(event: GuildJoinEvent): Unit = {
    guildUpdate(joined = true, event.getGuild)
    val embed = new EmbedBuilder()
      .setTitle("Thanks for adding Knuckles!")
      .setColor(Color.RED)
      .setDescription("Thanks for adding Knuckles! If you need help, you can join the support server.\n" +
        "It would also be greatly appreciated if you could fill out the knuckles user survey!\nThanks again!")
      .setThumbnail(event.getJDA.getSelfUser.getEffectiveAvatarUrl)
      .addField("Support Server", "https://discord.gg/3Scnd3GvCn", false)
      .addField("User Survey", "https://forms.gle/gvJGHf6NGyALuQVs9", false)
      .addField("Source Code", "https://github.com/Brod8362/knuckles-bot", false)
      .build()
    event.getGuild.getDefaultChannel.asTextChannel().sendMessageEmbeds(embed).queue()
  }

  override def onGuildLeave(event: GuildLeaveEvent): Unit = {
    guildUpdate(joined = false, event.getGuild)
  }

  def guildUpdate(joined: Boolean, guild: Guild): Unit = {
    val owner = guild.getJDA.retrieveApplicationInfo().complete().getOwner
    val txt = s"[s${guild.getJDA.getShardInfo.getShardId}] ${if (joined) "joined" else "left"} guild `${guild.getName}`:${guild.getId} (owned by <@${guild.getOwnerId}>) " +
      s"(${guild.getMemberCount} members) [${guild.getJDA.getGuilds.size()} servers]"
    val dm = owner.openPrivateChannel().complete()
    dm.sendMessage(txt).queue()
    try {
      analytics.updateGuilds(guild.getJDA.getGuilds.size(), guild.getJDA.getShardInfo.getShardId)
      analytics.updateGuildName(guild.getIdLong, guild.getName)
      analytics.log(txt)
    } catch {
      case e: Exception =>
        dm.sendMessage(s"couldn't update guild stats: $e").queue()
    }
    analytics.log(txt)
  }

  override def onGuildUpdateName(event: GuildUpdateNameEvent): Unit = {
    analytics.updateGuildName(event.getGuild.getIdLong, event.getNewName)
  }

  override def onException(event: ExceptionEvent): Unit = {
    analytics.error(event.getCause.toString)
  }
}