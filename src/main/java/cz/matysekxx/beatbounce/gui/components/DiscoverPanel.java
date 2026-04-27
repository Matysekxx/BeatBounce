package cz.matysekxx.beatbounce.gui.components;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.matysekxx.beatbounce.api.AudiusClient;
import cz.matysekxx.beatbounce.gui.screen.ScreenManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.concurrent.CompletableFuture;

public class DiscoverPanel extends JPanel {
    private final JPanel songListPanel;
    private final AudiusClient audiusClient;
    private final ObjectMapper objectMapper;
    private final ScreenManager screenManager;

    public DiscoverPanel(AudiusClient audiusClient, ObjectMapper objectMapper, ScreenManager screenManager) {
        this.audiusClient = audiusClient;
        this.objectMapper = objectMapper;
        this.screenManager = screenManager;

        this.setLayout(new BorderLayout());
        this.setOpaque(false);
        this.setBorder(new EmptyBorder(0, 50, 0, 50));

        final JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(40, 0, 30, 0));
        headerPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        
        final SearchField searchField = new SearchField("Search Tracks...");
        searchField.addActionListener(e -> performSearch(searchField.getText()));
        searchField.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerPanel.add(searchField);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        headerPanel.add(createCategoriesPanel(searchField));

        this.add(headerPanel, BorderLayout.NORTH);

        songListPanel = new JPanel();
        songListPanel.setLayout(new BoxLayout(songListPanel, BoxLayout.Y_AXIS));
        songListPanel.setOpaque(false);

        this.add(createTransparentScrollPane(songListPanel), BorderLayout.CENTER);

        performTrendingSearch("allTime", null);
    }

    private JPanel createCategoriesPanel(SearchField searchField) {
        final JPanel categoriesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        categoriesPanel.setOpaque(false);
        categoriesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        addCategoryButton(categoriesPanel, "All-Time Hits", () -> {
            searchField.setText("");
            performTrendingSearch("allTime", null);
        });
        
        addCategoryButton(categoriesPanel, "Trending Month", () -> {
            searchField.setText("");
            performTrendingSearch("month", null);
        });

        addCategoryButton(categoriesPanel, "Electronic", () -> {
            searchField.setText("");
            performTrendingSearch("allTime", "Electronic");
        });

        addCategoryButton(categoriesPanel, "Hip-Hop/Rap", () -> {
            searchField.setText("");
            performTrendingSearch("allTime", "Hip-Hop/Rap");
        });
        
        addCategoryButton(categoriesPanel, "Pop", () -> {
            searchField.setText("");
            performTrendingSearch("allTime", "Pop");
        });

        addCategoryButton(categoriesPanel, "World", () -> {
            searchField.setText("");
            performTrendingSearch("allTime", "World");
        });
        return categoriesPanel;
    }
    
    private void addCategoryButton(JPanel panel, String title, Runnable action) {
        final JButton catBtn = new CategoryButton(title);
        catBtn.addActionListener(e -> action.run());
        panel.add(catBtn);
    }

    private JScrollPane createTransparentScrollPane(JPanel listPanel) {
        final JPanel wrapperPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 15));
        wrapperPanel.setOpaque(false);
        wrapperPanel.add(listPanel);

        final JScrollPane scrollPane = new JScrollPane(wrapperPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUI(new ScrollBarUI());
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        scrollPane.getVerticalScrollBar().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(25);
        
        scrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 0));
        return scrollPane;
    }
    
    private void showLoading() {
        songListPanel.removeAll();
        final JLabel loadingLabel = new JLabel("Loading tracks...", SwingConstants.LEFT);
        loadingLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));
        loadingLabel.setForeground(Color.GRAY);
        songListPanel.add(loadingLabel);
        songListPanel.revalidate();
        songListPanel.repaint();
    }
    private void performSearch(String query) {
        if (query == null || query.trim().isEmpty()) return;
        showLoading();
        executeFuture(audiusClient.searchTracks(query));
    }
    private void performTrendingSearch(String time, String genre) {
        showLoading();
        
        CompletableFuture<String> future;
        if (genre != null) {
            future = audiusClient.getTrendingTracksByGenre(genre, time);
        } else {
            future = audiusClient.getTrendingTracks(time);
        }
        
        executeFuture(future);
    }
    
    private void executeFuture(CompletableFuture<String> future) {
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return future.get();
            }

            @Override
            protected void done() {
                try {
                    populateResults(get());
                } catch (Exception e) {
                    songListPanel.removeAll();
                    final JLabel error = new JLabel("Connection failed. Please check your internet.", SwingConstants.LEFT);
                    error.setForeground(new Color(255, 100, 100));
                    error.setFont(new Font("SansSerif", Font.PLAIN, 16));
                    songListPanel.add(error);
                    songListPanel.revalidate();
                    songListPanel.repaint();
                }
            }
        }.execute();
    }

    private void populateResults(String jsonResponse) {
        songListPanel.removeAll();
        try {
            final JsonNode rootNode = objectMapper.readTree(jsonResponse);
            final JsonNode dataNode = rootNode.path("data");

            if (dataNode.isMissingNode() || dataNode.isEmpty()) {
                final JLabel empty = new JLabel("No tracks found.", SwingConstants.LEFT);
                empty.setForeground(Color.GRAY);
                empty.setFont(new Font("SansSerif", Font.PLAIN, 16));
                songListPanel.add(empty);
            } else {
                int index = 0;
                for (JsonNode trackNode : dataNode) {
                    final String id = trackNode.path("id").asText();
                    final String title = trackNode.path("title").asText();
                    final String artist = trackNode.path("user").path("name").asText("Unknown Artist");

                    songListPanel.add(new TrackCard(id, title, artist, audiusClient, screenManager, index++));
                    songListPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing: " + e.getMessage());
        }
        songListPanel.revalidate();
        songListPanel.repaint();
    }
}
