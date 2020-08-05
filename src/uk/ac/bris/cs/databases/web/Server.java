/**
 * Mini implementation forum server and UI.
 */
package uk.ac.bris.cs.databases.web;

//java.lang.Object
//——————fi.iki.elonen.NanoHTTPD
//————————————fi.iki.elonen.router.RouterNanoHTTPD
import fi.iki.elonen.router.RouterNanoHTTPD;
import fi.iki.elonen.util.ServerRunner;
import freemarker.template.Configuration;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import uk.ac.bris.cs.databases.api.APIProvider;
import uk.ac.bris.cs.databases.cwk2.API;

/**
 * @author lily
 */
public class Server extends RouterNanoHTTPD {

    private static final String DATABASE = "jdbc:mariadb://localhost:3306/bb?user=student";

    public Server() {
        //RouterNanoHTTPD(int port)
        super(8003);
        addMappings();
    }

    //addMappings()方法定义了不同URL路径的处理程序
    @Override
    public void addMappings() {
        //默认路由，它们是可写的。
        super.addMappings();
        //增加路由
        addRoute("/person/:id", PersonHandler.class);
        addRoute("/people", PeopleHandler.class);
        addRoute("/newtopic", NewTopicHandler.class);
        addRoute("/forums", ForumsHandler.class);
        addRoute("/forum/:id", ForumHandler.class);
        addRoute("/topic/:id", TopicHandler.class);

        addRoute("/newforum", NewForumHandler.class);
        addRoute("/createforum", CreateForumHandler.class);

        addRoute("/newtopic/:id", NewTopicHandler.class);
        addRoute("/createtopic", CreateTopicHandler.class);

        addRoute("/newpost/:id", NewPostHandler.class);
        addRoute("/createpost", CreatePostHandler.class);

        addRoute("/newperson", NewPersonHandler.class);
        addRoute("/createperson", CreatePersonHandler.class);

        addRoute("/login", LoginHandler.class);
        addRoute("/login/:id", LoginHandler.class);

        addRoute("/styles.css", StyleHandler.class, "resources/styles.css");
        addRoute("/gridlex.css", StyleHandler.class, "resources/gridlex.css");
    }

    public static void main(String[] args) throws Exception {

        /**应用程序上下文。getInstance是一个函数，在java中，可以使用这种方式使用
         * 单例模式创建类的实例，所谓单例模式就是 一个类有且只有一个实例，
         * 不像object ob=new object();的这种方式去实例化后去使用。
         *
         * ~服务类
         */
        ApplicationContext c = ApplicationContext.getInstance();

        // database //

        Connection conn;
        try {
            String cs = DATABASE;
            if (args.length >= 1) {
                cs = cs + "&localSocket=" + args[0];
                System.out.println("Using DB socket file: " + args[0]);
            } else {
                System.out.println("Not using a DB socket file.");
            }
            //①加载数据库驱动的过程不手动指出也可以（即Class.forName不写也行）

            //②连接数据库
            conn = DriverManager.getConnection(cs);

            //③关闭了自动commit
            conn.setAutoCommit(false);

            //使用开放数据库连接创建API实例
            APIProvider api = new API(conn);

            //将该API注册到ApplicationContext，以便处理程序可以访问它。
            c.setApi(api);
            //当应用程序启动时，这一切只发生一次

            // AF: Info messages
            System.out.println("Server accessible at: http://localhost:8000");
            System.out.println("Forums accessible at: http://localhost:8000/forums");
        } catch (SQLException e) {
            System.out.println("Connection to database failed. " +
                "Check that the database is running and that the socket file " +
                "is correct if you are using one.");
            throw new RuntimeException(e);
        }

        // templating //

        /**
         * freemarker（模板引擎）的Configuration
         * */
        Configuration cfg = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        //设置模板加载的文件夹位置
        cfg.setDirectoryForTemplateLoading(new File("resources/templates"));
        //设置默认的编码方式
        cfg.setDefaultEncoding("UTF-8");
        //将freemarker的Configuration注册到api上
        c.setTemplateConfiguration(cfg);


        /**
         * 新建一个服务器并运行
         * */
        Server server = new Server();
        ServerRunner.run(Server.class);
    }
}
