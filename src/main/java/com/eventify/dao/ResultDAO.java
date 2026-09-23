package com.eventify.dao;

import com.eventify.database.DatabaseConnection;
import com.eventify.exception.DatabaseException;
import com.eventify.model.Result;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * DAO for managing Event Leaderboard Results and recalculating participant ranks.
 */
public class ResultDAO implements CrudDAO<Result, Integer> {

    @Override
    public boolean insert(Result result) {
        String sql = """
            INSERT INTO results (event_id, participant_id, score, rank)
            VALUES (?, ?, ?, 1)
            ON CONFLICT(event_id, participant_id) DO UPDATE SET score = excluded.score
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, result.getEventId());
            ps.setInt(2, result.getParticipantId());
            ps.setDouble(3, result.getScore());
            boolean ok = ps.executeUpdate() > 0;
            recalculateRanksForEvent(conn, result.getEventId());
            return ok;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save result: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Result result) {
        String sql = "UPDATE results SET score = ? WHERE result_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, result.getScore());
            ps.setInt(2, result.getResultId());
            boolean ok = ps.executeUpdate() > 0;
            recalculateRanksForEvent(conn, result.getEventId());
            return ok;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update result: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(Integer id) {
        Optional<Result> existing = findById(id);
        String sql = "DELETE FROM results WHERE result_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            boolean ok = ps.executeUpdate() > 0;
            if (existing.isPresent()) {
                recalculateRanksForEvent(conn, existing.get().getEventId());
            }
            return ok;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete result: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Result> findById(Integer id) {
        String sql = """
            SELECT r.*, e.event_name, p.name AS participant_name, p.student_id
            FROM results r
            JOIN events e ON r.event_id = e.event_id
            JOIN participants p ON r.participant_id = p.participant_id
            WHERE r.result_id = ?
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find result: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Result> findAll() {
        String sql = """
            SELECT r.*, e.event_name, p.name AS participant_name, p.student_id
            FROM results r
            JOIN events e ON r.event_id = e.event_id
            JOIN participants p ON r.participant_id = p.participant_id
            """;
        List<Result> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            // Sort using Java Collections and Result.compareTo (descending score)
            Collections.sort(list);
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load results: " + e.getMessage(), e);
        }
    }

    public List<Result> findByEventId(int eventId) {
        String sql = """
            SELECT r.*, e.event_name, p.name AS participant_name, p.student_id
            FROM results r
            JOIN events e ON r.event_id = e.event_id
            JOIN participants p ON r.participant_id = p.participant_id
            WHERE r.event_id = ?
            """;
        List<Result> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            Collections.sort(list);
            for (int i = 0; i < list.size(); i++) {
                list.get(i).setRank(i + 1);
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load event leaderboard: " + e.getMessage(), e);
        }
    }

    private void recalculateRanksForEvent(Connection conn, int eventId) throws SQLException {
        String selectSql = "SELECT result_id, score FROM results WHERE event_id = ?";
        List<Result> temp = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Result r = new Result();
                    r.setResultId(rs.getInt("result_id"));
                    r.setScore(rs.getDouble("score"));
                    temp.add(r);
                }
            }
        }
        Collections.sort(temp);
        String updateRankSql = "UPDATE results SET rank = ? WHERE result_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(updateRankSql)) {
            for (int i = 0; i < temp.size(); i++) {
                ps.setInt(1, i + 1);
                ps.setInt(2, temp.get(i).getResultId());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private Result mapRow(ResultSet rs) throws SQLException {
        return new Result(
                rs.getInt("result_id"),
                rs.getInt("event_id"),
                rs.getString("event_name"),
                rs.getInt("participant_id"),
                rs.getString("participant_name"),
                rs.getString("student_id"),
                rs.getDouble("score"),
                rs.getInt("rank")
        );
    }
}
