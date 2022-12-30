package com.ramankevich.fileimportmanager.services;

import com.ramankevich.fileimportmanager.db.DbHelper;
import com.ramankevich.fileimportmanager.exceptions.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

@Service
public class FileManagerService {

    @Autowired
    DbHelper dbHelper;

    private static final String DELIMITER = ";";
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z]{1,15}$");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<String[]> getRecords() {
        return dbHelper.getAllRecords();
    }

    public String processFiles(MultipartFile[] files) {
        List<String[]> records = Collections.synchronizedList(new ArrayList<>());
        ExecutorService executor = Executors.newFixedThreadPool(files.length);
        AtomicInteger threadCounter = new AtomicInteger(files.length);
        List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());


        for (int i = 0; i < files.length; i++) {
            int finalI = i;
            executor.submit(() -> {
                try {
                    String fileExtension = files[finalI].getOriginalFilename().substring(files[finalI].getOriginalFilename().lastIndexOf(".") + 1);
                    if (fileExtension.equals("csv") || fileExtension.equals("txt")) {
                        records.addAll(processTxtCSVFiles(files[finalI]));
                    } else if (fileExtension.equals("xml")) {
                        records.addAll(processXMLFile(files[finalI]));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    exceptions.add(e);
                    throw new RuntimeException("Exception during parsing the file! " + files[finalI].getOriginalFilename());
                }
                threadCounter.decrementAndGet();
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new CustomException(500, "The process was interrupted because it took more than 5 minutes!");
        }

        if (threadCounter.get() != 0) {
            StringBuilder builder = new StringBuilder();
            for (Exception ex : exceptions) {
                builder.append(ex.getMessage());
                builder.append("\n");
            }
            throw new CustomException(500, builder.toString());
        }

        dbHelper.insertRecords(records);

        return "Records where inserted!";

    }

    public List<String[]> processTxtCSVFiles(MultipartFile file) {
        List<String[]> records = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(DELIMITER);
                String name = parts[0].trim();
                String dateString = parts[1].trim();
                String expertise = parts[2].trim();

                if (!NAME_PATTERN.matcher(name).matches()) {
                    throw new CustomException(500, "Invalid name: " + name);
                }
                if (expertise.length() > 50) {
                    throw new CustomException(500, "Expertise too long: " + expertise);
                }

                try {
                    LocalDateTime.parse(dateString, DATE_FORMATTER);
                } catch (Exception e) {
                    throw new CustomException(500, "Invalid date: " + dateString);
                }
                records.add(new String[]{name, dateString, expertise});
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(500, "Error processing file: " + file.getOriginalFilename() + "\n" + e.getMessage());
        }

        return records;
    }

    public List<String[]> processXMLFile(MultipartFile file) {
        List<String[]> records = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream()) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(inputStream);
            Element rootElement = document.getDocumentElement();
            NodeList recordElements = rootElement.getElementsByTagName("record");

            for (int i = 0; i < recordElements.getLength(); i++) {
                Element recordElement = (Element) recordElements.item(i);

                Element nameElement = (Element) recordElement.getElementsByTagName("name").item(0);
                Element dateCreationElement = (Element) recordElement.getElementsByTagName("date_creation").item(0);
                Element expertiseElement = (Element) recordElement.getElementsByTagName("expertise").item(0);

                String name = nameElement.getTextContent().trim();
                if (!NAME_PATTERN.matcher(name).matches()) {
                    throw new CustomException(500, "Invalid name: " + name);
                }

                String dateString = dateCreationElement.getTextContent().trim();
                try {
                    LocalDateTime.parse(dateString, DATE_FORMATTER);
                } catch (Exception e) {
                    throw new CustomException(500, "Invalid date: " + dateString);
                }

                String expertise = expertiseElement.getTextContent().trim();
                if (expertise.length() > 50) {
                    throw new CustomException(500, "Expertise too long: " + expertise);
                }

                records.add(new String[]{name, dateString, expertise});
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(500, "Error processing file: " + file.getOriginalFilename() + "\n" + e.getMessage());
        }

        return records;
    }

}
