import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class clock {
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame("Digital + Analog Watch");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setContentPane(new WatchPanel());
			frame.setSize(900, 560);
			frame.setMinimumSize(new java.awt.Dimension(700, 460));
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		});
	}

	private static class WatchPanel extends JPanel {
		private static final Color BACKGROUND = new Color(13, 19, 34);
		private static final Color FACE = new Color(23, 32, 52);
		private static final Color MUTED = new Color(151, 166, 190);
		private static final Color ACCENT = new Color(72, 220, 190);
		private static final DateTimeFormatter DIGITAL_TIME = DateTimeFormatter.ofPattern("hh:mm:ss a");
		private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
		private final Timer timer;


		WatchPanel() {
			setBackground(BACKGROUND);
			setLayout(new BorderLayout());
			timer = new Timer(50, event -> repaint());
			timer.start();

			JPanel controls = new JPanel();
			controls.setBackground(BACKGROUND);
			JButton stopButton = new JButton("Stop");
			stopButton.addActionListener(event -> {
				if (timer.isRunning()) {
					timer.stop();
					stopButton.setText("Resume");
				} else {
					timer.start();
					stopButton.setText("Stop");
				}
			});
			controls.add(stopButton);
			add(controls, BorderLayout.SOUTH);
		}

		@Override
		protected void paintComponent(Graphics graphics) {
			super.paintComponent(graphics);
			Graphics2D g = (Graphics2D) graphics.create();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			int width = getWidth();
			int height = getHeight();
			int displayHeight = height - 48;
			int centerX = (int) (width * 0.30);
			int centerY = displayHeight / 2;
			int radius = Math.min((int) (width * 0.235), (int) (displayHeight * 0.39));

			drawAnalogClock(g, centerX, centerY, radius);
			drawDigitalClock(g, width, displayHeight);
			g.dispose();
		}

		private void drawAnalogClock(Graphics2D g, int cx, int cy, int radius) {
			g.setColor(new Color(0, 0, 0, 65));
			g.fillOval(cx - radius - 4, cy - radius + 8, radius * 2 + 8, radius * 2 + 8);
			g.setColor(ACCENT);
			g.setStroke(new BasicStroke(3f));
			g.drawOval(cx - radius, cy - radius, radius * 2, radius * 2);
			g.setColor(FACE);
			g.fillOval(cx - radius + 5, cy - radius + 5, (radius - 5) * 2, (radius - 5) * 2);

			for (int mark = 0; mark < 60; mark++) {
				double angle = Math.toRadians(mark * 6 - 90);
				boolean major = mark % 5 == 0;
				int inner = radius - (major ? 22 : 12);
				int outer = radius - 8;
				g.setColor(major ? Color.WHITE : new Color(112, 132, 160));
				g.setStroke(new BasicStroke(major ? 3f : 1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
				g.draw(new Line2D.Double(
					cx + Math.cos(angle) * inner,
					cy + Math.sin(angle) * inner,
					cx + Math.cos(angle) * outer,
					cy + Math.sin(angle) * outer
				));
			}

			g.setFont(new Font("SansSerif", Font.BOLD, Math.max(13, radius / 9)));
			g.setColor(new Color(224, 233, 246));
			FontMetrics metrics = g.getFontMetrics();
			for (int number = 1; number <= 12; number++) {
				double angle = Math.toRadians(number * 30 - 90);
				String label = Integer.toString(number);
				int x = (int) (cx + Math.cos(angle) * (radius * 0.72) - metrics.stringWidth(label) / 2.0);
				int y = (int) (cy + Math.sin(angle) * (radius * 0.72) + metrics.getAscent() / 3.0);
				g.drawString(label, x, y);
			}

			LocalDateTime now = LocalDateTime.now();
			double second = now.getSecond() + now.getNano() / 1_000_000_000.0;
			double minute = now.getMinute() + second / 60.0;
			double hour = (now.getHour() % 12) + minute / 60.0;
			drawHand(g, cx, cy, hour * 30 - 90, radius * 0.48, 6f, Color.WHITE);
			drawHand(g, cx, cy, minute * 6 - 90, radius * 0.68, 4f, ACCENT);
			drawHand(g, cx, cy, second * 6 - 90, radius * 0.78, 1.7f, new Color(255, 115, 125));
			g.setColor(new Color(255, 115, 125));
			g.fillOval(cx - 6, cy - 6, 12, 12);
		}

		private void drawHand(Graphics2D g, int cx, int cy, double degrees, double length, float thickness, Color color) {
			double angle = Math.toRadians(degrees);
			g.setColor(color);
			g.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			g.draw(new Line2D.Double(cx, cy,
				cx + Math.cos(angle) * length,
				cy + Math.sin(angle) * length));
		}

		private void drawDigitalClock(Graphics2D g, int width, int height) {
			int left = (int) (width * 0.58);
			int centerY = height / 2;
			LocalDateTime now = LocalDateTime.now();

			g.setColor(MUTED);
			g.setFont(new Font("SansSerif", Font.BOLD, 15));
			g.drawString("LOCAL TIME", left, centerY - 70);

			g.setColor(Color.WHITE);
			g.setFont(new Font("Monospaced", Font.BOLD, Math.max(38, Math.min(62, width / 13))));
			g.drawString(now.format(DIGITAL_TIME), left, centerY + 5);

			g.setColor(ACCENT);
			g.fillRoundRect(left, centerY + 30, 56, 4, 4, 4);

			g.setColor(MUTED);
			g.setFont(new Font("SansSerif", Font.PLAIN, 16));
			g.drawString(now.format(DATE), left, centerY + 70);
		}
	}
}
