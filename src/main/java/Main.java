import org.apache.commons.lang3.tuple.Pair;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    private static String sourcePath;
    private static String destinationPath;


    public static void main(String[] args) {
//arumenty wywołania np. C://Users//martb/Desktop//obrazki//1 C://Users//martb/Desktop//obrazki//2
        //          source                                destination
        long time = System.currentTimeMillis();
        sourcePath = "";
        destinationPath = "";
        if (args.length >= 1) sourcePath = args[0];
        if (args.length > 1) destinationPath = args[1];

        List<Path> files =null;
        Path source = Path.of(sourcePath);
        try (Stream<Path> stream = Files.list(source)) {
            files = stream.collect(Collectors.toList());
        } catch (IOException e) {
        }



        ForkJoinPool pool = new ForkJoinPool(4);
        try {
            List<Path> finalFiles = files;
            pool.submit(() -> {
                Stream<Path> stream1 = finalFiles.stream().parallel();
                Stream<Pair<String,BufferedImage>> stream2 = stream1.map(Main::toPair);
                Stream<Pair<String,BufferedImage>> stream3 = stream2.map(Main::toGray);
                stream3.forEach(stringBufferedImagePair -> {
                    try {
                        save(stringBufferedImagePair);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }).get();
        } catch (InterruptedException | ExecutionException e) {
        }

        System.out.println(System.currentTimeMillis() - time);

    }

    private static void save(Pair<String, BufferedImage> stringBufferedImagePair) throws IOException {

        String namePath = destinationPath+"\\"+stringBufferedImagePair.getLeft()+".jpg";

        ImageIO.write(stringBufferedImagePair.getRight(),"jpg",new File(namePath));

    }


    private static Pair<String,BufferedImage> toGray(Pair<String, BufferedImage> stringBufferedImagePair) {
            BufferedImage original = stringBufferedImagePair.getRight();
            BufferedImage image = new BufferedImage(original.getWidth(), original.getHeight(), original.getType());
            for (int i = 0; i < original.getWidth(); i++) {
                for (int j = 0; j < original.getHeight(); j++) {
                    int rgb = original.getRGB(i, j);
                    Color color = new Color(rgb);
                    int red = (int) (0.299 * color.getRed() +0.114  * color.getBlue()+0.587 * color.getGreen());
                    int blue = (int) (0.299 * color.getRed() +0.114  * color.getBlue()+0.587 * color.getGreen());
                    int green = (int) (0.299 * color.getRed() +0.114  * color.getBlue()+0.587 * color.getGreen());
                    Color outColor = new Color(red, blue, green);
                    int outRgb = outColor.getRGB();
                    image.setRGB(i, j, outRgb);
                }
            }
      return Pair.of(stringBufferedImagePair.getLeft(),image);
    }
    
    
    private static Pair<String,BufferedImage> toPair(Path path){
        try{
            BufferedImage image = ImageIO.read(path.toFile());
            String name = path.getFileName().toString();
            return(Pair.of(name, image));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Pair.of(null,null);
    }

}
