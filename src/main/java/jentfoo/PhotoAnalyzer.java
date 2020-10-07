package jentfoo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Stream;

import org.threadly.concurrent.CentralThreadlyPool;
import org.threadly.concurrent.SchedulerService;
import org.threadly.concurrent.future.FutureUtils;
import org.threadly.concurrent.future.ListenableFuture;
import org.threadly.util.ExceptionUtils;
import org.threadly.util.Pair;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

public class PhotoAnalyzer {
  public static void main(String[] args) {
    PhotoAnalyzer pa = new PhotoAnalyzer(CentralThreadlyPool.computationPool(), args[0]);
    
    pa.analyze();
    System.out.println("DONE!!");
  }

  private final SchedulerService scheduler;
  private final File root;
  private final Set<ListenableFuture<?>> waitingFutures = 
      Collections.newSetFromMap(new ConcurrentHashMap<>());
  private final LongAdder totalImages = new LongAdder();
  private final List<LensConfig> lensStats;
  
  public PhotoAnalyzer(SchedulerService scheduler, String path) {
    this.scheduler = scheduler;
    this.root = new File(path);
    
    if (! root.exists()) {
      throw new IllegalArgumentException("Path does not exist: " + path);
    }
    lensStats = new ArrayList<>(AnalyzerConfig.LENS_FOCAL_LENGTHS.length);
    for (Pair<Integer, Integer> p : AnalyzerConfig.LENS_FOCAL_LENGTHS) {
      lensStats.add(new LensConfig(p.getLeft(), p.getRight()));
    }
  }
  
  protected Stream<File> imageStream() {
    return FileCrawler.crawl(root, AnalyzerConfig::isOriginalImageFile);
  }
  
  public void analyze() {
    imageStream().forEach((f) -> {
      crawlThrottle();
      
      ListenableFuture<?> lf = scheduler.submit(() -> { analyzeImage(f); return null; });
      waitingFutures.add(lf);
      lf.listener(() -> waitingFutures.remove(lf));
      lf.failureCallback(this::handleFailure);
    });
    
    ListenableFuture<?> finishFuture = FutureUtils.makeCompleteFuture(waitingFutures);
    try {
      finishFuture.get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    } catch (ExecutionException e) {
      throw ExceptionUtils.makeRuntime(e.getCause());
    }
    
    long totalImages = this.totalImages.sum();
    for (LensConfig lc : lensStats) {
      System.out.println("Lens " + lc.wideMM + "-" + lc.teleMM + "mm -" + 
                            " tooWide:" + lc.tooWideCount.sum() + 
                            " \tmatch:" + lc.matchCount.sum() + 
                            " (" + ((lc.matchCount.sum() * 100) / totalImages) + "%)" + 
                            " \ttooTele:" + lc.tooTeleCount.sum());
    }
  }
  
  protected void analyzeImage(File f) throws ImageProcessingException, IOException {
    Metadata metadata = ImageMetadataReader.readMetadata(f);
    int imageFocalLength = -1;
    exifSearch: for (Directory directory : metadata.getDirectories()) {
      if (directory.getName().equalsIgnoreCase("Exif SubIFD")) {
        for (Tag tag : directory.getTags()) {
          if (tag.getTagName().equalsIgnoreCase("Focal Length")) {
            String focalLengthStr = tag.getDescription();
            int delim = focalLengthStr.indexOf(' ');
            if (delim > 0) {
              focalLengthStr = focalLengthStr.substring(0, delim);
            }
            imageFocalLength = Integer.parseInt(focalLengthStr);
            break exifSearch;
          }
        }
      }
    }
    
    if (imageFocalLength < 0) {
      throw new IllegalStateException("Failed to read image: " + f);
    }
    
    handleAnalyzedImage(imageFocalLength);
  }
  
  protected void handleAnalyzedImage(int focalLength) {
    totalImages.increment();
    
    for (LensConfig lc : lensStats) {
      lc.witnessFocalLength(focalLength);
    }
  }
  
  protected void handleFailure(Throwable t) {
    synchronized (this) {
      System.err.println("Failure analyzing image");
      t.printStackTrace(System.err);
    }
    
    System.exit(Math.abs(t.getClass().getName().hashCode()));
  }

  protected void crawlThrottle() {
    while (waitingFutures.size() > 2000) {
      try {
        ListenableFuture<?> lf = FutureUtils.makeFirstResultFuture(waitingFutures, false);
        if (lf.isDone()) {
          Thread.sleep(100);
        } else {
          lf.get(200, TimeUnit.MILLISECONDS);
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException(e);
      } catch (ExecutionException | TimeoutException e) {
        // retry
      }
    }
  }
  
  private static class LensConfig {
    private final int wideMM;
    private final int teleMM;
    private final LongAdder matchCount = new LongAdder();
    private final LongAdder tooWideCount = new LongAdder();
    private final LongAdder tooTeleCount = new LongAdder();

    public LensConfig(int wideMM, int teleMM) {
      this.wideMM = wideMM;
      this.teleMM = teleMM;
    }
    
    public void witnessFocalLength(int mm) {
      if (mm < wideMM) {
        tooWideCount.increment();
      } else if (mm > teleMM) {
        tooTeleCount.increment();
      } else {
        matchCount.increment();
      }
    }
  }
}
