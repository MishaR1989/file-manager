package com.ramankevich.fileimportmanager.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Component
public class DbHelper {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void createTable() {
        jdbcTemplate.execute("CREATE TABLE records(id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255) NOT NULL, date_creation TIMESTAMP NOT NULL, expertise VARCHAR(255) NOT NULL)");
    }


    public void insertRecords(List<String[]> records) {
        jdbcTemplate.execute("SET AUTOCOMMIT FALSE");
        try {
            String sql = "INSERT INTO records(name, date_creation, expertise) VALUES (?, ?, ?)";
            List<Object[]> params = new ArrayList<>();
            for (String[] record : records) {
                Object[] param = new Object[3];
                param[0] = record[0];
                param[1] = Timestamp.valueOf(record[1]);
                param[2] = record[2];
                params.add(param);
            }
            jdbcTemplate.batchUpdate(sql, params);
            jdbcTemplate.execute("COMMIT");
        } catch (Exception e) {
            jdbcTemplate.execute("ROLLBACK");
            e.printStackTrace();
        } finally {
            jdbcTemplate.execute("SET AUTOCOMMIT TRUE");
        }
    }

    public List<String[]> getAllRecords() {
        String sql = "SELECT * FROM records";
        return jdbcTemplate.query(sql, new StringArrayRowMapper());
    }

    private static class StringArrayRowMapper implements RowMapper<String[]> {
        @Override
        public String[] mapRow(ResultSet rs, int rowNum) throws SQLException {
            String[] record = new String[3];
            record[0] = rs.getString("name");
            record[1] = rs.getString("date_creation");
            record[2] = rs.getString("expertise");
            return record;
        }
    }

}
