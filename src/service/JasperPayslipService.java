package service;

import dao.EmployeeDAO;
import dao.PayrollDAO;
import model.Employee;
import model.Payroll;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;

import java.io.*;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.sql.Date;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * JasperReports Service for MotorPH Payroll System using the official template
 */
public class JasperPayslipService {
    private static final Logger LOGGER = Logger.getLogger(JasperPayslipService.class.getName());

    // Template path - using the MotorPH template from resources
    private static final String PAYSLIP_TEMPLATE = "/motorph_payslip.jrxml";
    private static final String COMPANY_LOGO = "/images/motorph_logo.png";

    // Services
    private final EmployeeDAO employeeDAO;
    private final PayrollCalculator payrollCalculator;
    private final PayrollDAO payrollDAO;

    /**
     * Constructor with dependency injection
     */
    public JasperPayslipService() throws JasperReportException {
        this.employeeDAO = new EmployeeDAO();
        this.payrollCalculator = new PayrollCalculator();
        this.payrollDAO = new PayrollDAO();
        validateEnvironment();
        LOGGER.info("JasperPayslipService initialized successfully with MotorPH template");
    }

    /**
     * Generate payslip report using MotorPH template
     */
    public byte[] generatePayslipReport(Employee employee, Payroll payroll) throws JasperReportException {
        try {
            LOGGER.info(String.format("Generating MotorPH payslip for employee %d", employee.getEmployeeId()));

            // Create JasperReports data source using MotorPH template structure
            PayslipData payslipData = createMotorPHPayslipData(employee, payroll);
            List<PayslipData> dataList = Arrays.asList(payslipData);
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(dataList);

            // Prepare parameters for MotorPH template
            Map<String, Object> parameters = createMotorPHReportParameters();

            // Compile and fill report using MotorPH template
            JasperReport jasperReport = getCompiledMotorPHReport();
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            // Export to PDF
            return exportToPDF(jasperPrint);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating MotorPH payslip report", e);
            throw new JasperReportException("Failed to generate MotorPH payslip report: " + e.getMessage(), e);
        }
    }

    /**
     * Generate payslip and save to file using MotorPH template
     */
    public File generatePayslipToFile(Employee employee, Payroll payroll, String outputDir)
            throws JasperReportException {

        try {
            byte[] reportData = generatePayslipReport(employee, payroll);

            // Create MotorPH filename format
            String filename = String.format("MotorPH_Payslip_%s_%d_%s.pdf",
                    employee.getLastName().replaceAll("\\s+", ""),
                    employee.getEmployeeId(),
                    payroll.getPeriodEnd().toLocalDate().format(DateTimeFormatter.ofPattern("yyyy_MM")));

            // Ensure output directory exists
            File outputDirectory = new File(outputDir);
            if (!outputDirectory.exists()) {
                boolean created = outputDirectory.mkdirs();
                if (!created) {
                    throw new JasperReportException("Failed to create output directory: " + outputDir);
                }
            }

            // Write file
            File outputFile = new File(outputDirectory, filename);
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                fos.write(reportData);
            }

            LOGGER.info(String.format("MotorPH payslip saved to: %s", outputFile.getAbsolutePath()));
            return outputFile;

        } catch (IOException e) {
            throw new JasperReportException("Failed to save MotorPH payslip to file: " + e.getMessage(), e);
        }
    }

    /**
     * Create payslip data object matching the MotorPH JRXML template exactly
     */
    private PayslipData createMotorPHPayslipData(Employee employee, Payroll payroll) {
        PayslipData data = new PayslipData();

        // Employee information
        data.setEmployeeId(employee.getEmployeeId());
        data.setEmployeeName(employee.getFullName());
        data.setPosition(employee.getPosition() != null ? employee.getPosition() : "N/A");
        data.setDepartment(getDepartmentFromPosition(employee.getPosition()));

        // Payslip identification - MotorPH format
        data.setPayslipNo(String.format("MP-%d-%s",
                employee.getEmployeeId(),
                payroll.getPeriodEnd().toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM"))));

        // Period information
        data.setPeriodStart(payroll.getPeriodStart());
        data.setPeriodEnd(payroll.getPeriodEnd());

        // Salary information
        data.setMonthlyRate(BigDecimal.valueOf(payroll.getMonthlyRate()));
        data.setDailyRate(BigDecimal.valueOf(payroll.getMonthlyRate() / 22.0));
        data.setDaysWorked(payroll.getDaysWorked());

        // Benefits/Allowances
        data.setRiceSubsidy(BigDecimal.valueOf(payroll.getRiceSubsidy()));
        data.setPhoneAllowance(BigDecimal.valueOf(payroll.getPhoneAllowance()));
        data.setClothingAllowance(BigDecimal.valueOf(payroll.getClothingAllowance()));
        data.setTotalBenefits(BigDecimal.valueOf(
                payroll.getRiceSubsidy() + payroll.getPhoneAllowance() + payroll.getClothingAllowance()));

        // Deductions
        data.setSss(BigDecimal.valueOf(payroll.getSss()));
        data.setPhilhealth(BigDecimal.valueOf(payroll.getPhilhealth()));
        data.setPagibig(BigDecimal.valueOf(payroll.getPagibig()));
        data.setTax(BigDecimal.valueOf(payroll.getTax()));
        data.setTotalDeductions(BigDecimal.valueOf(payroll.getTotalDeductions()));

        // Pay amounts
        data.setGrossPay(BigDecimal.valueOf(payroll.getGrossPay()));
        data.setNetPay(BigDecimal.valueOf(payroll.getNetPay()));

        return data;
    }

    /**
     * Get department from position with MotorPH structure
     */
    private String getDepartmentFromPosition(String position) {
        if (position == null) return "General";

        String pos = position.toLowerCase();
        if (pos.contains("hr") || pos.contains("human resource")) return "Human Resources";
        if (pos.contains("accounting") || pos.contains("payroll")) return "Accounting";
        if (pos.contains("marketing") || pos.contains("sales")) return "Sales & Marketing";
        if (pos.contains("it") || pos.contains("operations")) return "IT Operations";
        if (pos.contains("ceo") || pos.contains("chief executive")) return "Executive Office";
        if (pos.contains("coo") || pos.contains("chief operating")) return "Operations";
        if (pos.contains("cfo") || pos.contains("chief finance")) return "Finance";
        if (pos.contains("cmo") || pos.contains("chief marketing")) return "Marketing";

        return "General";
    }

    /**
     * Create report parameters for MotorPH template
     */
    private Map<String, Object> createMotorPHReportParameters() {
        Map<String, Object> parameters = new HashMap<>();

        // MotorPH company information
        parameters.put("COMPANY_NAME", "MotorPH");
        parameters.put("COMPANY_ADDRESS", "7 Jupiter Avenue cor. F. Sandoval Jr., Bagong Nayon, Quezon City");
        parameters.put("COMPANY_PHONE", "Phone: (028) 911-5071 / (028) 911-5072 / (028) 911-5073");
        parameters.put("COMPANY_EMAIL", "Email: corporate@motorph.com");

        // Handle logo loading
        InputStream logoStream = getClass().getResourceAsStream(COMPANY_LOGO);
        if (logoStream != null) {
            parameters.put("COMPANY_LOGO", logoStream);
        } else {
            LOGGER.warning("MotorPH logo not found, report will generate without logo");
            parameters.put("COMPANY_LOGO", null);
        }

        parameters.put("REPORT_TITLE", "EMPLOYEE PAYSLIP");
        parameters.put("GENERATED_BY", "MotorPH Payroll System");
        parameters.put("GENERATION_DATE", new java.util.Date());

        return parameters;
    }

    /**
     * Validate environment and MotorPH template
     */
    private void validateEnvironment() throws JasperReportException {
        InputStream templateStream = getClass().getResourceAsStream(PAYSLIP_TEMPLATE);
        if (templateStream == null) {
            throw new JasperReportException(
                    "MotorPH template not found: " + PAYSLIP_TEMPLATE +
                            "\nPlease ensure motorph_payslip.jrxml is in the resources folder"
            );
        }
        try {
            templateStream.close();
        } catch (IOException e) {
            LOGGER.warning("Failed to close template stream: " + e.getMessage());
        }

        LOGGER.info("MotorPH JasperReports environment validation completed");
    }

    /**
     * Get compiled MotorPH JasperReport
     */
    private JasperReport getCompiledMotorPHReport() throws JRException {
        InputStream jrxmlStream = getClass().getResourceAsStream(PAYSLIP_TEMPLATE);
        if (jrxmlStream == null) {
            throw new JRException("MotorPH report template not found: " + PAYSLIP_TEMPLATE);
        }

        try {
            LOGGER.info("Compiling MotorPH JRXML template: " + PAYSLIP_TEMPLATE);
            return JasperCompileManager.compileReport(jrxmlStream);
        } finally {
            try {
                jrxmlStream.close();
            } catch (IOException e) {
                LOGGER.warning("Failed to close JRXML stream: " + e.getMessage());
            }
        }
    }

    /**
     * Export report to PDF
     */
    private byte[] exportToPDF(JasperPrint jasperPrint) throws JRException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        JRPdfExporter pdfExporter = new JRPdfExporter();
        pdfExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        pdfExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));

        pdfExporter.exportReport();

        return outputStream.toByteArray();
    }

    /**
     * Check if JasperReports is available
     */
    public static boolean isJasperReportsAvailable() {
        try {
            Class.forName("net.sf.jasperreports.engine.JasperReport");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Data Transfer Object for MotorPH JasperReports - matches JRXML template exactly
     */
    public static class PayslipData {
        private Integer employeeId;
        private String employeeName;
        private String position;
        private String department;
        private String payslipNo;
        private Date periodStart;
        private Date periodEnd;
        private BigDecimal monthlyRate;
        private BigDecimal dailyRate;
        private Integer daysWorked;
        private BigDecimal riceSubsidy;
        private BigDecimal phoneAllowance;
        private BigDecimal clothingAllowance;
        private BigDecimal totalBenefits;
        private BigDecimal sss;
        private BigDecimal philhealth;
        private BigDecimal pagibig;
        private BigDecimal tax;
        private BigDecimal totalDeductions;
        private BigDecimal grossPay;
        private BigDecimal netPay;

        // Getters and setters
        public Integer getEmployeeId() { return employeeId; }
        public void setEmployeeId(Integer employeeId) { this.employeeId = employeeId; }

        public String getEmployeeName() { return employeeName; }
        public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

        public String getPosition() { return position; }
        public void setPosition(String position) { this.position = position; }

        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }

        public String getPayslipNo() { return payslipNo; }
        public void setPayslipNo(String payslipNo) { this.payslipNo = payslipNo; }

        public Date getPeriodStart() { return periodStart; }
        public void setPeriodStart(Date periodStart) { this.periodStart = periodStart; }

        public Date getPeriodEnd() { return periodEnd; }
        public void setPeriodEnd(Date periodEnd) { this.periodEnd = periodEnd; }

        public BigDecimal getMonthlyRate() { return monthlyRate; }
        public void setMonthlyRate(BigDecimal monthlyRate) { this.monthlyRate = monthlyRate; }

        public BigDecimal getDailyRate() { return dailyRate; }
        public void setDailyRate(BigDecimal dailyRate) { this.dailyRate = dailyRate; }

        public Integer getDaysWorked() { return daysWorked; }
        public void setDaysWorked(Integer daysWorked) { this.daysWorked = daysWorked; }

        public BigDecimal getRiceSubsidy() { return riceSubsidy; }
        public void setRiceSubsidy(BigDecimal riceSubsidy) { this.riceSubsidy = riceSubsidy; }

        public BigDecimal getPhoneAllowance() { return phoneAllowance; }
        public void setPhoneAllowance(BigDecimal phoneAllowance) { this.phoneAllowance = phoneAllowance; }

        public BigDecimal getClothingAllowance() { return clothingAllowance; }
        public void setClothingAllowance(BigDecimal clothingAllowance) { this.clothingAllowance = clothingAllowance; }

        public BigDecimal getTotalBenefits() { return totalBenefits; }
        public void setTotalBenefits(BigDecimal totalBenefits) { this.totalBenefits = totalBenefits; }

        public BigDecimal getSss() { return sss; }
        public void setSss(BigDecimal sss) { this.sss = sss; }

        public BigDecimal getPhilhealth() { return philhealth; }
        public void setPhilhealth(BigDecimal philhealth) { this.philhealth = philhealth; }

        public BigDecimal getPagibig() { return pagibig; }
        public void setPagibig(BigDecimal pagibig) { this.pagibig = pagibig; }

        public BigDecimal getTax() { return tax; }
        public void setTax(BigDecimal tax) { this.tax = tax; }

        public BigDecimal getTotalDeductions() { return totalDeductions; }
        public void setTotalDeductions(BigDecimal totalDeductions) { this.totalDeductions = totalDeductions; }

        public BigDecimal getGrossPay() { return grossPay; }
        public void setGrossPay(BigDecimal grossPay) { this.grossPay = grossPay; }

        public BigDecimal getNetPay() { return netPay; }
        public void setNetPay(BigDecimal netPay) { this.netPay = netPay; }
    }

    /**
     * Custom exception for JasperReports operations
     */
    public static class JasperReportException extends Exception {
        public JasperReportException(String message) {
            super(message);
        }

        public JasperReportException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}