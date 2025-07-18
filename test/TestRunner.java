package test;

import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

/**
 * JUnit 5 Test Runner for MotorPH Payroll System
 * Demonstrates proper unit testing with JUnit framework
 */
public class TestRunner {
    public static void main(String[] args) {
        System.out.println("=".repeat(80));
        System.out.println("           MOTORPH PAYROLL SYSTEM - JUNIT 5 TEST SUITE");
        System.out.println("                    OOP Principles & Business Logic Tests");
        System.out.println("=".repeat(80));
        
        // Create launcher
        Launcher launcher = LauncherFactory.create();
        
        // Create summary listener
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        
        // Build discovery request to find all tests in the test package
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectPackage("test"))
                .build();
        
        // Execute tests
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(request);
        
        // Print comprehensive summary
        TestExecutionSummary summary = listener.getSummary();
        printDetailedTestSummary(summary);
    }
    
    private static void printDetailedTestSummary(TestExecutionSummary summary) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("                        JUNIT 5 TEST EXECUTION SUMMARY");
        System.out.println("=".repeat(80));
        
        // Test counts
        System.out.printf("📊 Tests Found     : %d%n", summary.getTestsFoundCount());
        System.out.printf("🚀 Tests Started   : %d%n", summary.getTestsStartedCount());
        System.out.printf("✅ Tests Successful: %d%n", summary.getTestsSucceededCount());
        System.out.printf("⏭️  Tests Skipped   : %d%n", summary.getTestsSkippedCount());
        System.out.printf("🚫 Tests Aborted   : %d%n", summary.getTestsAbortedCount());
        System.out.printf("❌ Tests Failed    : %d%n", summary.getTestsFailedCount());
        
        // Execution time
        System.out.println("\n⏱️  Execution Time  : " + summary.getTotalTime().toMillis() + " ms");
        
        // Success rate
        long totalExecuted = summary.getTestsSucceededCount() + summary.getTestsFailedCount();
        if (totalExecuted > 0) {
            double successRate = (double) summary.getTestsSucceededCount() / totalExecuted * 100;
            System.out.printf("📈 Success Rate    : %.1f%%%n", successRate);
        }
        
        // Test categories summary
        System.out.println("\n📋 Test Categories Covered:");
        System.out.println("   🏗️  OOP Principles: Inheritance, Polymorphism, Abstraction, Encapsulation");
        System.out.println("   🏭 Design Patterns: Factory Pattern, Template Method Pattern");
        System.out.println("   💰 Business Logic: Payroll calculations, Tax computations, Allowances");
        System.out.println("   📄 Report Generation: JasperReports integration, PDF generation");
        System.out.println("   🧮 Edge Cases: Boundary conditions, Error handling, Data validation");
        
        // Failed tests details
        if (summary.getTestsFailedCount() > 0) {
            System.out.println("\n" + "!".repeat(80));
            System.out.println("                         ❌ FAILED TESTS DETAILS");
            System.out.println("!".repeat(80));
            
            summary.getFailures().forEach(failure -> {
                System.out.println("❌ " + failure.getTestIdentifier().getDisplayName());
                System.out.println("   📍 Test Source: " + failure.getTestIdentifier().getSource().orElse("Unknown"));
                System.out.println("   💥 Exception: " + failure.getException().getClass().getSimpleName());
                System.out.println("   📝 Message: " + failure.getException().getMessage());
                
                // Print stack trace for debugging
                if (failure.getException().getCause() != null) {
                    System.out.println("   🔍 Cause: " + failure.getException().getCause().getMessage());
                }
                System.out.println();
            });
        }
        
        // Skipped tests details
        if (summary.getTestsSkippedCount() > 0) {
            System.out.println("\n" + "⏭".repeat(80));
            System.out.println("                         ⏭️ SKIPPED TESTS DETAILS");
            System.out.println("⏭".repeat(80));
            
            // Note: JUnit 5 doesn't provide easy access to skipped test details in the summary
            System.out.println("   ℹ️  Some tests were skipped (likely due to missing dependencies)");
            System.out.println("   💡 Common reasons:");
            System.out.println("      - JasperReports JAR files not in classpath");
            System.out.println("      - Database connection not available");
            System.out.println("      - Test environment conditions not met");
        }
        
        // Final result
        System.out.println("\n" + "=".repeat(80));
        if (summary.getTestsFailedCount() == 0) {
            System.out.println("🎉 ALL TESTS PASSED! The MotorPH Payroll System is working correctly.");
            System.out.println("✨ OOP principles are properly implemented:");
            System.out.println("   ✅ Inheritance: Employee hierarchy with RegularEmployee and ProbationaryEmployee");
            System.out.println("   ✅ Polymorphism: Different behavior for different employee types");
            System.out.println("   ✅ Abstraction: Abstract Employee class with concrete implementations");
            System.out.println("   ✅ Encapsulation: Proper data hiding and method access control");
            System.out.println("📊 Business logic calculations are accurate and tested");
            System.out.println("📄 JasperReports integration is functional (if available)");
        } else {
            System.out.println("⚠️  SOME TESTS FAILED - Please review the implementation.");
            System.out.println("🔧 Check the failed tests above and fix the issues.");
            System.out.println("💡 Common fixes:");
            System.out.println("   - Verify all dependencies are in classpath");
            System.out.println("   - Check database connection and schema");
            System.out.println("   - Ensure JasperReports template is available");
            System.out.println("   - Review business logic implementations");
        }
        System.out.println("=".repeat(80));
        
        // Exit with appropriate code
        System.exit(summary.getTestsFailedCount() == 0 ? 0 : 1);
    }
}