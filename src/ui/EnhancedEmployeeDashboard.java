// 5. ENHANCED GUI WITH IMPROVED USABILITY AND BUG FIXES

package ui;

import dao.*;
import model.*;
import service.PayrollCalculator;
import reports.JasperReportGenerator;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Enhanced Employee Dashboard with improved usability and modern UI design
 */
public class EnhancedEmployeeDashboard extends JFrame {
    private Employee currentUser;
    private JTabbedPane tabbedPane;
    
    // Enhanced UI components
    private JPanel dashboardPanel;
    private JPanel quickActionsPanel;
    private JLabel welcomeLabel;
    private JProgressBar loadingProgressBar;
    
    // Service layers
    private PayrollCalculator payrollCalculator;
    private JasperReportGenerator reportGenerator;
    private AttendanceDAO attendanceDAO;
    
    // Modern color scheme
    private static final Color PRIMARY_COLOR = new Color(37, 99, 235);
    private static final Color SECONDARY_COLOR = new Color(147, 197, 253);
    private static final Color SUCCESS_COLOR = new Color(34, 197, 94);
    private static final Color WARNING_COLOR = new Color(251, 191, 36);
    private static final Color ERROR_COLOR = new Color(239, 68, 68);
    private static final Color BACKGROUND_COLOR = new Color(249, 250, 251);
    private static final Color CARD_COLOR = Color.WHITE;
    
    public EnhancedEmployeeDashboard(Employee user) {
        this.currentUser = user;
        
        try {
            // Initialize services
            this.payrollCalculator = new PayrollCalculator();
            this.reportGenerator = new JasperReportGenerator();
            this.attendanceDAO = new AttendanceDAO();
            
            // Set modern look and feel
            setModernLookAndFeel();
            
            // Initialize UI
            initializeEnhancedComponents();
            setupEnhancedLayout();
            setupEnhancedEventHandlers();
            
            // Load data asynchronously
            loadDataAsync();
            
            System.out.println("✅ Enhanced Employee Dashboard initialized successfully for: " + user.getFullName());
            
        } catch (Exception e) {
            System.err.println("❌ Enhanced Dashboard initialization failed: " + e.getMessage());
            e.printStackTrace();
            showErrorDialog("Dashboard Initialization Error", 
                "Failed to initialize dashboard. Please contact IT support.", e);
        }
        
        // Set enhanced window properties
        setTitle("MotorPH Payroll System - Employee Portal (Enhanced)");
        setSize(1400, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1200, 700));
    }
    
    /**
     * Set modern look and feel with custom styling
     */
    private void setModernLookAndFeel() {
        try {
            // Use system look and feel as base
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeel());
            
            // Customize UI defaults for modern appearance
            UIManager.put("Panel.background", BACKGROUND_COLOR);
            UIManager.put("TabbedPane.background", BACKGROUND_COLOR);
            UIManager.put("TabbedPane.selectedBackground", CARD_COLOR);
            UIManager.put("Button.background", PRIMARY_COLOR);
            UIManager.put("Button.foreground", Color.WHITE);
            UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 12));
            UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 12));
            UIManager.put("TextField.font", new Font("Segoe UI", Font.PLAIN, 12));
            UIManager.put("Table.font", new Font("Segoe UI", Font.PLAIN, 11));
            UIManager.put("Table.gridColor", new Color(229, 231, 235));
            UIManager.put("Table.selectionBackground", SECONDARY_COLOR);
            
        } catch (Exception e) {
            System.err.println("Could not set look and feel: " + e.getMessage());
        }
    }
    
    /**
     * Initialize enhanced UI components with modern design
     */
    private void initializeEnhancedComponents() {
        setBackground(BACKGROUND_COLOR);
        
        // Enhanced tabbed pane with modern styling
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabbedPane.setBackground(BACKGROUND_COLOR);
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Loading progress bar
        loadingProgressBar = new JProgressBar();
        loadingProgressBar.setStringPainted(true);
        loadingProgressBar.setString("Loading dashboard...");
        loadingProgressBar.setForeground(PRIMARY_COLOR);
        
        // Enhanced welcome label
        welcomeLabel = new JLabel();
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        welcomeLabel.setForeground(PRIMARY_COLOR);
    }
    
    /**
     * Setup enhanced layout with modern design principles
     */
    private void setupEnhancedLayout() {
        setLayout(new BorderLayout());
        
        // Enhanced header panel
        JPanel headerPanel = createEnhancedHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Main content with loading overlay
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        
        // Initially show loading panel
        JPanel loadingPanel = createLoadingPanel();
        mainPanel.add(loadingPanel, BorderLayout.CENTER);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Enhanced status bar
        JPanel statusPanel = createEnhancedStatusPanel();
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Create enhanced header panel with modern styling
     */
    private JPanel createEnhancedHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        // Left side - Company info and welcome
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(PRIMARY_COLOR);
        
        JLabel titleLabel = new JLabel("MotorPH Payroll System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        
        welcomeLabel.setText("Welcome back, " + currentUser.getFirstName() + "!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        welcomeLabel.setForeground(new Color(191, 219, 254));
        
        leftPanel.add(titleLabel, BorderLayout.NORTH);
        leftPanel.add(welcomeLabel, BorderLayout.SOUTH);
        
        // Right side - User info and actions
        JPanel rightPanel = createUserInfoPanel();
        
        headerPanel.add(leftPanel, BorderLayout.WEST);
        headerPanel.add(rightPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    /**
     * Create user info panel with profile picture placeholder and quick actions
     */
    private JPanel createUserInfoPanel() {
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userPanel.setBackground(PRIMARY_COLOR);
        
        // User info card
        JPanel userCard = new JPanel(new BorderLayout());
        userCard.setBackground(new Color(59, 130, 246));
        userCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(147, 197, 253), 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)));
        userCard.setPreferredSize(new Dimension(200, 60));
        
        // Profile icon (using emoji as placeholder)
        JLabel profileIcon = new JLabel("👤");
        profileIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        profileIcon.setForeground(Color.WHITE);
        
        // User details
        JPanel userDetails = new JPanel(new GridLayout(2, 1));
        userDetails.setBackground(new Color(59, 130, 246));
        
        JLabel nameLabel = new JLabel(currentUser.getFullName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        nameLabel.setForeground(Color.WHITE);
        
        JLabel roleLabel = new JLabel("Employee ID: " + currentUser.getEmployeeId());
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        roleLabel.setForeground(new Color(191, 219, 254));
        
        userDetails.add(nameLabel);
        userDetails.add(roleLabel);
        
        userCard.add(profileIcon, BorderLayout.WEST);
        userCard.add(userDetails, BorderLayout.CENTER);
        
        // Quick action buttons
        JButton settingsButton = createModernButton("⚙️ Settings", SECONDARY_COLOR);
        settingsButton.setPreferredSize(new Dimension(100, 35));
        settingsButton.addActionListener(e -> showUserSettings());
        
        JButton logoutButton = createModernButton("🚪 Logout", new Color(220, 38, 38));
        logoutButton.setPreferredSize(new Dimension(80, 35));
        logoutButton.addActionListener(e -> logout());
        
        userPanel.add(userCard);
        userPanel.add(settingsButton);
        userPanel.add(logoutButton);
        
        return userPanel;
    }
    
    /**
     * Create loading panel with progress indicator
     */
    private JPanel createLoadingPanel() {
        JPanel loadingPanel = new JPanel(new GridBagLayout());
        loadingPanel.setBackground(BACKGROUND_COLOR);
        
        JPanel loadingCard = new JPanel(new BorderLayout());
        loadingCard.setBackground(CARD_COLOR);
        loadingCard.setBorder(createModernCardBorder("Loading Dashboard"));
        loadingCard.setPreferredSize(new Dimension(400, 150));
        
        JLabel loadingIcon = new JLabel("⏳", JLabel.CENTER);
        loadingIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        
        JLabel loadingText = new JLabel("Please wait while we load your dashboard...", JLabel.CENTER);
        loadingText.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        loadingText.setForeground(new Color(107, 114, 128));
        
        loadingProgressBar.setPreferredSize(new Dimension(300, 25));
        
        JPanel contentPanel = new JPanel(new GridLayout(3, 1, 0, 10));
        contentPanel.setBackground(CARD_COLOR);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        contentPanel.add(loadingIcon);
        contentPanel.add(loadingText);
        contentPanel.add(loadingProgressBar);
        
        loadingCard.add(contentPanel, BorderLayout.CENTER);
        loadingPanel.add(loadingCard);
        
        return loadingPanel;
    }
    
    /**
     * Create enhanced status panel
     */
    private JPanel createEnhancedStatusPanel() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(new Color(243, 244, 246));
        statusPanel.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        
        // Status info
        JLabel statusLabel = new JLabel("Ready | Last login: " + LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusLabel.setForeground(new Color(107, 114, 128));
        
        // System info
        JLabel systemLabel = new JLabel("MotorPH Payroll System v2.0 | Enhanced UI");
        systemLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        systemLabel.setForeground(new Color(156, 163, 175));
        
        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(systemLabel, BorderLayout.EAST);
        
        return statusPanel;
    }
    
    /**
     * Load data asynchronously to improve perceived performance
     */
    private void loadDataAsync() {
        SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Simulate loading steps with progress updates
                publish(20);
                Thread.sleep(500); // Load user preferences
                
                publish(40);
                Thread.sleep(500); // Load attendance data
                
                publish(60);
                Thread.sleep(500); // Load payroll data
                
                publish(80);
                Thread.sleep(500); // Setup UI components
                
                publish(100);
                return null;
            }
            
            @Override
            protected void process(List<Integer> chunks) {
                int progress = chunks.get(chunks.size() - 1);
                loadingProgressBar.setValue(progress);
                loadingProgressBar.setString("Loading... " + progress + "%");
            }
            
            @Override
            protected void done() {
                try {
                    // Replace loading panel with actual dashboard content
                    setupMainDashboardContent();
                } catch (Exception e) {
                    showErrorDialog("Loading Error", "Failed to load dashboard content", e);
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Setup main dashboard content after loading
     */
    private void setupMainDashboardContent() {
        // Remove loading panel and add actual tabs
        getContentPane().removeAll();
        
        // Re-add header and status
        add(createEnhancedHeaderPanel(), BorderLayout.NORTH);
        add(createEnhancedStatusPanel(), BorderLayout.SOUTH);
        
        // Setup enhanced tabs
        setupEnhancedTabs();
        
        // Add tabbed pane to center
        add(tabbedPane, BorderLayout.CENTER);
        
        // Refresh layout
        revalidate();
        repaint();
        
        // Show success notification
        showNotification("Dashboard loaded successfully!", SUCCESS_COLOR);
    }
    
    /**
     * Setup enhanced tabs with modern design
     */
    private void setupEnhancedTabs() {
        // Dashboard Overview tab
        tabbedPane.addTab("📊 Dashboard", createDashboardOverviewTab());
        
        // Personal Information tab
        tabbedPane.addTab("👤 My Profile", createEnhancedPersonalInfoTab());
        
        // Attendance tab
        tabbedPane.addTab("📅 My Attendance", createEnhancedAttendanceTab());
        
        // Payroll tab
        tabbedPane.addTab("💰 My Payroll", createEnhancedPayrollTab());
        
        // Leave Requests tab
        tabbedPane.addTab("🏖️ Leave Requests", createEnhancedLeaveTab());
        
        // Reports tab
        tabbedPane.addTab("📄 My Reports", createEnhancedReportsTab());
    }
    
    /**
     * Create dashboard overview tab with key metrics and quick actions
     */
    private JPanel createDashboardOverviewTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Top metrics cards
        JPanel metricsPanel = createMetricsPanel();
        
        // Middle section with charts and recent activity
        JPanel middlePanel = new JPanel(new GridLayout(1, 2, 20, 0));
        middlePanel.setBackground(BACKGROUND_COLOR);
        
        // Recent attendance chart placeholder
        JPanel attendanceChartPanel = createChartCard("📊 Attendance Trend", "Your attendance for the last 30 days");
        
        // Recent activity feed
        JPanel activityPanel = createActivityFeedCard();
        
        middlePanel.add(attendanceChartPanel);
        middlePanel.add(activityPanel);
        
        // Quick actions panel
        JPanel quickActionsPanel = createQuickActionsPanel();
        
        panel.add(metricsPanel, BorderLayout.NORTH);
        panel.add(middlePanel, BorderLayout.CENTER);
        panel.add(quickActionsPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Create metrics panel with key performance indicators
     */
    private JPanel createMetricsPanel() {
        JPanel metricsPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        metricsPanel.setBackground(BACKGROUND_COLOR);
        metricsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        // Attendance rate metric
        JPanel attendanceMetric = createMetricCard("📊", "Attendance Rate", "96.5%", SUCCESS_COLOR);
        
        // Current month earnings
        JPanel earningsMetric = createMetricCard("💰", "This Month", "₱45,250", PRIMARY_COLOR);
        
        // Leave balance
        JPanel leaveMetric = createMetricCard("🏖️", "Leave Balance", "12 days", WARNING_COLOR);
        
        // Performance score
        JPanel performanceMetric = createMetricCard("⭐", "Performance", "Excellent", SUCCESS_COLOR);
        
        metricsPanel.add(attendanceMetric);
        metricsPanel.add(earningsMetric);
        metricsPanel.add(leaveMetric);
        metricsPanel.add(performanceMetric);
        
        return metricsPanel;
    }
    
    /**
     * Create individual metric card
     */
    private JPanel createMetricCard(String icon, String title, String value, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_COLOR);
        card.setBorder(createModernCardBorder(null));
        card.setPreferredSize(new Dimension(200, 100));
        
        // Icon and accent
        JPanel iconPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        iconPanel.setBackground(CARD_COLOR);
        
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        iconPanel.add(iconLabel);
        
        // Content
        JPanel contentPanel = new JPanel(new GridLayout(2, 1));
        contentPanel.setBackground(CARD_COLOR);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(new Color(107, 114, 128));
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        valueLabel.setForeground(accentColor);
        
        contentPanel.add(titleLabel);
        contentPanel.add(valueLabel);
        
        card.add(iconPanel, BorderLayout.WEST);
        card.add(contentPanel, BorderLayout.CENTER);
        
        return card;
    }
    
    /**
     * Create chart card placeholder
     */
    private JPanel createChartCard(String title, String description) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_COLOR);
        card.setBorder(createModernCardBorder(title));
        
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(CARD_COLOR);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(new Color(107, 114, 128));
        
        // Placeholder for actual chart
        JPanel chartPlaceholder = new JPanel();
        chartPlaceholder.setBackground(new Color(243, 244, 246));
        chartPlaceholder.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235)));
        chartPlaceholder.setPreferredSize(new Dimension(300, 200));
        
        JLabel chartLabel = new JLabel("📈 Chart will be displayed here", JLabel.CENTER);
        chartLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chartLabel.setForeground(new Color(156, 163, 175));
        chartPlaceholder.add(chartLabel);
        
        contentPanel.add(descLabel, BorderLayout.NORTH);
        contentPanel.add(chartPlaceholder, BorderLayout.CENTER);
        
        card.add(contentPanel, BorderLayout.CENTER);
        
        return card;
    }
    
    /**
     * Create activity feed card
     */
    private JPanel createActivityFeedCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_COLOR);
        card.setBorder(createModernCardBorder("📝 Recent Activity"));
        
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(CARD_COLOR);
        listPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Sample activity items
        String[] activities = {
            "✅ Clocked in at 8:00 AM",
            "📄 Payslip generated for October",
            "🏖️ Leave request approved",
            "⏰ Overtime logged: 2 hours",
            "📊 Performance review completed"
        };
        
        for (String activity : activities) {
            JPanel activityItem = createActivityItem(activity);
            listPanel.add(activityItem);
            listPanel.add(Box.createVerticalStrut(10));
        }
        
        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        card.add(scrollPane, BorderLayout.CENTER);
        
        return card;
    }
    
    /**
     * Create individual activity item
     */
    private JPanel createActivityItem(String text) {
        JPanel item = new JPanel(new BorderLayout());
        item.setBackground(CARD_COLOR);
        item.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        JLabel timeLabel = new JLabel("2 hours ago");
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        timeLabel.setForeground(new Color(156, 163, 175));
        
        item.add(label, BorderLayout.WEST);
        item.add(timeLabel, BorderLayout.EAST);
        
        // Add hover effect
        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                item.setBackground(BACKGROUND_COLOR);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                item.setBackground(CARD_COLOR);
            }
        });
        
        return item;
    }
    
    /**
     * Create quick actions panel
     */
    private JPanel createQuickActionsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        panel.setBackground(BACKGROUND_COLOR);
        
        // Quick action buttons
        JButton clockInOutButton = createModernActionButton("🕒 Clock In/Out", "Record your attendance", PRIMARY_COLOR);
        JButton viewPayslipButton = createModernActionButton("💰 View Latest Payslip", "See your current payslip", SUCCESS_COLOR);
        JButton requestLeaveButton = createModernActionButton("🏖️ Request Leave", "Submit a leave request", WARNING_COLOR);
        JButton updateProfileButton = createModernActionButton("👤 Update Profile", "Modify your information", SECONDARY_COLOR);
        
        // Add action listeners
        clockInOutButton.addActionListener(e -> showClockInOutDialog());
        viewPayslipButton.addActionListener(e -> viewLatestPayslip());
        requestLeaveButton.addActionListener(e -> showLeaveRequestDialog());
        updateProfileButton.addActionListener(e -> tabbedPane.setSelectedIndex(1)); // Switch to profile tab
        
        panel.add(clockInOutButton);
        panel.add(viewPayslipButton);
        panel.add(requestLeaveButton);
        panel.add(updateProfileButton);
        
        return panel;
    }
    
    /**
     * Create modern action button with description
     */
    private JButton createModernActionButton(String title, String description, Color color) {
        JButton button = new JButton();
        button.setLayout(new BorderLayout());
        button.setBackground(CARD_COLOR);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 2),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)));
        button.setPreferredSize(new Dimension(200, 80));
        button.setFocusPainted(false);
        
        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLabel.setForeground(color);
        
        JLabel descLabel = new JLabel(description, JLabel.CENTER);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        descLabel.setForeground(new Color(107, 114, 128));
        
        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setBackground(CARD_COLOR);
        textPanel.add(titleLabel);
        textPanel.add(descLabel);
        
        button.add(textPanel, BorderLayout.CENTER);
        
        // Add hover effects
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(249, 250, 251));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(CARD_COLOR);
            }
        });
        
        return button;
    }
    
    /**
     * Create enhanced personal info tab with modern cards
     */
    private JPanel createEnhancedPersonalInfoTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Personal information cards
        JPanel cardsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        cardsPanel.setBackground(BACKGROUND_COLOR);
        
        // Basic info card
        JPanel basicInfoCard = createPersonalInfoCard("👤 Basic Information", createBasicInfoPanel());
        
        // Contact info card
        JPanel contactInfoCard = createPersonalInfoCard("📞 Contact Information", createContactInfoPanel());
        
        // Employment info card
        JPanel employmentInfoCard = createPersonalInfoCard("💼 Employment Details", createEmploymentInfoPanel());
        
        // Compensation info card
        JPanel compensationCard = createPersonalInfoCard("💰 Compensation Package", createCompensationPanel());
        
        cardsPanel.add(basicInfoCard);
        cardsPanel.add(contactInfoCard);
        cardsPanel.add(employmentInfoCard);
        cardsPanel.add(compensationCard);
        
        panel.add(cardsPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Create personal info card container
     */
    private JPanel createPersonalInfoCard(String title, JPanel content) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_COLOR);
        card.setBorder(createModernCardBorder(title));
        
        content.setBackground(CARD_COLOR);
        card.add(content, BorderLayout.CENTER);
        
        return card;
    }
    
    /**
     * Create basic info panel
     */
    private JPanel createBasicInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.anchor = GridBagConstraints.WEST;
        
        addInfoRow(panel, gbc, 0, "Full Name:", currentUser.getFullName());
        addInfoRow(panel, gbc, 1, "Employee ID:", String.valueOf(currentUser.getEmployeeId()));
        addInfoRow(panel, gbc, 2, "Birthday:", currentUser.getBirthday() != null ? currentUser.getBirthday().toString() : "Not specified");
        addInfoRow(panel, gbc, 3, "Age:", String.valueOf(currentUser.getAge()));
        
        return panel;
    }
    
    /**
     * Create contact info panel
     */
    private JPanel createContactInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.anchor = GridBagConstraints.WEST;
        
        addInfoRow(panel, gbc, 0, "Phone:", current