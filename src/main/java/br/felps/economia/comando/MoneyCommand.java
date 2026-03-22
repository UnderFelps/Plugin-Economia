package br.felps.economia.comando;

import br.felps.economia.Economia;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MoneyCommand implements CommandExecutor {

    private final Connection conn = Economia.getInstance().getConnection();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Este comando só pode ser usado por jogadores.");
                return true;
            }
            Player p = (Player) sender;
            try {
                PreparedStatement ps = conn.prepareStatement("SELECT coins FROM players WHERE uuid = ?");
                ps.setString(1, p.getUniqueId().toString());
                ResultSet rs = ps.executeQuery();
                int coins = 0;
                if (rs.next()) {
                    coins = rs.getInt("coins");
                } else {
                    PreparedStatement insert = conn.prepareStatement("INSERT INTO players (uuid, name, coins) VALUES (?, ?, 0)");
                    insert.setString(1, p.getUniqueId().toString());
                    insert.setString(2, p.getName());
                    insert.executeUpdate();
                    insert.close();
                }
                p.sendMessage("Você tem " + coins + " coins.");
                rs.close();
                ps.close();
            } catch (SQLException e) {
                p.sendMessage("Erro ao acessar o banco de dados.");
                e.printStackTrace();
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            if (!sender.hasPermission("economia.setmoney")) {
                sender.sendMessage("Você não tem permissão para usar este comando.");
                return true;
            }
            String targetName = args[1];
            int amount;
            try {
                amount = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage("Quantia deve ser um número inteiro.");
                return true;
            }
            if (amount < 0) {
                sender.sendMessage("Quantia deve ser não negativa.");
                return true;
            }
            try {
                PreparedStatement ps = conn.prepareStatement("SELECT uuid FROM players WHERE name = ?");
                ps.setString(1, targetName);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String uuid = rs.getString("uuid");
                    PreparedStatement update = conn.prepareStatement("UPDATE players SET coins = ? WHERE uuid = ?");
                    update.setInt(1, amount);
                    update.setString(2, uuid);
                    update.executeUpdate();
                    update.close();
                    sender.sendMessage("Coins de " + targetName + " definidos para " + amount + ".");
                } else {
                    sender.sendMessage("Jogador não encontrado no banco de dados.");
                }
                rs.close();
                ps.close();
            } catch (SQLException e) {
                sender.sendMessage("Erro ao acessar o banco de dados.");
                e.printStackTrace();
            }
        } else {
            sender.sendMessage("Uso: /money ou /money set <jogador> <quantia>");
        }
        return true;
    }
}
