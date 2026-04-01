package com.kpollman.team1;

import com.kpollman.db.DatabaseHelper;
import com.kpollman.model.Voter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class VoterDAO {

    /**
     * Method Overloading: Search by EPIC No
     */
    public List<Voter> searchVoter(String epicNo, int boothId) {
        String query = "SELECT * FROM Voters WHERE epic_no = ? AND booth_id = ?";
        return executeSearch(query, epicNo, boothId);
    }

    /**
     * Method Overloading: Search by Name (Partial Match)
     */
    public List<Voter> searchVoterByName(String name, int boothId) {
        String query = "SELECT * FROM Voters WHERE voter_name LIKE ? AND booth_id = ?";
        return executeSearch(query, "%" + name + "%", boothId);
    }

    /**
     * Method Overloading: Search by Serial Number
     */
    public List<Voter> searchVoter(int serialNo, int boothId) {
        String query = "SELECT * FROM Voters WHERE serial_no = ? AND booth_id = ?";
        return executeSearch(query, String.valueOf(serialNo), boothId);
    }

    private List<Voter> executeSearch(String query, String param, int boothId) {
        List<Voter> voters = new ArrayList<>();
        try (Connection conn = DatabaseHelper.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, param);
            pstmt.setInt(2, boothId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                voters.add(new Voter(
                    rs.getString("epic_no"),
                    rs.getString("voter_name"),
                    rs.getInt("age"),
                    rs.getString("photo_path"),
                    rs.getInt("serial_no"),
                    rs.getInt("booth_id"),
                    rs.getBoolean("voted_status")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return voters;
    }
}
