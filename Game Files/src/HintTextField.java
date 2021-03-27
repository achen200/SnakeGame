package src;
import javax.swing.border.*;
import javax.swing.*;
import java.awt.*;

public class HintTextField extends JTextField {
    private static final long serialVersionUID = 1L;
    private static final Border line = BorderFactory.createLineBorder(Color.DARK_GRAY);
    private static final Border empty = new EmptyBorder(0, 20, 0, 0);
    private static final CompoundBorder border = new CompoundBorder(line, empty);

    public HintTextField(String hint) {
        setMargin(new Insets(0,20,0,0));
        setAlignmentX(Component.CENTER_ALIGNMENT);
        setOpaque(false);
        setForeground(MenuButton.darkGreen);
        setFont(GameCanvas.hsFont);
        setMaximumSize(new Dimension(280, 50));
        setBorder(border);
        _hint = hint;
    }
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (getText().length() == 0) {
            int h = getHeight();
            ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            Insets ins = getInsets();
            FontMetrics fm = g.getFontMetrics();
            int c0 = getBackground().getRGB();
            int c1 = getForeground().getRGB();
            int m = 0xfefefefe;
            int c2 = ((c0 & m) >>> 1) + ((c1 & m) >>> 1);
            g.setColor(new Color(c2, true));
            g.drawString(_hint, ins.left, h / 2 + fm.getAscent() / 2 - 2);
        }
    }
    private final String _hint;
}