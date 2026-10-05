package com.floodrelief.ui;

import com.floodrelief.config.AuthContext;
import com.floodrelief.service.AuthService;
import com.floodrelief.service.DatabaseManager;
import java.awt.*;
import javax.swing.*;

/**
 * Modern login dialog supporting PBKDF2 authentication and quick-test demo role profiles.
 */
public class LoginDialog extends JDialog {
    private final AuthService authService = new AuthService();
    private final JTextField txtUsername = new JTextField(18);
    private final JPasswordField txtPassword = new JPasswordField(18);
    private final JLabel lblStatus = new JLabel(" ");
    private boolean loginSuccessful = false;

    public LoginDialog(Frame parent) {
        super(parent, "FloodReliefTracker - Login", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        initComponents();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        JPanel contentPane = new JPanel(new BorderLayout(0, 16));
        contentPane.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        JLabel lblHeaderIcon = new JLabel("🌊 FloodReliefTracker");
        lblHeaderIcon.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblHeaderIcon.setForeground(new Color(13, 110, 253));
        lblHeaderIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Cloud-Connected Disaster Supply Chain System");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(Color.GRAY);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(lblHeaderIcon);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(lblSub);

        // Form Fields
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtUsername, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtPassword, gbc);

        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblStatus.setForeground(new Color(220, 53, 69));
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);

        JButton btnLogin = new JButton("Sign In");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLogin.setBackground(new Color(13, 110, 253));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setPreferredSize(new Dimension(0, 36));
        btnLogin.addActionListener(e -> performLogin());

        // Default button on Enter
        getRootPane().setDefaultButton(btnLogin);

        // Quick Demo Accounts Panel
        JPanel demoPanel = new JPanel(new GridLayout(2, 3, 6, 6));
        demoPanel.setBorder(BorderFactory.createTitledBorder("Quick Demo Roles"));

        JButton btnAdmin = new JButton("👑 Admin");
        btnAdmin.setToolTipText("Full administration & user management");
        btnAdmin.addActionListener(e -> fillAndLogin("admin", "admin123"));

        JButton btnCoord = new JButton("🚚 Coordinator");
        btnCoord.setToolTipText("Logistics, shipments, inventory & overrides");
        btnCoord.addActionListener(e -> fillAndLogin("coord", "coord123"));

        JButton btnCamp = new JButton("⛺ Camp Mgr");
        btnCamp.setToolTipText("Camp registration & distributions");
        btnCamp.addActionListener(e -> fillAndLogin("campmgr", "camp123"));

        JButton btnVol = new JButton("🤝 Volunteer");
        btnVol.setToolTipText("Aid distribution assistant");
        btnVol.addActionListener(e -> fillAndLogin("vol1", "vol123"));

        JButton btnDonor = new JButton("👁️ Donor");
        btnDonor.setToolTipText("Read-only transparency dashboard");
        btnDonor.addActionListener(e -> fillAndLogin("donor1", "donor123"));

        JButton btnClear = new JButton("Clear");
        btnClear.addActionListener(e -> {
            txtUsername.setText("");
            txtPassword.setText("");
            lblStatus.setText(" ");
        });

        demoPanel.add(btnAdmin);
        demoPanel.add(btnCoord);
        demoPanel.add(btnCamp);
        demoPanel.add(btnVol);
        demoPanel.add(btnDonor);
        demoPanel.add(btnClear);

        // Footer status
        JLabel lblDbStatus = new JLabel(DatabaseManager.isConnected() ? "🟢 " + DatabaseManager.getConnectionMessage() : "🔴 Offline");
        lblDbStatus.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblDbStatus.setForeground(DatabaseManager.isConnected() ? new Color(40, 167, 69) : Color.RED);
        lblDbStatus.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.add(formPanel, BorderLayout.NORTH);
        centerPanel.add(lblStatus, BorderLayout.CENTER);
        centerPanel.add(btnLogin, BorderLayout.SOUTH);

        JPanel bottomPanel = new JPanel(new BorderLayout(0, 8));
        bottomPanel.add(demoPanel, BorderLayout.CENTER);
        bottomPanel.add(lblDbStatus, BorderLayout.SOUTH);

        contentPane.add(headerPanel, BorderLayout.NORTH);
        contentPane.add(centerPanel, BorderLayout.CENTER);
        contentPane.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(contentPane);
        txtUsername.setText("admin");
        txtPassword.setText("admin123");
    }

    private void fillAndLogin(String user, String pass) {
        txtUsername.setText(user);
        txtPassword.setText(pass);
        performLogin();
    }

    private void performLogin() {
        String u = txtUsername.getText().trim();
        String p = new String(txtPassword.getPassword());

        if (u.isEmpty() || p.isEmpty()) {
            lblStatus.setText("Please enter username and password.");
            return;
        }

        lblStatus.setText("Authenticating with Cloud PostgreSQL...");
        lblStatus.setForeground(Color.DARK_GRAY);

        SwingUtilities.invokeLater(() -> {
            boolean success = authService.login(u, p);
            if (success) {
                loginSuccessful = true;
                dispose();
            } else {
                lblStatus.setText("Invalid username or password.");
                lblStatus.setForeground(new Color(220, 53, 69));
            }
        });
    }

    public boolean isLoginSuccessful() {
        return loginSuccessful;
    }
}
