package com.metriql.warehouse.mssqlserver

import com.metriql.db.FieldType
import com.metriql.db.QueryResult
import com.metriql.report.QueryTask
import com.metriql.warehouse.JDBCWarehouse
import com.metriql.warehouse.spi.WarehouseAuth
import java.util.Properties

class MSSQLDataSource(override val config: MSSQLWarehouse.MSSQLServerConfig) : JDBCWarehouse(
    config,
    arrayOf("TABLE", "VIEW"),
    config.usePool,
    true,
    config.database,
    config.schema
) {
    override val warehouse = MSSQLWarehouse

    override val dataSourceProperties by lazy {
        val properties = Properties()
        properties["jdbcUrl"] = "jdbc:sqlserver://${config.host}:${config.port}"
        properties["driverClassName"] = "com.microsoft.sqlserver.jdbc.SQLServerDriver"

        properties["dataSource.databaseName"] = config.database
        properties["dataSource.user"] = config.user
        properties["dataSource.password"] = config.password
        config.connectionParameters?.map { (k, v) ->
            properties[k] = v
        }
        properties
    }

    override fun getFieldType(sqlType: Int, dbType: String): FieldType? {
        return when (sqlType) {
            microsoft.sql.Types.DATETIME, microsoft.sql.Types.DATETIMEOFFSET, microsoft.sql.Types.SMALLDATETIME -> FieldType.TIMESTAMP
            else -> null
        }
    }

    override fun createQueryTask(
        auth: WarehouseAuth,
        query: QueryResult.QueryStats.QueryInfo,
        defaultSchema: String?,
        defaultDatabase: String?,
        limit: Int?,
        isBackgroundTask: Boolean
    ): QueryTask {
        return createSyncQueryTask(
            auth,
            query,
            defaultSchema ?: config.schema,
            defaultDatabase ?: config.database,
            limit
        )
    }
}
