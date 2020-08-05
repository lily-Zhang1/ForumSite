package uk.ac.bris.cs.databases.cwk2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.BlockingQueue;

import uk.ac.bris.cs.databases.api.APIProvider;
import uk.ac.bris.cs.databases.api.ForumSummaryView;
import uk.ac.bris.cs.databases.api.ForumView;
import uk.ac.bris.cs.databases.api.Result;
import uk.ac.bris.cs.databases.api.PersonView;
import uk.ac.bris.cs.databases.api.SimplePostView;
import uk.ac.bris.cs.databases.api.SimpleTopicSummaryView;
import uk.ac.bris.cs.databases.api.TopicView;

/**
 * @author lily
 */
public class API implements APIProvider {

    private final Connection c;

    public API(Connection c) { this.c = c; }

    /** tool method, judge whether the item is exist or not. */
    private Result judgeString(String item, String tableName, String attributeName) throws SQLException
    {
        try (PreparedStatement p = c.prepareStatement(
                "SELECT count(1) AS c FROM "+tableName+" WHERE "+attributeName+" = ?"
        )) {
            p.setString(1, item);
            ResultSet r = p.executeQuery();
            if (r.next() && r.getInt("c") > 0) {
                return Result.success();
            }
            return Result.failure("This String is not existed.");
        }
    }

    /** tool method, judge whether the item is exist or not. */
    private Result judgeNumber(int item, String tableName, String attributeName) throws SQLException
    {
        try (PreparedStatement p = c.prepareStatement(
                "SELECT count(1) AS c FROM "+tableName+" WHERE "+attributeName+" = ?"
        )) {
            p.setString(1, String.valueOf(item));
            ResultSet r = p.executeQuery();
            if (r.next() && r.getInt("c") > 0) {
                return Result.success();
            }
            return Result.failure("This number is not existed.");
        }
    }

    /** get forum title according to forum id. */
    private Result<String> getForumTitle(int id)
    {
        try (PreparedStatement p = c.prepareStatement(
                "SELECT title FROM Forum WHERE id = ?"
        )) {
            p.setString(1, String.valueOf(id));
            ResultSet r = p.executeQuery();
            if (r.next()) {
                return Result.success(r.getString("title"));
            }
            return Result.failure("failed to get forum title.");
        } catch (SQLException ex) {
            return Result.fatal("database error - " + ex.getMessage());
        }
    }

    /** get username according to person id. */
    private Result<String> getUsername(int id)
    {
        try (PreparedStatement p = c.prepareStatement(
                "SELECT username FROM Person WHERE id = ?"
        )) {
            p.setString(1, String.valueOf(id));
            ResultSet r = p.executeQuery();
            if (r.next()) {
                return Result.success(r.getString("username"));
            }
            return Result.failure("failed to get username.");
        } catch (SQLException ex) {
            return Result.fatal("database error - " + ex.getMessage());
        }
    }

    /** get person id according to username. */
    private Result<Integer> getPersonId(String username)
    {
        try (PreparedStatement p = c.prepareStatement(
                "SELECT id FROM Person WHERE username = ?"
        )) {
            p.setString(1, username);
            ResultSet r = p.executeQuery();
            if (r.next()) {
                return Result.success(r.getInt("id"));
            }
            return Result.failure("failed to get username.");
        } catch (SQLException ex) {
            return Result.fatal("database error - " + ex.getMessage());
        }
    }

    /** get current time */
    private String getTime()
    {
        SimpleDateFormat myFmt=new SimpleDateFormat("dd/MM/yy HH:mm:ss");
        Date now=new Date();
        return myFmt.format(now);
    }

    @Override
    public Result<Map<String, String>> getUsers() {
        try (Statement s = c.createStatement()) {
            ResultSet r = s.executeQuery("SELECT name, username FROM Person");
            Map<String, String> data = new HashMap<>();
            while (r.next()) {
                data.put(r.getString("username"), r.getString("name"));
            }
            return Result.success(data);
        } catch (SQLException ex) {
            return Result.fatal("database error - " + ex.getMessage());
        }
    }

    @Override
    public Result addNewPerson(String name, String username, String studentId) {
        if (studentId != null && studentId.equals("")) {
            return Result.failure("addNewPerson: StudentId can be null, but cannot be the empty string.");
        }
        if (name == null || name.equals("")) {
            return Result.failure("addNewPerson: Name cannot be empty.");
        }
        if (username == null || username.equals("")) {
            return Result.failure("addNewPerson: Username cannot be empty.");
        }
        try {
            if (judgeString(username, "Person", "username").isSuccess()) {
                return Result.failure("addNewPerson: Username is already exist.");
            }
        } catch (SQLException ex) {
            return Result.fatal("database error - " + ex.getMessage());
        }
        try (PreparedStatement p = c.prepareStatement(
                "INSERT INTO Person (name, username, stuId) VALUES (?, ?, ?)"
        )) {
            p.setString(1, name);
            p.setString(2, username);
            p.setString(3, studentId);
            p.executeUpdate();
            c.commit();
        } catch (SQLException e) {
            try { c.rollback(); }
            catch (SQLException f) {
                return Result.fatal("SQL error on rollback - [" + f +
                        "] from handling exception " + e);
            }
            return Result.fatal(e.getMessage());
        }
        return Result.success();
    }

    @Override
    public Result<PersonView> getPersonView(String username) {
        if (username == null || username.equals("")) {
            return Result.failure("getPersonView: username cannot be empty.");
        }
        try (PreparedStatement p = c.prepareStatement(
                "SELECT name, username, stuId FROM Person WHERE username = ?"
        )) {
            p.setString(1, username);
            ResultSet r = p.executeQuery();
            if (!r.next()) {
                return Result.failure("getPersonView: A user called "
                        + username + "do not exist.");
            }
            String name = r.getString("name");
            String studentId = r.getString("stuId");
            PersonView personViewResult = null;
            if(studentId != null) {
                personViewResult = new PersonView(name, username, studentId);
            } else {
                personViewResult = new PersonView(name, username, "null");
            }
            return Result.success(personViewResult);
        } catch (SQLException ex) {
            return Result.fatal("database error - " + ex.getMessage());
        }
    }

    @Override
    public Result<List<ForumSummaryView>> getForums() {
        try (Statement s = c.createStatement()) {
            ResultSet r = s.executeQuery("SELECT id, title FROM Forum");
            List<ForumSummaryView> data = new ArrayList<>();
            while (r.next()) {
                int id = r.getInt("id");
                String title = r.getString("title");
                ForumSummaryView forumSummaryView = new ForumSummaryView(id, title);
                data.add(forumSummaryView);
            }
            return Result.success(data);
        } catch (SQLException ex) {
            return Result.fatal("database error - " + ex.getMessage());
        }
    }

    @Override
    public Result<Integer> countPostsInTopic(int topicId) {
        if (topicId <= 0) {
            return Result.failure("countPostsInTopic: TopicId is wrong.");
        }
        try (PreparedStatement p = c.prepareStatement(
                "SELECT count(1) AS c FROM Post WHERE topicId = ?"
        )) {
            p.setString(1, String.valueOf(topicId));
            ResultSet r = p.executeQuery();
            int count = r.getInt("c");
            if (r.next() && count == 0) {
                return Result.failure("countPostsInTopic: This topic don't have any post.");
            }
            return Result.success(count);
        } catch (SQLException e) {
            return Result.fatal(e.getMessage());
        }
    }

    @Override
    public Result<TopicView> getTopic(int topicId) {
        if (topicId <= 0) {
            return Result.failure("getTopic: TopicId is wrong.");
        }
        try (PreparedStatement p = c.prepareStatement(
                "SELECT t.title, p.id, p.authorId, p.text, p.postedAt \n" +
                        "FROM Topic t JOIN Post p ON t.id = p.topicId\n" +
                        "WHERE t.id = ?;"
        )) {
            p.setString(1, String.valueOf(topicId));
            ResultSet r = p.executeQuery();
            if (!r.next()) {
                return Result.failure("getTopic: This topic don't have any post.");
            }
            List<SimplePostView> posts = new ArrayList<>();
            String topicTitle = null;
            int id = 0;
            do {
                topicTitle = r.getString("t.title");
                String author = getUsername(r.getInt("p.authorId")).getValue();
                String text = r.getString("p.text");
                String postedAt = r.getString("p.postedAt");
                SimplePostView post = new SimplePostView(++id, author, text, postedAt);
                posts.add(post);
            } while (r.next());
            TopicView topicView = new TopicView(topicId, topicTitle, posts);
            return Result.success(topicView);
        } catch (SQLException e) {
            return Result.fatal(e.getMessage());
        }
    }

    @Override
    public Result createForum(String title) {
        if (title == null || title.equals("")) {
            return Result.failure("createForum: Title cannot be empty.");
        }
        try {
            if (judgeString(title, "Forum", "title").isSuccess()) {
                return Result.failure("A forum called " + title + " already exists.");
            }
        } catch (SQLException ex) {
            return Result.fatal("database error - " + ex.getMessage());
        }
        try (PreparedStatement p = c.prepareStatement(
                "INSERT INTO Forum (title) VALUES (?)"
        )) {
            p.setString(1, title);
            p.executeUpdate();
            c.commit();
        } catch (SQLException e) {
            try { c.rollback(); }
            catch (SQLException f) {
                return Result.fatal("SQL error on rollback - [" + f +
                        "] from handling exception " + e);
            }
            return Result.fatal(e.getMessage());
        }
        return Result.success();
    }

    @Override
    public Result<ForumView> getForum(int id) {
        if (id <= 0) { return Result.failure("getForum: ForumId is wrong."); }
        try {
            if (!judgeNumber(id ,"Forum", "id").isSuccess()) {
                return Result.failure("getForum: Forum is not exist.");
            }
        } catch (SQLException ex) {
            return Result.fatal("database error - " + ex.getMessage());
        }
        String forumTitle = getForumTitle(id).getValue();
        try (PreparedStatement p = c.prepareStatement(
                "SELECT f.title, t.id, t.title FROM Forum f\n" +
                        "JOIN Topic t ON t.forumId = f.id WHERE f.id = ?"
        )) {
            p.setString(1, String.valueOf(id));
            ResultSet r = p.executeQuery();
            List<SimpleTopicSummaryView> topics = new ArrayList<>();
            while(r.next()) {
                int topicId = r.getInt("t.id");
                String topicTile = r.getString("t.title");
                SimpleTopicSummaryView topic = new SimpleTopicSummaryView(topicId,id,topicTile);
                topics.add(topic);
            }
            ForumView forumView = new ForumView(id, forumTitle, topics);
            return Result.success(forumView);
        } catch (SQLException e) {
            return Result.fatal(e.getMessage());
        }
    }

    @Override
    public Result createPost(int topicId, String username, String text) {
        if (topicId < 0) {
            return Result.failure("createPost: TopicId is wrong.");
        }
        try {
            if (!judgeNumber(topicId ,"Topic", "id").isSuccess()) {
                return Result.failure("createPost: Topic is not exist.");
            }
        } catch (SQLException ex) {
            return Result.fatal("database error - " + ex.getMessage());
        }
        if (username == null || username.equals("")) {
            return Result.failure("createPost: username cannot be empty.");
        }
        try {
            if (!judgeString(username, "Person", "username").isSuccess()) {
                return Result.failure("createPost: Username is not exist.");
            }
        } catch (SQLException ex) {
            return Result.fatal("database error - " + ex.getMessage());
        }
        if (text == null || text.equals("")) {
            return Result.failure("createPost: text cannot be empty.");
        }
        Result<Integer> id = getPersonId(username);
        int authorId = 0;
        if (id.isSuccess()) { authorId = getPersonId(username).getValue(); }
        try (PreparedStatement p = c.prepareStatement(
                "INSERT INTO Post (topicId, authorId, text, postedAt) VALUES (?, ?, ?, ?)"
        )) {
            p.setInt(1, topicId);
            p.setInt(2, authorId);
            p.setString(3, text);
            p.setString(4, getTime());
            p.executeUpdate();
            c.commit();
        } catch (SQLException e) {
            try { c.rollback(); }
            catch (SQLException f) {
                return Result.fatal("SQL error on rollback - [" + f +
                        "] from handling exception " + e);
            }
            return Result.fatal(e.getMessage());
        }
        return Result.success();
    }

    @Override
    public Result createTopic(int forumId, String username, String title, String text) {
        if (forumId < 0) {
            return Result.failure("createTopic: ForumId is wrong.");
        }
        try {
            if (!judgeNumber(forumId, "Forum", "id").isSuccess()) {
                return Result.failure("createTopic: Forum is not exist.");
            }
            if (!judgeString(username, "Person", "username").isSuccess()) {
                return Result.failure("createTopic: Username is not exist.");
            }
        } catch (SQLException ex) {
            return Result.fatal("database error - " + ex.getMessage());
        }
        if (title == null || title.equals("")) {
            return Result.failure("createTopic: Title cannot be empty.");
        }
        if (text == null || text.equals("")) {
            return Result.failure("createTopic: Text cannot be empty.");
        }
        try (PreparedStatement p = c.prepareStatement(
                "INSERT INTO Topic (title, forumId) VALUES (?, ?)"
        )) {
            p.setString(1, title);
            p.setInt(2, forumId);
            p.executeUpdate();
            c.commit();
        } catch (SQLException e) {
            try { c.rollback(); }
            catch (SQLException f) {
                return Result.fatal("SQL error on rollback - [" + f +
                        "] from handling exception " + e);
            }
            return Result.fatal(e.getMessage());
        }
        int id = 0;
        try (PreparedStatement p = c.prepareStatement(
                "SELECT id FROM Topic WHERE title = ?"
        )) {
            p.setString(1, title);
            ResultSet r = p.executeQuery();
            if (r.next()) { id = r.getInt("id"); }
        } catch (SQLException ex) {
            return Result.fatal("database error - " + ex.getMessage());
        }
        Result result = createPost(id, username, text);
        if (!result.isSuccess()) {
            try { c.rollback(); }
            catch (SQLException f) {
                return Result.fatal("SQL error on rollback - ["+f+"] from handling exception ");
            }
            return result;
        }
        return Result.success();
    }
}
