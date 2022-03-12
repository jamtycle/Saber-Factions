package com.massivecraft.factions.mysql.abstracts;

import com.massivecraft.factions.FactionsPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.Map;

public abstract class DBConnection {
    protected FactionsPlugin plugin;
    Connection conn = null;
    FileConfiguration conf;
    String conn_string;

    public DBConnection(FactionsPlugin _plugin) {
        plugin = _plugin;
        conf = plugin.getConfig();
        conn_string = "jdbc:mysql://" + conf.get("mysql-connection") + "/" + conf.get("mysql-db") +
                "?user=" + conf.get("mysql-user") +
                "&password=" + conf.get("mysql-password");
    }

    protected Boolean BuildCLass(ResultSet _class_info) {
        try {
            Field[] fields = this.getClass().getDeclaredFields();
            for (Field f : fields) {
                int column = _class_info.findColumn(f.getName());
                if (column == -1) continue;
                f.set(this, _class_info.getObject(column));
            }
            return true;
        } catch (SQLException | IllegalAccessException ex) {
            plugin.getLogger().info(ex.getMessage());
            return false;
        }
    }

    protected Boolean BuildCLass(Map<String, Object> _values) {
        try {
            Field[] fields = this.getClass().getDeclaredFields();
            for (Field f : fields) {
                Object field_value = _values.getOrDefault(f.getName(), null);
                if (field_value == null) continue;
                f.set(this, field_value);
            }
            return true;
        } catch (IllegalAccessException ex) {
            plugin.getLogger().info(ex.getMessage());
            return false;
        }
    }

    protected ResultSet getResultSet(String _text) {
        try (Connection conn = DriverManager.getConnection(conn_string)) {
            try (PreparedStatement statement = conn.prepareStatement(_text)) {
                statement.setFetchSize(100);
                try (ResultSet res = statement.executeQuery()) {
                    return res;
                }
            }
        } catch (SQLException ex) {
            plugin.getLogger().info("SQLException: " + ex.getMessage());
            plugin.getLogger().info("SQLState: " + ex.getSQLState());
            plugin.getLogger().info("VendorError: " + ex.getErrorCode());
        }
        return null;
    }

    protected ResultSet getResultSet(String _procedure, Object... args) {
        try (Connection conn = DriverManager.getConnection(conn_string)) {
            try (CallableStatement statement = conn.prepareCall("{CALL " + _procedure + "}")) {
                for (int i = 0; i < args.length; i++) {
                    Object param = args[i];
                    if (param instanceof Integer) {
                        statement.setInt(i, (Integer) param);
                    } else if (param instanceof String) {
                        statement.setString(i, param.toString());
                    } else if (param instanceof Boolean) {
                        statement.setBoolean(i, (Boolean) param);
                    } else if (param instanceof Date) {
                        statement.setString(i, ((Date) param).toString());
                    }
                }
                try (ResultSet res = statement.executeQuery()) {
                    return res;
                }
            }
        } catch (SQLException ex) {
            plugin.getLogger().info("SQLException: " + ex.getMessage());
            plugin.getLogger().info("SQLState: " + ex.getSQLState());
            plugin.getLogger().info("VendorError: " + ex.getErrorCode());
            return null;
        }
    }

    protected boolean executeNonQuery(String _procedure, Object... args) {
        try (Connection conn = DriverManager.getConnection(conn_string)) {
            try (CallableStatement statement = conn.prepareCall("{CALL " + _procedure + "}")) {
                for (int i = 0; i < args.length; i++) {
                    Object param = args[i];
                    if (param instanceof Integer) {
                        statement.setInt(i, (Integer) param);
                    } else if (param instanceof String) {
                        statement.setString(i, param.toString());
                    } else if (param instanceof Boolean) {
                        statement.setBoolean(i, (Boolean) param);
                    } else if (param instanceof Date) {
                        statement.setString(i, ((Date) param).toString());
                    }
                }
                return statement.execute();
            }
        } catch (SQLException ex) {
            plugin.getLogger().info("SQLException: " + ex.getMessage());
            plugin.getLogger().info("SQLState: " + ex.getSQLState());
            plugin.getLogger().info("VendorError: " + ex.getErrorCode());
            return false;
        }
    }
}