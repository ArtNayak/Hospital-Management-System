package assignment;

import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import javax.swing.SwingUtilities;

// Opens every main window once against the sample data and fails if any of them throws while starting.
// Needs a real display, so it is a manual check, not a JUnit test. Run it from the data/ folder:
//   mvn -q test-compile && cd data && java -cp ../target/classes:../target/test-classes:<jdatepicker jar> assignment.GuiSmoke
public class GuiSmoke {
    private static final List<String> failures = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> failures.add("uncaught in " + t.getName() + ": " + e));

        check("HomePage", () -> new HomePage());
        check("LoginFrame", () -> new LoginFrame());
        check("RegistrationFrame (patient)", () -> new RegistrationFrame("patient"));
        check("RegistrationFrame (doctor)", () -> new RegistrationFrame("doctor"));
        check("AdminDashboard", () -> new AdminDashboard("admin"));
        check("ManagerDashboard", () -> new ManagerDashboard("oliviaa"));
        check("StaffDashboard", () -> new StaffDashboard("amelia"));
        check("DoctorDashboard", () -> new DoctorDashboard("alex"));
        check("PatientDashboard", () -> new PatientDashboard("stephie"));

        System.out.println(failures.isEmpty() ? "GUI SMOKE PASSED" : "GUI SMOKE FAILED: " + failures);
        System.exit(failures.isEmpty() ? 0 : 1);
    }

    private static void check(String name, Callable<Window> open) throws Exception {
        try {
            Window[] w = new Window[1];
            SwingUtilities.invokeAndWait(() -> {
                try {
                    w[0] = open.call();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            Thread.sleep(1200);                      // let it paint and run its initial data loading
            SwingUtilities.invokeAndWait(() -> { if (w[0] != null) w[0].dispose(); });
            System.out.println("ok: " + name);
        } catch (Throwable t) {
            Throwable root = t.getCause() != null ? t.getCause() : t;
            failures.add(name + ": " + root);
            System.out.println("FAIL: " + name + " -> " + root);
        }
    }
}
