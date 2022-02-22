package com.hpe.registry.query.config

case class DbConf(
    name: String,
    dtype: String,
    username: String,
    password: String,
    host: String,
    port: Int,
    useSsl: Boolean,
    sslMode: Option[String],
    hikariConf: Option[HikariConf]
)

case class HikariConf(
    minimumIdle: Int,
    maximumPoolSize: Int,
    poolName: String,
    connectionTestQuery: String,
    connectionInitSql: String,
    maxLifetime: Long
)
