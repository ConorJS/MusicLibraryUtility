package util;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

import java.io.File;
import java.io.IOException;

public class MetadataUtil {

    /**
     * Read audio track title from a given file
     */
    public static String readMetadata(File file) {
        String songName = null;

        try {
            AudioFile audioFile = AudioFileIO.read(file);

            Tag tag = audioFile.getTag();
            for (FieldKey key : FieldKey.values()) {
                if (key == FieldKey.TITLE){
                    songName = tag.getFirst(key);
                }
            }

        } catch (CannotReadException e) {
            if (e.getMessage().contains("with this extension")) {
                System.out.println("Can't read file: " + e.getMessage().split("ion:")[1]);
            }
        } catch (IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException e) {
            e.printStackTrace();

        } finally {
            return songName;
        }

    }
}
