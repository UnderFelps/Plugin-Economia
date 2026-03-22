package br.felps.economia;

import br.felps.economia.comando.MoneyCommand;
import br.felps.economia.comando.PayCommand;
import br.felps.economia.comando.TopMoneyCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;

public final class Economia extends JavaPlugin {

    private static Economia instance;
    private Connection conn;

    @Override
    public void onEnable() {
        instance = this;
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:coins.db");
            Statement stmt = conn.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS players (uuid TEXT PRIMARY KEY, name TEXT, coins INTEGER DEFAULT 0)");
            stmt.close();
            getLogger().info("Database conectado com sucesso!");
        } catch (SQLException e) {
            getLogger().severe("Erro database: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getCommand("money").setExecutor(new MoneyCommand());
        getCommand("pay").setExecutor(new PayCommand());
        getCommand("topmoney").setExecutor(new TopMoneyCommand());

        getLogger().info("BEconomia ativado!");
    }

    @Override
    public void onDisable() {
        try {
            if (conn != null) conn.close();
        } catch (SQLException e) {
            getLogger().severe("Erro database: " + e.getMessage());
        }
        getLogger().info("BEconomia desativado!");
    }

    public static Economia getInstance() {
        return instance;
    }

    public Connection getConnection() {
        return conn;
    }
}