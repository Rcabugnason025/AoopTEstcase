/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package reports;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.*;
import net.sf.jasperreports.engine.type.*;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JasperViewer;

import model.*;
import dao.*;
import service.PayrollCalculator;
import util.DBConnection;

import java.io.*;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.awt.Color;

/**
 * Enhanced Report Generator using JasperReports for professional PDF generation
 * Follows the MotorPH template requirements
 */
public class JasperReportGenerator {
    
    private static final String REPORTS_PATH = "src/main/resources/reports/";
    private static final String OUTPUT_PATH = "reports/output/";
    
    // Company information
    private static final String COMPANY_NAME = "MotorPH";
    private static final String COMPANY_ADDRESS = "7 Jupiter Avenue cor. F. Sandoval Jr., Bagong Nayon, Quezon City";
    private static final String COMPANY_PHONE = "(028) 911-5071 / (028) 911-5072 / (028) 911-5073";
    private static final String COMPANY_EMAIL = "corporate@motorph.com";
    
    public JasperReportGenerator() {
        // Create output directory if it doesn't exist
        new File(OUTPUT_PATH).mkdirs();
    }
    
    /**
     * Generate employee payslip using JasperReports
     */
    public String generatePayslipReport(Employee employee, Payroll payroll) throws Exception {
        // Create payslip data
        PayslipData payslipData = new PayslipData(employee, payroll);
        
        // Create data source
        List<PayslipData> dataList = Arrays.asList(payslipData);
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(dataList);
        
        // Parameters for the report
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("COMPANY_NAME", COMPANY_NAME);
        parameters.put("COMPANY_ADDRESS", COMPANY_ADDRESS);
        parameters.put("COMPANY_PHONE", COMPANY_PHONE);
        parameters.put("COMPANY_EMAIL", COMPANY_EMAIL);
        parameters.put("REPORT_TITLE", "EMPLOYEE PAYSLIP");
        parameters.put("GENERATED_DATE", LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        
        // Compile and fill report
        JasperReport jasperReport = compilePayslipReport();
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
        
        // Export to PDF
        String fileName = String.format("Payslip_%s_%s.pdf", 
            employee.getLastName().replaceAll("\\s+", ""),
            payroll.getStartDateAsLocalDate().format(DateTimeFormatter.ofPattern("yyyy_MM")));
        String outputPath = OUTPUT_PATH + fileName;
        
        JasperExportManager.exportReportToPdfFile(jasperPrint, outputPath);
        
        return outputPath;
    }
    
    /**
     * Generate monthly payroll report for all employees
     */
    public String generateMonthlyPayrollReport(LocalDate month, String generatedBy) throws Exception {
        EmployeeDAO employeeDAO = new EmployeeDAO();
        PayrollCalculator payrollCalculator = new PayrollCalculator();
        
        List<Employee> employees = employeeDAO.getAllEmployees();
        List<PayrollSummaryData> payrollDataList = new ArrayList<>();
        
        LocalDate periodStart = month.withDayOfMonth(1);
        LocalDate periodEnd = month.withDayOfMonth(month.lengthOfMonth());
        
        // Calculate payroll for each employee
        for (Employee emp : employees) {
            try {
                Payroll payroll = payrollCalculator.calculatePayroll(emp.getEmployeeId(), periodStart, periodEnd);
                payrollDataList.add(new PayrollSummaryData(emp, payroll));
            } catch (Exception e) {
                System.err.println("Error calculating payroll for employee " + emp.getEmployeeId() + ": " + e.getMessage());
            }
        }
        
        // Create data source
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(payrollDataList);
        
        // Parameters
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("COMPANY_NAME", COMPANY_NAME);
        parameters.put("COMPANY_ADDRESS", COMPANY_ADDRESS);
        parameters.put("REPORT_TITLE", "MONTHLY PAYROLL REPORT");
        parameters.put("REPORT_PERIOD", month.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        parameters.put("GENERATED_BY", generatedBy);
        parameters.put("GENERATED_DATE", LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        
        // Calculate totals
        double totalGrossPay = payrollDataList.stream().mapToDouble(p -> p.getGrossPay()).sum();
        double totalDeductions = payrollDataList.stream().mapToDouble(p -> p.getTotalDeductions()).sum();
        double totalNetPay = payrollDataList.stream().mapToDouble(p -> p.getNetPay()).sum();
        
        parameters.put("TOTAL_GROSS_PAY", totalGrossPay);
        parameters.put("TOTAL_DEDUCTIONS", totalDeductions);
        parameters.put("TOTAL_NET_PAY", totalNetPay);
        parameters.put("TOTAL_EMPLOYEES", payrollDataList.size());
        
        // Compile and fill report
        JasperReport jasperReport = compileMonthlyPayrollReport();
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
        
        // Export to PDF
        String fileName = String.format("Monthly_Payroll_%s.pdf", 
            month.format(DateTimeFormatter.ofPattern("yyyy_MM")));
        String outputPath = OUTPUT_PATH + fileName;
        
        JasperExportManager.exportReportToPdfFile(jasperPrint, outputPath);
        
        return outputPath;
    }
    
    /**
     * Generate government contributions report
     */
    public String generateGovernmentContributionsReport(LocalDate month, String generatedBy) throws Exception {
        EmployeeDAO employeeDAO = new EmployeeDAO();
        PayrollCalculator payrollCalculator = new PayrollCalculator();
        
        List<Employee> employees = employeeDAO.getAllEmployees();
        List<GovernmentContributionData> contributionDataList = new ArrayList<>();
        
        LocalDate periodStart = month.withDayOfMonth(1);
        LocalDate periodEnd = month.withDayOfMonth(month.lengthOfMonth());
        
        // Calculate contributions for each employee
        for (Employee emp : employees) {
            try {
                Payroll payroll = payrollCalculator.calculatePayroll(emp.getEmployeeId(), periodStart, periodEnd);
                contributionDataList.add(new GovernmentContributionData(emp, payroll));
            } catch (Exception e) {
                System.err.println("Error calculating contributions for employee " + emp.getEmployeeId() + ": " + e.getMessage());
            }
        }
        
        // Create data source
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(contributionDataList);
        
        // Parameters
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("COMPANY_NAME", COMPANY_NAME);
        parameters.put("COMPANY_ADDRESS", COMPANY_ADDRESS);
        parameters.put("REPORT_TITLE", "GOVERNMENT CONTRIBUTIONS REPORT");
        parameters.put("REPORT_PERIOD", month.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        parameters.put("GENERATED_BY", generatedBy);
        parameters.put("GENERATED_DATE", LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        
        // Calculate totals
        double totalSSS = contributionDataList.stream().mapToDouble(c -> c.getSssContribution()).sum();
        double totalPhilHealth = contributionDataList.stream().mapToDouble(c -> c.getPhilhealthContribution()).sum();
        double totalPagIBIG = contributionDataList.stream().mapToDouble(c -> c.getPagibigContribution()).sum();
        double totalTax = contributionDataList.stream().mapToDouble(c -> c.getWithholdingTax()).sum();
        
        parameters.put("TOTAL_SSS", totalSSS);
        parameters.put("TOTAL_PHILHEALTH", totalPhilHealth);
        parameters.put("TOTAL_PAGIBIG", totalPagIBIG);
        parameters.put("TOTAL_TAX", totalTax);
        parameters.put("TOTAL_CONTRIBUTIONS", totalSSS + totalPhilHealth + totalPagIBIG + totalTax);
        
        // Compile and fill report
        JasperReport jasperReport = compileGovernmentContributionsReport();
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
        
        // Export to PDF
        String fileName = String.format("Government_Contributions_%s.pdf", 
            month.format(DateTimeFormatter.ofPattern("yyyy_MM")));
        String outputPath = OUTPUT_PATH + fileName;
        
        JasperExportManager.exportReportToPdfFile(jasperPrint, outputPath);
        
        return outputPath;
    }
    
    /**
     * Programmatically create payslip report design
     */
    private JasperReport compilePayslipReport() throws Exception {
        JasperDesign jasperDesign = new JasperDesign();
        jasperDesign.setName("PayslipReport");
        jasperDesign.setPageWidth(595); // A4 width
        jasperDesign.setPageHeight(842); // A4 height
        jasperDesign.setLeftMargin(40);
        jasperDesign.setRightMargin(40);
        jasperDesign.setTopMargin(50);
        jasperDesign.setBottomMargin(50);
        jasperDesign.setColumnWidth(515);
        
        // Add parameters
        addParameter(jasperDesign, "COMPANY_NAME", String.class);
        addParameter(jasperDesign, "COMPANY_ADDRESS", String.class);
        addParameter(jasperDesign, "COMPANY_PHONE", String.class);
        addParameter(jasperDesign, "COMPANY_EMAIL", String.class);
        addParameter(jasperDesign, "REPORT_TITLE", String.class);
        addParameter(jasperDesign, "GENERATED_DATE", String.class);
        
        // Add fields from PayslipData
        addField(jasperDesign, "employeeId", Integer.class);
        addField(jasperDesign, "fullName", String.class);
        addField(jasperDesign, "position", String.class);
        addField(jasperDesign, "department", String.class);
        addField(jasperDesign, "payPeriod", String.class);
        addField(jasperDesign, "daysWorked", Integer.class);
        addField(jasperDesign, "basicPay", Double.class);
        addField(jasperDesign, "totalAllowances", Double.class);
        addField(jasperDesign, "overtimePay", Double.class);
        addField(jasperDesign, "grossPay", Double.class);
        addField(jasperDesign, "sssDeduction", Double.class);
        addField(jasperDesign, "philhealthDeduction", Double.class);
        addField(jasperDesign, "pagibigDeduction", Double.class);
        addField(jasperDesign, "taxDeduction", Double.class);
        addField(jasperDesign, "totalDeductions", Double.class);
        addField(jasperDesign, "netPay", Double.class);
        
        // Create bands
        createPayslipHeader(jasperDesign);
        createPayslipDetail(jasperDesign);
        createPayslipFooter(jasperDesign);
        
        return JasperCompileManager.compileReport(jasperDesign);
    }
    
    /**
     * Create payslip header band
     */
    private void createPayslipHeader(JasperDesign jasperDesign) throws Exception {
        JRDesignBand headerBand = new JRDesignBand();
        headerBand.setHeight(200);
        
        // Company name
        JRDesignStaticText companyName = new JRDesignStaticText();
        companyName.setX(0);
        companyName.setY(0);
        companyName.setWidth(515);
        companyName.setHeight(30);
        companyName.setText("$P{COMPANY_NAME}");
        companyName.setFontSize(20f);
        companyName.setBold(true);
        companyName.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        headerBand.addElement(companyName);
        
        // Company address
        JRDesignStaticText companyAddress = new JRDesignStaticText();
        companyAddress.setX(0);
        companyAddress.setY(35);
        companyAddress.setWidth(515);
        companyAddress.setHeight(15);
        companyAddress.setText("$P{COMPANY_ADDRESS}");
        companyAddress.setFontSize(10f);
        companyAddress.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        headerBand.addElement(companyAddress);
        
        // Report title
        JRDesignStaticText reportTitle = new JRDesignStaticText();
        reportTitle.setX(0);
        reportTitle.setY(80);
        reportTitle.setWidth(515);
        reportTitle.setHeight(25);
        reportTitle.setText("$P{REPORT_TITLE}");
        reportTitle.setFontSize(16f);
        reportTitle.setBold(true);
        reportTitle.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        headerBand.addElement(reportTitle);
        
        // Employee information section
        addEmployeeInfoSection(headerBand, 120);
        
        jasperDesign.setTitle(headerBand);
    }
    
    /**
     * Add employee information section to header
     */
    private void addEmployeeInfoSection(JRDesignBand band, int startY) throws Exception {
        // Employee ID
        addLabelField(band, "Employee ID:", "$F{employeeId}", 0, startY, 250, 15);
        
        // Employee Name
        addLabelField(band, "Employee Name:", "$F{fullName}", 0, startY + 20, 250, 15);
        
        // Position
        addLabelField(band, "Position:", "$F{position}", 0, startY + 40, 250, 15);
        
        // Pay Period
        addLabelField(band, "Pay Period:", "$F{payPeriod}", 265, startY, 250, 15);
        
        // Days Worked
        addLabelField(band, "Days Worked:", "$F{daysWorked}", 265, startY + 20, 250, 15);
    }
    
    /**
     * Create payslip detail band with earnings and deductions
     */
    private void createPayslipDetail(JasperDesign jasperDesign) throws Exception {
        JRDesignBand detailBand = new JRDesignBand();
        detailBand.setHeight(300);
        
        // Earnings section
        createEarningsSection(detailBand, 0);
        
        // Deductions section
        createDeductionsSection(detailBand, 150);
        
        // Summary section
        createSummarySection(detailBand, 250);
        
        jasperDesign.setDetail(detailBand);
    }
    
    /**
     * Create earnings section
     */
    private void createEarningsSection(JRDesignBand band, int startY) throws Exception {
        // Section header
        JRDesignStaticText earningsHeader = new JRDesignStaticText();
        earningsHeader.setX(0);
        earningsHeader.setY(startY);
        earningsHeader.setWidth(200);
        earningsHeader.setHeight(20);
        earningsHeader.setText("EARNINGS");
        earningsHeader.setFontSize(12f);
        earningsHeader.setBold(true);
        earningsHeader.setBackcolor(Color.LIGHT_GRAY);
        band.addElement(earningsHeader);
        
        // Earnings items
        addAmountField(band, "Basic Pay:", "$F{basicPay}", 0, startY + 25, 200, 15);
        addAmountField(band, "Allowances:", "$F{totalAllowances}", 0, startY + 45, 200, 15);
        addAmountField(band, "Overtime Pay:", "$F{overtimePay}", 0, startY + 65, 200, 15);
        
        // Gross pay total
        JRDesignLine line1 = new JRDesignLine();
        line1.setX(120);
        line1.setY(startY + 85);
        line1.setWidth(80);
        line1.setHeight(1);
        band.addElement(line1);
        
        addAmountField(band, "GROSS PAY:", "$F{grossPay}", 0, startY + 90, 200, 15, true);
    }
    
    /**
     * Create deductions section
     */
    private void createDeductionsSection(JRDesignBand band, int startY) throws Exception {
        // Section header
        JRDesignStaticText deductionsHeader = new JRDesignStaticText();
        deductionsHeader.setX(265);
        deductionsHeader.setY(startY);
        deductionsHeader.setWidth(200);
        deductionsHeader.setHeight(20);
        deductionsHeader.setText("DEDUCTIONS");
        deductionsHeader.setFontSize(12f);
        deductionsHeader.setBold(true);
        deductionsHeader.setBackcolor(Color.LIGHT_GRAY);
        band.addElement(deductionsHeader);
        
        // Deduction items
        addAmountField(band, "SSS:", "$F{sssDeduction}", 265, startY + 25, 200, 15);
        addAmountField(band, "PhilHealth:", "$F{philhealthDeduction}", 265, startY + 45, 200, 15);
        addAmountField(band, "Pag-IBIG:", "$F{pagibigDeduction}", 265, startY + 65, 200, 15);
        addAmountField(band, "Withholding Tax:", "$F{taxDeduction}", 265, startY + 85, 200, 15);
        
        // Total deductions
        JRDesignLine line2 = new JRDesignLine();
        line2.setX(385);
        line2.setY(startY + 105);
        line2.setWidth(80);
        line2.setHeight(1);
        band.addElement(line2);
        
        addAmountField(band, "TOTAL DEDUCTIONS:", "$F{totalDeductions}", 265, startY + 110, 200, 15, true);
    }
    
    /**
     * Create summary section
     */
    private void createSummarySection(JRDesignBand band, int startY) throws Exception {
        // Net pay calculation
        JRDesignRectangle rect = new JRDesignRectangle();
        rect.setX(150);
        rect.setY(startY);
        rect.setWidth(215);
        rect.setHeight(40);
        rect.setPen(LineStyleEnum.SOLID);
        band.addElement(rect);
        
        addAmountField(band, "NET PAY:", "$F{netPay}", 155, startY + 12, 205, 20, true, 14f);
    }
    
    /**
     * Create payslip footer
     */
    private void createPayslipFooter(JasperDesign jasperDesign) throws Exception {
        JRDesignBand footerBand = new JRDesignBand();
        footerBand.setHeight(60);
        
        // Footer text
        JRDesignStaticText footerText = new JRDesignStaticText();
        footerText.setX(0);
        footerText.setY(20);
        footerText.setWidth(515);
        footerText.setHeight(15);
        footerText.setText("This payslip is computer-generated and does not require signature.");
        footerText.setFontSize(9f);
        footerText.setItalic(true);
        footerText.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        footerBand.addElement(footerText);
        
        // Generated date
        JRDesignTextField generatedDate = new JRDesignTextField();
        generatedDate.setX(0);
        generatedDate.setY(40);
        generatedDate.setWidth(515);
        generatedDate.setHeight(15);
        generatedDate.setExpression(new JRDesignExpression("\"Generated on: \" + $P{GENERATED_DATE}"));
        generatedDate.setFontSize(8f);
        generatedDate.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        footerBand.addElement(generatedDate);
        
        jasperDesign.setPageFooter(footerBand);
    }
    
    /**
     * Compile monthly payroll report
     */
    private JasperReport compileMonthlyPayrollReport() throws Exception {
        JasperDesign jasperDesign = new JasperDesign();
        jasperDesign.setName("MonthlyPayrollReport");
        jasperDesign.setPageWidth(842); // A4 landscape
        jasperDesign.setPageHeight(595);
        jasperDesign.setOrientation(OrientationEnum.LANDSCAPE);
        jasperDesign.setLeftMargin(40);
        jasperDesign.setRightMargin(40);
        jasperDesign.setTopMargin(50);
        jasperDesign.setBottomMargin(50);
        jasperDesign.setColumnWidth(762);
        
        // Add parameters
        addParameter(jasperDesign, "COMPANY_NAME", String.class);
        addParameter(jasperDesign, "REPORT_TITLE", String.class);
        addParameter(jasperDesign, "REPORT_PERIOD", String.class);
        addParameter(jasperDesign, "GENERATED_BY", String.class);
        addParameter(jasperDesign, "GENERATED_DATE", String.class);
        addParameter(jasperDesign, "TOTAL_EMPLOYEES", Integer.class);
        addParameter(jasperDesign, "TOTAL_GROSS_PAY", Double.class);
        addParameter(jasperDesign, "TOTAL_DEDUCTIONS", Double.class);
        addParameter(jasperDesign, "TOTAL_NET_PAY", Double.class);
        
        // Add fields from PayrollSummaryData
        addField(jasperDesign, "employeeId", Integer.class);
        addField(jasperDesign, "fullName", String.class);
        addField(jasperDesign, "position", String.class);
        addField(jasperDesign, "basicPay", Double.class);
        addField(jasperDesign, "totalAllowances", Double.class);
        addField(jasperDesign, "grossPay", Double.class);
        addField(jasperDesign, "totalDeductions", Double.class);
        addField(jasperDesign, "netPay", Double.class);
        
        // Create bands
        createMonthlyPayrollHeader(jasperDesign);
        createMonthlyPayrollColumnHeader(jasperDesign);
        createMonthlyPayrollDetail(jasperDesign);
        createMonthlyPayrollSummary(jasperDesign);
        
        return JasperCompileManager.compileReport(jasperDesign);
    }
    
    /**
     * Create monthly payroll report header
     */
    private void createMonthlyPayrollHeader(JasperDesign jasperDesign) throws Exception {
        JRDesignBand headerBand = new JRDesignBand();
        headerBand.setHeight(120);
        
        // Company name
        JRDesignTextField companyName = new JRDesignTextField();
        companyName.setX(0);
        companyName.setY(0);
        companyName.setWidth(762);
        companyName.setHeight(25);
        companyName.setExpression(new JRDesignExpression("$P{COMPANY_NAME}"));
        companyName.setFontSize(18f);
        companyName.setBold(true);
        companyName.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        headerBand.addElement(companyName);
        
        // Report title and period
        JRDesignTextField reportTitle = new JRDesignTextField();
        reportTitle.setX(0);
        reportTitle.setY(30);
        reportTitle.setWidth(762);
        reportTitle.setHeight(20);
        reportTitle.setExpression(new JRDesignExpression("$P{REPORT_TITLE} + \" - \" + $P{REPORT_PERIOD}"));
        reportTitle.setFontSize(14f);
        reportTitle.setBold(true);
        reportTitle.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        headerBand.addElement(reportTitle);
        
        // Generated info
        JRDesignTextField generatedInfo = new JRDesignTextField();
        generatedInfo.setX(0);
        generatedInfo.setY(60);
        generatedInfo.setWidth(762);
        generatedInfo.setHeight(15);
        generatedInfo.setExpression(new JRDesignExpression("\"Generated by: \" + $P{GENERATED_BY} + \" on \" + $P{GENERATED_DATE}"));
        generatedInfo.setFontSize(10f);
        generatedInfo.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        headerBand.addElement(generatedInfo);
        
        jasperDesign.setTitle(headerBand);
    }
    
    /**
     * Create column headers for monthly payroll
     */
    private void createMonthlyPayrollColumnHeader(JasperDesign jasperDesign) throws Exception {
        JRDesignBand columnHeaderBand = new JRDesignBand();
        columnHeaderBand.setHeight(25);
        
        String[] headers = {"ID", "Employee Name", "Position", "Basic Pay", "Allowances", "Gross Pay", "Deductions", "Net Pay"};
        int[] widths = {50, 150, 120, 80, 80, 90, 90, 102};
        int x = 0;
        
        for (int i = 0; i < headers.length; i++) {
            JRDesignStaticText header = new JRDesignStaticText();
            header.setX(x);
            header.setY(0);
            header.setWidth(widths[i]);
            header.setHeight(25);
            header.setText(headers[i]);
            header.setFontSize(10f);
            header.setBold(true);
            header.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
            header.setBackcolor(Color.LIGHT_GRAY);
            header.setMode(ModeEnum.OPAQUE);
            columnHeaderBand.addElement(header);
            x += widths[i];
        }
        
        jasperDesign.setColumnHeader(columnHeaderBand);
    }
    
    /**
     * Create detail band for monthly payroll
     */
    private void createMonthlyPayrollDetail(JasperDesign jasperDesign) throws Exception {
        JRDesignBand detailBand = new JRDesignBand();
        detailBand.setHeight(20);
        
        String[] fields = {"employeeId", "fullName", "position", "basicPay", "totalAllowances", "grossPay", "totalDeductions", "netPay"};
        int[] widths = {50, 150, 120, 80, 80, 90, 90, 102};
        boolean[] isNumeric = {true, false, false, true, true, true, true, true};
        int x = 0;
        
        for (int i = 0; i < fields.length; i++) {
            JRDesignTextField field = new JRDesignTextField();
            field.setX(x);
            field.setY(0);
            field.setWidth(widths[i]);
            field.setHeight(20);
            
            if (isNumeric[i] && !fields[i].equals("employeeId")) {
                field.setExpression(new JRDesignExpression("\"₱\" + new java.text.DecimalFormat(\"#,##0.00\").format($F{" + fields[i] + "})"));
                field.setHorizontalTextAlign(HorizontalTextAlignEnum.RIGHT);
            } else {
                field.setExpression(new JRDesignExpression("$F{" + fields[i] + "}"));
                field.setHorizontalTextAlign(isNumeric[i] ? HorizontalTextAlignEnum.CENTER : HorizontalTextAlignEnum.LEFT);
            }
            
            field.setFontSize(9f);
            detailBand.addElement(field);
            x += widths[i];
        }
        
        jasperDesign.setDetail(detailBand);
    }
    
    /**
     * Create summary band for monthly payroll
     */
    private void createMonthlyPayrollSummary(JasperDesign jasperDesign) throws Exception {
        JRDesignBand summaryBand = new JRDesignBand();
        summaryBand.setHeight(80);
        
        // Summary title
        JRDesignStaticText summaryTitle = new JRDesignStaticText();
        summaryTitle.setX(0);
        summaryTitle.setY(10);
        summaryTitle.setWidth(762);
        summaryTitle.setHeight(20);
        summaryTitle.setText("PAYROLL SUMMARY");
        summaryTitle.setFontSize(12f);
        summaryTitle.setBold(true);
        summaryTitle.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        summaryBand.addElement(summaryTitle);
        
        // Summary details
        int y = 35;
        addSummaryField(summaryBand, "Total Employees:", "$P{TOTAL_EMPLOYEES}", 200, y, 150, 15);
        addSummaryField(summaryBand, "Total Gross Pay:", "\"₱\" + new java.text.DecimalFormat(\"#,##0.00\").format($P{TOTAL_GROSS_PAY})", 400, y, 150, 15);
        
        y += 20;
        addSummaryField(summaryBand, "Total Deductions:", "\"₱\" + new java.text.DecimalFormat(\"#,##0.00\").format($P{TOTAL_DEDUCTIONS})", 200, y, 150, 15);
        addSummaryField(summaryBand, "Total Net Pay:", "\"₱\" + new java.text.DecimalFormat(\"#,##0.00\").format($P{TOTAL_NET_PAY})", 400, y, 150, 15);
        
        jasperDesign.setSummary(summaryBand);
    }
    
    /**
     * Compile government contributions report
     */
    private JasperReport compileGovernmentContributionsReport() throws Exception {
        // Similar structure to monthly payroll but with government contribution fields
        JasperDesign jasperDesign = new JasperDesign();
        jasperDesign.setName("GovernmentContributionsReport");
        jasperDesign.setPageWidth(842); // A4 landscape
        jasperDesign.setPageHeight(595);
        jasperDesign.setOrientation(OrientationEnum.LANDSCAPE);
        jasperDesign.setLeftMargin(40);
        jasperDesign.setRightMargin(40);
        jasperDesign.setTopMargin(50);
        jasperDesign.setBottomMargin(50);
        jasperDesign.setColumnWidth(762);
        
        // Add parameters and fields for government contributions
        addParameter(jasperDesign, "COMPANY_NAME", String.class);
        addParameter(jasperDesign, "REPORT_TITLE", String.class);
        addParameter(jasperDesign, "REPORT_PERIOD", String.class);
        addParameter(jasperDesign, "TOTAL_SSS", Double.class);
        addParameter(jasperDesign, "TOTAL_PHILHEALTH", Double.class);
        addParameter(jasperDesign, "TOTAL_PAGIBIG", Double.class);
        addParameter(jasperDesign, "TOTAL_TAX", Double.class);
        
        addField(jasperDesign, "employeeId", Integer.class);
        addField(jasperDesign, "fullName", String.class);
        addField(jasperDesign, "sssContribution", Double.class);
        addField(jasperDesign, "philhealthContribution", Double.class);
        addField(jasperDesign, "pagibigContribution", Double.class);
        addField(jasperDesign, "withholdingTax", Double.class);
        addField(jasperDesign, "totalContributions", Double.class);
        
        // Create bands similar to monthly payroll but with contribution-specific fields
        createGovernmentContributionsHeader(jasperDesign);
        createGovernmentContributionsDetail(jasperDesign);
        createGovernmentContributionsSummary(jasperDesign);
        
        return JasperCompileManager.compileReport(jasperDesign);
    }
    
    // Helper methods for creating report elements
    
    private void addParameter(JasperDesign jasperDesign, String name, Class<?> type) throws Exception {
        JRDesignParameter parameter = new JRDesignParameter();
        parameter.setName(name);
        parameter.setValueClass(type);
        jasperDesign.addParameter(parameter);
    }
    
    private void addField(JasperDesign jasperDesign, String name, Class<?> type) throws Exception {
        JRDesignField field = new JRDesignField();
        field.setName(name);
        field.setValueClass(type);
        jasperDesign.addField(field);
    }
    
    private void addLabelField(JRDesignBand band, String label, String fieldExpression, 
                              int x, int y, int width, int height) throws Exception {
        // Label
        JRDesignStaticText labelText = new JRDesignStaticText();
        labelText.setX(x);
        labelText.setY(y);
        labelText.setWidth(120);
        labelText.setHeight(height);
        labelText.setText(label);
        labelText.setFontSize(10f);
        labelText.setBold(true);
        band.addElement(labelText);
        
        // Field
        JRDesignTextField fieldText = new JRDesignTextField();
        fieldText.setX(x + 125);
        fieldText.setY(y);
        fieldText.setWidth(width - 125);
        fieldText.setHeight(height);
        fieldText.setExpression(new JRDesignExpression(fieldExpression));
        fieldText.setFontSize(10f);
        band.addElement(fieldText);
    }
    
    private void addAmountField(JRDesignBand band, String label, String fieldExpression, 
                               int x, int y, int width, int height) throws Exception {
        addAmountField(band, label, fieldExpression, x, y, width, height, false, 10f);
    }
    
    private void addAmountField(JRDesignBand band, String label, String fieldExpression, 
                               int x, int y, int width, int height, boolean bold) throws Exception {
        addAmountField(band, label, fieldExpression, x, y, width, height, bold, 10f);
    }
    
    private void addAmountField(JRDesignBand band, String label, String fieldExpression, 
                               int x, int y, int width, int height, boolean bold, float fontSize) throws Exception {
        // Label
        JRDesignStaticText labelText = new JRDesignStaticText();
        labelText.setX(x);
        labelText.setY(y);
        labelText.setWidth(120);
        labelText.setHeight(height);
        labelText.setText(label);
        labelText.setFontSize(fontSize);
        labelText.setBold(bold);
        band.addElement(labelText);
        
        // Amount field with currency formatting
        JRDesignTextField amountText = new JRDesignTextField();
        amountText.setX(x + 125);
        amountText.setY(y);
        amountText.setWidth(width - 125);
        amountText.setHeight(height);
        amountText.setExpression(new JRDesignExpression("\"₱\" + new java.text.DecimalFormat(\"#,##0.00\").format(" + fieldExpression + ")"));
        amountText.setFontSize(fontSize);
        amountText.setBold(bold);
        amountText.setHorizontalTextAlign(HorizontalTextAlignEnum.RIGHT);
        band.addElement(amountText);
    }
    
    private void addSummaryField(JRDesignBand band, String label, String expression, 
                                int x, int y, int width, int height) throws Exception {
        JRDesignTextField summaryField = new JRDesignTextField();
        summaryField.setX(x);
        summaryField.setY(y);
        summaryField.setWidth(width);
        summaryField.setHeight(height);
        summaryField.setExpression(new JRDesignExpression("\"" + label + " \" + " + expression));
        summaryField.setFontSize(10f);
        summaryField.setBold(true);
        band.addElement(summaryField);
    }
    
    // Additional helper methods for government contributions report
    private void createGovernmentContributionsHeader(JasperDesign jasperDesign) throws Exception {
        // Similar to monthly payroll header but with government contributions title
        // Implementation details...
    }
    
    private void createGovernmentContributionsDetail(JasperDesign jasperDesign) throws Exception {
        // Detail band with SSS, PhilHealth, Pag-IBIG, and tax columns
        // Implementation details...
    }
    
    private void createGovernmentContributionsSummary(JasperDesign jasperDesign) throws Exception {
        // Summary with total contributions by type
        // Implementation details...
    }
}

// Data transfer objects for JasperReports

/**
 * Data class for individual payslip
 */
class PayslipData {
    private Integer employeeId;
    private String fullName;
    private String position;
    private String department;
    private String payPeriod;
    private Integer daysWorked;
    private Double basicPay;
    private Double totalAllowances;
    private Double overtimePay;
    private Double grossPay;
    private Double sssDeduction;
    private Double philhealthDeduction;
    private Double pagibigDeduction;
    private Double taxDeduction;
    private Double totalDeductions;
    private Double netPay;
    
    public PayslipData(Employee employee, Payroll payroll) {
        this.employeeId = employee.getEmployeeId();
        this.fullName = employee.getFullName();
        this.position = employee.getPosition();
        this.department = employee.getPosition(); // Assuming position contains department info
        this.payPeriod = payroll.getStartDateAsLocalDate().format(DateTimeFormatter.ofPattern("MMM yyyy"));
        this.daysWorked = payroll.getDaysWorked();
        this.basicPay = payroll.getGrossEarnings();
        this.totalAllowances = payroll.getTotalAllowances();
        this.overtimePay = payroll.getOvertimePay();
        this.grossPay = payroll.getGrossPay();
        this.sssDeduction = payroll.getSss();
        this.philhealthDeduction = payroll.getPhilhealth();
        this.pagibigDeduction = payroll.getPagibig();
        this.taxDeduction = payroll.getTax();
        this.totalDeductions = payroll.getTotalDeductions();
        this.netPay = payroll.getNetPay();
    }
    
    // Getters (required for JasperReports bean data source)
    public Integer getEmployeeId() { return employeeId; }
    public String getFullName() { return fullName; }
    public String getPosition() { return position; }
    public String getDepartment() { return department; }
    public String getPayPeriod() { return payPeriod; }
    public Integer getDaysWorked() { return daysWorked; }
    public Double getBasicPay() { return basicPay; }
    public Double getTotalAllowances() { return totalAllowances; }
    public Double getOvertimePay() { return overtimePay; }
    public Double getGrossPay() { return grossPay; }
    public Double getSssDeduction() { return sssDeduction; }
    public Double getPhilhealthDeduction() { return philhealthDeduction; }
    public Double getPagibigDeduction() { return pagibigDeduction; }
    public Double getTaxDeduction() { return taxDeduction; }
    public Double getTotalDeductions() { return totalDeductions; }
    public Double getNetPay() { return netPay; }
}

/**
 * Data class for monthly payroll summary
 */
class PayrollSummaryData {
    private Integer employeeId;
    private String fullName;
    private String position;
    private Double basicPay;
    private Double totalAllowances;
    private Double grossPay;
    private Double totalDeductions;
    private Double netPay;
    
    public PayrollSummaryData(Employee employee, Payroll payroll) {
        this.employeeId = employee.getEmployeeId();
        this.fullName = employee.getFullName();
        this.position = employee.getPosition();
        this.basicPay = payroll.getGrossEarnings();
        this.totalAllowances = payroll.getTotalAllowances();
        this.grossPay = payroll.getGrossPay();
        this.totalDeductions = payroll.getTotalDeductions();
        this.netPay = payroll.getNetPay();
    }
    
    // Getters
    public Integer getEmployeeId() { return employeeId; }
    public String getFullName() { return fullName; }
    public String getPosition() { return position; }
    public Double getBasicPay() { return basicPay; }
    public Double getTotalAllowances() { return totalAllowances; }
    public Double getGrossPay() { return grossPay; }
    public Double getTotalDeductions() { return totalDeductions; }
    public Double getNetPay() { return netPay; }
}

/**
 * Data class for government contributions
 */
class GovernmentContributionData {
    private Integer employeeId;
    private String fullName;
    private Double sssContribution;
    private Double philhealthContribution;
    private Double pagibigContribution;
    private Double withholdingTax;
    private Double totalContributions;
    
    public GovernmentContributionData(Employee employee, Payroll payroll) {
        this.employeeId = employee.getEmployeeId();
        this.fullName = employee.getFullName();
        this.sssContribution = payroll.getSss();
        this.philhealthContribution = payroll.getPhilhealth();
        this.pagibigContribution = payroll.getPagibig();
        this.withholdingTax = payroll.getTax();
        this.totalContributions = this.sssContribution + this.philhealthContribution + 
                                 this.pagibigContribution + this.withholdingTax;
    }
    
    // Getters
    public Integer getEmployeeId() { return employeeId; }
    public String getFullName() { return fullName; }
    public Double getSssContribution() { return sssContribution; }
    public Double getPhilhealthContribution() { return philhealthContribution; }
    public Double getPagibigContribution() { return pagibigContribution; }
    public Double getWithholdingTax() { return withholdingTax; }
    public Double getTotalContributions() { return totalContributions; }
}
        
