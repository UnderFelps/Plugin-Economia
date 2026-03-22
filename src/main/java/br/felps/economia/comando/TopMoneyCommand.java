package br.felps.economia.comando;

import br.felps.economia.Economia;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TopMoneyCommand implements CommandExecutor {

    private final Connection conn = Economia.getInstance().getConnection();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT name, coins FROM players ORDER BY coins DESC LIMIT 5");
            ResultSet rs = ps.executeQuery();
            sender.sendMessage("§eTop 5 jogadores com mais coins:");
            int rank = 1;
            while (rs.next()) {
                String name = rs.getString("name");
                int coins = rs.getInt("coins");
                sender.sendMessage("§6" + rank + ". §f" + name + " - " + coins + " coins");
                rank++;
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            sender.sendMessage("Erro ao acessar o banco de dados.");
            e.printStackTrace();
        }
        return true;
    }
}
