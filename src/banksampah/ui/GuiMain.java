package banksampah.ui;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Color;

/**
 * Titik masuk aplikasi GUI Desktop Bank Sampah (Pure Java + FlatLaf + Neon PostgreSQL).
 */
public class GuiMain {

    public static void main(String[] args) {
        try {
            FlatLightLaf.setup();
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("TextComponent.arc", 10);
            UIManager.put("TabbedPane.selectedBackground", Color.WHITE);
            UIManager.put("TabbedPane.underlineColor", Theme.TECH_BLUE);
        } catch (Exception ignored) {
            // Gunakan Look and Feel default bila FlatLaf gagal dimuat
        }

        SwingUtilities.invokeLater(() -> {
            BankSampahContext context = new BankSampahContext();
            try {
                context.loadInitialData();
                new LoginFrame(context).setVisible(true);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(
                        null,
                        "Gagal terhubung ke database Neon PostgreSQL:\n" + ex.getMessage()
                                + "\n\nPastikan koneksi internet aktif dan file .env berisi DATABASE_URL yang valid.",
                        "Koneksi Neon DB Gagal",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
}
