package com.eventify.model;

/**
 * Represents a Participant's score and rank in a university Event.
 * Implements Comparable to demonstrate Java Collections sorting by score descending.
 */
public class Result implements Comparable<Result> {

    private int resultId;
    private int eventId;
    private String eventName;
    private int participantId;
    private String participantName;
    private String studentId;
    private double score;
    private int rank;

    public Result() {
    }

    public Result(int resultId, int eventId, String eventName, int participantId,
                  String participantName, String studentId, double score, int rank) {
        this.resultId = resultId;
        this.eventId = eventId;
        this.eventName = eventName;
        this.participantId = participantId;
        this.participantName = participantName;
        this.studentId = studentId;
        this.score = score;
        this.rank = rank;
    }

    @Override
    public int compareTo(Result other) {
        return Double.compare(other.score, this.score);
    }

    public int getResultId() {
        return resultId;
    }

    public void setResultId(int resultId) {
        this.resultId = resultId;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public int getParticipantId() {
        return participantId;
    }

    public void setParticipantId(int participantId) {
        this.participantId = participantId;
    }

    public String getParticipantName() {
        return participantName;
    }

    public void setParticipantName(String participantName) {
        this.participantName = participantName;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }
}
