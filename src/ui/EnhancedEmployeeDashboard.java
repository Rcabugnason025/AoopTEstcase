package ui;

import dao.EmployeeDAO;
import dao.AttendanceDAO;
import model.Employee;
import model.Attendance;
import model.Payroll;
import service.PayrollCalculator;
import service.JasperReportService;
import ui.PayrollDetailsDialog;
import ui.LoginForm;
import ui.LeaveRequestDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.SwingWorker;

/**
 * Enhanced Employee Dashboard with improved usability and bug fixes
 * Addresses mentor feedback: "GUI could use improvements in terms of usability. Found bugs in functionality."
 */
public class EnhancedEmployeeDashboard extends JFrame {
    private Employee currentUser;
    private JTabbedPane tabbedPane;

    // Enhanced UI components with better validation
    private JLabel nameLabel, positionLabel, statusLabel, salaryLabel;
    private JLabel phoneLabel, addressLabel, sssLabel, philhealthLabel;
    private JTable attendanceTable;
    private DefaultTableModel attendanceTableModel;
    private TableRowSorter<DefaultTableModel> attendanceTableSorter;
    private JLabel totalDaysLabel, averageHoursLabel, attendanceRateLabel;
    private JTable payrollTable;
    private DefaultTableModel payrollTableModel;
    private JComboBox<String> monthComboBox;
    private JComboBox<String> yearComboBox;
    private JProgressBar loadingProgressBar;
    private JLabel statusBarLabel;

    // Enhanced search and filter functionality
    private JTextField attendanceSearchField;
    private JComboBox<String> attendanceFilterComboBox;
    private JButton attendanceRefreshButton;
    private JButton payrollRefreshButton;

    // Services
    private AttendanceDAO attendanceDAO;
    private PayrollCalculator payrollCalculator;
    private JasperReportService jasperReportService;

    // Loading state management
    private boolean isLoadingData = false;

    public EnhancedEmployeeDashboard(Employee user) {
        this.currentUser = user;

        try {
            // Initialize services with error handling
            initializeServices();
            
            // Initialize UI components with enhanced features
            initializeEnhancedComponents();
            setupEnhancedLayout();
            setupEnhancedEventHandlers();

            // Load initial data with progress indication
            loadDataWithProgress();

            System.out.println("✅ Enhanced Employee Dashboard initialized successfully for: " + user.getFullName());

        } catch (Exception e) {
            System.err.println("❌ Enhanced Employee Dashboard initialization failed: " + e.getMessage());
            e.printStackTrace();
            createErrorInterface(e);
        }

        // Enhanced window properties
        setTitle("MotorPH Payroll System - Employee Portal (Enhanced)");
        setSize(1200, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        // Add window icon and enhanced features
        setupEnhancedWindow();
    }

    private void initializeServices() throws Exception {
        this.attendanceDAO = new AttendanceDAO();
        this.payrollCalculator = new PayrollCalculator();
        this.jasperReportService = new JasperReportService();
        
        // Test services to ensure they work
        attendanceDAO.getAttendanceByEmployeeId(currentUser.getEmployeeId());
        System.out.println("✅ Services initialized and tested successfully");
    }

    private void initializeEnhancedComponents() {
        // Enhanced tabbed pane with better styling
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));
        tabbedPane.setBackground(Color.WHITE);

        // Enhanced personal info labels with tooltips
        nameLabel = createEnhancedLabel("Loading...", "Employee's full name");
        positionLabel = createEnhancedLabel("Loading...", "Current job position");
        statusLabel = createEnhancedLabel("Loading...", "Employment status (Regular/Probationary)");
        salaryLabel = createEnhancedLabel("Loading...", "Basic monthly salary");
        phoneLabel = createEnhancedLabel("Loading...", "Contact phone number");
        addressLabel = createEnhancedLabel("Loading...", "Home address");
        sssLabel = createEnhancedLabel("Loading...", "Social Security System number");
        philhealthLabel = createEnhancedLabel("Loading...", "PhilHealth insurance number");

        // Enhanced attendance table with sorting and filtering
        String[] attendanceColumns = {"Date", "Log In", "Log Out", "Work Hours", "Status", "Late (min)", "Undertime (min)"};
        attendanceTableModel = new DefaultTableModel(attendanceColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                // Enhanced column type detection for better sorting
                if (columnIndex == 0) return java.sql.Date.class; // Date
                if (columnIndex == 3 || columnIndex == 5 || columnIndex == 6) return Double.class; // Numbers
                return String.class;
            }
        };

        attendanceTable = new JTable(attendanceTableModel);
        attendanceTableSorter = new TableRowSorter<>(attendanceTableModel);
        attendanceTable.setRowSorter(attendanceTableSorter);
        setupEnhancedTableStyling(attendanceTable);

        // Enhanced summary labels
        totalDaysLabel = createEnhancedLabel("Total Days: 0", "Total attendance days recorded");
        averageHoursLabel = createEnhancedLabel("Average Hours: 0.00", "Average work hours per day");
        attendanceRateLabel = createEnhancedLabel("Attendance Rate: 0%", "Percentage of expected work days attended");

        // Enhanced payroll table
        String[] payrollColumns = {"Period", "Days Worked", "Gross Pay", "Deductions", "Net Pay", "Actions"};
        payrollTableModel = new DefaultTableModel(payrollColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Only Actions column
            }
        };
        payrollTable = new JTable(payrollTableModel);
        setupEnhancedTableStyling(payrollTable);

        // Enhanced month/year selectors with validation
        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        monthComboBox = new JComboBox<>(months);
        monthComboBox.setSelectedIndex(LocalDate.now().getMonthValue() - 1);
        monthComboBox.setFont(new Font("Arial", Font.PLAIN, 12));
        monthComboBox.setToolTipText("Select month for payroll calculation");

        // Enhanced year selector with more years
        String[] years = {"2020", "2021", "2022", "2023", "2024", "2025", "2026"};
        yearComboBox = new JComboBox<>(years);
        yearComboBox.setSelectedItem("2024");
        yearComboBox.setFont(new Font("Arial", Font.PLAIN, 12));
        yearComboBox.setToolTipText("Select year for payroll calculation");

        // Enhanced search and filter components
        attendanceSearchField = new JTextField(15);
        attendanceSearchField.setFont(new Font("Arial", Font.PLAIN, 12));
        attendanceSearchField.setToolTipText("Search attendance records by date or status");
        
        String[] filterOptions = {"All Records", "Present Only", "Late Only", "Undertime Only", "Full Day Only"};
        attendanceFilterComboBox = new JComboBox<>(filterOptions);
        attendanceFilterComboBox.setFont(new Font("Arial", Font.PLAIN, 12));
        attendanceFilterComboBox.setToolTipText("Filter attendance records by type");

        // Enhanced action buttons
        attendanceRefreshButton = createEnhancedButton("🔄 Refresh", "Reload attendance data", 
                new Color(108, 117, 125), Color.WHITE);
        payrollRefreshButton = createEnhancedButton("🔄 Refresh", "Reload payroll data", 
                new Color(108, 117, 125), Color.WHITE);

        // Progress bar for loading states
        loadingProgressBar = new JProgressBar();
        loadingProgressBar.setStringPainted(true);
        loadingProgressBar.setVisible(false);

        // Enhanced status bar
        statusBarLabel = new JLabel("Ready");
        statusBarLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        statusBarLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    }

    private JLabel createEnhancedLabel(String text, String tooltip) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        label.setToolTipText(tooltip);
        return label;
    }

    private JButton createEnhancedButton(String text, String tooltip, Color bgColor, Color fgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 11));
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setToolTipText(tooltip);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }

    private void setupEnhancedTableStyling(JTable table) {
        table.setRowHeight(28);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(70, 130, 180));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(173, 216, 230));
        table.setGridColor(new Color(200, 200, 200));
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));
        
        // Enhanced selection model
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        // Add alternating row colors
        table.setDefaultRenderer(Object.class, new AlternatingRowRenderer());
    }

    private void setupEnhancedLayout() {
        setLayout(new BorderLayout());

        // Enhanced Header Panel with user info
        JPanel headerPanel = createEnhancedHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Enhanced tabs with improved content
        tabbedPane.addTab("📊 Personal Information", createEnhancedPersonalInfoTab());
        tabbedPane.addTab("📅 My Attendance", createEnhancedAttendanceTab());
        tabbedPane.addTab("💰 My Payroll", createEnhancedPayrollTab());

        add(tabbedPane, BorderLayout.CENTER);

        // Enhanced status bar with progress indicator
        JPanel statusPanel = createEnhancedStatusPanel();
        add(statusPanel, BorderLayout.SOUTH);
    }

  // CONTINUING FROM WHERE paste.txt LEFT OFF - Complete the Enhanced Employee Dashboard

    private JPanel createEnhancedHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(25, 25, 112));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Left side - title and user info
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setBackground(new Color(25, 25, 112));
        
        JLabel titleLabel = new JLabel("🏍️ MotorPH Payroll System - Employee Portal");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel userInfoLabel = new JLabel("Welcome, " + currentUser.getFullName() + " (ID: " + currentUser.getEmployeeId() + ")");
        userInfoLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        userInfoLabel.setForeground(Color.LIGHT_GRAY);
        
        leftPanel.add(titleLabel);
        leftPanel.add(Box.createHorizontalStrut(20));
        leftPanel.add(userInfoLabel);

        // Right side - action buttons
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setBackground(new Color(25, 25, 112));

        JButton profileButton = createHeaderButton("👤 Profile", "View/Edit Profile");
        JButton settingsButton = createHeaderButton("⚙️ Settings", "Application Settings");
        JButton logoutButton = createHeaderButton("🚪 Logout", "Logout from System");
        
        profileButton.addActionListener(e -> showUserProfile());
        settingsButton.addActionListener(e -> showSettings());
        logoutButton.addActionListener(e -> logout());

        rightPanel.add(profileButton);
        rightPanel.add(settingsButton);
        rightPanel.add(logoutButton);

        headerPanel.add(leftPanel, BorderLayout.WEST);
        headerPanel.add(rightPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JButton createHeaderButton(String text, String tooltip) {
        JButton button = new JButton(text);
        button.setBackground(Color.WHITE);
        button.setForeground(new Color(25, 25, 112));
        button.setFont(new Font("Arial", Font.BOLD, 11));
        button.setToolTipText(tooltip);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(100, 30));
        
        // Enhanced hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(230, 230, 230));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.WHITE);
            }
        });
        
        return button;
    }

    private JPanel createEnhancedPersonalInfoTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create scrollable main info panel
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();

        // Enhanced title with icon
        JLabel titleLabel = new JLabel("👤 Personal Information");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(25, 25, 112));
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 30, 0);
        gbc.anchor = GridBagConstraints.CENTER;
        infoPanel.add(titleLabel, gbc);

        // Reset grid settings
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;

        // Enhanced field addition with better spacing
        addEnhancedInfoField(infoPanel, gbc, "👤 Full Name:", nameLabel, 1);
        addEnhancedInfoField(infoPanel, gbc, "💼 Position:", positionLabel, 2);
        addEnhancedInfoField(infoPanel, gbc, "📋 Status:", statusLabel, 3);
        addEnhancedInfoField(infoPanel, gbc, "💰 Basic Salary:", salaryLabel, 4);
        addEnhancedInfoField(infoPanel, gbc, "📱 Phone:", phoneLabel, 5);
        addEnhancedInfoField(infoPanel, gbc, "🏠 Address:", addressLabel, 6);
        addEnhancedInfoField(infoPanel, gbc, "🆔 SSS Number:", sssLabel, 7);
        addEnhancedInfoField(infoPanel, gbc, "🏥 PhilHealth:", philhealthLabel, 8);

        // Wrap in scroll pane
        JScrollPane scrollPane = new JScrollPane(infoPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null);

        // Enhanced allowances panel
        JPanel allowancesPanel = createEnhancedAllowancesPanel();

        // Enhanced action panel
        JPanel actionPanel = createEnhancedActionPanel();

        panel.add(scrollPane, BorderLayout.NORTH);
        panel.add(allowancesPanel, BorderLayout.CENTER);
        panel.add(actionPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void addEnhancedInfoField(JPanel parent, GridBagConstraints gbc, String labelText, JLabel valueLabel, int row) {
        gbc.gridx = 0; gbc.gridy = row;
        gbc.insets = new Insets(10, 0, 10, 20);
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setPreferredSize(new Dimension(150, 25));
        parent.add(label, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        valueLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        valueLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        valueLabel.setOpaque(true);
        valueLabel.setBackground(new Color(248, 248, 255));
        parent.add(valueLabel, gbc);
    }

    private JPanel createEnhancedAllowancesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 2), 
                "💼 Monthly Allowances & Benefits",
                TitledBorder.LEFT, TitledBorder.TOP, 
                new Font("Arial", Font.BOLD, 16), 
                new Color(70, 130, 180));
        panel.setBorder(border);
        panel.setBackground(Color.WHITE);

        JPanel allowanceGrid = new JPanel(new GridLayout(2, 2, 20, 15));
        allowanceGrid.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        allowanceGrid.setBackground(Color.WHITE);

        // Enhanced allowance cards with animations
        JPanel ricePanel = createEnhancedAllowanceCard("🍚 Rice Subsidy",
                String.format("₱%.2f", currentUser.getRiceSubsidy()),
                new Color(144, 238, 144), "Monthly rice allowance");

        JPanel phonePanel = createEnhancedAllowanceCard("📱 Phone Allowance",
                String.format("₱%.2f", currentUser.getPhoneAllowance()),
                new Color(173, 216, 230), "Monthly communication allowance");

        JPanel clothingPanel = createEnhancedAllowanceCard("👔 Clothing Allowance",
                String.format("₱%.2f", currentUser.getClothingAllowance()),
                new Color(255, 182, 193), "Monthly clothing allowance");

        // Calculate total with better formatting
        double totalAllowances = currentUser.getRiceSubsidy() +
                currentUser.getPhoneAllowance() +
                currentUser.getClothingAllowance();
        JPanel totalPanel = createEnhancedAllowanceCard("💰 Total Allowances",
                String.format("₱%.2f", totalAllowances),
                new Color(255, 215, 0), "Sum of all monthly allowances");

        allowanceGrid.add(ricePanel);
        allowanceGrid.add(phonePanel);
        allowanceGrid.add(clothingPanel);
        allowanceGrid.add(totalPanel);

        panel.add(allowanceGrid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createEnhancedAllowanceCard(String title, String amount, Color bgColor, String tooltip) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(bgColor);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        card.setToolTipText(tooltip);

        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));

        JLabel amountLabel = new JLabel(amount, JLabel.CENTER);
        amountLabel.setFont(new Font("Arial", Font.BOLD, 20));

        // Add subtle animation on hover
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                card.setBackground(bgColor.brighter());
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(bgColor.darker(), 2),
                    BorderFactory.createEmptyBorder(14, 14, 14, 14)));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                card.setBackground(bgColor);
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.GRAY, 1),
                    BorderFactory.createEmptyBorder(15, 15, 15, 15)));
            }
        });

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(amountLabel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createEnhancedActionPanel() {
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        TitledBorder border = BorderFactory.createTitledBorder("🚀 Quick Actions");
        border.setTitleFont(new Font("Arial", Font.BOLD, 14));
        actionPanel.setBorder(border);
        actionPanel.setBackground(Color.WHITE);

        JButton leaveRequestButton = createEnhancedButton("📝 Submit Leave Request", 
                "Submit a new leave request", new Color(70, 130, 180), Color.WHITE);
        leaveRequestButton.setPreferredSize(new Dimension(200, 40));

        JButton viewPayslipButton = createEnhancedButton("💰 View Latest Payslip", 
                "View your latest payslip", new Color(34, 139, 34), Color.WHITE);
        viewPayslipButton.setPreferredSize(new Dimension(200, 40));

        JButton updateProfileButton = createEnhancedButton("👤 Update Profile", 
                "Update your personal information", new Color(255, 140, 0), Color.WHITE);
        updateProfileButton.setPreferredSize(new Dimension(200, 40));

        // Enhanced event handlers
        leaveRequestButton.addActionListener(e -> showLeaveRequestDialog());
        viewPayslipButton.addActionListener(e -> showLatestPayslip());
        updateProfileButton.addActionListener(e -> showUserProfile());

        actionPanel.add(leaveRequestButton);
        actionPanel.add(viewPayslipButton);
        actionPanel.add(updateProfileButton);

        return actionPanel;
    }

    private JPanel createEnhancedAttendanceTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Enhanced top panel with search and filter
        JPanel topPanel = createEnhancedAttendanceControlPanel();

        // Enhanced summary panel
        JPanel summaryPanel = createEnhancedAttendanceSummaryPanel();

        // Table panel with better styling
        JPanel tablePanel = new JPanel(new BorderLayout());
        TitledBorder tableBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                "📅 Attendance Records", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14), new Color(70, 130, 180));
        tablePanel.setBorder(tableBorder);

        JScrollPane attendanceScrollPane = new JScrollPane(attendanceTable);
        attendanceScrollPane.setPreferredSize(new Dimension(0, 400));
        tablePanel.add(attendanceScrollPane, BorderLayout.CENTER);

        // Combine panels
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(topPanel, BorderLayout.NORTH);
        northPanel.add(summaryPanel, BorderLayout.SOUTH);

        panel.add(northPanel, BorderLayout.NORTH);
        panel.add(tablePanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createEnhancedAttendanceControlPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Left side - search and filter
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.add(new JLabel("🔍 Search:"));
        leftPanel.add(attendanceSearchField);
        leftPanel.add(Box.createHorizontalStrut(10));
        leftPanel.add(new JLabel("🔽 Filter:"));
        leftPanel.add(attendanceFilterComboBox);

        // Right side - action buttons
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.add(attendanceRefreshButton);

        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createEnhancedAttendanceSummaryPanel() {
        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        TitledBorder border = BorderFactory.createTitledBorder("📊 Attendance Summary");
        border.setTitleFont(new Font("Arial", Font.BOLD, 12));
        summaryPanel.setBorder(border);
        summaryPanel.setBackground(new Color(248, 248, 255));

        // Create summary cards
        JPanel totalDaysCard = createSummaryCard("📅", totalDaysLabel, new Color(173, 216, 230));
        JPanel avgHoursCard = createSummaryCard("⏰", averageHoursLabel, new Color(144, 238, 144));
        JPanel attendanceRateCard = createSummaryCard("📈", attendanceRateLabel, new Color(255, 182, 193));

        summaryPanel.add(totalDaysCard);
        summaryPanel.add(avgHoursCard);
        summaryPanel.add(attendanceRateCard);

        return summaryPanel;
    }

    private JPanel createSummaryCard(String icon, JLabel dataLabel, Color bgColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(bgColor);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        JLabel iconLabel = new JLabel(icon, JLabel.CENTER);
        iconLabel.setFont(new Font("Arial", Font.BOLD, 20));

        dataLabel.setHorizontalAlignment(JLabel.CENTER);
        dataLabel.setFont(new Font("Arial", Font.BOLD, 12));

        card.add(iconLabel, BorderLayout.NORTH);
        card.add(dataLabel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createEnhancedPayrollTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Enhanced top panel with period selection
        JPanel topPanel = createEnhancedPayrollControlPanel();

        // Table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        TitledBorder tableBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(34, 139, 34), 2),
                "💰 Payroll Information", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14), new Color(34, 139, 34));
        tablePanel.setBorder(tableBorder);

        JScrollPane payrollScrollPane = new JScrollPane(payrollTable);
        tablePanel.add(payrollScrollPane, BorderLayout.CENTER);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(tablePanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createEnhancedPayrollControlPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Left side - period selection
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.add(new JLabel("📅 Payroll Period:"));
        leftPanel.add(monthComboBox);
        leftPanel.add(yearComboBox);

        // Right side - action buttons
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton calculateButton = createEnhancedButton("🧮 Calculate Payroll", 
                "Calculate payroll for selected period", new Color(70, 130, 180), Color.WHITE);
        calculateButton.addActionListener(e -> calculatePayrollWithProgress());
        
        rightPanel.add(payrollRefreshButton);
        rightPanel.add(calculateButton);

        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createEnhancedStatusPanel() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        statusPanel.setBackground(Color.WHITE);

        // Left side - status label
        statusBarLabel.setText("Welcome, " + currentUser.getFullName() + " | Employee ID: " + currentUser.getEmployeeId());
        
        // Right side - progress bar (hidden by default)
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.add(loadingProgressBar);

        statusPanel.add(statusBarLabel, BorderLayout.WEST);
        statusPanel.add(rightPanel, BorderLayout.EAST);

        return statusPanel;
    }

    private void setupEnhancedEventHandlers() {
        // Enhanced search functionality with real-time filtering
        attendanceSearchField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {}

            @Override
            public void keyReleased(KeyEvent e) {
                filterAttendanceData();
            }
        });

        // Enhanced filter functionality
        attendanceFilterComboBox.addActionListener(e -> filterAttendanceData());

        // Enhanced refresh buttons
        attendanceRefreshButton.addActionListener(e -> refreshAttendanceDataWithProgress());
        payrollRefreshButton.addActionListener(e -> refreshPayrollDataWithProgress());

        // Enhanced period selection
        monthComboBox.addActionListener(e -> loadPayrollDataWithProgress());
        yearComboBox.addActionListener(e -> loadPayrollDataWithProgress());
    }

    private void setupEnhancedWindow() {
        // Set application icon
        try {
            setIconImage(createApplicationIcon());
        } catch (Exception e) {
            System.err.println("Could not set application icon: " + e.getMessage());
        }

        // Enhanced window close operation
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                confirmAndExit();
            }
        });

        // Add keyboard shortcuts
        setupKeyboardShortcuts();
    }

    private Image createApplicationIcon() {
        java.awt.image.BufferedImage icon = new java.awt.image.BufferedImage(32, 32, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = icon.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw MotorPH icon
        g2d.setColor(new Color(25, 25, 112));
        g2d.fillRoundRect(0, 0, 32, 32, 8, 8);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("MP", 6, 20);
        
        g2d.dispose();
        return icon;
    }

    // PART 2: Enhanced Methods and Bug Fixes - Continuing the Enhanced Employee Dashboard

    private void setupKeyboardShortcuts() {
        // Add keyboard shortcuts for better usability
        JRootPane rootPane = getRootPane();
        
        // F5 - Refresh current tab
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "refresh");
        rootPane.getActionMap().put("refresh", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshCurrentTab();
            }
        });
        
        // Ctrl+L - Submit Leave Request
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK), "leave");
        rootPane.getActionMap().put("leave", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showLeaveRequestDialog();
            }
        });
        
        // Ctrl+P - View Payslip
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK), "payslip");
        rootPane.getActionMap().put("payslip", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showLatestPayslip();
            }
        });
    }

    private void refreshCurrentTab() {
        int selectedIndex = tabbedPane.getSelectedIndex();
        switch (selectedIndex) {
            case 0: // Personal Info
                loadPersonalInfo();
                break;
            case 1: // Attendance
                refreshAttendanceDataWithProgress();
                break;
            case 2: // Payroll
                refreshPayrollDataWithProgress();
                break;
        }
    }

    // ENHANCED DATA LOADING WITH PROGRESS INDICATION
    private void loadDataWithProgress() {
        setLoadingState(true, "Loading employee data...");
        
        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                publish("Loading personal information...");
                loadPersonalInfo();
                Thread.sleep(200); // Brief pause for UX
                
                publish("Loading attendance data...");
                loadAttendanceDataWithValidation();
                Thread.sleep(200);
                
                publish("Loading payroll data...");
                loadPayrollDataWithValidation();
                
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                if (!chunks.isEmpty()) {
                    updateStatusBar(chunks.get(chunks.size() - 1));
                }
            }

            @Override
            protected void done() {
                try {
                    get(); // Check for exceptions
                    updateStatusBar("Data loaded successfully");
                } catch (Exception e) {
                    updateStatusBar("Error loading data: " + e.getMessage());
                    showErrorDialog("Data Loading Error", 
                        "Failed to load some data. Please refresh to try again.\n\nError: " + e.getMessage());
                } finally {
                    setLoadingState(false, "Ready");
                }
            }
        };
        
        worker.execute();
    }

    private void setLoadingState(boolean loading, String message) {
        isLoadingData = loading;
        loadingProgressBar.setVisible(loading);
        updateStatusBar(message);
        
        // Disable/enable components during loading
        tabbedPane.setEnabled(!loading);
        attendanceRefreshButton.setEnabled(!loading);
        payrollRefreshButton.setEnabled(!loading);
        monthComboBox.setEnabled(!loading);
        yearComboBox.setEnabled(!loading);
    }

    private void updateStatusBar(String message) {
        statusBarLabel.setText(message);
        statusBarLabel.repaint();
    }

    // ENHANCED PERSONAL INFO LOADING WITH VALIDATION
    private void loadPersonalInfo() {
        try {
            // Validate user data first
            if (currentUser == null) {
                throw new IllegalStateException("User data is null");
            }

            // Load with null checks and default values
            nameLabel.setText(currentUser.getFullName() != null ? currentUser.getFullName() : "N/A");
            positionLabel.setText(currentUser.getPosition() != null ? currentUser.getPosition() : "N/A");
            statusLabel.setText(currentUser.getStatus() != null ? currentUser.getStatus() : "N/A");
            salaryLabel.setText(String.format("₱%.2f", currentUser.getBasicSalary()));
            phoneLabel.setText(currentUser.getPhoneNumber() != null ? currentUser.getPhoneNumber() : "Not provided");
            addressLabel.setText(currentUser.getAddress() != null ? currentUser.getAddress() : "Not provided");
            sssLabel.setText(currentUser.getSssNumber() != null ? currentUser.getSssNumber() : "Not provided");
            philhealthLabel.setText(currentUser.getPhilhealthNumber() != null ? currentUser.getPhilhealthNumber() : "Not provided");
            
            System.out.println("✅ Personal information loaded successfully");
        } catch (Exception e) {
            System.err.println("❌ Error loading personal info: " + e.getMessage());
            // Set error indicators
            nameLabel.setText("Error loading data");
            showErrorDialog("Personal Info Error", "Failed to load personal information: " + e.getMessage());
        }
    }

    // ENHANCED ATTENDANCE DATA LOADING WITH VALIDATION AND FILTERING
    private void loadAttendanceDataWithValidation() {
        try {
            attendanceTableModel.setRowCount(0);
            
            if (attendanceDAO == null) {
                throw new IllegalStateException("Attendance DAO is not initialized");
            }

            List<Attendance> attendanceList = attendanceDAO.getAttendanceByEmployeeId(currentUser.getEmployeeId());
            
            if (attendanceList == null) {
                attendanceList = new ArrayList<>();
            }

            double totalHours = 0;
            int totalDays = attendanceList.size();
            int lateDays = 0;
            int undertimeDays = 0;
            int fullDays = 0;

            for (Attendance att : attendanceList) {
                if (att == null) continue; // Skip null records
                
                double workHours = att.getWorkHours();
                totalHours += workHours;

                // Enhanced status determination
                String status = determineAttendanceStatus(att);
                if (att.isLate()) lateDays++;
                if (att.hasUndertime()) undertimeDays++;
                if (att.isFullDay()) fullDays++;

                // Safe data extraction with null checks
                Object[] row = {
                    att.getDate() != null ? att.getDate() : "N/A",
                    att.getLogIn() != null ? att.getLogIn().toString() : "No Log In",
                    att.getLogOut() != null ? att.getLogOut().toString() : "No Log Out",
                    String.format("%.2f hrs", workHours),
                    status,
                    att.isLate() ? String.format("%.0f", att.getLateMinutes()) : "0",
                    att.hasUndertime() ? String.format("%.0f", att.getUndertimeMinutes()) : "0"
                };
                attendanceTableModel.addRow(row);
            }

            // Enhanced summary calculations with validation
            updateAttendanceSummary(totalDays, totalHours, lateDays, undertimeDays, fullDays);
            
            System.out.println("✅ Attendance data loaded: " + totalDays + " records");
            
        } catch (Exception e) {
            System.err.println("❌ Error loading attendance data: " + e.getMessage());
            e.printStackTrace();
            showErrorDialog("Attendance Data Error", 
                "Failed to load attendance data: " + e.getMessage() + 
                "\n\nThis might be due to:\n" +
                "• Database connection issues\n" +
                "• Missing attendance records\n" +
                "• System configuration problems");
        }
    }

    private String determineAttendanceStatus(Attendance att) {
        if (att.getLogIn() == null) {
            return "❌ No Log In";
        }
        if (att.getLogOut() == null) {
            return "⚠️ No Log Out";
        }

        boolean isLate = att.isLate();
        boolean hasUndertime = att.hasUndertime();
        boolean isFullDay = att.isFullDay();

        if (isLate && hasUndertime) {
            return "🔴 Late & Undertime";
        } else if (isLate) {
            return "🟡 Late";
        } else if (hasUndertime) {
            return "🟠 Undertime";
        } else if (isFullDay) {
            return "🟢 Full Day";
        } else {
            return "🔵 Present";
        }
    }

    private void updateAttendanceSummary(int totalDays, double totalHours, int lateDays, int undertimeDays, int fullDays) {
        try {
            // Calculate working days in current month for attendance rate
            LocalDate now = LocalDate.now();
            LocalDate startOfMonth = now.withDayOfMonth(1);
            int workingDaysInMonth = calculateWorkingDays(startOfMonth, now);
            
            double attendanceRate = workingDaysInMonth > 0 ? 
                (double) totalDays / workingDaysInMonth * 100 : 0.0;
            
            double avgHours = totalDays > 0 ? totalHours / totalDays : 0.0;

            // Update labels with enhanced formatting and tooltips
            totalDaysLabel.setText(String.format("Total Days: %d", totalDays));
            totalDaysLabel.setToolTipText(String.format(
                "Breakdown: %d Full Days, %d Late, %d Undertime", 
                fullDays, lateDays, undertimeDays));
            
            averageHoursLabel.setText(String.format("Average Hours: %.2f", avgHours));
            averageHoursLabel.setToolTipText(String.format("Total Hours: %.2f", totalHours));
            
            attendanceRateLabel.setText(String.format("Attendance Rate: %.1f%%", attendanceRate));
            attendanceRateLabel.setToolTipText(String.format(
                "Days present (%d) out of working days (%d) this month", 
                totalDays, workingDaysInMonth));
            
        } catch (Exception e) {
            System.err.println("❌ Error updating attendance summary: " + e.getMessage());
            totalDaysLabel.setText("Total Days: Error");
            averageHoursLabel.setText("Average Hours: Error");
            attendanceRateLabel.setText("Attendance Rate: Error");
        }
    }

    private int calculateWorkingDays(LocalDate start, LocalDate end) {
        int workingDays = 0;
        LocalDate current = start;
        
        while (!current.isAfter(end)) {
            // Count Monday to Friday as working days
            if (current.getDayOfWeek().getValue() <= 5) {
                workingDays++;
            }
            current = current.plusDays(1);
        }
        
        return workingDays;
    }

    // ENHANCED PAYROLL DATA LOADING WITH VALIDATION
    private void loadPayrollDataWithValidation() {
        try {
            payrollTableModel.setRowCount(0);
            
            if (payrollCalculator == null) {
                throw new IllegalStateException("Payroll calculator is not initialized");
            }

            // Get selected period with validation
            int selectedMonth = monthComboBox.getSelectedIndex() + 1;
            int selectedYear = Integer.parseInt((String) Objects.requireNonNull(yearComboBox.getSelectedItem()));
            
            // Validate date range
            LocalDate periodStart = LocalDate.of(selectedYear, selectedMonth, 1);
            LocalDate periodEnd = periodStart.withDayOfMonth(periodStart.lengthOfMonth());
            
            // Don't calculate for future months
            if (periodStart.isAfter(LocalDate.now())) {
                Object[] row = {
                    periodStart.format(DateTimeFormatter.ofPattern("MMM yyyy")),
                    "N/A",
                    "Future Period",
                    "Future Period", 
                    "Future Period",
                    "N/A"
                };
                payrollTableModel.addRow(row);
                return;
            }

            // Calculate payroll with error handling
            Payroll payroll = payrollCalculator.calculatePayroll(
                currentUser.getEmployeeId(), periodStart, periodEnd);
            
            if (payroll == null) {
                throw new IllegalStateException("Payroll calculation returned null");
            }

            // Add row with enhanced formatting
            Object[] row = {
                periodStart.format(DateTimeFormatter.ofPattern("MMM yyyy")),
                String.valueOf(payroll.getDaysWorked()),
                String.format("₱%,.2f", payroll.getGrossPay()),
                String.format("₱%,.2f", payroll.getTotalDeductions()),
                String.format("₱%,.2f", payroll.getNetPay()),
                "📄 View Payslip"
            };
            payrollTableModel.addRow(row);
            
            System.out.println("✅ Payroll data loaded for " + periodStart.format(DateTimeFormatter.ofPattern("MMM yyyy")));
            
        } catch (Exception e) {
            System.err.println("❌ Error loading payroll data: " + e.getMessage());
            e.printStackTrace();
            
            // Add error row
            Object[] errorRow = {
                "Error",
                "Error",
                "Error calculating",
                "Error calculating",
                "Error calculating",
                "❌ View Error"
            };
            payrollTableModel.addRow(errorRow);
            
            showErrorDialog("Payroll Calculation Error", 
                "Failed to calculate payroll: " + e.getMessage() +
                "\n\nPossible causes:\n" +
                "• Missing attendance data\n" +
                "• Database connectivity issues\n" +
                "• Invalid employee data");
        }
    }

    // ENHANCED FILTERING FUNCTIONALITY
    private void filterAttendanceData() {
        if (attendanceTableSorter == null) return;
        
        String searchText = attendanceSearchField.getText().trim();
        String filterType = (String) attendanceFilterComboBox.getSelectedItem();
        
        if (searchText.isEmpty() && "All Records".equals(filterType)) {
            attendanceTableSorter.setRowFilter(null);
            return;
        }
        
        try {
            List<RowFilter<Object, Object>> filters = new ArrayList<>();
            
            // Add search filter
            if (!searchText.isEmpty()) {
                filters.add(RowFilter.regexFilter("(?i)" + searchText));
            }
            
            // Add type filter
            if (!"All Records".equals(filterType)) {
                switch (filterType) {
                    case "Present Only":
                        filters.add(RowFilter.regexFilter("Present|Full Day"));
                        break;
                    case "Late Only":
                        filters.add(RowFilter.regexFilter("Late"));
                        break;
                    case "Undertime Only":
                        filters.add(RowFilter.regexFilter("Undertime"));
                        break;
                    case "Full Day Only":
                        filters.add(RowFilter.regexFilter("Full Day"));
                        break;
                }
            }
            
            // Combine filters
            if (filters.size() == 1) {
                attendanceTableSorter.setRowFilter(filters.get(0));
            } else if (filters.size() > 1) {
                attendanceTableSorter.setRowFilter(RowFilter.andFilter(filters));
            }
            
            updateStatusBar("Filter applied - showing " + attendanceTable.getRowCount() + " records");
            
        } catch (Exception e) {
            System.err.println("❌ Error applying filter: " + e.getMessage());
            attendanceTableSorter.setRowFilter(null);
            updateStatusBar("Filter error - showing all records");
        }
    }

    // ENHANCED REFRESH METHODS WITH PROGRESS INDICATION
    private void refreshAttendanceDataWithProgress() {
        if (isLoadingData) {
            showInfoDialog("Please Wait", "Data is currently being loaded. Please wait for the current operation to complete.");
            return;
        }
        
        setLoadingState(true, "Refreshing attendance data...");
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                Thread.sleep(500); // Brief pause for UX
                loadAttendanceDataWithValidation();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    updateStatusBar("Attendance data refreshed successfully");
                    showSuccessMessage("Attendance data refreshed!");
                } catch (Exception e) {
                    updateStatusBar("Failed to refresh attendance data");
                    showErrorDialog("Refresh Error", "Failed to refresh attendance data: " + e.getMessage());
                } finally {
                    setLoadingState(false, "Ready");
                }
            }
        };
        
        worker.execute();
    }

    private void refreshPayrollDataWithProgress() {
        if (isLoadingData) {
            showInfoDialog("Please Wait", "Data is currently being loaded. Please wait for the current operation to complete.");
            return;
        }
        
        setLoadingState(true, "Refreshing payroll data...");
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                Thread.sleep(500); // Brief pause for UX
                loadPayrollDataWithValidation();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    updateStatusBar("Payroll data refreshed successfully");
                    showSuccessMessage("Payroll data refreshed!");
                } catch (Exception e) {
                    updateStatusBar("Failed to refresh payroll data");
                    showErrorDialog("Refresh Error", "Failed to refresh payroll data: " + e.getMessage());
                } finally {
                    setLoadingState(false, "Ready");
                }
            }
        };
        
        worker.execute();
    }

    private void loadPayrollDataWithProgress() {
        if (isLoadingData) return;
        
        setLoadingState(true, "Loading payroll data for selected period...");
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                Thread.sleep(300);
                loadPayrollDataWithValidation();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    updateStatusBar("Payroll data loaded");
                } catch (Exception e) {
                    updateStatusBar("Error loading payroll data");
                } finally {
                    setLoadingState(false, "Ready");
                }
            }
        };
        
        worker.execute();
    }

    private void calculatePayrollWithProgress() {
        if (isLoadingData) {
            showInfoDialog("Please Wait", "Please wait for current operation to complete.");
            return;
        }
        
        setLoadingState(true, "Calculating payroll...");
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                Thread.sleep(1000); // Simulate calculation time
                loadPayrollDataWithValidation();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    updateStatusBar("Payroll calculated successfully");
                    showSuccessMessage("Payroll calculated for selected period!");
                } catch (Exception e) {
                    updateStatusBar("Payroll calculation failed");
                    showErrorDialog("Calculation Error", "Failed to calculate payroll: " + e.getMessage());
                } finally {
                    setLoadingState(false, "Ready");
                }
            }
        };
        
        worker.execute();
    }

    // ENHANCED DIALOG METHODS
    private void showLeaveRequestDialog() {
        try {
            LeaveRequestDialog dialog = new LeaveRequestDialog(this, currentUser);
            dialog.setVisible(true);
            // Refresh attendance data after potential leave submission
            refreshAttendanceDataWithProgress();
        } catch (Exception e) {
            showErrorDialog("Leave Request Error", 
                "Error opening leave request dialog: " + e.getMessage());
        }
    }

    private void showLatestPayslip() {
        try {
            int selectedMonth = monthComboBox.getSelectedIndex() + 1;
            int selectedYear = Integer.parseInt((String) yearComboBox.getSelectedItem());

            LocalDate periodStart = LocalDate.of(selectedYear, selectedMonth, 1);
            LocalDate periodEnd = periodStart.withDayOfMonth(periodStart.lengthOfMonth());

            setLoadingState(true, "Generating payslip...");
            
            SwingWorker<Payroll, Void> worker = new SwingWorker<Payroll, Void>() {
                @Override
                protected Payroll doInBackground() throws Exception {
                    return payrollCalculator.calculatePayroll(currentUser.getEmployeeId(), periodStart, periodEnd);
                }

                @Override
                protected void done() {
                    try {
                        Payroll payroll = get();
                        PayrollDetailsDialog dialog = new PayrollDetailsDialog(
                            EnhancedEmployeeDashboard.this, currentUser, payroll);
                        dialog.setVisible(true);
                        updateStatusBar("Payslip displayed");
                    } catch (Exception e) {
                        showErrorDialog("Payslip Error", "Error generating payslip: " + e.getMessage());
                        updateStatusBar("Error generating payslip");
                    } finally {
                        setLoadingState(false, "Ready");
                    }
                }
            };
            
            worker.execute();
            
        } catch (Exception e) {
            setLoadingState(false, "Ready");
            showErrorDialog("Payslip Error", "Error generating payslip: " + e.getMessage());
        }
    }

    private void showUserProfile() {
        try {
            // Create a simple profile dialog
            JDialog profileDialog = new JDialog(this, "User Profile", true);
            profileDialog.setSize(400, 300);
            profileDialog.setLocationRelativeTo(this);
            
            JLabel messageLabel = new JLabel("<html><center>Profile management coming soon!<br><br>" +
                "Current user: " + currentUser.getFullName() + "<br>" +
                "Employee ID: " + currentUser.getEmployeeId() + "</center></html>");
            messageLabel.setHorizontalAlignment(JLabel.CENTER);
            
            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(e -> profileDialog.dispose());
            
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(messageLabel, BorderLayout.CENTER);
            panel.add(closeButton, BorderLayout.SOUTH);
            
            profileDialog.add(panel);
            profileDialog.setVisible(true);
            
        } catch (Exception e) {
            showErrorDialog("Profile Error", "Error opening profile: " + e.getMessage());
        }
    }

    private void showSettings() {
        showInfoDialog("Settings", "Settings panel coming soon!");
    }

    private void confirmAndExit() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to exit the Employee Dashboard?",
                "Confirm Exit", 
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            System.out.println("👋 User " + currentUser.getFullName() + " logged out");
            dispose();
            new LoginForm().setVisible(true);
        }
    }

    private void logout() {
        confirmAndExit();
    }

    // ENHANCED ERROR HANDLING AND USER FEEDBACK
    private void showErrorDialog(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private void showInfoDialog(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    private void showSuccessMessage(String message) {
        // Show success message in status bar with green color
        statusBarLabel.setText("✅ " + message);
        statusBarLabel.setForeground(new Color(0, 128, 0));
        
        // Reset color after 3 seconds
        Timer timer = new Timer(3000, e -> {
            statusBarLabel.setForeground(Color.BLACK);
            statusBarLabel.setText("Ready");
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void createErrorInterface(Exception error) {
        // Enhanced error interface (keeping your existing structure but with improvements)
        setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(220, 53, 69));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel headerLabel = new JLabel("⚠️ Employee Dashboard Error", JLabel.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);

        JPanel errorPanel = new JPanel(new BorderLayout());
        errorPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        errorPanel.setBackground(Color.WHITE);

        String errorMessage = "<html><center>" +
                "<h2>🔧 System Initialization Error</h2>" +
                "<p><b>The Employee Dashboard failed to initialize properly.</b></p>" +
                "<br>" +
                "<p><b>User:</b> " + (currentUser != null ? currentUser.getFullName() : "Unknown") + "</p>" +
                "<p><b>Error Type:</b> " + error.getClass().getSimpleName() + "</p>" +
                "<p><b>Error Details:</b> " + error.getMessage() + "</p>" +
                "<br>" +
                "<p><i>Please try the options below or contact IT support for assistance.</i></p>" +
                "</center></html>";

        JLabel messageLabel = new JLabel(errorMessage, JLabel.CENTER);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton retryButton = createEnhancedButton("🔄 Retry", "Retry dashboard initialization", 
                new Color(40, 167, 69), Color.WHITE);
        JButton logoutButton = createEnhancedButton("🚪 Logout", "Return to login screen", 
                new Color(108, 117, 125), Color.WHITE);
        JButton exitButton = createEnhancedButton("❌ Exit", "Exit application", 
                new Color(220, 53, 69), Color.WHITE);

        retryButton.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new EnhancedEmployeeDashboard(currentUser).setVisible(true));
        });

        logoutButton.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
        });

        exitButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to exit the application?",
                    "Confirm Exit", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                System.exit(1);
            }
        });

        buttonPanel.add(retryButton);
        buttonPanel.add(logoutButton);
        buttonPanel.add(exitButton);

        errorPanel.add(messageLabel, BorderLayout.CENTER);
        errorPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(headerPanel, BorderLayout.NORTH);
        add(errorPanel, BorderLayout.CENTER);
    }

    // CUSTOM RENDERER FOR ALTERNATING ROW COLORS
    private class AlternatingRowRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (!isSelected) {
                if (row % 2 == 0) {
                    c.setBackground(Color.WHITE);
                } else {
                    c.setBackground(new Color(248, 248, 255));
                }
            }
            
            return c;
        }
    }

    // MAIN METHOD FOR TESTING
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Set system look and feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeel());
            } catch (Exception e) {
                System.err.println("Could not set system look and feel");
            }
            
            // Create test user
            Employee testUser = new Employee();
            testUser.setEmployeeId(10001);
            testUser.setFirstName("Test");
            testUser.setLastName("Employee");
            testUser.setPosition("Software Developer");
            testUser.setStatus("Regular");
            testUser.setBasicSalary(50000.0);
            testUser.setRiceSubsidy(1500.0);
            testUser.setPhoneAllowance(1000.0);
            testUser.setClothingAllowance(800.0);

            new EnhancedEmployeeDashboard(testUser).setVisible(true);
        });
    }
}