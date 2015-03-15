package GUI.progressgui;

import javax.swing.*;
import java.awt.*;
import java.lang.ref.WeakReference;
import java.lang.reflect.MalformedParametersException;

public class ProgressBarPanel extends JPanel implements ProgressFrame {

    public interface ProgressCompleteListener {
        void progressComplete();
    }

    private int max = 0, value = 0;
    private WeakReference<ProgressCompleteListener> progressCompleteListenerWeakRef;
    private String process = "";

    public ProgressBarPanel(ProgressCompleteListener listener) {
        max = -1;
        value = 0;
        progressCompleteListenerWeakRef = new WeakReference<>(listener);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        if (max != -1) {
            Dimension size = this.getSize();

            Graphics2D g2 = (Graphics2D) g;

            int x = 5, y = 5;
            int w = size.width - 10, h = size.height - 10;

            g2.drawRect(x, y, w, h);
            g2.setColor(Color.lightGray);
            g2.fillRect(x + 1, y + 1, (int) ((w - 2) * getPercentComplete()), h - 2);

            // draw string
            g2.setColor(Color.BLACK);
            drawCenteredString(getText(), size.width, size.height, g2);
        }
    }

    public String getText() {
        if (value == 0) {
            return "Starting...";
        }
        if (value == max) {
            return "Done!";
        }
        return process + ": " + (int) Math.floor(getPercentComplete() * 100) + "%";
    }

    protected void drawCenteredString(String s, int w, int h, Graphics g) {
        FontMetrics fm = g.getFontMetrics();
        int x = (w - fm.stringWidth(s)) / 2;
        int y = (fm.getAscent() + (h - (fm.getAscent() + fm.getDescent())) / 2);
        g.drawString(s, x, y);
    }

    @Override
    public void setProcess(String process) {
        this.process = process;
        updateUI();
    }

    @Override
    public void setMax(int max) {
        if (max == 0) throw new MalformedParametersException("Max must be greater than 0");

        this.value = 0;
        this.max = max;
        updateUI();
    }

    @Override
    public void setProgress(int progress) {
        this.value = progress;
        updateUI();
    }

    @Override
    public void setDone() {
        ProgressCompleteListener listener = progressCompleteListenerWeakRef.get();
        if (listener != null) {
            listener.progressComplete();
        }
        value = max;
        updateUI();
    }

    @Override
    public void increment() {
        if (value < max) {
            ++value;
            updateUI();
        }
    }

    public double getPercentComplete() {
        return value / (double) max;
    }
}
