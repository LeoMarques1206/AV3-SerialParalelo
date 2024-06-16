package com.example;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CsvChart extends JFrame {

    public CsvChart(String title) {
        super(title);

        CategoryDataset dataset = createDataset();

        JFreeChart chart = ChartFactory.createBarChart(
                "Performance Comparativa", 
                "Método", 
                "Tempo de Execução (ms)", 
                dataset, 
                PlotOrientation.VERTICAL,
                true, 
                true, 
                false 
        );

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));
        setContentPane(chartPanel);
    }

    private CategoryDataset createDataset() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    
        String[] csvFiles = {"cpu_paralelo_csv.csv", "cpu_serial_csv.csv", "gpu_paralelo_csv.csv"};
        for (String csvFile : csvFiles) {
            try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
                String line;
                boolean firstLine = true;
                while ((line = br.readLine()) != null) {
                    if (firstLine) {
                        firstLine = false;
                        continue; 
                    }
                    String[] fields = line.split(",");
                    
                    // Verifique se a linha tem o número esperado de colunas
                    if (fields.length < 4) {
                        System.err.println("Linha inválida no arquivo " + csvFile + ": " + line);
                        continue;
                    }
                    
                    String metodo = fields[0];
                    String palavra = fields[1];
                    int count = Integer.parseInt(fields[2]);
                    long tempo = Long.parseLong(fields[3]);
    
                    dataset.addValue(tempo, metodo, palavra);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    
        return dataset;
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CsvChart example = new CsvChart("Comparação de Performance");
            example.setSize(800, 600);
            example.setLocationRelativeTo(null);
            example.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            example.setVisible(true);
        });
    }
}
