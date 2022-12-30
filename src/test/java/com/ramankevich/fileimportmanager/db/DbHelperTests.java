package com.ramankevich.fileimportmanager.db;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DbHelperTests {

    @Autowired
    private DbHelper dbHelper;

    @Test
    public void testInsertRecords() {
        List<String[]> records = Arrays.asList(
                new String[] {"Alice", "2022-12-28 12:00:00", "Software Engineering"},
                new String[] {"Bob", "2022-12-28 13:00:00", "Data Science"}
        );
        dbHelper.insertRecords(records);

        List<String[]> actualRecords = dbHelper.getAllRecords();
        assertArrayEquals(records.get(0), actualRecords.get(0));
        assertArrayEquals(records.get(1), actualRecords.get(1));
    }

    @Test
    public void testGetAllRecords() {
        List<String[]> records = Arrays.asList(
                new String[] {"Alice", "2022-12-28 12:00:00", "Software Engineering"},
                new String[] {"Bob", "2022-12-28 13:00:00", "Data Science"}
        );

        List<String[]> actualRecords = dbHelper.getAllRecords();
        assertEquals(records.size(), 2);
    }
}
