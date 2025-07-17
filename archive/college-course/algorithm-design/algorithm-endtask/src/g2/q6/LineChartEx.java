package g2.q6;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class LineChartEx extends JFrame {

  public LineChartEx(Map<? extends Number, Double> map, String title) {
    XYDataset dataset = createDataset(map);
    JFreeChart chart = createChart(dataset, title);
    ChartPanel chartPanel = new ChartPanel(chart);
    chartPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
    chartPanel.setBackground(Color.white);
    add(chartPanel);

    pack();
    setTitle(title);
    setLocationRelativeTo(null);
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
  }

  private XYDataset createDataset(Map<? extends Number, Double> map) {
    XYSeries series = new XYSeries("graph");
    for (Entry<? extends Number, Double> entry : map.entrySet()) {
      series.add(entry.getKey().doubleValue(), entry.getValue());
    }

    XYSeriesCollection dataset = new XYSeriesCollection();
    dataset.addSeries(series);

    return dataset;
  }

  private JFreeChart createChart(XYDataset dataset, String title) {
    JFreeChart chart = ChartFactory.createXYLineChart("Param vs. Has Loop", "param", "has loop",
        dataset, PlotOrientation.VERTICAL, true, true, false);

    XYPlot plot = chart.getXYPlot();

    XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
    renderer.setSeriesPaint(0, Color.RED);
    renderer.setSeriesStroke(0, new BasicStroke(2.0f));

    plot.setRenderer(renderer);
    plot.setBackgroundPaint(Color.white);

    plot.setRangeGridlinesVisible(true);
    plot.setRangeGridlinePaint(Color.BLACK);

    plot.setDomainGridlinesVisible(true);
    plot.setDomainGridlinePaint(Color.BLACK);

    chart.getLegend().setFrame(BlockBorder.NONE);

    chart.setTitle(new TextTitle(title, new Font("Serif", java.awt.Font.BOLD, 18)));

    return chart;
  }

}
