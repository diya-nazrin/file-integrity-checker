import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
 
/**
 * ╔══════════════════════════════════╗
 * ║   FILE INTEGRITY CHECKER v5.0   ║
 * ║   Swing GUI Edition             ║
 * ╚══════════════════════════════════╝
 *
 * A polished desktop GUI to generate and verify SHA-256
 * checksums. Features:
 *   - Drag-and-drop file loading
 *   - Browse button
 *   - Hash display area with copy button
 *   - Verify button with colour-coded result
 *   - Tracked files list
 *
 * Concepts: Swing components, JTabbedPane, SwingWorker,
 *           GridBagLayout, custom painting, DnD.
 *
 * Author  : Diya
 * Version : 5.0
 */
public class FileIntegrityCheckerGUI extends JFrame {
 
    // ── Constants ─────────────────────────────────────────────────────────────
    private static final String HASH_STORE  = "hashes.txt";
    private static final int    BUFFER_SIZE = 8192;
 
    // ── Colour Palette ────────────────────────────────────────────────────────
    private static final Color BG        = new Color(10,  12,  16);
    private static final Color SURFACE   = new Color(17,  19,  24);
    private static final Color PANEL_BG  = new Color(22,  26,  34);
    private static final Color BORDER_C  = new Color(30,  36,  48);
    private static final Color ACCENT    = new Color(0,   229, 255);
    private static final Color ACCENT2   = new Color(124, 58,  237);
    private static final Color SAFE      = new Color(0,   224, 150);
    private static final Color WARN      = new Color(255, 75,  75);
    private static final Color TEXT      = new Color(226, 232, 240);
    private static final Color MUTED     = new Color(100, 116, 139);
 
    // ── Fonts ─────────────────────────────────────────────────────────────────
    private static final Font FONT_MONO  = new Font("JetBrains Mono", Font.PLAIN, 12);
    private static final Font FONT_MONO_B= new Font("JetBrains Mono", Font.BOLD,  12);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD,  22);
    private static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
 
    // ── State ─────────────────────────────────────────────────────────────────
    private File selectedFile = null;
 
    // ── Generate Tab components ───────────────────────────────────────────────
    private JLabel   genDropLabel;
    private JLabel   genFileNameLabel;
    private JLabel   genFileSizeLabel;
    private JTextArea genHashArea;
    private JButton  genBrowseBtn;
    private JButton  genHashBtn;
    private JButton  genCopyBtn;
    private JPanel   genFileInfoPanel;
    private JPanel   genResultPanel;
    private JLabel   genStatusLabel;
 
    // ── Verify Tab components ─────────────────────────────────────────────────
    private JLabel   verDropLabel;
    private JLabel   verFileNameLabel;
    private JLabel   verFileSizeLabel;
    private JTextArea verExpectedArea;
    private JButton  verBrowseBtn;
    private JButton  verifyBtn;
    private JPanel   verFileInfoPanel;
    private JPanel   verResultPanel;
    private JLabel   verResultLabel;
    private JTextArea verCurrentHashArea;
    private JTextArea verExpectedHashDisplay;
 
    // ── List Tab components ───────────────────────────────────────────────────
    private JTextArea listArea;
 
    // ─────────────────────────────────────────────────────────────────────────
 
    public FileIntegrityCheckerGUI() {
        setTitle("File Integrity Checker v5.0");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(760, 620);
        setMinimumSize(new Dimension(640, 520));
        setLocationRelativeTo(null);
        setBackground(BG);
 
        // Use system look for native file chooser dialogs
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
 
        // Build UI
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        root.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
 
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildTabs(),   BorderLayout.CENTER);
 
        setContentPane(root);
        setVisible(true);
    }
 
    // ── Header ────────────────────────────────────────────────────────────────
 
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(SURFACE);
        header.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, BORDER_C),
            BorderFactory.createEmptyBorder(18, 28, 18, 28)
        ));
 
        JLabel title = new JLabel("🔐  File Integrity Checker");
        title.setFont(FONT_TITLE);
        title.setForeground(TEXT);
 
        JLabel sub = new JLabel("SHA-256 · Generate · Verify · Track");
        sub.setFont(FONT_SMALL);
        sub.setForeground(MUTED);
 
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        left.add(title);
        left.add(Box.createVerticalStrut(4));
        left.add(sub);
 
        JLabel badge = new JLabel("v5.0");
        badge.setFont(new Font("JetBrains Mono", Font.BOLD, 11));
        badge.setForeground(ACCENT);
        badge.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 229, 255, 60), 1, true),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
 
        header.add(left,  BorderLayout.WEST);
        header.add(badge, BorderLayout.EAST);
        return header;
    }
 
    // ── Tabbed Pane ───────────────────────────────────────────────────────────
 
    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(BG);
        tabs.setForeground(TEXT);
        tabs.setFont(FONT_LABEL);
 
        // Style tabs
        UIManager.put("TabbedPane.selected",           PANEL_BG);
        UIManager.put("TabbedPane.background",         SURFACE);
        UIManager.put("TabbedPane.foreground",         TEXT);
        UIManager.put("TabbedPane.contentAreaColor",   BG);
        UIManager.put("TabbedPane.tabAreaBackground",  SURFACE);
        UIManager.put("TabbedPane.focus",              new Color(0,0,0,0));
 
        tabs.addTab("⬡  Generate Hash",  buildGeneratePanel());
        tabs.addTab("✦  Verify File",    buildVerifyPanel());
        tabs.addTab("☰  Tracked Files",  buildListPanel());
 
        return tabs;
    }
 
    // ── Generate Panel ────────────────────────────────────────────────────────
 
    private JPanel buildGeneratePanel() {
        JPanel panel = new JPanel();
        panel.setBackground(BG);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
 
        // Drop zone
        JPanel dropZone = buildDropZone("gen");
        panel.add(dropZone);
        panel.add(Box.createVerticalStrut(12));
 
        // File info
        genFileInfoPanel = buildFileInfoPanel("gen");
        genFileInfoPanel.setVisible(false);
        panel.add(genFileInfoPanel);
        panel.add(Box.createVerticalStrut(12));
 
        // Browse + Generate buttons row
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnRow.setOpaque(false);
 
        genBrowseBtn = buildButton("Browse File", SURFACE, ACCENT);
        genBrowseBtn.addActionListener(e -> browseFile("gen"));
 
        genHashBtn = buildButton("Generate SHA-256", ACCENT2, Color.WHITE);
        genHashBtn.setEnabled(false);
        genHashBtn.addActionListener(e -> runGenerate());
 
        btnRow.add(genBrowseBtn);
        btnRow.add(genHashBtn);
 
        panel.add(btnRow);
        panel.add(Box.createVerticalStrut(16));
 
        // Result panel
        genResultPanel = new JPanel();
        genResultPanel.setLayout(new BoxLayout(genResultPanel, BoxLayout.Y_AXIS));
        genResultPanel.setBackground(PANEL_BG);
        genResultPanel.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(BORDER_C, 1, true),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
        genResultPanel.setVisible(false);
 
        JLabel hashLabel = styledLabel("SHA-256 CHECKSUM", MUTED, new Font("JetBrains Mono", Font.PLAIN, 10));
        hashLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        genResultPanel.add(hashLabel);
        genResultPanel.add(Box.createVerticalStrut(8));
 
        genHashArea = new JTextArea(3, 50);
        genHashArea.setEditable(false);
        genHashArea.setLineWrap(true);
        genHashArea.setWrapStyleWord(false);
        genHashArea.setBackground(BG);
        genHashArea.setForeground(ACCENT);
        genHashArea.setFont(FONT_MONO);
        genHashArea.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        JScrollPane hashScroll = new JScrollPane(genHashArea);
        hashScroll.setBorder(BorderFactory.createLineBorder(BORDER_C, 1, true));
        hashScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        hashScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        genResultPanel.add(hashScroll);
        genResultPanel.add(Box.createVerticalStrut(10));
 
        genCopyBtn = buildSmallButton("Copy Hash");
        genCopyBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        genCopyBtn.addActionListener(e -> {
            StringSelection sel = new StringSelection(genHashArea.getText());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
            genCopyBtn.setText("Copied ✔");
            Timer t = new Timer(1800, ev -> genCopyBtn.setText("Copy Hash"));
            t.setRepeats(false);
            t.start();
        });
        genResultPanel.add(genCopyBtn);
 
        genStatusLabel = styledLabel("", SAFE, FONT_SMALL);
        genStatusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        genResultPanel.add(Box.createVerticalStrut(8));
        genResultPanel.add(genStatusLabel);
 
        genResultPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        genResultPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        panel.add(genResultPanel);
 
        setupDrop(dropZone, "gen");
        return panel;
    }
 
    // ── Verify Panel ──────────────────────────────────────────────────────────
 
    private JPanel buildVerifyPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(BG);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
 
        JPanel dropZone = buildDropZone("ver");
        panel.add(dropZone);
        panel.add(Box.createVerticalStrut(12));
 
        verFileInfoPanel = buildFileInfoPanel("ver");
        verFileInfoPanel.setVisible(false);
        panel.add(verFileInfoPanel);
        panel.add(Box.createVerticalStrut(12));
 
        JLabel expLabel = styledLabel("PASTE EXPECTED HASH", MUTED, new Font("JetBrains Mono", Font.PLAIN, 10));
        expLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(expLabel);
        panel.add(Box.createVerticalStrut(6));
 
        verExpectedArea = new JTextArea(2, 50);
        verExpectedArea.setBackground(SURFACE);
        verExpectedArea.setForeground(TEXT);
        verExpectedArea.setCaretColor(ACCENT);
        verExpectedArea.setFont(FONT_MONO);
        verExpectedArea.setLineWrap(true);
        verExpectedArea.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        JScrollPane expScroll = new JScrollPane(verExpectedArea);
        expScroll.setBorder(BorderFactory.createLineBorder(BORDER_C, 1, true));
        expScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        expScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        panel.add(expScroll);
        panel.add(Box.createVerticalStrut(14));
 
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnRow.setOpaque(false);
 
        verBrowseBtn = buildButton("Browse File", SURFACE, ACCENT);
        verBrowseBtn.addActionListener(e -> browseFile("ver"));
 
        verifyBtn = buildButton("Verify Integrity", ACCENT2, Color.WHITE);
        verifyBtn.setEnabled(false);
        verifyBtn.addActionListener(e -> runVerify());
 
        btnRow.add(verBrowseBtn);
        btnRow.add(verifyBtn);
        panel.add(btnRow);
        panel.add(Box.createVerticalStrut(16));
 
        // Result
        verResultPanel = new JPanel();
        verResultPanel.setLayout(new BoxLayout(verResultPanel, BoxLayout.Y_AXIS));
        verResultPanel.setBackground(PANEL_BG);
        verResultPanel.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(BORDER_C, 1, true),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
        verResultPanel.setVisible(false);
 
        verResultLabel = new JLabel("—");
        verResultLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        verResultLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        verResultPanel.add(verResultLabel);
        verResultPanel.add(Box.createVerticalStrut(12));
 
        JPanel hashRow = new JPanel(new GridLayout(1, 2, 12, 0));
        hashRow.setOpaque(false);
        hashRow.setAlignmentX(Component.LEFT_ALIGNMENT);
 
        verCurrentHashArea     = makeReadonlyHashArea();
        verExpectedHashDisplay = makeReadonlyHashArea();
 
        JPanel leftCol  = labeledHashBox("Current Hash",  verCurrentHashArea);
        JPanel rightCol = labeledHashBox("Expected Hash", verExpectedHashDisplay);
        hashRow.add(leftCol);
        hashRow.add(rightCol);
 
        verResultPanel.add(hashRow);
        verResultPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        verResultPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        panel.add(verResultPanel);
 
        setupDrop(dropZone, "ver");
        return panel;
    }
 
    // ── List Panel ────────────────────────────────────────────────────────────
 
    private JPanel buildListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
 
        JLabel title = styledLabel("TRACKED FILES", MUTED, new Font("JetBrains Mono", Font.PLAIN, 10));
        panel.add(title, BorderLayout.NORTH);
 
        listArea = new JTextArea();
        listArea.setEditable(false);
        listArea.setBackground(SURFACE);
        listArea.setForeground(TEXT);
        listArea.setFont(FONT_MONO);
        listArea.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        listArea.setText("  No files tracked yet.\n  Generate a hash to get started.");
 
        JScrollPane scroll = new JScrollPane(listArea);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_C, 1, true));
        scroll.setBackground(SURFACE);
 
        panel.add(Box.createVerticalStrut(10), BorderLayout.CENTER);
 
        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        center.add(scroll, BorderLayout.CENTER);
 
        JButton refreshBtn = buildSmallButton("↻  Refresh");
        refreshBtn.addActionListener(e -> refreshList());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 8));
        bottom.setOpaque(false);
        bottom.add(refreshBtn);
        center.add(bottom, BorderLayout.SOUTH);
 
        panel.add(center, BorderLayout.CENTER);
        return panel;
    }
 
    // ── Shared Components ─────────────────────────────────────────────────────
 
    private JPanel buildDropZone(String slot) {
        JPanel dz = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // dashed border
                g2.setColor(BORDER_C);
                float[] dash = {6, 5};
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, dash, 0));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 14, 14);
                g2.dispose();
            }
        };
        dz.setOpaque(false);
        dz.setPreferredSize(new Dimension(0, 110));
        dz.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        dz.setAlignmentX(Component.LEFT_ALIGNMENT);
        dz.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        dz.setName(slot);
 
        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
 
        JLabel icon = new JLabel(slot.equals("gen") ? "🗂" : "🔍");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
 
        JLabel msg = new JLabel("Drop a file here, or click Browse");
        msg.setForeground(MUTED);
        msg.setFont(FONT_LABEL);
        msg.setAlignmentX(Component.CENTER_ALIGNMENT);
 
        JLabel hint = new JLabel("Any file type · Processed locally");
        hint.setForeground(new Color(100, 116, 139, 140));
        hint.setFont(FONT_SMALL);
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);
 
        if (slot.equals("gen")) genDropLabel = msg;
        else                    verDropLabel = msg;
 
        inner.add(icon);
        inner.add(Box.createVerticalStrut(6));
        inner.add(msg);
        inner.add(Box.createVerticalStrut(3));
        inner.add(hint);
        dz.add(inner);
        return dz;
    }
 
    private JPanel buildFileInfoPanel(String slot) {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setBackground(SURFACE);
        p.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(BORDER_C, 1, true),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
 
        JLabel icon = new JLabel("📄");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
 
        JLabel name = new JLabel("—");
        name.setForeground(TEXT);
        name.setFont(FONT_MONO_B);
 
        JLabel size = new JLabel("—");
        size.setForeground(MUTED);
        size.setFont(FONT_SMALL);
 
        JPanel meta = new JPanel();
        meta.setOpaque(false);
        meta.setLayout(new BoxLayout(meta, BoxLayout.Y_AXIS));
        meta.add(name);
        meta.add(size);
 
        if (slot.equals("gen")) { genFileNameLabel = name; genFileSizeLabel = size; }
        else                    { verFileNameLabel = name; verFileSizeLabel = size; }
 
        JButton clearBtn = buildSmallButton("✕ Remove");
        clearBtn.addActionListener(e -> clearSelectedFile(slot));
 
        p.add(icon,     BorderLayout.WEST);
        p.add(meta,     BorderLayout.CENTER);
        p.add(clearBtn, BorderLayout.EAST);
        return p;
    }
 
    private JTextArea makeReadonlyHashArea() {
        JTextArea a = new JTextArea(2, 20);
        a.setEditable(false);
        a.setLineWrap(true);
        a.setBackground(BG);
        a.setForeground(MUTED);
        a.setFont(new Font("JetBrains Mono", Font.PLAIN, 11));
        a.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        return a;
    }
 
    private JPanel labeledHashBox(String label, JTextArea area) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel lbl = styledLabel(label.toUpperCase(), MUTED, new Font("JetBrains Mono", Font.PLAIN, 10));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lbl);
        p.add(Box.createVerticalStrut(5));
        JScrollPane sp = new JScrollPane(area);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_C, 1, true));
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(sp);
        return p;
    }
 
    // ── Button Factories ──────────────────────────────────────────────────────
 
    private JButton buildButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (!isEnabled()) g2.setColor(BORDER_C);
                else if (getModel().isPressed()) g2.setColor(bg.darker());
                else if (getModel().isRollover()) g2.setColor(bg.brighter());
                else g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(isEnabled() ? fg : MUTED);
        btn.setBackground(bg);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 22));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
 
    private JButton buildSmallButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("JetBrains Mono", Font.PLAIN, 11));
        btn.setForeground(ACCENT);
        btn.setBackground(new Color(0, 229, 255, 20));
        btn.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 229, 255, 60), 1, true),
            BorderFactory.createEmptyBorder(5, 12, 5, 12)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
 
    private JLabel styledLabel(String text, Color fg, Font font) {
        JLabel l = new JLabel(text);
        l.setForeground(fg);
        l.setFont(font);
        return l;
    }
 
    // ── Drag and Drop Setup ───────────────────────────────────────────────────
 
    private void setupDrop(JPanel dropZone, String slot) {
        new DropTarget(dropZone, DnDConstants.ACTION_COPY, new DropTargetAdapter() {
            @Override public void dragEnter(DropTargetDragEvent e) {
                dropZone.setBackground(new Color(0, 229, 255, 15));
                dropZone.repaint();
            }
            @Override public void dragExit(DropTargetEvent e) {
                dropZone.setBackground(null);
                dropZone.repaint();
            }
            @Override public void drop(DropTargetDropEvent e) {
                try {
                    e.acceptDrop(DnDConstants.ACTION_COPY);
                    @SuppressWarnings("unchecked")
                    java.util.List<File> files = (java.util.List<File>)
                        e.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (!files.isEmpty()) attachFile(slot, files.get(0));
                } catch (Exception ex) { ex.printStackTrace(); }
                dropZone.setBackground(null);
                dropZone.repaint();
            }
        });
 
        // Click also triggers browse
        dropZone.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { browseFile(slot); }
        });
    }
 
    // ── File Operations ───────────────────────────────────────────────────────
 
    private void browseFile(String slot) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select a file");
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            attachFile(slot, fc.getSelectedFile());
        }
    }
 
    private void attachFile(String slot, File f) {
        selectedFile = f;
        String name = f.getName();
        String size = formatSize(f.length());
 
        if (slot.equals("gen")) {
            genFileNameLabel.setText(name);
            genFileSizeLabel.setText(size);
            genFileInfoPanel.setVisible(true);
            genHashBtn.setEnabled(true);
            genResultPanel.setVisible(false);
        } else {
            verFileNameLabel.setText(name);
            verFileSizeLabel.setText(size);
            verFileInfoPanel.setVisible(true);
            verifyBtn.setEnabled(true);
            verResultPanel.setVisible(false);
        }
        revalidate(); repaint();
    }
 
    private void clearSelectedFile(String slot) {
        selectedFile = null;
        if (slot.equals("gen")) {
            genFileInfoPanel.setVisible(false);
            genHashBtn.setEnabled(false);
            genResultPanel.setVisible(false);
        } else {
            verFileInfoPanel.setVisible(false);
            verifyBtn.setEnabled(false);
            verResultPanel.setVisible(false);
        }
        revalidate(); repaint();
    }
 
    // ── Generate Action ───────────────────────────────────────────────────────
 
    private void runGenerate() {
        if (selectedFile == null) return;
        genHashBtn.setText("Computing…");
        genHashBtn.setEnabled(false);
 
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override protected String doInBackground() throws Exception {
                return generateSHA256(selectedFile);
            }
            @Override protected void done() {
                try {
                    String hash = get();
                    genHashArea.setText(hash);
                    saveHash(selectedFile.getName(), hash);
                    genStatusLabel.setText("✔  Hash saved to " + HASH_STORE);
                    genStatusLabel.setForeground(SAFE);
                    genResultPanel.setVisible(true);
                    refreshList();
                } catch (Exception ex) {
                    genStatusLabel.setText("✘  Error: " + ex.getMessage());
                    genStatusLabel.setForeground(WARN);
                    genResultPanel.setVisible(true);
                }
                genHashBtn.setText("Generate SHA-256");
                genHashBtn.setEnabled(true);
                revalidate(); repaint();
            }
        };
        worker.execute();
    }
 
    // ── Verify Action ─────────────────────────────────────────────────────────
 
    private void runVerify() {
        if (selectedFile == null) return;
        String expected = verExpectedArea.getText().trim().toLowerCase();
        if (expected.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please paste the expected hash into the text area.",
                "Missing Hash", JOptionPane.WARNING_MESSAGE);
            return;
        }
 
        verifyBtn.setText("Verifying…");
        verifyBtn.setEnabled(false);
 
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override protected String doInBackground() throws Exception {
                return generateSHA256(selectedFile);
            }
            @Override protected void done() {
                try {
                    String current = get();
                    verCurrentHashArea.setText(current);
                    verExpectedHashDisplay.setText(expected);
 
                    boolean match = current.equals(expected);
                    if (match) {
                        verResultLabel.setText("✔  INTEGRITY VERIFIED — File is unchanged");
                        verResultLabel.setForeground(SAFE);
                        verCurrentHashArea.setForeground(SAFE);
                        verExpectedHashDisplay.setForeground(SAFE);
                    } else {
                        verResultLabel.setText("⚠  WARNING — File has been MODIFIED!");
                        verResultLabel.setForeground(WARN);
                        verCurrentHashArea.setForeground(WARN);
                        verExpectedHashDisplay.setForeground(ACCENT);
                    }
                    verResultPanel.setVisible(true);
                } catch (Exception ex) {
                    verResultLabel.setText("✘  Error: " + ex.getMessage());
                    verResultLabel.setForeground(WARN);
                    verResultPanel.setVisible(true);
                }
                verifyBtn.setText("Verify Integrity");
                verifyBtn.setEnabled(true);
                revalidate(); repaint();
            }
        };
        worker.execute();
    }
 
    // ── List Refresh ──────────────────────────────────────────────────────────
 
    private void refreshList() {
        File store = new File(HASH_STORE);
        if (!store.exists()) {
            listArea.setText("  No files tracked yet.\n  Generate a hash to get started.");
            return;
        }
 
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(store))) {
            int count = 0;
            String name;
            while ((name = reader.readLine()) != null) {
                String hash = reader.readLine();
                String ts   = reader.readLine();
                reader.readLine(); // "---"
                if (name != null && hash != null) {
                    count++;
                    sb.append("  ").append(count).append(".  ").append(name).append("\n");
                    sb.append("      ").append(hash, 0, Math.min(40, hash.length())).append("...\n");
                    if (ts != null && !ts.equals("---"))
                        sb.append("      Saved: ").append(ts).append("\n");
                    sb.append("\n");
                }
            }
            if (count == 0) sb.append("  No entries found.");
        } catch (IOException e) {
            sb.append("  Error reading ").append(HASH_STORE).append(": ").append(e.getMessage());
        }
        listArea.setText(sb.toString());
        listArea.setCaretPosition(0);
    }
 
    // ── Core SHA-256 ──────────────────────────────────────────────────────────
 
    public static String generateSHA256(File file)
            throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }
        byte[] hashBytes = digest.digest();
        StringBuilder hex = new StringBuilder();
        for (byte b : hashBytes) hex.append(String.format("%02x", b));
        return hex.toString();
    }
 
    // ── Persistence ───────────────────────────────────────────────────────────
 
    private static void saveHash(String fileName, String hash) throws IOException {
        File store = new File(HASH_STORE);
        StringBuilder sb = new StringBuilder();
        boolean replaced = false;
 
        if (store.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(store))) {
                String name;
                while ((name = reader.readLine()) != null) {
                    String storedHash = reader.readLine();
                    String ts         = reader.readLine();
                    reader.readLine(); // "---"
                    if (name.equals(fileName)) {
                        appendEntry(sb, fileName, hash);
                        replaced = true;
                    } else if (storedHash != null) {
                        sb.append(name).append("\n").append(storedHash).append("\n")
                          .append(ts).append("\n").append("---").append("\n");
                    }
                }
            }
        }
        if (!replaced) appendEntry(sb, fileName, hash);
 
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(store, false))) {
            writer.write(sb.toString());
        }
    }
 
    private static void appendEntry(StringBuilder sb, String name, String hash) {
        String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        sb.append(name).append("\n").append(hash).append("\n")
          .append(ts).append("\n").append("---").append("\n");
    }
 
    // ── Utils ─────────────────────────────────────────────────────────────────
 
    private static String formatSize(long bytes) {
        if (bytes < 1024)    return bytes + " B";
        if (bytes < 1048576) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.2f MB", bytes / 1048576.0);
    }
 
    // ── Main ──────────────────────────────────────────────────────────────────
 
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new FileIntegrityCheckerGUI();
        });
    }
}
