package com.ramankevich.fileimportmanager;

import com.ramankevich.fileimportmanager.db.DbHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class Runner implements ApplicationRunner {

    @Autowired
    DbHelper dbHelper;

    @Override
    public void run(ApplicationArguments args) {
        dbHelper.createTable();
    }
}
