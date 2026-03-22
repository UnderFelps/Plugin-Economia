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

public class PayCommand implements CommandExecutor {

    private final Connection conn = Economia.getInstance().getConnection();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Este comando só pode ser usado por jogadores.");
            return true;
        }
        if (args.length != 2) {
            sender.sendMessage("Uso: /pay <jogador> <quantia>");
            return true;
        }

        Player p = (Player) sender;
        String targetName = args[0];
        int amount;
        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            p.sendMessage("Quantia deve ser um número inteiro.");
            return true;
        }
        if (amount <= 0) {
            p.sendMessage("Quantia deve ser positiva.");
            return true;
        }
        Player target = Economia.getInstance().getServer().getPlayer(targetName);
        if (target == null) {
            p.sendMessage("Jogador não encontrado ou não está online.");
            return true;
        }
        if (target.equals(p)) {
            p.sendMessage("Você não pode transferir para si mesmo.");
            return true;
        }
        try {
            conn.setAutoCommit(false);
            // Check
            PreparedStatement ps = conn.prepareStatement("SELECT coins FROM players WHERE uuid = ?");
            ps.setString(1, p.getUniqueId().toString());
            ResultSet rs = ps.executeQuery();
            int senderCoins = 0;
            if (rs.next()) {
                senderCoins = rs.getInt("coins");
            }
            rs.close();
            ps.close();
            if (senderCoins < amount) {
                p.sendMessage("Você não tem coins suficientes.");
                conn.rollback();
                return true;
            }

            // sender
            PreparedStatement updateSender = conn.prepareStatement("UPDATE players SET coins = coins - ? WHERE uuid = ?");
            updateSender.setInt(1, amount);
            updateSender.setString(2, p.getUniqueId().toString());
            updateSender.executeUpdate();
            updateSender.close();

            // receiver
            PreparedStatement updateReceiver = conn.prepareStatement("INSERT OR REPLACE INTO players (uuid, name, coins) VALUES (?, ?, COALESCE((SELECT coins FROM players WHERE uuid = ?), 0) + ?)");
            updateReceiver.setString(1, target.getUniqueId().toString());
            updateReceiver.setString(2, target.getName());
            updateReceiver.setString(3, target.getUniqueId().toString());
            updateReceiver.setInt(4, amount);
            updateReceiver.executeUpdate();
            updateReceiver.close();
            conn.commit();

            p.sendMessage("Transferido " + amount + " coins para " + target.getName() + ".");
            target.sendMessage("Você recebeu " + amount + " coins de " + p.getName() + ".");
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            p.sendMessage("Erro ao processar a transferência.");
            e.printStackTrace();
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}
