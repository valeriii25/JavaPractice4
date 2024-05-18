import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class BuildingUI extends JFrame {
    private final Building building;
    private final JPanel[] liftPanels;

    public BuildingUI(Building building) {
        this.building = building;
        this.liftPanels = new JPanel[building.lifts.size()];
        setTitle("Lifts");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(1, 3));
        for (int i = 0; i < building.lifts.size(); i++) {
            liftPanels[i] = createLiftPanel(i + 1);
        }
        add(liftPanels[0]);
        add(createControlPanel());
        add(liftPanels[1]);
    }

    private JPanel createLiftPanel(int liftId) {
        var panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder("Lift " + liftId));
        panel.setLayout(new GridLayout(10, 1));
        for (int i = Building.UpperFloor; i >= Building.LowerFloor; i--) {
            JLabel label = new JLabel("Floor " + i);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            panel.add(label);
        }
        return panel;
    }

    private JPanel createControlPanel() {
        var panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder("Control Panel"));
        panel.setLayout(new GridLayout(2, 1));
        var floorComboBox = createFloorSelect();
        var requestButton = createRequestButton(floorComboBox);
        panel.add(floorComboBox);
        panel.add(requestButton);
        return panel;
    }

    private JButton createRequestButton(JComboBox<Integer> floorComboBox) {
        var searchIcon = new ImageIcon("src/lupa4.png");
        var requestButton = new JButton("Call lift", searchIcon);
        requestButton.addActionListener(e -> {
            if (floorComboBox.getSelectedItem() == null) return;
            int floor = (int) floorComboBox.getSelectedItem();
            building.chooseSuitableLift(floor);
        });
        requestButton.setBackground(Color.GREEN);
        return requestButton;
    }

    private static JComboBox<Integer> createFloorSelect() {
        var floorComboBox = new JComboBox<Integer>();
        for (int i = Building.LowerFloor; i <= Building.UpperFloor; i++) {
            floorComboBox.addItem(i);
        }
        floorComboBox.updateUI();
        return floorComboBox;
    }

    public void manageLiftChanges(int elevatorId, int floor, LiftStatus state) {
        SwingUtilities.invokeLater(() -> {
            var panel = liftPanels[elevatorId - 1];
            for (var component : panel.getComponents()) {
                if (component instanceof JLabel label) {
                    if (label.getText().contains("Floor")) {
                        label.setBackground(null);
                        label.setOpaque(false);
                    }
                }
            }
            var currentLabel = (JLabel) panel.getComponent(Building.UpperFloor - floor);
            currentLabel.setBackground(getColor(state));
            currentLabel.setOpaque(true);
        });
    }

    private static Color getColor(LiftStatus state) {
        if (Objects.requireNonNull(state) == LiftStatus.FREE) {
            return Color.GREEN;
        } else if (state == LiftStatus.UP) {
            return Color.CYAN;
        }
        return Color.BLUE;
    }
}
