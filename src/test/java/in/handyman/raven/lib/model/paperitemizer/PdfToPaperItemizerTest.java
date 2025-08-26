package in.handyman.raven.lib.model.paperitemizer;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
class PdfToPaperItemizerTest {
    @Test
    public void testPdfToPaperItemizerVersion1() {
        String inputFilePath = "/194-files/";

        List<PaperItemizerOutputTable> paperItemizerOutputTables = new ArrayList<>();
        List<PaperItemizerInputTable> paperItemizerInputTables = generateInputTablesFromFolder(inputFilePath);

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("paper.itemizer.image.type.rgb", "true");
        actionExecutionAudit.getContext().put("extraction.preprocess.paper.itemizer.processing.paper.count", "100");
        actionExecutionAudit.getContext().put("extraction.preprocess.paper.itemizer.processing.paper.limiter.activator", "false");
        actionExecutionAudit.getContext().put("paper.itemizer.resize.width", "2550");
        actionExecutionAudit.getContext().put("paper.itemizer.resize.height", "3301");
        actionExecutionAudit.getContext().put("paper.itemizer.output.format", "jpg");
        actionExecutionAudit.getContext().put("paper.itemizer.file.dpi", "300");
        actionExecutionAudit.getContext().put("paper.itemization.resize.activator", "false");
        actionExecutionAudit.getContext().put("paper.itemizer.consumer.API.count", "7");
        actionExecutionAudit.getContext().put("document_type", "UM_FAX");

        // Record start time
        long startTime = System.currentTimeMillis();

        paperItemizerInputTables.forEach(paperItemizerInputTable -> {
            PdfToPaperItemizer pdfToPaperItemizer = new PdfToPaperItemizer(actionExecutionAudit, log);
            List<PaperItemizerOutputTable> pdfItemizedOutputs = pdfToPaperItemizer.paperItemizer(paperItemizerInputTable.getFilePath(), paperItemizerInputTable.getOutputDir(), paperItemizerInputTable);
            paperItemizerOutputTables.addAll(pdfItemizedOutputs);
        });

        // Calculate and log execution time
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        // Convert milliseconds to minutes and seconds
        long minutes = TimeUnit.MILLISECONDS.toMinutes(executionTime);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(executionTime) - TimeUnit.MINUTES.toSeconds(minutes);


        System.out.println("Total input files " + paperItemizerInputTables.size());
        System.out.println("Total output files   " + paperItemizerOutputTables.size());
        System.out.println("Total execution time " + minutes + "mins " + seconds + "secs");
    }
// Output:
//Total input files 216
//Total output files 2111
//Total execution time milliseconds 479620
// Total execution time 8mins 0secs

//Total input files 216
//Total output files   2111
//Total execution time 8mins 22secs


    @Test
    public void testPdfToPaperItemizerWithExecutorService() {
        String inputFilePath = "/input/";
        int nThreads = 20;
        List<PaperItemizerOutputTable> paperItemizerOutputTables = new ArrayList<>();
        List<PaperItemizerInputTable> paperItemizerInputTables = generateInputTablesFromFolder(inputFilePath);

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("paper.itemizer.image.type.rgb", "true");
        actionExecutionAudit.getContext().put("extraction.preprocess.paper.itemizer.processing.paper.count", "100");
        actionExecutionAudit.getContext().put("extraction.preprocess.paper.itemizer.processing.paper.limiter.activator", "false");
        actionExecutionAudit.getContext().put("paper.itemizer.resize.width", "2550");
        actionExecutionAudit.getContext().put("paper.itemizer.resize.height", "3301");
        actionExecutionAudit.getContext().put("paper.itemizer.output.format", "jpg");
        actionExecutionAudit.getContext().put("paper.itemizer.file.dpi", "300");
        actionExecutionAudit.getContext().put("paper.itemization.resize.activator", "false");
        actionExecutionAudit.getContext().put("paper.itemizer.consumer.API.count", "7");
        actionExecutionAudit.getContext().put("document_type", "UM_FAX");

        // Executor service to process files in parallel

        ExecutorService executorService = Executors.newFixedThreadPool(nThreads); // Adjust the pool size as needed
        List<Future<List<PaperItemizerOutputTable>>> futures = new ArrayList<>();

        // Record start time
        long startTime = System.currentTimeMillis();

        // Submit tasks to executor service
        for (PaperItemizerInputTable paperItemizerInputTable : paperItemizerInputTables) {
            futures.add(executorService.submit(() -> {
                PdfToPaperItemizer pdfToPaperItemizer = new PdfToPaperItemizer(actionExecutionAudit, log);
                return pdfToPaperItemizer.paperItemizer(paperItemizerInputTable.getFilePath(), paperItemizerInputTable.getOutputDir(), paperItemizerInputTable);
            }));
        }

        // Wait for all tasks to complete and collect results
        futures.forEach(future -> {
            try {
                List<PaperItemizerOutputTable> pdfItemizedOutputs = future.get();
                paperItemizerOutputTables.addAll(pdfItemizedOutputs);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });

        // Shutdown executor service
        executorService.shutdown();

        // Calculate and log execution time
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        // Convert milliseconds to minutes and seconds
        long minutes = TimeUnit.MILLISECONDS.toMinutes(executionTime);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(executionTime) - TimeUnit.MINUTES.toSeconds(minutes);

        // Print results
        System.out.println("Total input files: " + paperItemizerInputTables.size());
        System.out.println("Total output files: " + paperItemizerOutputTables.size());
        System.out.println("Total execution time: " + minutes + " mins " + seconds + " secs");
    }

// Output: version 2.x
//Total input files: 216
//Total output files: 2111
//Total execution time: 5 mins 31 secs(10 threads)

//Total input files: 216
//Total output files: 2105
//Total execution time: 6 mins 3 secs(20 threads)

//-Xms2g -Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:ParallelGCThreads=8 -Xlog:gc*:file=gc.log
//Total input files: 216
//Total output files: 2109
//Total execution time: 6 mins 16 secs(20 threads)

//-Xms12g -Xmx20g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:ParallelGCThreads=12 -Xlog:gc*:file=gc.log
//Total input files: 216
//Total output files: 2111
//Total execution time: 4 mins 36 secs(10 threads)

//-Xms12g -Xmx20g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:ParallelGCThreads=12 -Xlog:gc*:file=gc.log
//Total input files: 216
//Total output files: 2111
//Total execution time: 5 mins 4 secs(20 threads)

    //version 3.0.5
//
    @Test
    public void testPdfToPaperItemizerVersion2() {
        String inputFilePath = "/194-files/";

        List<PaperItemizerOutputTable> paperItemizerOutputTables = new ArrayList<>();
        List<PaperItemizerInputTable> paperItemizerInputTables = generateInputTablesFromFolder(inputFilePath);

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("paper.itemizer.image.type.rgb", "true");
        actionExecutionAudit.getContext().put("extraction.preprocess.paper.itemizer.processing.paper.count", "100");
        actionExecutionAudit.getContext().put("extraction.preprocess.paper.itemizer.processing.paper.limiter.activator", "false");
        actionExecutionAudit.getContext().put("paper.itemizer.resize.width", "2550");
        actionExecutionAudit.getContext().put("paper.itemizer.resize.height", "3301");
        actionExecutionAudit.getContext().put("paper.itemizer.output.format", "jpg");
        actionExecutionAudit.getContext().put("paper.itemizer.file.dpi", "300");
        actionExecutionAudit.getContext().put("paper.itemization.resize.activator", "false");
        actionExecutionAudit.getContext().put("paper.itemizer.consumer.API.count", "7");
        actionExecutionAudit.getContext().put("document_type", "UM_FAX");

        // Record start time
        long startTime = System.currentTimeMillis();

        paperItemizerInputTables.forEach(paperItemizerInputTable -> {
            PdfToPaperItemizer pdfToPaperItemizer = new PdfToPaperItemizer(actionExecutionAudit, log);
            List<PaperItemizerOutputTable> pdfItemizedOutputs = pdfToPaperItemizer.paperItemizer(paperItemizerInputTable.getFilePath(), paperItemizerInputTable.getOutputDir(), paperItemizerInputTable);
            paperItemizerOutputTables.addAll(pdfItemizedOutputs);
        });

        // Calculate and log execution time
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        // Convert milliseconds to minutes and seconds
        long minutes = TimeUnit.MILLISECONDS.toMinutes(executionTime);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(executionTime) - TimeUnit.MINUTES.toSeconds(minutes);


        System.out.println("Total input files " + paperItemizerInputTables.size());
        System.out.println("Total output files   " + paperItemizerOutputTables.size());
        System.out.println("Total execution time " + minutes + "mins " + seconds + "secs");

    }


    @Test
    public void paperItemizerPdfBox() throws IOException {
        String inputFilePath = "/input/FM202505151413047.pdf";
//        String inputFileName = "FM202505151413047";
            String inputFileName=getFileNameFromPath(inputFilePath);
        byte[] byteArray = getFileContent(inputFilePath);
        convertPdfToImagesById(byteArray, inputFileName, "jpg", 300);

    }

    @Test
    public void paperItemizerPdfBoxWithExecutorSubmit() throws IOException, InterruptedException, ExecutionException {
        String inputFilePath = "/194-files/";
        List<PaperItemizerInputTable> paperItemizerInputTables = generateInputTablesFromFolder(inputFilePath);

        // Create an ExecutorService with a fixed thread pool
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        // List to hold all CompletableFutures
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // Iterate over each PaperItemizerInputTable and submit each task to executor
        for (PaperItemizerInputTable inputTable : paperItemizerInputTables) {
            // Submit task to executor service
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    // Process each inputTable and inputFilePath
                    itemisePdfIntoPapers(inputTable.getFilePath());
                } catch (IOException e) {
                    e.printStackTrace();  // Log error (consider using a logging framework)
                }
            }, executorService);

            // Add the future to the list
            futures.add(future);
        }

        // Wait for all tasks to complete
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allOf.get();  // Block until all tasks are finished

        // Shut down the executor
        executorService.shutdown();
        if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
            executorService.shutdownNow();
        }
    }

    private void itemisePdfIntoPapers(String inputFilePath) throws IOException {
        byte[] byteArray = getFileContent(inputFilePath);
        String inputFileName=getFileNameFromPath(inputFilePath);
        convertPdfToImagesById(byteArray, inputFileName, "jpg", 300);
    }

    public static String getFileNameFromPath(String filePath) {
        // Convert the file path string to a Path object
        Path path = Paths.get(filePath);

        // Get the file name from the Path object (the last part of the path)
        String fileName = path.getFileName().toString();

        return fileName;
    }
    public void convertPdfToImagesById(byte[] fileContent, String originalName, String format, int dpi) throws IOException {
        List<ItemizedOuputFile> itemizedOutputFiles=new ArrayList<>();

        try (PDDocument document = Loader.loadPDF(fileContent)) {
            PDFRenderer renderer = new PDFRenderer(document);
            String normalizedFormat = format.toUpperCase();
            if ("JPEG".equals(normalizedFormat)) normalizedFormat = "JPG";

            for (int i = 0; i < document.getNumberOfPages(); i++) {
                BufferedImage image = renderer.renderImageWithDPI(i, dpi, ImageType.RGB);
                String fileName = removeExtension(originalName) + "_page_" + (i + 1) + "." + normalizedFormat.toLowerCase();
                Path outputPath = createOutputFile(fileName,originalName);
                ItemizedOuputFile itemizedOuputFile=new ItemizedOuputFile();
                itemizedOuputFile.setImage(image);
                itemizedOuputFile.setNormalizedFormat(normalizedFormat);
                itemizedOuputFile.setOutputPath(outputPath.toFile());
                itemizedOutputFiles.add(itemizedOuputFile);
//                writeImage(itemizedOuputFile);

            }
        }
        writeImagesSingleShot(itemizedOutputFiles);

    }

    private static void writeImagesSingleShot(List<ItemizedOuputFile> itemizedOutputFiles) throws IOException {
        ExecutorService executorService = Executors.newFixedThreadPool(10); // Size of threads based on the number of images
        List<Future<Void>> futures = new ArrayList<>();

        for (ItemizedOuputFile item : itemizedOutputFiles) {
            // Submit each write operation as a separate task
            futures.add(executorService.submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    writeImage(item);
                    return null;
                }
            }));
        }

        // Wait for all tasks to complete
        for (Future<Void> future : futures) {
            try {
                future.get(); // Ensures all tasks are completed
            } catch (Exception e) {
                e.printStackTrace(); // Handle any exception that occurs in individual tasks
            }
        }

        executorService.shutdown(); // Shutdown executor service
    }

    // Helper method to write a single image
    private static void writeImage(ItemizedOuputFile item) throws IOException {
        if (!ImageIO.write(item.getImage(), item.getNormalizedFormat(), item.getOutputPath())) {
            throw new IOException("Unable to write image in format: " + item.getNormalizedFormat());
        }

        File savedFile = item.getOutputPath();
        if (!savedFile.exists() || savedFile.length() == 0) {
            throw new IOException("File is empty or not written: " + savedFile.getName());
        }

        System.out.println("Filename: " + savedFile.getName() + ", Size: " + savedFile.length() + " bytes");
    }

    // Data class to hold image info
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ItemizedOuputFile {
        private BufferedImage image;
        private String normalizedFormat;
        private File outputPath;
    }
    private Path createOutputFile(String fileName,String originalFileName) throws IOException {
        Path outputDir = Paths.get("/output/", "processed",originalFileName);
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }
        return outputDir.resolve(fileName);
    }

    private static String removeExtension(final String fileName) {
        if (fileName.indexOf(".") > 0) {
            return fileName.substring(0, fileName.lastIndexOf("."));
        } else {
            return fileName;
        }
    }

    public byte[] getFileContent(String filePathInput) throws IOException {

        Path filePath = Paths.get(filePathInput);
        return Files.readAllBytes(filePath);
    }

    public static List<PaperItemizerInputTable> generateInputTablesFromFolder(String folderPath) {
        List<PaperItemizerInputTable> inputTables = new ArrayList<>();
        File folder = new File(folderPath);
        File[] files = folder.listFiles();
        long processId = System.currentTimeMillis();
        int groupId = 1;
        long tenantId = 100L;
        String templateId = UUID.randomUUID().toString();
        String outputDir = folderPath + File.separator + "output";
        long rootPipelineId = processId + 1000;
        String batchId = "batch-1";

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    PaperItemizerInputTable table = PaperItemizerInputTable.builder()
                            .originId(UUID.randomUUID().toString())
                            .processId(processId)
                            .groupId(groupId)
                            .tenantId(tenantId)
                            .templateId(templateId)
                            .filePath(file.getAbsolutePath())
                            .outputDir(outputDir)
                            .rootPipelineId(rootPipelineId)
                            .batchId(batchId)
                            .createdOn(new Timestamp(System.currentTimeMillis()))
                            .build();
                    inputTables.add(table);
                    groupId++;
                }
            }
        }
        return inputTables;
    }


    @Test
    public void paperItemizerPdfBoxWithExecutorLatest() throws Exception {
        long startTime = System.currentTimeMillis(); // start timing
        AtomicInteger totalFilesProcessed = new AtomicInteger(0);
        AtomicInteger totalPagesProcessed = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(
                Math.max(2, Runtime.getRuntime().availableProcessors())
        );

        String inputDir = "/194-files/";
        List<PaperItemizerInputTable> inputs = generateInputTablesFromFolder(inputDir);

        // Submit one task per file
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (PaperItemizerInputTable in : inputs) {
            futures.add(CompletableFuture.runAsync(() -> {
                try {
                    int pages = itemisePdfIntoPapers_streaming(in.getFilePath());
                    totalPagesProcessed.addAndGet(pages);
                    totalFilesProcessed.incrementAndGet();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }, executor));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        executor.shutdown();
        executor.awaitTermination(60, TimeUnit.SECONDS);

        long totalTimeMillis = System.currentTimeMillis() - startTime;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(totalTimeMillis);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(totalTimeMillis) % 60;

        System.out.printf(
                "✅ Processed %d files with %d total pages in %d min %d sec%n",
                totalFilesProcessed.get(),
                totalPagesProcessed.get(),
                minutes,
                seconds
        );
    }


    // 2) Stream pages: render -> write -> flush -> discard
    private int itemisePdfIntoPapers_streaming(String pdfPath) throws IOException {
        Path path = Paths.get(pdfPath);
        int pageCount;
        // Use temp-file backed memory to prevent heap blowups
        var mem = MemoryUsageSetting.setupMixed(64 * 1024 * 1024); // 64MB heap, rest temp files
        try (PDDocument document =Loader.loadPDF(Files.readAllBytes(path))) {
            PDFRenderer renderer = new PDFRenderer(document);
            renderer.setSubsamplingAllowed(true); // less memory & faster

            String originalName = getFileNameFromPath(pdfPath);
            String normalizedFormat = "JPG"; // prefer JPG for smaller outputs
            int dpi = 300;
                int resizeWidth = 2550;
                int resizeHeight = 3301;
            pageCount = document.getNumberOfPages();

            for (int i = 0; i < document.getNumberOfPages(); i++) {
                // Render a single page, then write & free it
                BufferedImage image = renderer.renderImageWithDPI(i, dpi, ImageType.RGB);
                BufferedImage bufferedImage= resizeImage(image, resizeWidth, resizeHeight);
                String fileName = removeExtension(originalName) + "_page_" + (i + 1) + ".jpg";
                Path out = createOutputFile(fileName, originalName);
                System.out.println("File name "+fileName+" Writing page " + (i + 1));

                // Write immediately, no list accumulation
                if (!ImageIO.write(bufferedImage, normalizedFormat, out.toFile())) {
                    throw new IOException("Failed to write " + out);
                }

                image.flush();         // free native buffers
                image = null;
                bufferedImage.flush();         // free native buffers
                bufferedImage = null; // help GC
            }
        }
        return pageCount;
    }
    private static BufferedImage resizeImage(BufferedImage originalImage, int width, int height) {
        BufferedImage resizedImage = new BufferedImage(width, height, originalImage.getType());
        Graphics2D graphics = resizedImage.createGraphics();
        graphics.drawImage(originalImage, 0, 0, width, height, null);
        graphics.dispose();
        return resizedImage;
    }


    @Test
    public void testPdfToPaperItemizerWithExecutorServiceLatest() {
        String inputFilePath = "/194-files";
        int nThreads = 10;
        List<PaperItemizerOutputTable> paperItemizerOutputTables = new ArrayList<>();
        List<PaperItemizerInputTable> paperItemizerInputTables = generateInputTablesFromFolder(inputFilePath);

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("paper.itemizer.image.type.rgb", "true");
        actionExecutionAudit.getContext().put("extraction.preprocess.paper.itemizer.processing.paper.count", "100");
        actionExecutionAudit.getContext().put("extraction.preprocess.paper.itemizer.processing.paper.limiter.activator", "false");
        actionExecutionAudit.getContext().put("paper.itemizer.resize.width", "2550");
        actionExecutionAudit.getContext().put("paper.itemizer.resize.height", "3301");
        actionExecutionAudit.getContext().put("paper.itemizer.output.format", "jpg");
        actionExecutionAudit.getContext().put("paper.itemizer.file.dpi", "300");
        actionExecutionAudit.getContext().put("paper.itemization.resize.activator", "false");
        actionExecutionAudit.getContext().put("paper.itemizer.consumer.API.count", "7");
        actionExecutionAudit.getContext().put("document_type", "UM_FAX");

        // Executor service to process files in parallel

        ExecutorService executorService = Executors.newFixedThreadPool(nThreads); // Adjust the pool size as needed
        List<Future<List<PaperItemizerOutputTable>>> futures = new ArrayList<>();

        // Record start time
        long startTime = System.currentTimeMillis();

        // Submit tasks to executor service
        for (PaperItemizerInputTable paperItemizerInputTable : paperItemizerInputTables) {
            futures.add(executorService.submit(() -> {
                PdfItemizerWithStreaming pdfToPaperItemizer = new PdfItemizerWithStreaming(actionExecutionAudit, log);
                return pdfToPaperItemizer.paperItemizer(paperItemizerInputTable.getFilePath(), paperItemizerInputTable.getOutputDir(), paperItemizerInputTable);
            }));
        }

        // Wait for all tasks to complete and collect results
        futures.forEach(future -> {
            try {
                List<PaperItemizerOutputTable> pdfItemizedOutputs = future.get();
                paperItemizerOutputTables.addAll(pdfItemizedOutputs);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });

        // Shutdown executor service
        executorService.shutdown();

        // Calculate and log execution time
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        // Convert milliseconds to minutes and seconds
        long minutes = TimeUnit.MILLISECONDS.toMinutes(executionTime);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(executionTime) - TimeUnit.MINUTES.toSeconds(minutes);

        // Print results
        System.out.println("Total input files: " + paperItemizerInputTables.size());
        System.out.println("Total output files: " + paperItemizerOutputTables.size());
        System.out.println("Total execution time: " + minutes + " mins " + seconds + " secs");
    }

}