package com.ramankevich.fileimportmanager.services;

import com.ramankevich.fileimportmanager.exceptions.CustomException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class FileManagerServiceTest {

    @InjectMocks
    FileManagerService fileManagerService;

    @Test
    public void testProcessTxtCSVFiles_InvalidName() {
        String csv = "invalid name;2022-01-01 00:00:00;Software development\n" +
                "Jane Doe;2022-01-01 00:00:00;Data analysis";
        MultipartFile file = new MockMultipartFile("file", "records.csv", "text/csv", csv.getBytes());

        try {
            fileManagerService.processTxtCSVFiles(file);
            fail("Expected CustomException to be thrown");
        } catch (CustomException e) {
            assertEquals(500, e.getErrorCode());
            assertEquals("Error processing file: records.csv\n" +
                    "Invalid name: invalid name", e.getMessage());
        }
    }

    @Test
    public void testProcessXMLFile_ValidInput() throws IOException {
        // Create a dummy XML file with multiple records
        String xml = "<root>" +
                "  <record>" +
                "    <name>John</name>" +
                "    <date_creation>2022-01-01 00:00:00</date_creation>" +
                "    <expertise>Software development</expertise>" +
                "  </record>" +
                "  <record>" +
                "    <name>Jane</name>" +
                "    <date_creation>2022-01-01 00:00:00</date_creation>" +
                "    <expertise>Data analysis</expertise>" +
                "  </record>" +
                "</root>";
        MultipartFile file = new MockMultipartFile("file", "records.xml", "text/xml", xml.getBytes());

        List<String[]> records = fileManagerService.processXMLFile(file);

        assertEquals(2, records.size());
        assertArrayEquals(new String[]{"John", "2022-01-01 00:00:00", "Software development"}, records.get(0));
        assertArrayEquals(new String[]{"Jane", "2022-01-01 00:00:00", "Data analysis"}, records.get(1));
    }

    @Test
    public void testProcessXMLFile_InvalidXML() {
        String xml = "This is not a valid XML file";
        MultipartFile file = new MockMultipartFile("file", "invalid.xml", "text/xml", xml.getBytes());

        try {
            fileManagerService.processXMLFile(file);
            Assertions.fail("Expected CustomException to be thrown");
        } catch (CustomException e) {
            assertEquals(500, e.getErrorCode());
            assertTrue(e.getMessage().startsWith("Error processing file: invalid.xml\n"));
        }
    }

    @Test
    public void testProcessXMLFile_InvalidName() {
        String xml = "<root>" +
                "  <record>" +
                "    <name>invalid name</name>" +
                "    <date_creation>2022-01-01 00:00:00</date_creation>" +
                "    <expertise>Software development</expertise>" +
                "  </record>" +
                "</root>";
        MultipartFile file = new MockMultipartFile("file", "records.xml", "text/xml", xml.getBytes());

        try {
            fileManagerService.processXMLFile(file);
            Assertions.fail("Expected CustomException to be thrown");
        } catch (CustomException e) {
            assertEquals(500, e.getErrorCode());
            assertEquals("Error processing file: records.xml\n" +
                    "Invalid name: invalid name", e.getMessage());
        }
    }

    @Test
    public void testProcessXMLFile_InvalidDateFormat() {
        String xml = "<root>" +
                "  <record>" +
                "    <name>John Smith</name>" +
                "    <date_creation>2022-01-0:00:00</date_creation>" +
                "    <expertise>Software development</expertise>" +
                "  </record>" +
                "</root>";
        MultipartFile file = new MockMultipartFile("file", "records.xml", "text/xml", xml.getBytes());

        try {
            fileManagerService.processXMLFile(file);
            Assertions.fail("Expected CustomException to be thrown");
        } catch (CustomException e) {
            assertEquals(500, e.getErrorCode());
            assertEquals("Error processing file: records.xml\n" +
                    "Invalid name: John Smith", e.getMessage());
        }
    }

    @Test
    public void testProcessXMLFile_ExpertiseTooLong() {
        String xml = "<root>" +
                "  <record>" +
                "    <name>John</name>" +
                "    <date_creation>2022-01-01 00:00:00</date_creation>" +
                "    <expertise>This expertise field is more than 50 characters long and should trigger an exception</expertise>" +
                "  </record>" +
                "</root>";
        MultipartFile file = new MockMultipartFile("file", "records.xml", "text/xml", xml.getBytes());

        try {
            fileManagerService.processXMLFile(file);
            fail("Expected CustomException to be thrown");
        } catch (CustomException e) {
            assertEquals(500, e.getErrorCode());
            assertEquals("Error processing file: records.xml\n" +
                    "Expertise too long: This expertise field is more than 50 characters long and should trigger an exception", e.getMessage());
        }
    }
}
