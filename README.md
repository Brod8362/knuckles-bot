# Knuckles

Have knuckles rate your meme!

[Invite knuckles to your server!](https://discord.com/api/oauth2/authorize?client_id=712487801463242812&permissions=34816&scope=bot%20applications.commands)

[Need help? Join the knuckles support server!](https://discord.gg/3Scnd3GvCn)

# Running

The bot reads from the following configuration file:

```
token=yourtokenhere
home=1234566789
analytics=http://localhost:9646
```

You can run the bot via `sbt`.

```bash
sbt compile
sbt run
```

You can build the bot to a fat jar with `sbt assembly`, and run it like so:

`java -jar knuckles-assembly-0.X.jar -Xmx256M`

# How to load memes

Knuckles will scan two directories for memes:

- `approve/` (for memes that are approved)
- `deny/`  (for memes that are denied)

A random file from these directories will be chosen and uploaded.

# Analytics API

Knuckles collects very rudimentary analytics. Notably, the number of severs, and the usage per server.

This API is an instance of my [bot analytics API](https://github.com/Brod8362/bot-analytics).

It is expected to be available at http://localhost:9646. Currently, this is not configurable.