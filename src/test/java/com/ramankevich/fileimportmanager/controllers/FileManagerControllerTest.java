package com.ramankevich.fileimportmanager.controllers;

import com.ramankevich.fileimportmanager.exceptions.CustomException;
import com.ramankevich.fileimportmanager.services.FileManagerService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FileManagerControllerTest {

    @Mock
    private FileManagerService fileManagerService;

    @InjectMocks
    private FileManagerController fileManagerController;

    private MockMvc mockMvc;
    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(fileManagerController).build();
    }

    @Test
    public void testUploadMultipleFiles_Success() throws Exception {
        MockMultipartFile file1 = new MockMultipartFile("file", "file1.txt", "text/plain", "file1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("file", "file2.txt", "text/plain", "file2".getBytes());
        MockMultipartFile[] files = {file1, file2};
        when(fileManagerService.processFiles(files)).thenReturn("Success");

        String result = fileManagerController.uploadMultipleFiles(files);

        assertEquals("Success", result);
        verify(fileManagerService).processFiles(files);
    }

    @Test(expected = CustomException.class)
    public void testUploadMultipleFiles_InvalidFileFormat() throws Exception {
        MockMultipartFile file1 = new MockMultipartFile("file", "file1.txt", "text/plain", "file1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("file", "file2.jpg", "image/jpeg", "file2".getBytes());
        MockMultipartFile[] files = {file1, file2};

        fileManagerController.uploadMultipleFiles(files);
    }

    @Test
    public void testGetAllRecords_Success() {
        List<String[]> expectedRecords = Arrays.asList(new String[]{"1", "record1"}, new String[]{"2", "record2"});
        when(fileManagerService.getRecords()).thenReturn(expectedRecords);

        List<String[]> result = fileManagerController.getAllRecords();

        assertEquals(expectedRecords, result);
        verify(fileManagerService).getRecords();
    }
}
