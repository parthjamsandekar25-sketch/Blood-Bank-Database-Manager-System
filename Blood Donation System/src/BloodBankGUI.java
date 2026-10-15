import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.*;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.imageio.ImageIO;

class Donor {
    String donorType, prn, aadhar, name, dob, gender, address, bloodGroup, phone, email, lastDonation, status;
    int id, donationCount, calculatedAge, bloodAmount;
    double height, weight, hemoglobin, bloodPressure;

    public Donor(int id, String donorType, String prn, String aadhar, String name, String dob, String gender,
                 String address, String bloodGroup, double height, double weight, String phone, String email,
                 String lastDonation, int donationCount, String status, double hemoglobin, double bloodPressure, int bloodAmount) {
        this.id = id; this.donorType = donorType; this.prn = prn; this.aadhar = aadhar; this.name = name;
        this.dob = dob; this.gender = gender; this.address = address; this.bloodGroup = bloodGroup;
        this.height = height; this.weight = weight; this.phone = phone; this.email = email;
        this.lastDonation = lastDonation; this.donationCount = donationCount; this.status = status;
        this.hemoglobin = hemoglobin; this.bloodPressure = bloodPressure; this.bloodAmount = bloodAmount;
        this.calculatedAge = calculateAge(dob);
    }

    private int calculateAge(String dobStr) {
        try { return Period.between(LocalDate.parse(dobStr.replace("-", "/"), DateTimeFormatter.ofPattern("dd/MM/yyyy")), LocalDate.now()).getYears(); }
        catch (Exception e) { return 0; }
    }

    public String getFullDetails() {
        String identifier = donorType.equals("SAKEC") ? "<b>PRN:</b> " + prn : "<b>Aadhar:</b> " + aadhar;
        return "<html><body style='width: 350px; font-family: Segoe UI; padding: 15px; background-color:#f9f9f9;'>" +
                "<h2 style='color: #c0392b; margin-bottom: 5px; border-bottom: 2px solid #c0392b;'>Donor Profile (" + donorType + ")</h2>" +
                "<div style='margin-top:10px;'><b>Name:</b> " + name + "<br><b>Status:</b> <b>" + status + "</b><br>" + identifier + "<br>" +
                "<b>DOB:</b> " + dob + " (Age: " + calculatedAge + ")<br><b>Gender:</b> " + gender + " | <b>Donations:</b> <b style='color:green'>" + donationCount + "</b><br>" +
                "<b>Address:</b> " + address + "</div><br><h3 style='color: #2980b9;'>Medical Data</h3>" +
                "<div style='background-color:#eaf2f8; padding:5px;'><b>Group:</b> <span style='color:#c0392b; font-weight:bold; font-size: 16px;'>" + bloodGroup + "</span><br>" +
                "<b>Available Blood:</b> " + bloodAmount + " ml<br>" +
                "<b>Physique:</b> " + height + " cm | " + weight + " kg<br>" +
                "<b>Hemoglobin:</b> " + hemoglobin + " g/dL | <b>BP:</b> " + bloodPressure + " mmHg<br>" +
                "<b>Last Donation:</b> " + lastDonation + "</div><br>" +
                "<h3 style='color: #2980b9;'>Contact</h3><b>Phone:</b> " + phone + "<br><b>Email:</b> " + email + "</body></html>";
    }
}

public class BloodBankGUI extends JFrame implements ActionListener {
    static final String DB_URL = "jdbc:mysql://localhost:3306/bloodbank";
    static final String DB_USER = "root";
    static final String DB_PASS = "enter_your_password";

    JTextField bloodGroupField;
    JPanel resultsPanel, statsPanel;
    JLabel statusLabel, lblTotalDonors, lblActiveDonors;
    JButton searchBgButton, advancedFilterButton, addSakecButton, addOtherButton, historyButton, exportButton;
    final Color COL_RED = new Color(192, 57, 43), COL_BG = new Color(245, 246, 250);

    public BloodBankGUI() {
        setTitle("LifeLine: Advanced Donor Management System"); setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); setLayout(new BorderLayout()); getContentPane().setBackground(COL_BG);
        JPanel headerPanel = new JPanel(new BorderLayout()); headerPanel.setBackground(COL_RED); headerPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        JLabel titleLabel = new JLabel("EMERGENCY BLOOD DONOR DATABASE", JLabel.CENTER); titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32)); titleLabel.setForeground(Color.WHITE); headerPanel.add(titleLabel, BorderLayout.CENTER);

        statsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 10)); statsPanel.setBackground(Color.WHITE); statsPanel.setBorder(BorderFactory.createMatteBorder(0,0,1,0, Color.LIGHT_GRAY));
        lblTotalDonors = createStatLabel("Total Network", "0", new Color(41, 128, 185)); lblActiveDonors = createStatLabel("Active Donors", "0", new Color(39, 174, 96));
        statsPanel.add(lblTotalDonors); statsPanel.add(lblActiveDonors); refreshStats();

        JPanel controlsContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15)); controlsContainer.setBackground(Color.WHITE);
        bloodGroupField = new JTextField(4); bloodGroupField.setFont(new Font("Segoe UI", Font.BOLD, 16));
        searchBgButton = createStyledButton("SEARCH", new Color(41, 128, 185));
        advancedFilterButton = createStyledButton("ADVANCED FILTER", new Color(230, 126, 34));
        addSakecButton = createStyledButton("+ SAKEC DONOR", new Color(30, 132, 73));
        addOtherButton = createStyledButton("+ OTHER DONOR", new Color(40, 116, 166));
        historyButton = createStyledButton("HISTORY", new Color(127, 140, 141));
        exportButton = createStyledButton("EXPORT", new Color(142, 68, 173));

        controlsContainer.add(new JLabel("Blood Group:")); controlsContainer.add(bloodGroupField);
        controlsContainer.add(searchBgButton); controlsContainer.add(advancedFilterButton);
        controlsContainer.add(Box.createHorizontalStrut(10)); controlsContainer.add(addSakecButton);
        controlsContainer.add(addOtherButton); controlsContainer.add(historyButton); controlsContainer.add(exportButton);

        resultsPanel = new JPanel(); resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS)); resultsPanel.setBackground(COL_BG);
        JScrollPane scrollPane = new JScrollPane(resultsPanel); scrollPane.getVerticalScrollBar().setUnitIncrement(20); scrollPane.setBorder(new EmptyBorder(10,10,10,10));
        statusLabel = new JLabel("System Ready..."); statusLabel.setBorder(new EmptyBorder(5, 10, 5, 10)); statusLabel.setOpaque(true); statusLabel.setBackground(Color.WHITE);

        JPanel topWrapper = new JPanel(new BorderLayout()); topWrapper.add(headerPanel, BorderLayout.NORTH); topWrapper.add(statsPanel, BorderLayout.CENTER); topWrapper.add(controlsContainer, BorderLayout.SOUTH);
        add(topWrapper, BorderLayout.NORTH); add(scrollPane, BorderLayout.CENTER); add(statusLabel, BorderLayout.SOUTH);
    }

    private JLabel createStatLabel(String title, String value, Color color) {
        String hex = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
        JLabel lbl = new JLabel("<html><div style='text-align:center;'><span style='font-size:24px; color:"+ hex +"; font-weight:bold;'>" + value + "</span><br><span style='font-size:12px; color:gray;'>" + title + "</span></div></html>");
        lbl.setBorder(new LineBorder(Color.LIGHT_GRAY, 1, true)); lbl.setPreferredSize(new Dimension(150, 60)); return lbl;
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text); btn.setFont(new Font("Segoe UI", Font.BOLD, 12)); btn.setBackground(bg); btn.setForeground(Color.WHITE); btn.setFocusPainted(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); btn.addActionListener(this); return btn;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == searchBgButton) {
            if (bloodGroupField.getText().trim().isEmpty()) JOptionPane.showMessageDialog(this, "Enter Blood Group!");
            else performSearch(false);
        }
        else if (e.getSource() == advancedFilterButton) openAdvancedFilterWindow(resultsPanel, fetchDonorsFromDB("Active"));
        else if (e.getSource() == historyButton) performSearch(true);
        else if (e.getSource() == addSakecButton) openSearchAndAddWindow("SAKEC");
        else if (e.getSource() == addOtherButton) openSearchAndAddWindow("OTHER");
        else if (e.getSource() == exportButton) exportData();
    }

    private void performSearch(boolean showHistory) {
        String group = bloodGroupField.getText().toUpperCase().trim();
        if (!showHistory && group.isEmpty()) return;
        resultsPanel.removeAll();
        List<Donor> list = fetchDonorsFromDB(showHistory ? "Inactive" : "Active"); List<Donor> filtered = new ArrayList<>();
        for (Donor d : list) if (showHistory || isCompatible(group, d.bloodGroup)) filtered.add(d);
        if (!showHistory) sortDonors(filtered, group);
        for (Donor d : filtered) addDonorCard(d, resultsPanel);
        statusLabel.setText("Found " + filtered.size() + " records."); resultsPanel.revalidate(); resultsPanel.repaint();
    }

    private void openAdvancedFilterWindow(JPanel targetPanel, List<Donor> sourceList) {
        JDialog d = new JDialog(this, "Advanced Multi-Filter", true); d.setSize(400, 500); d.setLayout(new GridLayout(8, 2, 10, 10));
        ((JComponent)d.getContentPane()).setBorder(new EmptyBorder(15,15,15,15));
        JTextField fName = new JTextField(); JTextField fPhone = new JTextField(); JTextField fId = new JTextField(); JComboBox<String> fGender = new JComboBox<>(new String[]{"Any", "Male", "Female", "Other"});
        JPanel ageP = new JPanel(new GridLayout(1,2)); JTextField ageMin = new JTextField(); JTextField ageMax = new JTextField(); ageP.add(ageMin); ageP.add(ageMax);
        JPanel wP = new JPanel(new GridLayout(1,2)); JTextField wMin = new JTextField(); JTextField wMax = new JTextField(); wP.add(wMin); wP.add(wMax);
        JTextField fMonths = new JTextField();
        d.add(new JLabel("Name (contains):")); d.add(fName); d.add(new JLabel("Phone (contains):")); d.add(fPhone); d.add(new JLabel("PRN / Aadhar:")); d.add(fId); d.add(new JLabel("Gender:")); d.add(fGender);
        d.add(new JLabel("Age Range (Min - Max):")); d.add(ageP); d.add(new JLabel("Weight Range (Min - Max):")); d.add(wP); d.add(new JLabel("Donated X months ago (Min):")); d.add(fMonths);
        JButton apply = new JButton("APPLY FILTER"); apply.setBackground(new Color(230, 126, 34)); apply.setForeground(Color.WHITE);
        apply.addActionListener(e -> {
            targetPanel.removeAll(); int matchCount = 0;
            for (Donor donor : sourceList) {
                boolean match = true;
                if (!fName.getText().isEmpty() && !donor.name.toLowerCase().contains(fName.getText().toLowerCase())) match = false;
                if (!fPhone.getText().isEmpty() && !donor.phone.contains(fPhone.getText())) match = false;
                if (!fId.getText().isEmpty() && !(donor.prn.equals(fId.getText()) || donor.aadhar.equals(fId.getText()))) match = false;
                if (!fGender.getSelectedItem().toString().equals("Any") && !donor.gender.equalsIgnoreCase(fGender.getSelectedItem().toString())) match = false;
                try { if (!ageMin.getText().isEmpty() && donor.calculatedAge < Integer.parseInt(ageMin.getText())) match = false;
                    if (!ageMax.getText().isEmpty() && donor.calculatedAge > Integer.parseInt(ageMax.getText())) match = false;
                    if (!wMin.getText().isEmpty() && donor.weight < Double.parseDouble(wMin.getText())) match = false;
                    if (!wMax.getText().isEmpty() && donor.weight > Double.parseDouble(wMax.getText())) match = false;
                } catch (Exception ex) {}
                if (!fMonths.getText().isEmpty()) { if (donor.lastDonation.equals("New")) { if (Integer.parseInt(fMonths.getText()) > 0) match = false; }
                else if ((getDaysSince(donor.lastDonation) / 30.0) < Double.parseDouble(fMonths.getText())) match = false; }
                if (match) { addDonorCard(donor, targetPanel); matchCount++; }
            }
            statusLabel.setText("Filtered " + matchCount + " records."); targetPanel.revalidate(); targetPanel.repaint(); d.dispose();
        });
        d.add(new JLabel("")); d.add(apply); d.setLocationRelativeTo(this); d.setVisible(true);
    }

    private void openSearchAndAddWindow(String type) {
        JDialog d = new JDialog(this, "Manage " + type + " Donors", true); d.setSize(800, 600); d.setLayout(new BorderLayout());
        JPanel topPanel = new JPanel(new BorderLayout(10, 10)); topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JButton btnAddNew = createStyledButton("+ Add New " + type + " Donor", type.equals("SAKEC") ? new Color(30, 132, 73) : new Color(40, 116, 166));
        btnAddNew.addActionListener(e -> { d.dispose(); openManualAddWindow(type); });
        topPanel.add(btnAddNew, BorderLayout.WEST);
        JPanel searchP = new JPanel(new FlowLayout(FlowLayout.RIGHT)); String idLabel = type.equals("SAKEC") ? "PRN" : "Aadhar";
        JComboBox<String> cmbCriteria = new JComboBox<>(new String[]{"Name", idLabel, "Blood Group", "Phone"});
        JTextField txtSearch = new JTextField(15); searchP.add(new JLabel("Search By:")); searchP.add(cmbCriteria); searchP.add(txtSearch);
        topPanel.add(searchP, BorderLayout.CENTER);
        JPanel dynResultsP = new JPanel(); dynResultsP.setLayout(new BoxLayout(dynResultsP, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(dynResultsP); scroll.getVerticalScrollBar().setUnitIncrement(20);

        List<Donor> cachedDonors = fetchDonorsFromDB("ALL");

        Runnable doSearch = () -> {
            dynResultsP.removeAll(); String term = txtSearch.getText().toLowerCase(); String crit = cmbCriteria.getSelectedItem().toString();
            for (Donor donor : cachedDonors) {
                if (!donor.donorType.equals(type)) continue;
                boolean match = false;
                if (term.isEmpty()) match = true;
                else if (crit.equals("Name") && donor.name.toLowerCase().contains(term)) match = true;
                else if (crit.equals(idLabel) && (type.equals("SAKEC") ? donor.prn.toLowerCase().contains(term) : donor.aadhar.toLowerCase().contains(term))) match = true;
                else if (crit.equals("Blood Group") && donor.bloodGroup.toLowerCase().contains(term)) match = true;
                else if (crit.equals("Phone") && donor.phone.contains(term)) match = true;
                if (match) {
                    JPanel card = new JPanel(new BorderLayout()); card.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(5,5,5,5), new LineBorder(Color.LIGHT_GRAY, 1)));
                    card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90)); card.setBackground(Color.WHITE);
                    JLabel lblName = new JLabel(donor.name); lblName.setFont(new Font("Segoe UI", Font.BOLD, 18));
                    JLabel lblSub = new JLabel(idLabel + ": " + (type.equals("SAKEC") ? donor.prn : donor.aadhar) + "  |  Group: " + donor.bloodGroup + "  |  Available: " + donor.bloodAmount + " ml");
                    JPanel info = new JPanel(new GridLayout(2,1)); info.setOpaque(false); info.setBorder(new EmptyBorder(10,10,10,10));
                    info.add(lblName); info.add(lblSub);
                    JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 20)); btns.setOpaque(false);
                    JButton btnView = new JButton("View Profile"); btnView.addActionListener(ev -> showProfile(donor));
                    JButton btnDonate = new JButton("Add to Donation"); btnDonate.setBackground(new Color(39, 174, 96)); btnDonate.setForeground(Color.WHITE);
                    btnDonate.addActionListener(ev -> { if (donor.calculatedAge < 18) JOptionPane.showMessageDialog(d, "ERROR: Donor is under 18. Cannot donate.", "Underage Warning", JOptionPane.ERROR_MESSAGE);
                    else { processDonation(d, donor); txtSearch.setText(""); } });
                    btns.add(btnView); btns.add(btnDonate); card.add(info, BorderLayout.CENTER); card.add(btns, BorderLayout.EAST); dynResultsP.add(card);
                }
            }
            dynResultsP.revalidate(); dynResultsP.repaint();
        };
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { doSearch.run(); }
            public void removeUpdate(DocumentEvent e) { doSearch.run(); }
            public void changedUpdate(DocumentEvent e) { doSearch.run(); }
        });
        cmbCriteria.addActionListener(e -> doSearch.run()); doSearch.run();
        d.add(topPanel, BorderLayout.NORTH); d.add(scroll, BorderLayout.CENTER); d.setLocationRelativeTo(this); d.setVisible(true);
    }

    private void openManualAddWindow(String type) {
        JDialog d = new JDialog(this, "Add New " + type + " Donor", true); d.setSize(500, 750); d.setLayout(new BorderLayout());
        JPanel form = new JPanel(new GridLayout(14, 2, 10, 10)); form.setBorder(new EmptyBorder(20, 20, 20, 20));
        JTextField txtID = new JTextField(); JTextField txtName = new JTextField(); JTextField txtDob = new JTextField(); JComboBox<String> cmbGender = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        JTextField txtAddr = new JTextField(); JComboBox<String> cmbBg = new JComboBox<>(new String[]{"A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"});
        JTextField txtH = new JTextField(); JTextField txtW = new JTextField(); JTextField txtPhone = new JTextField(); JTextField txtEmail = new JTextField(); JTextField txtDate = new JTextField("New");
        JTextField txtHb = new JTextField(); JTextField txtBp = new JTextField(); JTextField txtAmount = new JTextField();

        addF(form, type.equals("SAKEC") ? "PRN Number:" : "Aadhar Number:", txtID); addF(form, "Name:", txtName); addF(form, "DOB (DD/MM/YYYY):", txtDob); addF(form, "Gender:", cmbGender);
        addF(form, "Address:", txtAddr); addF(form, "Blood Group:", cmbBg); addF(form, "Height (cm):", txtH); addF(form, "Weight (kg):", txtW);
        addF(form, "Hemoglobin (g/dL):", txtHb); addF(form, "Blood Pressure (mmHg):", txtBp); addF(form, "Amount Donated (ml):", txtAmount);
        addF(form, "Phone:", txtPhone); addF(form, "Email ID:", txtEmail); addF(form, "Last Donation Date:", txtDate);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        JButton btnSaveInfo = createStyledButton("SAVE DONOR'S INFO", new Color(127, 140, 141));
        JButton btnAddDonation = createStyledButton("ADD TO DONATION", new Color(39, 174, 96));
        btnPanel.add(btnSaveInfo); btnPanel.add(btnAddDonation);

        btnSaveInfo.addActionListener(e -> saveDonorData(d, type, txtID.getText(), txtName.getText(), txtDob.getText(), cmbGender.getSelectedItem().toString(), txtAddr.getText(), cmbBg.getSelectedItem().toString(), txtH.getText(), txtW.getText(), txtHb.getText(), txtBp.getText(), txtAmount.getText(), txtPhone.getText(), txtEmail.getText(), txtDate.getText(), false));
        btnAddDonation.addActionListener(e -> saveDonorData(d, type, txtID.getText(), txtName.getText(), txtDob.getText(), cmbGender.getSelectedItem().toString(), txtAddr.getText(), cmbBg.getSelectedItem().toString(), txtH.getText(), txtW.getText(), txtHb.getText(), txtBp.getText(), txtAmount.getText(), txtPhone.getText(), txtEmail.getText(), txtDate.getText(), true));
        d.add(form, BorderLayout.CENTER); d.add(btnPanel, BorderLayout.SOUTH); d.setLocationRelativeTo(this); d.setVisible(true);
    }

    private void saveDonorData(JDialog d, String type, String id, String name, String dob, String gender, String addr, String bg, String h, String w, String hbStr, String bpStr, String amtStr, String phone, String email, String dateStr, boolean activate) {
        String cleanDob = normalizeDate(dob); if (cleanDob == null) { JOptionPane.showMessageDialog(d, "Invalid DOB format!"); return; }
        if (calculateAgeTemp(cleanDob) < 18) { JOptionPane.showMessageDialog(d, "Donor must be 18+"); return; }

        double hb = 0, bp = 0; int amt = 0;
        try { hb = Double.parseDouble(hbStr); bp = Double.parseDouble(bpStr); amt = Integer.parseInt(amtStr); }
        catch (Exception ex) { JOptionPane.showMessageDialog(d, "Invalid medical numbers."); return; }

        if (hb < 12 || hb > 20 || bp < 100 || bp > 180) { JOptionPane.showMessageDialog(d, "Not Eligible. Hemoglobin must be 12-20 and BP 100-180."); return; }

        String lastDon = normalizeDate(dateStr);
        if (activate && lastDon.equalsIgnoreCase("New")) lastDon = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        else if (activate && !lastDon.equalsIgnoreCase("New") && getDaysSince(lastDon) < 90) { if (JOptionPane.showConfirmDialog(d, "WARNING: Donated < 90 days ago. Proceed?", "Ineligible", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) return; }
        String status = activate ? "Active" : "Pending"; int dCountAdd = activate ? 1 : 0;
        try { Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            String idCol = type.equals("SAKEC") ? "prn" : "aadhar";
            PreparedStatement ins = conn.prepareStatement("INSERT INTO donors (donor_type, "+idCol+", name, dob, gender, address, blood_group, height, weight, phone, email, last_donation, status, donation_count, hemoglobin, blood_pressure, blood_amount) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE name=?, dob=?, gender=?, address=?, blood_group=?, height=?, weight=?, phone=?, email=?, last_donation=?, status=?, donation_count=donation_count+?, hemoglobin=?, blood_pressure=?, blood_amount=blood_amount+?");
            ins.setString(1, type); ins.setString(2, id); ins.setString(3, name); ins.setString(4, cleanDob); ins.setString(5, gender); ins.setString(6, addr); ins.setString(7, bg); ins.setDouble(8, Double.parseDouble(h)); ins.setDouble(9, Double.parseDouble(w)); ins.setString(10, phone); ins.setString(11, email); ins.setString(12, lastDon); ins.setString(13, status); ins.setInt(14, dCountAdd); ins.setDouble(15, hb); ins.setDouble(16, bp); ins.setInt(17, amt);
            ins.setString(18, name); ins.setString(19, cleanDob); ins.setString(20, gender); ins.setString(21, addr); ins.setString(22, bg); ins.setDouble(23, Double.parseDouble(h)); ins.setDouble(24, Double.parseDouble(w)); ins.setString(25, phone); ins.setString(26, email); ins.setString(27, lastDon); ins.setString(28, status); ins.setInt(29, dCountAdd); ins.setDouble(30, hb); ins.setDouble(31, bp); ins.setInt(32, amt);
            ins.executeUpdate(); conn.close();

            if (activate) {
                generateCertificate(name, lastDon, bg);
            }

            refreshStats(); performSearch(false); d.dispose();
            JOptionPane.showMessageDialog(this, type + " Donor successfully " + (activate ? "added to Active Donations!\nCertificate Generated in 'Certificates' folder." : "saved to the database!"));
            openSearchAndAddWindow(type);
        } catch (Exception ex) { JOptionPane.showMessageDialog(d, "Error: " + ex.getMessage()); }
    }

    private void processDonation(JDialog parent, Donor d) {
        if (!d.lastDonation.equalsIgnoreCase("New") && getDaysSince(d.lastDonation) < 90 && JOptionPane.showConfirmDialog(parent, "Medical Warning: Last donation was " + getDaysSince(d.lastDonation) + " days ago. Add anyway?", "Wait 90 Days", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) return;

        JPanel p = new JPanel(new GridLayout(4, 2));
        JTextField txtDate = new JTextField(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        JTextField txtHb = new JTextField(String.valueOf(d.hemoglobin)); JTextField txtBp = new JTextField(String.valueOf(d.bloodPressure)); JTextField txtAmt = new JTextField("0");
        p.add(new JLabel("Date (DD/MM/YYYY):")); p.add(txtDate); p.add(new JLabel("Hemoglobin (g/dL):")); p.add(txtHb); p.add(new JLabel("Blood Pressure (mmHg):")); p.add(txtBp); p.add(new JLabel("Amount Donated (ml):")); p.add(txtAmt);

        if (JOptionPane.showConfirmDialog(parent, p, "Enter Donation Details", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;

        String normalized = normalizeDate(txtDate.getText()); double hb = 0, bp = 0; int amt = 0;
        try { hb = Double.parseDouble(txtHb.getText()); bp = Double.parseDouble(txtBp.getText()); amt = Integer.parseInt(txtAmt.getText()); } catch(Exception ex){ return; }
        if (hb < 12 || hb > 20 || bp < 100 || bp > 180) { JOptionPane.showMessageDialog(parent, "Not Eligible. Hemoglobin must be 12-20 and BP 100-180."); return; }

        try { Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            PreparedStatement up = c.prepareStatement("UPDATE donors SET last_donation=?, donation_count=donation_count+1, status='Active', hemoglobin=?, blood_pressure=?, blood_amount=blood_amount+? WHERE id=?");
            up.setString(1, normalized); up.setDouble(2, hb); up.setDouble(3, bp); up.setInt(4, amt); up.setInt(5, d.id); up.executeUpdate(); c.close();

            generateCertificate(d.name, normalized, d.bloodGroup);

            JOptionPane.showMessageDialog(parent, d.name + " added to Active Donors!\nCertificate Generated in 'Certificates' folder.");
            refreshStats(); performSearch(false); parent.dispose();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    // --- CERTIFICATE GENERATOR METHOD ---
    private void generateCertificate(String donorName, String date, String bloodGroup) {
        try {
            File templateFile = new File("template.jpg");
            if (!templateFile.exists()) {
                System.out.println("Warning: 'template.jpg' not found in project folder. Certificate not created.");
                return;
            }
            BufferedImage image = ImageIO.read(templateFile);
            Graphics2D g = image.createGraphics();

            // Make the text perfectly smooth
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            g.setColor(new Color(60, 20, 20)); // Dark red/brown to match your template aesthetics

            // 1. Draw Name (Increased font size to 120, updated coordinates)
            g.setFont(new Font("Segoe UI", Font.BOLD, 120));
            g.drawString(donorName, 1120, 1360);

            // 2. Draw Date (Increased font size to 80, updated coordinates)
            g.setFont(new Font("Segoe UI", Font.BOLD, 80));
            g.drawString(date, 1230, 2268);

            // 3. Draw Blood Group (Uses the same font size 80, updated coordinates)
            g.drawString(bloodGroup, 2929, 2268);

            g.dispose();

            // Save the File
            File outDir = new File("Certificates");
            if (!outDir.exists()) outDir.mkdir();

            String safeName = donorName.replace(" ", "_");
            File outputFile = new File("Certificates/" + safeName + "_Certificate.jpg");
            ImageIO.write(image, "jpg", outputFile);

        } catch (Exception e) {
            System.out.println("Failed to generate certificate: " + e.getMessage());
        }
    }

    private void addDonorCard(Donor d, JPanel targetPanel) {
        JPanel card = new JPanel(new BorderLayout()); Color bg, fg;
        if (d.donorType.equalsIgnoreCase("SAKEC")) { bg = d.status.equalsIgnoreCase("Active") ? new Color(30, 132, 73) : new Color(169, 223, 191); fg = d.status.equalsIgnoreCase("Active") ? Color.WHITE : Color.BLACK; }
        else { bg = d.status.equalsIgnoreCase("Active") ? new Color(40, 116, 166) : new Color(174, 214, 241); fg = d.status.equalsIgnoreCase("Active") ? Color.WHITE : Color.BLACK; }
        card.setBackground(bg); card.setBorder(new EmptyBorder(5, 15, 5, 15)); card.setPreferredSize(new Dimension(800, 70)); card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        String idText = d.donorType.equals("SAKEC") ? "PRN: " + d.prn : "Aadhar: " + d.aadhar;
        JLabel nameLabel = new JLabel(d.name + " (" + d.donorType + ") " + (d.status.equals("Inactive") ? "[REMOVED]" : "")); nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16)); nameLabel.setForeground(fg);
        JLabel subLabel = new JLabel("Group: " + d.bloodGroup + " | Age: " + d.calculatedAge + " | Available: " + d.bloodAmount + " ml | " + idText); subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12)); subLabel.setForeground(fg);
        JPanel info = new JPanel(new GridLayout(2, 1)); info.setOpaque(false); info.add(nameLabel); info.add(subLabel);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT)); btns.setOpaque(false);
        JButton view = new JButton("View Profile"); view.setBackground(Color.WHITE); view.setForeground(Color.BLACK); view.addActionListener(e -> showProfile(d)); btns.add(view);
        if (d.status.equalsIgnoreCase("Active")) { JButton remove = new JButton("Use Blood"); remove.setBackground(COL_RED); remove.setForeground(Color.WHITE); remove.addActionListener(e -> removeDonor(d, targetPanel)); btns.add(remove); }
        card.add(info, BorderLayout.CENTER); card.add(btns, BorderLayout.EAST); JPanel wrapper = new JPanel(new BorderLayout()); wrapper.setOpaque(false); wrapper.add(card); wrapper.setBorder(new EmptyBorder(0,0,5,0)); targetPanel.add(wrapper);
    }

    private List<Donor> fetchDonorsFromDB(String statusMode) {
        List<Donor> list = new ArrayList<>();
        try { Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM donors" + (statusMode.equals("ALL") ? "" : " WHERE status='" + statusMode + "'"));
            while (rs.next()) list.add(new Donor(rs.getInt("id"), rs.getString("donor_type"), rs.getString("prn"), rs.getString("aadhar"), rs.getString("name"), rs.getString("dob"), rs.getString("gender"), rs.getString("address"), rs.getString("blood_group"), rs.getDouble("height"), rs.getDouble("weight"), rs.getString("phone"), rs.getString("email"), rs.getString("last_donation"), rs.getInt("donation_count"), rs.getString("status"), rs.getDouble("hemoglobin"), rs.getDouble("blood_pressure"), rs.getInt("blood_amount")));
            conn.close(); } catch (Exception e) { e.printStackTrace(); } return list;
    }

    private String normalizeDate(String date) { if (date.equalsIgnoreCase("New")) return "New"; try { String c = date.replace("-", "/"); LocalDate.parse(c, DateTimeFormatter.ofPattern("dd/MM/yyyy")); return c; } catch (Exception e) { return null; } }
    private long getDaysSince(String date) { try { return ChronoUnit.DAYS.between(LocalDate.parse(date.replace("-", "/"), DateTimeFormatter.ofPattern("dd/MM/yyyy")), LocalDate.now()); } catch (Exception e) { return -1; } }
    private int calculateAgeTemp(String dobStr) { try { return Period.between(LocalDate.parse(dobStr.replace("-", "/"), DateTimeFormatter.ofPattern("dd/MM/yyyy")), LocalDate.now()).getYears(); } catch (Exception e) { return 0; } }

    private void refreshStats() {
        try { Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS); Statement s = conn.createStatement();
            ResultSet rs1 = s.executeQuery("SELECT COUNT(*) FROM donors"); if(rs1.next()) lblTotalDonors.setText(createStatLabel("Total Network", rs1.getString(1), new Color(41, 128, 185)).getText());
            ResultSet rs2 = s.executeQuery("SELECT COUNT(*) FROM donors WHERE status='Active'"); if(rs2.next()) lblActiveDonors.setText(createStatLabel("Active Donors", rs2.getString(1), new Color(39, 174, 96)).getText());
            conn.close(); } catch (Exception e) {}
    }

    private void exportData() {
        try { FileWriter fw = new FileWriter("Donors_Export.csv"); PrintWriter pw = new PrintWriter(fw); pw.println("Type,PRN,Aadhar,Name,DOB,Gender,Address,BloodGroup,Height,Weight,Hb,BP,BloodAmount,Phone,Email,LastDonation,TotalDonations,Status");
            ResultSet rs = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS).createStatement().executeQuery("SELECT * FROM donors WHERE status='Active'");
            while(rs.next()) pw.println(rs.getString("donor_type")+","+rs.getString("prn")+","+rs.getString("aadhar")+","+rs.getString("name")+","+rs.getString("dob")+","+rs.getString("gender")+","+rs.getString("address")+","+rs.getString("blood_group")+","+rs.getDouble("height")+","+rs.getDouble("weight")+","+rs.getDouble("hemoglobin")+","+rs.getDouble("blood_pressure")+","+rs.getInt("blood_amount")+","+rs.getString("phone")+","+rs.getString("email")+","+rs.getString("last_donation")+","+rs.getInt("donation_count")+","+rs.getString("status"));
            pw.close(); fw.close(); JOptionPane.showMessageDialog(this, "Data Exported to 'Donors_Export.csv'"); } catch (Exception e) {}
    }

    private void addF(JPanel p, String l, JComponent c) { p.add(new JLabel(l)); p.add(c); }

    private void removeDonor(Donor d, JPanel currentPanel) {
        String input = JOptionPane.showInputDialog(this, "Enter amount of blood to use in ml (Available: " + d.bloodAmount + " ml):");
        if(input == null || input.trim().isEmpty()) return;
        try {
            int used = Integer.parseInt(input);
            if (used < 0 || used > d.bloodAmount) { JOptionPane.showMessageDialog(this, "Invalid amount. Max available is " + d.bloodAmount + " ml."); return; }
            Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            if (used == d.bloodAmount) c.createStatement().executeUpdate("UPDATE donors SET status='Inactive', blood_amount=0 WHERE id=" + d.id);
            else c.createStatement().executeUpdate("UPDATE donors SET blood_amount=" + (d.bloodAmount - used) + " WHERE id=" + d.id);
            c.close(); refreshStats();
            if(currentPanel == resultsPanel) performSearch(false);
            else { currentPanel.removeAll(); for(Donor donor : fetchDonorsFromDB("ALL")) addDonorCard(donor, currentPanel); currentPanel.revalidate(); currentPanel.repaint(); }
        } catch(Exception e){}
    }

    private void showProfile(Donor d) { JOptionPane.showMessageDialog(this, new JLabel(d.getFullDetails()), "Profile", JOptionPane.PLAIN_MESSAGE); }

    private void sortDonors(List<Donor> list, String patientGroup) {
        Collections.sort(list, (d1, d2) -> {
            int p1 = getPriority(patientGroup, d1.bloodGroup);
            int p2 = getPriority(patientGroup, d2.bloodGroup);
            if (p1 != p2) return Integer.compare(p1, p2);
            return Integer.compare(d2.bloodAmount, d1.bloodAmount);
        });
    }

    private int getPriority(String p, String d) { if(p.equals(d)) return 1; if(d.equals("O-")) return 6; if(d.equals("O+")) return 5; if(p.startsWith("AB")) return d.startsWith("AB") ? 2 : (d.contains("+") ? 3 : 4); return 2; }
    private boolean isCompatible(String p, String d) { if(!p.contains("+") && d.contains("+")) return false; return d.contains("O") || p.contains("AB") || p.charAt(0) == d.charAt(0); }

    public static void main(String[] args) { SwingUtilities.invokeLater(() -> new BloodBankGUI().setVisible(true)); }
}