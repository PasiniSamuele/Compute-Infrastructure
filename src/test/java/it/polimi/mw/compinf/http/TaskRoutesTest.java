package it.polimi.mw.compinf.http;

import akka.http.javadsl.model.*;
import akka.http.javadsl.testkit.JUnitRouteTest;
import akka.http.javadsl.testkit.TestRoute;
import org.junit.*;
import org.junit.runners.MethodSorters;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.StatusCodes;



@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TaskRoutesTest extends JUnitRouteTest {

    /*@ClassRule
    public static TestKitJunitResource testkit = new TestKitJunitResource();

    // shared registry for all tests
    private static ActorRef<TaskRegistry.Command> taskRegistry;
    private TestRoute appRoute;

    @BeforeClass
    public static void beforeClass() {
        taskRegistry = testkit.spawn(TaskRegistry.create());
    }

    @Before
    public void before() {
        TaskRoutes taskRoutes = new TaskRoutes(testkit.system(), taskRegistry);
        appRoute = testRoute(taskRoutes.taskRoutes());
    }

    @AfterClass
    public static void afterClass() {
        testkit.stop(taskRegistry);
    }

    //#actual-test
    @Test
    public void test1NoTasks() {
        appRoute.run(HttpRequest.GET("/tasks"))
                .assertStatusCode(StatusCodes.OK)
                .assertMediaType("application/json")
                .assertEntity("{\"users\":[]}");
    }

    //#actual-test
    //#testing-post
    @Test
    public void test2HandlePOST() {
        appRoute.run(HttpRequest.POST("/tasks")
                .withEntity(MediaTypes.APPLICATION_JSON.toContentType(),
                        "{\"name\": \"Kapi\"}"))
                .assertStatusCode(StatusCodes.CREATED)
                .assertMediaType("application/json")
                .assertEntity("{\"description\":\"Task Kapi created.\"}");
    }
    //#testing-post

    @Test
    public void test3Remove() {
        appRoute.run(HttpRequest.DELETE("/tasks/Kapi"))
                .assertStatusCode(StatusCodes.OK)
                .assertMediaType("application/json")
                .assertEntity("{\"description\":\"Task Kapi deleted.\"}");

    }*/
}
