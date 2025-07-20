package service;

import model.Employee;
import model.Payroll;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;

import javax.swing.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * JasperReports Service for generating professional PDF reports
 * Implements the MotorPH template design as required by mentor feedback
 */
public class JasperReportService {
    private static final Logger LOGGER = Logger.getLogger(JasperReportService.class.getName());
    
    // Report templates directory
    private static final String REPORTS_DIR = "src/reports/";
    private static final String OUTPUT_DIR = "reports/output/";
    
    public JasperReportService() {
        // Ensure output directory exists
        createOutputDirectory();
    }

    /**
     * Generate MotorPH Payslip PDF using JasperReports
     * @param employee Employee information
     * @param payroll Payroll calculation data
     * @return File path of generated PDF
     */
    public String generateMotorPHPayslip(Employee employee, Payroll payroll) throws JRException, IOException {
        LOGGER.info("Generating MotorPH payslip for employee: " + employee.getFullName());
        
        try {
            // Create payslip data bean
            PayslipDataBean payslipData = createPayslipDataBean(employee, payroll);
            
            // Prepare data source
            List<PayslipDataBean> dataList = Arrays.asList(payslipData);
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(dataList);
            
            // Load and compile report template
            JasperReport jasperReport = loadOrCompileReport("motorph_payslip.jrxml");
            
            // Parameters for the report
            Map<String, Object> parameters = createReportParameters(employee, payroll);
            
            // Fill the report
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
            
            // Generate output filename
            String outputFileName = generatePayslipFileName(employee, payroll);
            String outputPath = OUTPUT_DIR + outputFileName;
            
            // Export to PDF
            exportToPDF(jasperPrint, outputPath);
            
            LOGGER.info("✅ Payslip PDF generated successfully: " + outputPath);
            return outputPath;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "❌ Error generating payslip PDF", e);
            throw new JRException("Failed to generate payslip PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Generate Monthly Payroll Report PDF
     */
    public String generateMonthlyPayrollReport(List<Employee> employees, List<Payroll> payrolls, 
                                             String monthYear, String generatedBy) throws JRException, IOException {
        LOGGER.info("Generating monthly payroll report for: " + monthYear);
        
        try {
            // Create report data
            List<MonthlyPayrollDataBean> reportData = createMonthlyReportData(employees, payrolls);
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(reportData);
            
            // Load report template
            JasperReport jasperReport = loadOrCompileReport("motorph_monthly_payroll.jrxml");
            
            // Parameters
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("COMPANY_NAME", "MotorPH");
            parameters.put("COMPANY_ADDRESS", "7 Jupiter Avenue cor. F. Sandoval Jr., Bagong Nayon, Quezon City");
            parameters.put("REPORT_TITLE", "Monthly Payroll Report");
            parameters.put("REPORT_PERIOD", monthYear);
            parameters.put("GENERATED_BY", generatedBy);
            parameters.put("GENERATED_DATE", LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy HH:mm:ss")));
            parameters.put("COMPANY_LOGO", loadCompanyLogo());
            
            // Calculate totals
            double totalGross = payrolls.stream().mapToDouble(Payroll::getGrossPay).sum();
            double totalDeductions = payrolls.stream().mapToDouble(Payroll::getTotalDeductions).sum();
            double totalNet = payrolls.stream().mapToDouble(Payroll::getNetPay).sum();
            
            parameters.put("TOTAL_EMPLOYEES", employees.size());
            parameters.put("TOTAL_GROSS_PAY", totalGross);
            parameters.put("TOTAL_DEDUCTIONS", totalDeductions);
            parameters.put("TOTAL_NET_PAY", totalNet);
            
            // Fill and export
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
            
            String outputFileName = "MotorPH_Monthly_Payroll_" + 
                monthYear.replace(" ", "_") + "_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
            String outputPath = OUTPUT_DIR + outputFileName;
            
            exportToPDF(jasperPrint, outputPath);
            
            LOGGER.info("✅ Monthly payroll report generated: " + outputPath);
            return outputPath;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "❌ Error generating monthly payroll report", e);
            throw new JRException("Failed to generate monthly payroll report: " + e.getMessage(), e);
        }
    }

    /**
     * Generate Employee Directory Report PDF
     */
    public String generateEmployeeDirectoryReport(List<Employee> employees, String generatedBy) 
            throws JRException, IOException {
        LOGGER.info("Generating employee directory report");
        
        try {
            // Create directory data
            List<EmployeeDirectoryDataBean> directoryData = createDirectoryData(employees);
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(directoryData);
            
            // Load report template
            JasperReport jasperReport = loadOrCompileReport("motorph_employee_directory.jrxml");
            
            // Parameters
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("COMPANY_NAME", "MotorPH");
            parameters.put("COMPANY_ADDRESS", "7 Jupiter Avenue cor. F. Sandoval Jr., Bagong Nayon, Quezon City");
            parameters.put("REPORT_TITLE", "Employee Directory");
            parameters.put("GENERATED_BY", generatedBy);
            parameters.put("GENERATED_DATE", LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy HH:mm:ss")));
            parameters.put("TOTAL_EMPLOYEES", employees.size());
            parameters.put("COMPANY_LOGO", loadCompanyLogo());
            
            // Calculate statistics
            long regularEmployees = employees.stream().filter(e -> "Regular".equals(e.getStatus())).count();
            long probationaryEmployees = employees.stream().filter(e -> "Probationary".equals(e.getStatus())).count();
            double avgSalary = employees.stream().mapToDouble(Employee::getBasicSalary).average().orElse(0.0);
            
            parameters.put("REGULAR_EMPLOYEES", regularEmployees);
            parameters.put("PROBATIONARY_EMPLOYEES", probationaryEmployees);
            parameters.put("AVERAGE_SALARY", avgSalary);
            
            // Fill and export
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
            
            String outputFileName = "MotorPH_Employee_Directory_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
            String outputPath = OUTPUT_DIR + outputFileName;
            
            exportToPDF(jasperPrint, outputPath);
            
            LOGGER.info("✅ Employee directory report generated: " + outputPath);
            return outputPath;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "❌ Error generating employee directory report", e);
            throw new JRException("Failed to generate employee directory report: " + e.getMessage(), e);
        }
    }

    // HELPER METHODS
    
    private void createOutputDirectory() {
        File outputDir = new File(OUTPUT_DIR);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
            LOGGER.info("Created output directory: " + OUTPUT_DIR);
        }
    }
    
    private JasperReport loadOrCompileReport(String reportFileName) throws JRException {
        String jrxmlPath = REPORTS_DIR + reportFileName;
        String jasperPath = REPORTS_DIR + reportFileName.replace(".jrxml", ".jasper");
        
        File jrxmlFile = new File(jrxmlPath);
        File jasperFile = new File(jasperPath);
        
        // Check if compiled report exists and is newer than source
        if (jasperFile.exists() && jasperFile.lastModified() > jrxmlFile.lastModified()) {
            LOGGER.info("Loading compiled report: " + jasperPath);
            return (JasperReport) JRLoader.loadObject(jasperFile);
        } else {
            // Compile the report
            LOGGER.info("Compiling report: " + jrxmlPath);
            JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlPath);
            
            // Save compiled report
            JRSaver.saveObject(jasperReport, jasperPath);
            return jasperReport;
        }
    }
    
    private void exportToPDF(JasperPrint jasperPrint, String outputPath) throws JRException {
        JRPdfExporter exporter = new JRPdfExporter();
        
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputPath));
        
        SimplePdfExporterConfiguration configuration = new SimplePdfExporterConfiguration();
        configuration.setMetadataAuthor("MotorPH Payroll System");
        configuration.setMetadataCreator("MotorPH Payroll System");
        configuration.setMetadataSubject("Payroll Report");
        
        exporter.setConfiguration(configuration);
        exporter.exportReport();
        
        LOGGER.info("PDF exported successfully to: " + outputPath);
    }
    
    private PayslipDataBean createPayslipDataBean(Employee employee, Payroll payroll) {
        PayslipDataBean bean = new PayslipDataBean();
        
        // Employee information
        bean.setEmployeeId(employee.getEmployeeId());
        bean.setEmployeeName(employee.getFullName());
        bean.setPosition(employee.getPosition());
        bean.setStatus(employee.getStatus());
        
        // Payroll period
        bean.setPeriodStart(payroll.getStartDateAsLocalDate());
        bean.setPeriodEnd(payroll.getEndDateAsLocalDate());
        bean.setPayslipNumber(generatePayslipNumber(employee, payroll));
        
        // Earnings
        bean.setBasicPay(payroll.getGrossEarnings());
        bean.setOvertimePay(payroll.getOvertimePay());
        bean.setRiceSubsidy(payroll.getRiceSubsidy());
        bean.setPhoneAllowance(payroll.getPhoneAllowance());
        bean.setClothingAllowance(payroll.getClothingAllowance());
        bean.setGrossPay(payroll.getGrossPay());
        
        // Deductions
        bean.setSssDeduction(payroll.getSss());
        bean.setPhilhealthDeduction(payroll.getPhilhealth());
        bean.setPagibigDeduction(payroll.getPagibig());
        bean.setTaxDeduction(payroll.getTax());
        bean.setLateDeduction(payroll.getLateDeduction());
        bean.setUndertimeDeduction(payroll.getUndertimeDeduction());
        bean.setUnpaidLeaveDeduction(payroll.getUnpaidLeaveDeduction());
        bean.setTotalDeductions(payroll.getTotalDeductions());
        
        // Summary
        bean.setNetPay(payroll.getNetPay());
        bean.setDaysWorked(payroll.getDaysWorked());
        bean.setMonthlyRate(payroll.getMonthlyRate());
        
        return bean;
    }
    
    private Map<String, Object> createReportParameters(Employee employee, Payroll payroll) {
        Map<String, Object> parameters = new HashMap<>();
        
        // Company information
        parameters.put("COMPANY_NAME", "MotorPH");
        parameters.put("COMPANY_ADDRESS", "7 Jupiter Avenue cor. F. Sandoval Jr., Bagong Nayon, Quezon City");
        parameters.put("COMPANY_PHONE", "Phone: (028) 911-5071 / (028) 911-5072 / (028) 911-5073");
        parameters.put("COMPANY_EMAIL", "Email: corporate@motorph.com");
        parameters.put("COMPANY_LOGO", loadCompanyLogo());
        
        // Report information
        parameters.put("REPORT_TITLE", "EMPLOYEE PAYSLIP");
        parameters.put("GENERATED_DATE", LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy HH:mm:ss")));
        parameters.put("PAYSLIP_NUMBER", generatePayslipNumber(employee, payroll));
        
        return parameters;
    }
    
    private String generatePayslipNumber(Employee employee, Payroll payroll) {
        return String.format("%d-%s", 
            employee.getEmployeeId(),
            payroll.getEndDateAsLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    }
    
    private String generatePayslipFileName(Employee employee, Payroll payroll) {
        return String.format("MotorPH_Payslip_%s_%s_%s.pdf",
            employee.getLastName().replaceAll("\\s+", ""),
            employee.getFirstName().replaceAll("\\s+", ""),
            payroll.getStartDateAsLocalDate().format(DateTimeFormatter.ofPattern("yyyy_MM")));
    }
    
    private List<MonthlyPayrollDataBean> createMonthlyReportData(List<Employee> employees, List<Payroll> payrolls) {
        List<MonthlyPayrollDataBean> reportData = new ArrayList<>();
        
        // Create a map for quick payroll lookup
        Map<Integer, Payroll> payrollMap = new HashMap<>();
        for (Payroll payroll : payrolls) {
            payrollMap.put(payroll.getEmployeeId(), payroll);
        }
        
        for (Employee employee : employees) {
            MonthlyPayrollDataBean bean = new MonthlyPayrollDataBean();
            bean.setEmployeeId(employee.getEmployeeId());
            bean.setEmployeeName(employee.getFullName());
            bean.setPosition(employee.getPosition());
            bean.setStatus(employee.getStatus());
            
            Payroll payroll = payrollMap.get(employee.getEmployeeId());
            if (payroll != null) {
                bean.setDaysWorked(payroll.getDaysWorked());
                bean.setBasicPay(payroll.getGrossEarnings());
                bean.setAllowances(payroll.getRiceSubsidy() + payroll.getPhoneAllowance() + payroll.getClothingAllowance());
                bean.setOvertimePay(payroll.getOvertimePay());
                bean.setGrossPay(payroll.getGrossPay());
                bean.setDeductions(payroll.getTotalDeductions());
                bean.setNetPay(payroll.getNetPay());
            } else {
                // No payroll data available
                bean.setDaysWorked(0);
                bean.setBasicPay(0.0);
                bean.setAllowances(0.0);
                bean.setOvertimePay(0.0);
                bean.setGrossPay(0.0);
                bean.setDeductions(0.0);
                bean.setNetPay(0.0);
            }
            
            reportData.add(bean);
        }
        
        return reportData;
    }
    
    private List<EmployeeDirectoryDataBean> createDirectoryData(List<Employee> employees) {
        List<EmployeeDirectoryDataBean> directoryData = new ArrayList<>();
        
        for (Employee employee : employees) {
            EmployeeDirectoryDataBean bean = new EmployeeDirectoryDataBean();
            bean.setEmployeeId(employee.getEmployeeId());
            bean.setFirstName(employee.getFirstName());
            bean.setLastName(employee.getLastName());
            bean.setFullName(employee.getFullName());
            bean.setPosition(employee.getPosition());
            bean.setStatus(employee.getStatus());
            bean.setPhoneNumber(employee.getPhoneNumber());
            bean.setAddress(employee.getAddress());
            bean.setBasicSalary(employee.getBasicSalary());
            bean.setImmediateSupervisor(employee.getImmediateSupervisor());
            
            directoryData.add(bean);
        }
        
        return directoryData;
    }
    
    private InputStream loadCompanyLogo() {
        try {
            // Try to load company logo from resources
            InputStream logoStream = getClass().getClassLoader().getResourceAsStream("images/motorph_logo.png");
            if (logoStream != null) {
                return logoStream;
            }
            
            // If not found, create a simple placeholder
            return createPlaceholderLogo();
        } catch (Exception e) {
            LOGGER.warning("Could not load company logo: " + e.getMessage());
            return createPlaceholderLogo();
        }
    }
    
    private InputStream createPlaceholderLogo() {
        // Create a simple text-based logo placeholder
        try {
            String logoText = "MotorPH";
            return new ByteArrayInputStream(logoText.getBytes());
        } catch (Exception e) {
            return null;
        }
    }

    // DATA BEAN CLASSES FOR JASPER REPORTS
    
    /**
     * Data bean for individual payslip reports
     */
    public static class PayslipDataBean {
        private int employeeId;
        private String employeeName;
        private String position;
        private String status;
        private java.time.LocalDate periodStart;
        private java.time.LocalDate periodEnd;
        private String payslipNumber;
        private double basicPay;
        private double overtimePay;
        private double riceSubsidy;
        private double phoneAllowance;
        private double clothingAllowance;
        private double grossPay;
        private double sssDeduction;
        private double philhealthDeduction;
        private double pagibigDeduction;
        private double taxDeduction;
        private double lateDeduction;
        private double undertimeDeduction;
        private double unpaidLeaveDeduction;
        private double totalDeductions;
        private double netPay;
        private int daysWorked;
        private double monthlyRate;
        
        // Getters and setters
        public int getEmployeeId() { return employeeId; }
        public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }
        
        public String getEmployeeName() { return employeeName; }
        public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
        
        public String getPosition() { return position; }
        public void setPosition(String position) { this.position = position; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public java.time.LocalDate getPeriodStart() { return periodStart; }
        public void setPeriodStart(java.time.LocalDate periodStart) { this.periodStart = periodStart; }
        
        public java.time.LocalDate getPeriodEnd() { return periodEnd; }
        public void setPeriodEnd(java.time.LocalDate periodEnd) { this.periodEnd = periodEnd; }
        
        public String getPayslipNumber() { return payslipNumber; }
        public void setPayslipNumber(String payslipNumber) { this.payslipNumber = payslipNumber; }
        
        public double getBasicPay() { return basicPay; }
        public void setBasicPay(double basicPay) { this.basicPay = basicPay; }
        
        public double getOvertimePay() { return overtimePay; }
        public void setOvertimePay(double overtimePay) { this.overtimePay = overtimePay; }
        
        public double getRiceSubsidy() { return riceSubsidy; }
        public void setRiceSubsidy(double riceSubsidy) { this.riceSubsidy = riceSubsidy; }
        
        public double getPhoneAllowance() { return phoneAllowance; }
        public void setPhoneAllowance(double phoneAllowance) { this.phoneAllowance = phoneAllowance; }
        
        public double getClothingAllowance() { return clothingAllowance; }
        public void setClothingAllowance(double clothingAllowance) { this.clothingAllowance = clothingAllowance; }
        
        public double getGrossPay() { return grossPay; }
        public void setGrossPay(double grossPay) { this.grossPay = grossPay; }
        
        public double getSssDeduction() { return sssDeduction; }
        public void setSssDeduction(double sssDeduction) { this.sssDeduction = sssDeduction; }
        
        public double getPhilhealthDeduction() { return philhealthDeduction; }
        public void setPhilhealthDeduction(double philhealthDeduction) { this.philhealthDeduction = philhealthDeduction; }
        
        public double getPagibigDeduction() { return pagibigDeduction; }
        public void setPagibigDeduction(double pagibigDeduction) { this.pagibigDeduction = pagibigDeduction; }
        
        public double getTaxDeduction() { return taxDeduction; }
        public void setTaxDeduction(double taxDeduction) { this.taxDeduction = taxDeduction; }
        
        public double getLateDeduction() { return lateDeduction; }
        public void setLateDeduction(double lateDeduction) { this.lateDeduction = lateDeduction; }
        
        public double getUndertimeDeduction() { return undertimeDeduction; }
        public void setUndertimeDeduction(double undertimeDeduction) { this.undertimeDeduction = undertimeDeduction; }
        
        public double getUnpaidLeaveDeduction() { return unpaidLeaveDeduction; }
        public void setUnpaidLeaveDeduction(double unpaidLeaveDeduction) { this.unpaidLeaveDeduction = unpaidLeaveDeduction; }
        
        public double getTotalDeductions() { return totalDeductions; }
        public void setTotalDeductions(double totalDeductions) { this.totalDeductions = totalDeductions; }
        
        public double getNetPay() { return netPay; }
        public void setNetPay(double netPay) { this.netPay = netPay; }
        
        public int getDaysWorked() { return daysWorked; }
        public void setDaysWorked(int daysWorked) { this.daysWorked = daysWorked; }
        
        public double getMonthlyRate() { return monthlyRate; }
        public void setMonthlyRate(double monthlyRate) { this.monthlyRate = monthlyRate; }
    }
    
    /**
     * Data bean for monthly payroll reports
     */
    public static class MonthlyPayrollDataBean {
        private int employeeId;
        private String employeeName;
        private String position;
        private String status;
        private int daysWorked;
        private double basicPay;
        private double allowances;
        private double overtimePay;
        private double grossPay;
        private double deductions;
        private double netPay;
        
        // Getters and setters
        public int getEmployeeId() { return employeeId; }
        public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }
        
        public String getEmployeeName() { return employeeName; }
        public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
        
        public String getPosition() { return position; }
        public void setPosition(String position) { this.position = position; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public int getDaysWorked() { return daysWorked; }
        public void setDaysWorked(int daysWorked) { this.daysWorked = daysWorked; }
        
        public double getBasicPay() { return basicPay; }
        public void setBasicPay(double basicPay) { this.basicPay = basicPay; }
        
        public double getAllowances() { return allowances; }
        public void setAllowances(double allowances) { this.allowances = allowances; }
        
        public double getOvertimePay() { return overtimePay; }
        public void setOvertimePay(double overtimePay) { this.overtimePay = overtimePay; }
        
        public double getGrossPay() { return grossPay; }
        public void setGrossPay(double grossPay) { this.grossPay = grossPay; }
        
        public double getDeductions() { return deductions; }
        public void setDeductions(double deductions) { this.deductions = deductions; }
        
        public double getNetPay() { return netPay; }
        public void setNetPay(double netPay) { this.netPay = netPay; }
    }
    
    /**
     * Data bean for employee directory reports
     */
    public static class EmployeeDirectoryDataBean {
        private int employeeId;
        private String firstName;
        private String lastName;
        private String fullName;
        private String position;
        private String status;
        private String phoneNumber;
        private String address;
        private double basicSalary;
        private String immediateSupervisor;
        
        // Getters and setters
        public int getEmployeeId() { return employeeId; }
        public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }
        
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        
        public String getPosition() { return position; }
        public void setPosition(String position) { this.position = position; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        
        public double getBasicSalary() { return basicSalary; }
        public void setBasicSalary(double basicSalary) { this.basicSalary = basicSalary; }
        
        public String getImmediateSupervisor() { return immediateSupervisor; }
        public void setImmediateSupervisor(String immediateSupervisor) { this.immediateSupervisor = immediateSupervisor; }
    }

    /**
     * Open generated PDF file with system default application
     */
    public boolean openPDFFile(String filePath) {
        try {
            File pdfFile = new File(filePath);
            if (!pdfFile.exists()) {
                LOGGER.warning("PDF file does not exist: " + filePath);
                return false;
            }
            
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(pdfFile);
                return true;
            } else {
                LOGGER.warning("Desktop is not supported on this system");
                return false;
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error opening PDF file: " + filePath, e);
            return false;
        }
    }

    /**
     * Show file save dialog for PDF export
     */
    public String showSaveDialog(String defaultFileName) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save PDF Report");
        fileChooser.setSelectedFile(new File(defaultFileName));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF files", "pdf"));
        
        int userSelection = fileChooser.showSaveDialog(null);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".pdf")) {
                filePath += ".pdf";
            }
            return filePath;
        }
        
        return null;
    }
}