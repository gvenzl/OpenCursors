package org.example;

import oracle.jdbc.pool.OracleDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) throws SQLException, InterruptedException {

        OracleDataSource ods = new OracleDataSource();

        ods.setURL("jdbc:oracle:thin:system/oracle@//localhost:1193/CDB193");
        Connection conn_system = ods.getConnection();

        ods.setURL("jdbc:oracle:thin:gerald/gerald@//localhost:1193/ORCLPDB1");
        Connection conn_gerald_1 = ods.getConnection();

        // 170 explicit open cursors
        for (int i=1;i <=170; i++) {
            PreparedStatement stmt_oc1 = conn_gerald_1.prepareStatement("SELECT 'open_" + i + "' FROM dual" );
            stmt_oc1.executeQuery();

            // Not gonna close my Statement, aka cursor here.
            // stmt_oc1.close();
        }

        PreparedStatement stmt_voc = conn_system.prepareStatement("SELECT sid, COUNT(1) FROM v$open_cursor WHERE user_name = 'GERALD' GROUP BY sid");
        ResultSet rslt_voc = stmt_voc.executeQuery();
        rslt_voc.next();
        System.out.println("Open cursors for SID " + rslt_voc.getString(1) + ": " + rslt_voc.getInt(2));
        rslt_voc.close();

        conn_gerald_1.close();
        System.out.println("Sleep for 5s.");
        Thread.sleep(5000);

        Connection conn_gerald_2 = ods.getConnection();

        // no open cursors

        for (int i=1;i <=170; i++) {
            PreparedStatement stmt_oc2 = conn_gerald_2.prepareStatement("SELECT 'closed_" + i + "' FROM dual");
            stmt_oc2.executeQuery();

            // I close my cursor here.
            stmt_oc2.close();
        }

        for (int i=1;i <=170; i++) {
            try (PreparedStatement stmt_oc2 = conn_gerald_2.prepareStatement("SELECT 'closed_" + i + "' FROM dual")) {
                stmt_oc2.executeQuery();
            }
        }


        ResultSet rslt_open_cursor = stmt_voc.executeQuery();
        rslt_open_cursor.next();
        System.out.println("Open cursors for SID " + rslt_open_cursor.getString(1) + ": " + rslt_open_cursor.getInt(2));
        rslt_open_cursor.close();

        conn_gerald_2.close();
    }
}