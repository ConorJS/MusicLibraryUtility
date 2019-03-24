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
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
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

    public void clickCalculateButton() {
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

    public void clickToggleButton() {
        this.duplicateCheck = !this.duplicateCheck;
        this.calculateButton.setText(this.duplicateCheck ? "Find duplicates" : "  Find uniques ");
    }

    public void performTrackSearch(String path) {
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

        for (List<File> occurences : songOccurences.values()) {

            if (((occurences.size() > 1) && (this.duplicateCheck)) | ((occurences.size() == 1) && (!this.duplicateCheck))) {
                String songName = MetadataUtil.readMetadata(occurences.get(0));

                this.appendUIText("<b>" + ((songName.toString().length() > 0) ? songName : "[unnamed song]") + " has "
                        + occurences.size() + " occurence(s)</b>", true);
                for (File occurrence : occurences) {
                    this.appendUIText("\t" + trimRootFromPath(occurrence.toString(), path), true);
                }

                this.appendUIText("", true);
            }
        }

        // Old HashMap access implementation
        /*for (String songName : songOccurences.keySet()) {
            System.out.println(count);
            count++;
            if ((songOccurences.get(songName).size() > 1) && songName != null) {
                this.appendUIText(((songName.toString().length() > 0) ? songName : "[unnamed song]") + " has "
                        + songOccurences.get(songName).size() + " occurence(s)", true);
                for (File occurrence : songOccurences.get(songName)) {
                    this.appendUIText("\t" + trimRootFromPath(occurrence.toString(), path), true);
                }

                this.appendUIText("", true);
            }
        }*/
    }

    public void clearTextOfUI() {
        this.setTextOfUI("");
    }


    public void setTextOfUI(String text) {
        this.uiText = text;

        this.containerMiddleText_Label.setText("<html>" + this.uiText + "</html>");
    }

    public void appendUIText(Object text, boolean newLine) {
        String str = text.toString();
        this.setTextOfUI(this.uiText + (newLine ? "<br>" : "") + str);
    }

    public String trimRootFromPath(String fullPath, String root) {
        String trimmed = fullPath;

        if (fullPath.startsWith(root)) {
            trimmed = fullPath.substring(root.length());
        }

        return trimmed;
    }

}
