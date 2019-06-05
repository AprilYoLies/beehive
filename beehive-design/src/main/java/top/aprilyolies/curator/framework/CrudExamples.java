package top.aprilyolies.curator.framework;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * @Author EvaJohnson
 * @Date 2019-06-05
 * @Email g863821569@gmail.com
 */
public class CrudExamples {
    Logger logger = Logger.getLogger(CrudExamples.class);

    private RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);

    private CuratorFramework client = CuratorFrameworkFactory.builder()
            .connectString("127.0.0.1:2181")
            .sessionTimeoutMs(5000)
            .connectionTimeoutMs(5000)
            .retryPolicy(retryPolicy)
            .build();

    private String ROOT_PATH = "/curator_test";

    private byte[] payload = "Something as payload".getBytes();

    @Before
    public void startClient() throws Exception {
        client.start();
        try {
            client.create().forPath(ROOT_PATH);
        } catch (Exception e) {
            if (e instanceof KeeperException.NodeExistsException) {
                logger.info("Path " + ROOT_PATH + " has existed, ignore create operation.");
            } else {
                throw e;
            }
        }
    }

    @Test
    public void create() throws Exception {
        // this will create the given ZNode with the given data
        try {
            client.create().forPath(ROOT_PATH + "/create", payload);
        } catch (Exception e) {
            if (e instanceof KeeperException.NodeExistsException) {
                logger.info("Path " + ROOT_PATH + " has existed, ignore create operation.");
            } else {
                throw e;
            }
        }
    }

    @Test
    public void createEphemeral() throws Exception {
        // this will create the given EPHEMERAL ZNode with the given data
        try {
            client.create().withMode(CreateMode.EPHEMERAL).forPath(ROOT_PATH + "/createEphemeral");
        } catch (Exception e) {
            if (e instanceof KeeperException.NodeExistsException) {
                logger.info("Path " + ROOT_PATH + " has existed, ignore create operation.");
            } else {
                throw e;
            }
        }
    }

    @Test
    public void createEphemeralSequential() throws Exception {
        // this will create the given EPHEMERAL-SEQUENTIAL ZNode with the given data using Curator protection.

        /*
            Protection Mode:

            It turns out there is an edge case that exists when creating sequential-ephemeral nodes. The creation
            can succeed on the server, but the server can crash before the created node name is returned to the
            client. However, the ZK session is still valid so the ephemeral node is not deleted. Thus, there is no
            way for the client to determine what node was created for them.

            Even without sequential-ephemeral, however, the create can succeed on the sever but the client (for various
            reasons) will not know it. Putting the create builder into protection mode works around this. The name of
            the node that is created is prefixed with a GUID. If node creation fails the normal retry mechanism will
            occur. On the retry, the parent path is first searched for a node that has the GUID in it. If that node is
            found, it is assumed to be the lost node that was successfully created on the first try and is returned to
            the caller.
         */
        try {
            String s = client.create().withProtection().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(ROOT_PATH + "/createEphemeralSequential", payload);
            logger.info("createEphemeralSequential() return " + s);
        } catch (Exception e) {
            if (e instanceof KeeperException.NodeExistsException) {
                logger.info("Path " + ROOT_PATH + " has existed, ignore create operation.");
            } else {
                throw e;
            }
        }
    }

    @Test
    public void setData() throws Exception {
        // set data for the given node
        client.setData().forPath(ROOT_PATH + "/create", "New data".getBytes());
    }

    @Test
    public void setDataAsync() throws Exception {
        // this is one method of getting event/async notifications
        CuratorListener listener = new CuratorListener() {
            @Override
            public void eventReceived(CuratorFramework client, CuratorEvent event) throws Exception {
                logger.info("The set data operation has finished.");
            }
        };
        client.getCuratorListenable().addListener(listener);
//        set data for the given node asynchronously.The completion notification
//        is done via the CuratorListener.
        try {
            client.create().forPath(ROOT_PATH + "/setDataAsync", payload);
        } catch (Exception e) {
            if (e instanceof KeeperException.NodeExistsException) {
                logger.info("Path " + ROOT_PATH + " has existed, ignore create operation.");
            } else {
                throw e;
            }
        }
        client.setData().inBackground().forPath(ROOT_PATH + "/setDataAsync", "New data".getBytes());
    }

    @Test
    public void setDataAsyncWithCallback() throws Exception {
        // this is another method of getting notification of an async completion
        BackgroundCallback callback = new BackgroundCallback() {
            @Override
            public void processResult(CuratorFramework client, CuratorEvent event) throws Exception {
                logger.info("Process result.");
            }
        };
        client.setData().inBackground(callback).forPath(ROOT_PATH + "/setDataAsyncWithCallback", payload);
    }

    @Test
    public void delete() throws Exception {
        // delete the given node
        client.delete().forPath(ROOT_PATH + "/setDataAsync");
    }

    public static void guaranteedDelete(CuratorFramework client, String path) throws Exception {
        // delete the given node and guarantee that it completes

        /*
            Guaranteed Delete

            Solves this edge case: deleting a node can fail due to connection issues. Further, if the node was
            ephemeral, the node will not get auto-deleted as the session is still valid. This can wreak havoc
            with lock implementations.


            When guaranteed is set, Curator will record failed node deletions and attempt to delete them in the
            background until successful. NOTE: you will still get an exception when the deletion fails. But, you
            can be assured that as long as the CuratorFramework instance is open attempts will be made to delete
            the node.
         */

        client.delete().guaranteed().forPath(path);
    }

    @Test
    public void watchedGetChildren() throws Exception {
        /**
         * Get children and set a watcher on the node. The watcher notification will come through the
         * CuratorListener (see setDataAsync() above).
         */
        List<String> list = client.getChildren().watched().forPath(ROOT_PATH + "/setDataAsync");
    }

    public static List<String> watchedGetChildren(CuratorFramework client, String path, Watcher watcher) throws Exception {
        /**
         * Get children and set the given watcher on the node.
         */
        return client.getChildren().usingWatcher(watcher).forPath(path);
    }
}
