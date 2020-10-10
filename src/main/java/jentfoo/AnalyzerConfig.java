package jentfoo;

import java.io.File;

import org.threadly.util.Pair;

public class AnalyzerConfig {
  public static final boolean ANALYZE_ONLY_EDITED = false;
  // within this MM, photos will be considered a lens match even if technically it was outside the range
  public static final int MM_ALLOWED_VARIANCE = 10;
  @SuppressWarnings({"unchecked", "rawtypes"})
  public static final Pair<Integer, Integer>[] LENS_FOCAL_LENGTHS = 
      new Pair[] { new Pair<>(18, 135), new Pair<>(55, 250), 
                   new Pair<>(35, 150), new Pair<>(70, 200), 
                   new Pair<>(24, 24), new Pair<>(50, 50), 
                   new Pair<>(85, 85), new Pair<>(105, 105), 
                   new Pair<>(135, 135), new Pair<>(200, 200) };
  
  public static boolean isOriginalImageFile(File f) {
    String name = f.getName();
    return name.startsWith("IMG") && name.endsWith(".JPG") && ! name.contains("edit");
  }
  
  public static boolean isEditedImageFile(File f) {
    String name = f.getName();
    return name.startsWith("IMG") && name.contains("edit") && 
              (name.endsWith(".jpg") || name.endsWith(".JPG"));
  }
}
