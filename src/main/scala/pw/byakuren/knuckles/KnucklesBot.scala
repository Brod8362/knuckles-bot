package pw.byakuren.knuckles

import net.dv8tion.jda.api.entities.{Activity, Guild}
import net.dv8tion.jda.api.events.ExceptionEvent
import net.dv8tion.jda.api.events.guild.update.GuildUpdateNameEvent
import net.dv8tion.jda.api.events.guild.{GuildJoinEvent, GuildLeaveEvent}
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.session.{ReadyEvent, ShutdownEvent}
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.sharding.{DefaultShardManager, DefaultShardManagerBuilder, ShardManager}
import net.dv8tion.jda.api.{EmbedBuilder, JDABuilder}
import pw.byakuren.knuckles.commands.{InviteCommand, MemeCommand, StopCommand, UnhomeCommand}
import pw.byakuren.knuckles.external.{APIAnalytics, DummyAPIAnalytics}
import pw.byakuren.knuckles.i18n.{TranslationParser, Translations}

import java.awt.Color
import scala.io.Source

object KnucklesBot extends ListenerAdapter {

  val BOT_VERSION = "v0.8"
  val DEFAULT_CONFIG_PATH = "./config"
  implicit val DEFAULT_LOCALE: String = "en-US"

  private val configPath: String = Option(System.getenv("KNUCKLES_CONFIG_PATH")).getOrElse(DEFAULT_CONFIG_PATH)

  private val botConfig: Map[String, String] = ConfigParser.parse(configPath)

  implicit val i18n: Translations = {
    val parser = new TranslationParser(DEFAULT_LOCALE)
    val fileFromResources = Source.fromResource("translations.i18n").getLines().toSeq
    parser.parse(fileFromResources)
  }

  implicit val analytics: APIAnalytics = botConfig.get("analytics") match {
    case Some("disabled") =>
      println("analytics disabled")
      DummyAPIAnalytics
    case Some(x) => new APIAnalytics("knuckles", x)
    case None =>
      println("analytics not provided, disabling")
      DummyAPIAnalytics
  }

  private val commandsSeq = Seq(
    new InviteCommand(),
    new MemeCommand(),
    new StopCommand(),
    new UnhomeCommand()
  )

  def main(args: Array[String]): Unit = {

    val shards = botConfig.get("shards") match {
      case Some(x) =>
        x.toIntOption match {
          case Some(i) => i
          case _ => throw new RuntimeException(s"failed to parse shards value '$x'")
        }
      case None =>
        analytics.log("shards value not provided, defaulting to 1")
        1
    }

    println(s"starting up with $shards shards")
    DefaultShardManagerBuilder.createLight(botConfig("token"))
      .addEventListeners(this)
      .setShardsTotal(shards)
      .setShards(0, shards-1)
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
          event.reply(i18n.sub(Translations.CMD_PERMISSION_DENIED)).setEphemeral(true).queue()
          return
        }
        command.onSlash(event)
      case _ =>
        event.reply(i18n.sub(Translations.CMD_NOT_FOUND)).setEphemeral(true).queue()
    }

  }

  override def onGuildJoin(event: GuildJoinEvent): Unit = {
    guildUpdate(joined = true, event.getGuild)
    val locale = event.getGuild.getLocale.getLocale
    val embed = new EmbedBuilder()
      .setTitle(i18n.sub(Translations.GUILD_JOIN_MSG_HEADER)(locale))
      .setColor(Color.RED)
      .setDescription(i18n.sub(Translations.GUILD_JOIN_MSG_BODY)(locale))
      .setThumbnail(event.getJDA.getSelfUser.getEffectiveAvatarUrl)
      .addField(i18n.sub(Translations.SUPPORT_SERVER_TEXT)(locale), "https://discord.gg/3Scnd3GvCn", false)
      .addField(i18n.sub(Translations.SOURCE_CODE_TEXT)(locale), "https://github.com/Brod8362/knuckles-bot", false)
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