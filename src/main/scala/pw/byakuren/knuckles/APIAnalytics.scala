package pw.byakuren.knuckles
import java.net.{HttpURLConnection, URL, URLEncoder}

class APIAnalytics(name: String, base: String = "http://127.0.0.1:9646") {
  private def buildParams(params: Map[String, String]): String = {
    params.map({
      case (k, v) => URLEncoder.encode(k, "UTF-8") + "=" + URLEncoder.encode(v, "UTF-8")
    }
    ).mkString("&")
  }

  private def post(uri: String, params: Map[String, String] = Map()): Int = {
    val url = new URL(uri)
    val conn = url.openConnection().asInstanceOf[HttpURLConnection]
    conn.setRequestMethod("POST")
    conn.setConnectTimeout(5000)
    conn.setReadTimeout(5000)
    if (params.nonEmpty) {
      val data = buildParams(params).getBytes("UTF-8")
      conn.setRequestProperty("Content-Length", data.length.toString)
      conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
      conn.setDoOutput(true)
      conn.getOutputStream.write(data)
    }
    conn.getResponseCode
  }

  def updateGuilds(count: Int): Unit = {
    post(s"$base/v1/guild/$name/$count")
  }

  def updateUsage(guild: Long, channel: Long): Unit = {
    post(s"$base/v1/data/$name/$guild/$channel")
  }

  def updateGuildName(guild: Long, name: String): Unit = {
    post(s"$base/v1/guild_name/$guild", params = Map("name" -> name))
  }

  private def logGeneric(msg: String, level: String): Unit = {
    post(s"$base/v1/log/$name", params = Map("message" -> msg, "level" -> level))
  }

  def log(msg: String): Unit = {
    logGeneric(msg, "log")
  }

  def warning(msg: String): Unit = {
    logGeneric(msg, "warn")
  }

  def error(msg: String): Unit = {
    logGeneric(msg, "error")
  }
}