import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.PrinterException;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * DiabetesPredictorUI
 *
 * Requirements:
 * - Your predictor.py should accept 8 numeric args and print a readable result (prediction + suggestions).
 * - predictor.py and diabetes_model.pkl should be placed in the location referenced in PYTHON_SCRIPT (same folder).
 *
 * To save as PDF: use the system's "Print to PDF" option in the print dialog (e.g., "Microsoft Print to PDF").
 */
public class DiabetesPredictorUI extends JFrame {

    // ----- Configure these if needed -----
    private static final String PYTHON_EXE    = "python"; // or full path to python.exe
    private static final String PYTHON_SCRIPT = "C:\\Users\\hp\\OneDrive\\Desktop\\project1\\python\\predictor.py";
    // -------------------------------------

    private final String[] labels = {
            "Pregnancies", "Glucose", "Blood Pressure", "Skin Thickness",
            "Insulin", "BMI", "Diabetes Pedigree Function", "Age"
    };

    private final String[] ranges = {
            "(0–20)", "(0–300 mg/dL)", "(0–200 mmHg)", "(0–100 mm)",
            "(0–900 µU/mL)", "(0–70 kg/m²)", "(0.0–2.5)", "(1–120 yrs)"
    };

    private JTextField[] fields;
    private JTextArea reportArea;
    private JLabel statusLabel;
    private JButton predictBtn, testAgainBtn, saveBtn, printBtn;

    public DiabetesPredictorUI() {
        setTitle("Diabetes Screening");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(960, 640);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10,10));
        ((JComponent)getContentPane()).setBorder(new EmptyBorder(10,10,10,10));

        // Header
        JLabel header = new JLabel("Diabetes Screening", SwingConstants.CENTER);
        header.setOpaque(true);
        header.setBackground(new Color(22, 110, 170));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 26));
        header.setBorder(new EmptyBorder(12,12,12,12));
        add(header, BorderLayout.NORTH);

        // Center panel with inputs (left) and report (right)
        JPanel center = new JPanel(new GridLayout(1,2,12,12));

        // Left: Inputs
        JPanel left = new JPanel(new BorderLayout(8,8));
        left.setBorder(BorderFactory.createTitledBorder("Patient Inputs"));

        JPanel grid = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6,6,6,6);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;

        fields = new JTextField[labels.length];

        for (int i = 0; i < labels.length; i++) {
            gc.gridx = 0; gc.gridy = i;
            gc.weightx = 0.0;
            JLabel lbl = new JLabel(labels[i] + " :");
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            grid.add(lbl, gc);

            gc.gridx = 1; gc.gridy = i;
            gc.weightx = 1.0;
            JTextField tf = new JTextField();
            tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            tf.setToolTipText(labels[i] + " " + ranges[i]);
            fields[i] = tf;
            grid.add(tf, gc);

            gc.gridx = 2; gc.gridy = i;
            gc.weightx = 0.0;
            JLabel hint = new JLabel(ranges[i]);
            hint.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            hint.setForeground(new Color(100,100,100));
            grid.add(hint, gc);

            final int idx = i;
            // Enter moves to next field; last triggers predict
            tf.addActionListener(e -> {
                if (idx < fields.length - 1) fields[idx+1].requestFocusInWindow();
                else predictAction();
            });
        }

        left.add(grid, BorderLayout.CENTER);

        // Quick instructions
        JTextArea instr = new JTextArea("Tip: Fill all fields. Press Enter to move next. Click Predict to run model.");
        instr.setLineWrap(true);
        instr.setWrapStyleWord(true);
        instr.setEditable(false);
        instr.setBackground(new Color(240,240,240));
        instr.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        instr.setBorder(new EmptyBorder(8,8,8,8));
        left.add(instr, BorderLayout.SOUTH);

        // Right: Report
        JPanel right = new JPanel(new BorderLayout(8,8));
        right.setBorder(BorderFactory.createTitledBorder("Diabetes Test Report"));

        reportArea = new JTextArea();
        reportArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        reportArea.setLineWrap(true);
        reportArea.setWrapStyleWord(true);
        reportArea.setEditable(false);
        JScrollPane reportScroll = new JScrollPane(reportArea);
        right.add(reportScroll, BorderLayout.CENTER);

        center.add(left);
        center.add(right);
        add(center, BorderLayout.CENTER);

        // Bottom: Buttons and status
        JPanel bottom = new JPanel(new BorderLayout(8,8));
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));

        predictBtn = new JButton("Predict");
        predictBtn.setBackground(new Color(28, 150, 65));
        predictBtn.setForeground(Color.red);
        predictBtn.setFocusPainted(false);
        predictBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        predictBtn.addActionListener(e -> predictAction());

        testAgainBtn = new JButton("Test Again");
        testAgainBtn.setBackground(new Color(245, 130, 40));
        testAgainBtn.setForeground(Color.black);
        testAgainBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        testAgainBtn.setFocusPainted(false);
        testAgainBtn.addActionListener(e -> clearForm());

        saveBtn = new JButton("Save Report");
        saveBtn.setBackground(new Color(60,120,200));
        saveBtn.setForeground(Color.black);
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveBtn.setFocusPainted(false);
        saveBtn.addActionListener(e -> saveReport());

        printBtn = new JButton("Print Report");
        printBtn.setBackground(new Color(90,40,160));
        printBtn.setForeground(Color.black);
        printBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        printBtn.setFocusPainted(false);
        printBtn.addActionListener(e -> printReport());

        buttons.add(predictBtn);
        buttons.add(testAgainBtn);
        buttons.add(saveBtn);
        buttons.add(printBtn);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusLabel.setForeground(new Color(130,0,0));

        bottom.add(buttons, BorderLayout.WEST);
        bottom.add(statusLabel, BorderLayout.CENTER);

        add(bottom, BorderLayout.SOUTH);

        // default focus
        SwingUtilities.invokeLater(() -> fields[0].requestFocusInWindow());
    }

    private void predictAction() {
        // Validate inputs (basic)
        String[] vals = new String[fields.length];
        StringBuilder missing = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            String t = fields[i].getText().trim();
            if (t.isEmpty()) {
                missing.append(" - ").append(labels[i]).append("\n");
            } else {
                vals[i] = t;
            }
        }
        if (missing.length() > 0) {
            JOptionPane.showMessageDialog(this, "Please fill all fields:\n" + missing.toString(),
                    "Missing Inputs", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Build report header
        StringBuilder report = new StringBuilder();
        report.append("========== Diabetes Test Report ==========\n");
        report.append("Date: ").append(new SimpleDateFormat("dd-MMM-yyyy HH:mm").format(new Date())).append("\n\n");
        for (int i = 0; i < labels.length; i++) {
            report.append(String.format("%-32s : %s %n", labels[i], vals[i]));
        }
        report.append("\n--------------- Prediction ---------------\n");

        // Run Python predictor
        try {
            statusLabel.setText("Running prediction...");
            predictBtn.setEnabled(false);

            // Build command array
            String[] cmd = new String[2 + vals.length];
            cmd[0] = PYTHON_EXE;
            cmd[1] = PYTHON_SCRIPT;
            System.arraycopy(vals, 0, cmd, 2, vals.length);

            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true); // merge stderr into stdout
            Process p = pb.start();

            // Read output
            StringBuilder out = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    out.append(line).append("\n");
                }
            }

            int exit = p.waitFor();

            if (exit == 0) {
                report.append(out.toString().trim()).append("\n");
                statusLabel.setText("Prediction completed.");
            } else {
                report.append("Prediction failed (exit code ").append(exit).append(")\n");
                report.append(out.toString()).append("\n");
                statusLabel.setText("Prediction failed. See report for details.");
            }

            report.append("==========================================\n");
            reportArea.setText(report.toString());
            reportArea.setCaretPosition(0);
        } catch (IOException ioe) {
            statusLabel.setText("Failed to run Python. See details.");
            JOptionPane.showMessageDialog(this,
                    "Error launching Python:\n" + ioe.getMessage() + "\n\n" +
                    "Check:\n - PYTHON_EXE path\n - PYTHON_SCRIPT path\n - That Python is installed and predictor.py exists",
                    "Execution Error", JOptionPane.ERROR_MESSAGE);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            statusLabel.setText("Prediction interrupted.");
        } finally {
            predictBtn.setEnabled(true);
        }
    }

    private void clearForm() {
        for (JTextField f : fields) f.setText("");
        reportArea.setText("");
        statusLabel.setText(" ");
        fields[0].requestFocusInWindow();
    }

    private void saveReport() {
        if (reportArea.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No report to save. Run a prediction first.",
                    "Nothing to save", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Report");
        chooser.setSelectedFile(new File("Diabetes_Report.txt"));
        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            try (PrintWriter pw = new PrintWriter(new FileWriter(f))) {
                pw.print(reportArea.getText());
                JOptionPane.showMessageDialog(this, "Report saved:\n" + f.getAbsolutePath(), "Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Failed to save report:\n" + e.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void printReport() {
        if (reportArea.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No report to print. Run a prediction first.",
                    "Nothing to print", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            boolean done = reportArea.print();
            if (done) statusLabel.setText("Report sent to printer.");
            else statusLabel.setText("Printing cancelled.");
        } catch (PrinterException e) {
            JOptionPane.showMessageDialog(this, "Printing failed:\n" + e.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        // Use system look & feel
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> {
            DiabetesPredictorUI ui = new DiabetesPredictorUI();
            ui.setVisible(true);
        });
    }
}
