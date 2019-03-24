package gui;

import util.FileHandlerUtil;
import util.MetadataUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class UI {

    private final JFrame windowFrame = new JFrame("Music Library Utility");
    private final JPanel containerMiddleText = new JPanel();
    private final JScrollPane containerMiddle_ScrollableWrapper = new JScrollPane(containerMiddleText);
    private final JPanel containerBottom = new JPanel();
    private final JLabel containerMiddleText_Label = new JLabel();
    private final JTextField pathInputField = new JTextField(48);
    private final JButton calculateButton = new JButton("Find duplicates");
    private final JButton toggleButton = new JButton("T");

    private boolean duplicateCheck = true; // true: check for duplicates; false: check for uniques

    private String uiText = "";

    public UI() { // set up
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");

        } catch (ClassNotFoundException | InstantiationException | UnsupportedLookAndFeelException |
                IllegalAccessException e) {

            e.printStackTrace();
        }

        this.windowFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.windowFrame.setMinimumSize(new Dimension(850, 375));

        this.containerMiddleText_Label.setFont(new Font("Segoe UI", 0, 12));
        this.containerMiddleText.add(this.containerMiddleText_Label, BorderLayout.NORTH);
        this.containerMiddle_ScrollableWrapper.getVerticalScrollBar().setUnitIncrement(24);
        this.windowFrame.getContentPane().add(this.containerMiddle_ScrollableWrapper, BorderLayout.CENTER);

        this.calculateButton.setFont(new Font("Segoe UI", 0, 14));
        this.calculateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clickCalculateButton();
            }
        });

        this.toggleButton.setFont(new Font("Segoe UI", 1, 14));
        this.toggleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clickToggleButton();
            }
        });


        this.windowFrame.add(this.containerBottom, BorderLayout.SOUTH);
        this.containerBottom.add(pathInputField, BorderLayout.WEST);
        this.containerBottom.add(calculateButton, BorderLayout.CENTER);
        this.containerBottom.add(toggleButton, BorderLayout.EAST);
    }

    public void showUI() {
        this.windowFrame.pack();
        this.windowFrame.setVisible(true);
    }

    private void clickCalculateButton() {
        String tryPath = pathInputField.getText();

        File tryFile = new File(tryPath);

        this.clearTextOfUI();
        if (tryFile.exists()) {

            if (tryFile.isDirectory()) {
                performTrackSearch(tryPath);

            } else {
                this.appendUIText("Path: \"" + tryPath + "\" is not a directory.", true);

            }
        } else {
            this.appendUIText("Path: \"" + tryPath + "\" doesn't exist.", true);

        }
    }

    private void clickToggleButton() {
        this.duplicateCheck = !this.duplicateCheck;
        this.calculateButton.setText(this.duplicateCheck ? "Find duplicates" : "  Find uniques ");
    }

    private void performTrackSearch(String path) {
        java.util.List<File> musicFiles = FileHandlerUtil.getAllFiles(path, true);

        Map<String, List<File>> songOccurences = new HashMap<>();
        List<String> musicFileTypes = new ArrayList<>();
        List<String> otherFileTypes = new ArrayList<>();

        List<String> testOutputLines = new ArrayList<>();

        int duplicatesCount = 0;
        int uniquesCount = musicFiles.size();
        int unnamedCount = 0;
        int nonMusicCount = 0;
        for (File file : musicFiles) {

            String[] chunks = file.getPath().split("\\.");
            String extension = chunks[chunks.length-1];

            String songName = MetadataUtil.readMetadata(file);

            if (songName != null) {

                String uppercaseSongName = songName.toUpperCase();

                if (!songOccurences.containsKey(uppercaseSongName)) {
                    songOccurences.put(uppercaseSongName, new ArrayList<>());
                    if (uppercaseSongName.length() == 0) {
                        unnamedCount++;
                        uniquesCount--;
                    }

                } else if (uppercaseSongName.length() == 0) {
                    unnamedCount++;
                    uniquesCount--; // cant tell if an unnamed song is a duplicate, track separately

                } else {
                    // cheap way to track total duplicates/uniques
                    duplicatesCount++;
                    uniquesCount--;
                }

                songOccurences.get(uppercaseSongName).add(file);

                if (!musicFileTypes.contains(extension)) {
                    musicFileTypes.add(extension);
                }

            } else {
                uniquesCount--;
                nonMusicCount++;

                if (!otherFileTypes.contains(extension)) {
                    otherFileTypes.add(extension);
                }
            }
        }

        for (String line : testOutputLines) {
            this.appendUIText(line, true);
        }

        String musicFileTypeLine = "";
        for (int i = 0; i < musicFileTypes.size(); i++) {
            musicFileTypeLine += musicFileTypes.get(i) + (i == musicFileTypes.size()-1 ? "" : ", ");
        }

        String otherFileTypeLine = "";
        for (int i = 0; i < otherFileTypes.size(); i++) {
            otherFileTypeLine += otherFileTypes.get(i) + (i == otherFileTypes.size()-1 ? "" : ", ");
        }

        this.appendUIText("<b>Total songs: " + (musicFiles.size()-nonMusicCount), true);
        this.appendUIText("Total unique songs: " + uniquesCount, true);
        this.appendUIText("Total duplicates: " + duplicatesCount, true);
        this.appendUIText("Total unnamed songs: " + unnamedCount, true);
        this.appendUIText("Total non-music files: " + nonMusicCount, true);
        this.appendUIText("", true);
        this.appendUIText("Music file types: " + musicFileTypeLine, true);
        this.appendUIText("Non-music file types: " + otherFileTypeLine, true);
        this.appendUIText("</b>", true);

        // order the song name list
        List<String> songNamesSorted = songOccurences.keySet().stream()
                .sorted()
                .collect(Collectors.toList());

        // A song ending with a section in parentheses - tolerant of trailing whitespace
        Pattern pattern = Pattern.compile("^(.*)\\((.*)\\) *$");

        for (String songName : songNamesSorted) {
            List<File> occurrences = songOccurences.get(songName);

            // if a song ends with something in parentheses e.g -> (feat. Some Artist),
            // then check if we have a song by the same name, without the parentheses ending
            Matcher parenthesesEndingMatcher = pattern.matcher(songName);
            boolean possibleModifiedSong = false;
            if (parenthesesEndingMatcher.matches()) {
                String trueSongName = parenthesesEndingMatcher.group(1).trim();
                possibleModifiedSong = songOccurences.containsKey(trueSongName);

            }

            if (((occurrences.size() > 1) && (this.duplicateCheck)) | ((occurrences.size() == 1) && (!this.duplicateCheck))) {

                String caseCorrectSongName = MetadataUtil.readMetadata(occurrences.get(0));

                this.appendUIText(((caseCorrectSongName.length() > 0) ? caseCorrectSongName : "[unnamed song]") + " has "
                        + occurrences.size() + " occurence(s)",
                        true, true,
                        possibleModifiedSong ? Color.RED : Color.BLACK
                );

                for (File occurrence : occurrences) {
                    this.appendUIText("\t" + trimRootFromPath(occurrence.toString(), path), true);
                }

                this.appendUIText("", true);
            }
        }
    }

    private void clearTextOfUI() {
        this.setTextOfUI("");
    }

    // TODO: Refactor these three methods - API is too busy
    private void setTextOfUI(String text) {
        this.uiText = text;

        this.containerMiddleText_Label.setText("<html>" + this.uiText + "</html>");
    }

    private void appendUIText(Object text, boolean newLine) {
        String str = text.toString();
        this.setTextOfUI(this.uiText + (newLine ? "<br>" : "") + str);
    }

    private void appendUIText(Object text, boolean newLine, boolean isBold, Color color) {
        StringBuilder bob = new StringBuilder();

        if (isBold) {
            bob.append("<b");
        }

        if (!color.equals(Color.BLACK)) {
            if (!isBold) {
                bob.append("<");
            } else {
                bob.append(" ");
            }

            bob.append("style=\"color: rgb(")
                    .append(color.getRed()).append(",")
                    .append(color.getGreen()).append(",")
                    .append(color.getBlue()).append(")\"");
        }

        bob.append(">");

        bob.append(text);

        if (isBold) {
            bob.append("</b>");
        }

        appendUIText(bob.toString(), newLine);
    }

    private String trimRootFromPath(String fullPath, String root) {
        String trimmed = fullPath;

        if (fullPath.startsWith(root)) {
            trimmed = fullPath.substring(root.length());
        }

        return trimmed;
    }
}
