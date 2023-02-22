package pw.byakuren.knuckles.external

object PhonyShardAPIWrapper extends ShardAPIWrapper("solo", "") {
  override def join(): (Int, Int) = (0, 1)

  override def leave(): Unit = {}

  override def reset(): Unit = {}

  override def ping(): Unit = {}
}
