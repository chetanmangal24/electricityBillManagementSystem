package com.ebms.ui;

import com.ebms.service.AuthService;
import com.ebms.service.DashboardService;
import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Map;

/**
 * Main application frame with navigation and dashboard.
 */
public class MainFrame extends JFrame {

    private final AuthService authService;
    private final DashboardService dashboardService;
    private JPanel contentPanel;
    private CardLayout cardLayout;

    public MainFrame(AuthService authService) {
        this.authService = authService;
        this.dashboardService = new DashboardService();
        initComponents();
        showDashboard();
    }

    private void initComponents() {
        setTitle("EBMS - Electricity Bill Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);

        // Menu bar
        JMenuBar menuBar = new JMenuBar();

        // Consumers menu
        JMenu consumersMenu = new JMenu("Consumers");
        JMenuItem addConsumer = new JMenuItem("Add Consumer");
        addConsumer.addActionListener(e -> showPanel("addConsumer"));
        JMenuItem viewConsumers = new JMenuItem("View Consumers");
        viewConsumers.addActionListener(e -> showPanel("viewConsumers"));
        consumersMenu.add(addConsumer);
        consumersMenu.add(viewConsumers);

        // Meter Readings menu
        JMenu readingsMenu = new JMenu("Meter Readings");
        JMenuItem addReading = new JMenuItem("Record Reading");
        addReading.addActionListener(e -> showPanel("addReading"));
        JMenuItem viewReadings = new JMenuItem("View Readings");
        viewReadings.addActionListener(e -> showPanel("viewReadings"));
        readingsMenu.add(addReading);
        readingsMenu.add(viewReadings);

        // Bills menu
        JMenu billsMenu = new JMenu("Bills");
        JMenuItem generateBill = new JMenuItem("Generate Bill");
        generateBill.addActionListener(e -> showPanel("generateBill"));
        JMenuItem viewBills = new JMenuItem("View Bills");
        viewBills.addActionListener(e -> showPanel("viewBills"));
        billsMenu.add(generateBill);
        billsMenu.add(viewBills);

        // Payments menu
        JMenu paymentsMenu = new JMenu("Payments");
        JMenuItem recordPayment = new JMenuItem("Record Payment");
        recordPayment.addActionListener(e -> showPanel("recordPayment"));
        JMenuItem viewPayments = new JMenuItem("Payment History");
        viewPayments.addActionListener(e -> showPanel("viewPayments"));
        paymentsMenu.add(recordPayment);
        paymentsMenu.add(viewPayments);

        // System menu
        JMenu systemMenu = new JMenu("System");
        JMenuItem dashboard = new JMenuItem("Dashboard");
        dashboard.addActionListener(e -> showDashboard());
        JMenuItem logout = new JMenuItem("Logout");
        logout.addActionListener(e -> handleLogout());
        systemMenu.add(dashboard);
        systemMenu.addSeparator();
        systemMenu.add(logout);

        menuBar.add(consumersMenu);
        menuBar.add(readingsMenu);
        menuBar.add(billsMenu);
        menuBar.add(paymentsMenu);
        menuBar.add(systemMenu);

        setJMenuBar(menuBar);

        // Content panel with CardLayout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // Add panels
        contentPanel.add(createDashboardPanel(), "dashboard");
        contentPanel.add(new ConsumerPanel(), "addConsumer");
        contentPanel.add(new ViewConsumersPanel(), "viewConsumers");
        contentPanel.add(new MeterReadingPanel(), "addReading");
        contentPanel.add(new ViewReadingsPanel(), "viewReadings");
        contentPanel.add(new GenerateBillPanel(), "generateBill");
        contentPanel.add(new ViewBillsPanel(), "viewBills");
        contentPanel.add(new PaymentPanel(), "recordPayment");
        contentPanel.add(new ViewPaymentsPanel(), "viewPayments");

        add(contentPanel);
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Dashboard", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        // Load statistics
        try {
            Map<String, Object> stats = dashboardService.getStatistics();

            statsPanel.add(createStatCard("Total Consumers",
                String.valueOf(stats.get("totalConsumers")), Color.decode("#4CAF50")));
            statsPanel.add(createStatCard("Pending Bills",
                String.valueOf(stats.get("pendingBills")), Color.decode("#FF9800")));
            statsPanel.add(createStatCard("Total Pending Amount",
                "₹" + ((BigDecimal)stats.get("totalPendingAmount")).toString(), Color.decode("#F44336")));
            statsPanel.add(createStatCard("System Status",
                "Active", Color.decode("#2196F3")));

        } catch (SQLException e) {
            JLabel errorLabel = new JLabel("Error loading statistics: " + e.getMessage());
            errorLabel.setForeground(Color.RED);
            statsPanel.add(errorLabel);
        }

        panel.add(statsPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 2),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        titleLabel.setForeground(Color.GRAY);

        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 32));
        valueLabel.setForeground(color);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private void showPanel(String panelName) {
        cardLayout.show(contentPanel, panelName);
    }

    private void showDashboard() {
        contentPanel.remove(0);
        contentPanel.add(createDashboardPanel(), "dashboard", 0);
        cardLayout.show(contentPanel, "dashboard");
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            authService.logout();
            dispose();
            SwingUtilities.invokeLater(() -> {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            });
        }
    }
}
