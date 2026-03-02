package ht.heist.plugins.breakhandler.ui;

import ht.heist.services.breakhandler.BreakHandler;
import ht.heist.services.breakhandler.Break;
import net.runelite.client.RuneLite;
import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class BreakHandlerPanel extends PluginPanel {
    private BreakHandler breakHandler = RuneLite.getInjector().getInstance(BreakHandler.class);
    private final JPanel breaksContainer;
    private final JLabel noBreaksLabel;
    private final JLabel fatigueLabel;

    public BreakHandlerPanel() {

        setLayout(new BorderLayout(0, 6));
        setBackground(new Color(40, 40, 40));

        JLabel title = new JLabel("Heist Break Handler");
        title.setForeground(Color.WHITE);
        title.setFont(getFont().deriveFont(Font.BOLD, 14f));
        title.setBorder(BorderFactory.createEmptyBorder(6, 6, 4, 6));

        add(title, BorderLayout.NORTH);

        breaksContainer = new JPanel();
        breaksContainer.setLayout(new BoxLayout(breaksContainer, BoxLayout.Y_AXIS));
        breaksContainer.setBackground(new Color(45, 45, 45));
        breaksContainer.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));

        noBreaksLabel = new JLabel("No break is scheduled");
        noBreaksLabel.setForeground(Color.LIGHT_GRAY);
        noBreaksLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        breaksContainer.add(noBreaksLabel);
        add(breaksContainer, BorderLayout.CENTER);

        JPanel southPanel = new JPanel();
        southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
        southPanel.setBackground(new Color(40, 40, 40));

        fatigueLabel = new JLabel("Fatigue: 0%");
        fatigueLabel.setForeground(Color.YELLOW);
        fatigueLabel.setFont(getFont().deriveFont(11f));
        fatigueLabel.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        southPanel.add(fatigueLabel);

        BreakSettingsPanel settingsPanel = new BreakSettingsPanel();
        southPanel.add(settingsPanel);

        add(southPanel, BorderLayout.SOUTH);

        refreshBreakList();
    }

    public void refreshBreakList() {
        breaksContainer.removeAll();

        List<Break> breaks = getActiveBreaks();
        if (breaks.isEmpty()) {
            breaksContainer.add(noBreaksLabel);
        } else {
            for (Break b : breaks) {
                breaksContainer.add(new BreakPanel(b));
                breaksContainer.add(Box.createVerticalStrut(4));
            }
        }

        breaksContainer.revalidate();
        breaksContainer.repaint();

        int fatiguePct = (int) (breakHandler.getFatigueLevel() * 100);
        fatigueLabel.setText("Fatigue: " + fatiguePct + "%");
    }

    private List<Break> getActiveBreaks() {
        return breakHandler.getAllBreaks();
    }

}
