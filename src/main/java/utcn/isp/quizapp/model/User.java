package utcn.isp.quizapp.model;

public class User implements Comparable<User> {
    private String name;
    private int score;

    public User(String name, int score) {
        this.name = name;
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public int compareTo(User other) {
        return Integer.compare(other.score, this.score); // Sort by score descending
    }

    @Override
    public String toString() {
        return name + ": " + score + " points";
    }
}
