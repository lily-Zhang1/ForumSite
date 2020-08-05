package uk.ac.bris.cs.databases.api;
import java.util.List;
import java.util.Map;

/**
 * @author lily
 */

/**
 * 接口中指定了应该实现的方法：
 * 这个类和api包中的所有其他类中的注释一起构成了应用程序规范。
 * APIProvider接口包含要实现的每个方法的描述。
 * 例如，getforum()方法在APIProvider类中定义，
 * 以返回按标题排序的ForumSummaryView对象列表;
 * ForumSummaryView类本身指定，如果论坛中没有主题，
 * 那么lastTopic字段应该为NULL。
 * */
public interface APIProvider {
    /**
     * about person
     */

    /**
     * Get a list of all users in the system as a map username -> name.
     * @return A map with one entry per user of the form username -> name
     * (note that usernames are unique).
     *
     * Used by: /people (PeopleHandler)
     *
     */
    public Result<Map<String, String>> getUsers();
    
    /**
     * Get a PersonView for the person with the given username.
     * @param username - the username to search for, cannot be empty.
     * @return If a person with the given username exists, a fully populated
     * PersonView. Otherwise, failure (or fatal on a database error).
     *
     * Used by: /person/:id (PersonHandler)
     */
    public Result<PersonView> getPersonView(String username);
       
    /**
     * Create a new person.
     * @param name - the person's name, cannot be empty.
     * @param username - the person's username, cannot be empty.
     * @param studentId - the person's student id. May be either NULL if the
     * person is not a student or a non-empty string if they are; can not be
     * an empty string.
     * @return Success if no person with this username existed yet and a new
     * one was created, failure if a person with this username already exists,
     * fatal if something else went wrong.
     *
     * Used by: /newperson => /createperson (CreatePersonHandler)
     *
     */
    public Result addNewPerson(String name, String username, String studentId);
    
    /**
     * Forums only
     */
    
    /**
     * Get the "main page" containing a list of forums ordered alphabetically
     * by title. 
     * @return the list of all forums; an empty list if there are no forums.
     *
     * Used by: /forums (ForumsHandler)
     */
     public Result<List<ForumSummaryView>> getForums();

    /**
     * Create a new forum.
     * @param title - the title of the forum. Must not be null or empty and
     * no forum with this name must exist yet.
     * @return success if the forum was created, failure if the title was
     * null, empty or such a forum already existed; fatal on other errors.
     *
     * Used by: /newforum => /createforum (CreateForumHandler)
     */
    public Result createForum(String title);
    
    /**
     * 3 Forums, topics, posts.
     */
    
    /**
     * Get the detailed view of a single forum.
     * @param id - the id of the forum to get.
     * @return A view of this forum if it exists, otherwise failure.
     *
     * Used by: /forum/:id (ForumHandler)
     */
    public Result<ForumView> getForum(int id);
    
    /**
     * Get a view of a topic.
     * @param topicId - the topic to get.
     * @return The topic view if one exists with the given id,
     * otherwise failure or fatal on database errors. 
     *
     * Used by: /topic/:id (TopicHandler)
     */
    public Result<TopicView> getTopic(int topicId);

    /**
     * Create a post in an existing topic.
     * @param topicId - the id of the topic to post in. Must refer to
     * an existing topic.
     * @param username - the name under which to post; user must exist.
     * @param text - the content of the post, cannot be empty.
     * @return success if the post was made, failure if any of the preconditions
     * were not met and fatal if something else went wrong.
     *
     * Used by: /newpost/:id => /createpost (CreatePostHandler)
     */
    public Result createPost(int topicId, String username, String text);
    
    /**
     * Create a new topic in a forum.
     * @param forumId - the id of the forum in which to create the topic. This
     * forum must exist.
     * @param username - the username under which to make this post. Must refer
     * to an existing username.
     * @param title - the title of this topic. Cannot be empty.
     * @param text - the text of the initial post. Cannot be empty.
     * @return failure if any of the preconditions are not met (forum does not
     * exist, user does not exist, title or text empty);
     * success if the post was created and fatal if something else went wrong.
     *
     * Used by: /newtopic/:id => /createtopic (CreateTopicHandler)
     */
    public Result createTopic(int forumId, String username, String title, String text);
     
    /**dong.sql
     * Count the number of posts in a topic (without fetching them all).
     * @param topicId - the topic to look at.
     * @return The number of posts in this topic if it exists, otherwise a
     * failure.
     *
     * Not used in web interface.
     */
    public Result<Integer> countPostsInTopic(int topicId);
    
}
