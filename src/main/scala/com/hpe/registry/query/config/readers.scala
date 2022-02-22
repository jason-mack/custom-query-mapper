package com.hpe.registry.query.config

import harmony.config.Conf

object readers {

  val BU = for {
    b <- Conf.get[String]("bu")
  } yield b

  val Server = for {
    p <- Conf.get[Int]("port")
    h <- Conf.get[String]("host")
  } yield HostConf(host = h, port = p)

  val TsdbDatabase = for {
    u   <- Conf.get[String]("username")
    w   <- Conf.get[String]("password")
    h   <- Conf.get[String]("host")
    p   <- Conf.get[Int]("port")
    t   <- Conf.get[String]("dtype")
    n   <- Conf.get[String]("name")
    ssl <- Conf.attempt[String]("use-ssl")
    sm  <- Conf.attempt[String]("ssl-mode")
    hc  <- Conf.section("hikaricp") andThen HikariConfig("0")
  } yield DbConf(n, t, u, w, h, p, ssl.forall(s => java.lang.Boolean.parseBoolean(s)), sm, Some(hc))

  def TsdbClusters =
    for {
      cl <- Conf.get[String]("clusters")
      l   = cl.split(',').map(_.trim).toList
    } yield l

  val TsdbHosts = for {
    h <- Conf.get[String]("hosts")
    l  = h.split(',').map(_.trim).toList
  } yield l

  val ShardClient = for {
    p <- Conf.get[Int]("port")
    h <- Conf.get[String]("host")
  } yield HostConf(host = h, port = p)

  def TsdbShard(host: String, index: Int) =
    for {
      u   <- Conf.get[String]("username")
      w   <- Conf.get[String]("password")
      p   <- Conf.get[Int]("port")
      t   <- Conf.get[String]("dtype")
      n   <- Conf.get[String]("name")
      ssl <- Conf.attempt[String]("use-ssl")
      sm  <- Conf.attempt[String]("ssl-mode")
      hc  <- Conf.section("hikaricp") andThen HikariConfig(index.toString)
    } yield DbConf(n, t, u, w, host, p, ssl.forall(s => java.lang.Boolean.parseBoolean(s)), sm, Some(hc))

  val PgDatabase = for {
    u   <- Conf.get[String]("username")
    w   <- Conf.get[String]("password")
    h   <- Conf.get[String]("host")
    p   <- Conf.get[Int]("port")
    t   <- Conf.get[String]("dtype")
    n   <- Conf.get[String]("name")
    ssl <- Conf.attempt[String]("use-ssl")
    sm  <- Conf.attempt[String]("ssl-mode")
    hc  <- Conf.section("hikaricp") andThen HikariConfig("0")
  } yield DbConf(n, t, u, w, h, p, ssl.forall(s => java.lang.Boolean.parseBoolean(s)), sm, Some(hc))

  def HikariConfig(index: String) =
    for {
      minIdle     <- Conf.get[Int]("minimumIdle")
      maxPoolSize <- Conf.get[Int]("maximumPoolSize")
      poolName    <- Conf.get[String]("poolName")
      testQuery   <- Conf.get[String]("connectionTestQuery")
      initSql     <- Conf.get[String]("connectionInitSql")
      maxLifeTime <- Conf.get[Long]("maxLifetime")
    } yield HikariConf(minIdle, maxPoolSize, poolName + index, testQuery, initSql, maxLifeTime)

}
